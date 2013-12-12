/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 21.10.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.data.filestore;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.StringValue;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.LRUCache;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 * Basic abstract file store cell storing a document and its cell meta information, such as its offset in the file
 * store file, and the length and uuid of the document. The deserialization of a document from the file store file is
 * already implemented, as well as all basic cell methods, such as toString(), hashCode(), getSstringValue(), and
 * getDocumentValue(). Deserialized documents are cached in order to avoid multiple deserialization of the same
 * document.
 * Furthermore it provides static methods for the serialization of a document into a byte array, and vice versa.
 *
 * Classes extending this class need to implement the serialization of the document (or its byte array) into the file
 * store file.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
abstract class AbstractDocumentFileStoreCell extends FileStoreCell implements DocumentValue, StringValue {

    /** SerialVersionID. */
    private static final long serialVersionUID = -6571228838857812542L;

    /* Logger */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(AbstractDocumentFileStoreCell.class);

    /** Default cache size. */
    protected static final int DEF_CACHE_SIZE = 1000;

    /* Document cache. */
    private static final LRUCache<UUID, Document> DOCUMENT_CACHE = new LRUCache<UUID, Document>(DEF_CACHE_SIZE);


    /** Document to store. */
    protected Document m_document;

    /** Flag to specify whether document data of cell was serialized or not, in order to avoid multiple writes. */
    protected AtomicBoolean m_serialized = new AtomicBoolean(false);

    /** Offset marking the documents position in file. */
    protected long m_offset;

    /** Length of the byte array storing the serialized document. */
    protected int m_length;

    /** UUID as unique identifier of document. */
    protected UUID m_docUuid;

    /**
     * Constructor of AbstractDocumentFileStoreCell. Creates new instance with given document and file store
     * to store document at.
     * @param fileStore File store to store document at.
     * @param document Document to encapsulate and store in file store.
     * @throws IOException if document can not be serialized into file store file.
     */
    public AbstractDocumentFileStoreCell(final FileStore fileStore, final Document document) throws IOException {
        super(fileStore);
        m_document = document;
        m_docUuid = m_document.getUUID();
    }

    /**
     * Empty constructor.
     */
    AbstractDocumentFileStoreCell() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getStringValue();
    }

    /**
     * {@inheritDoc}
     * Returns hash code of encapsulated document.
     */
    @Override
    public int hashCode() {
        readDocumentData();
        return m_document.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        readDocumentData();
        return m_document.equals(((DocumentValue)dc).getDocument());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        readDocumentData();
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        sb.append(m_document.getTitle());
        sb.append("\"");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getDocument() {
        readDocumentData();
        return m_document;
    }

    /**
     * Serializes the document data into the file store file and the address (offset) of the document as well as its
     * length and uuid into the given data output.
     * @param output The data output to write the address information of the document to.
     * @throws IOException If document data can not be written to file store file.
     */
    protected synchronized void serializeCell(final DataCellDataOutput output) throws IOException {
        // write document to file store (if it has not been written bevore)
        flushToFileStore();

        // write meta data
        output.writeLong(m_offset);
        output.writeInt(m_length);
        output.writeUTF(m_docUuid.toString());
    }

    /**
     * Deserializes document address information from given data input. The document itself is not deserialzed at this
     * point, only its offset, length and uuid information.
     * @param input The input to read the document address information from.
     * @throws IOException If document address information can not be deserialized from given data input.
     */
    protected synchronized void deserializeCell(final DataCellDataInput input) throws IOException {
        // read offset, length, and uuid, set serialized flag true (since cell has obviously been serialized before)
        m_serialized = new AtomicBoolean(true);
        m_offset = input.readLong();
        m_length = input.readInt();
        m_docUuid = UUID.fromString(input.readUTF());
    }

    /**
     * Prepares the read document data process.
     */
    protected abstract void prepareReadDocumentData();

    /**
     * If document has not been deserialized and is not already in cache it is deserialized from file store file and
     * than put to cache.
     */
    protected synchronized void readDocumentData() {
        if (m_document == null && m_docUuid != null) {
            m_document = DOCUMENT_CACHE.get(m_docUuid);
            // only deserialize of document is not in cache
            if (m_document == null) {
                // first prepare to be ready to deserialize document from file store file
                prepareReadDocumentData();

                InputStream is = null;
                try {
                    is = new BufferedInputStream(new FileInputStream(getFileStore().getFile()), m_length);
                    // jump to beginning of document data
                    is.skip(m_offset);

                    final byte[] serializedDoc = new byte[m_length];
                    int redBytes = is.read(serializedDoc, 0, m_length);
                    if (redBytes == m_length) {
                        m_document = deserializedDocument(serializedDoc);
                        DOCUMENT_CACHE.put(m_docUuid, m_document);
                    } else {
                        LOGGER.error("Could not read all bytes of document.");
                    }
                } catch (final IOException e) {
                    LOGGER.error("Could not read document.", e);
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            LOGGER.error("Could not close FileStore input file stream.", e);
                        }
                    }
                }
            }
        }
    }


    /**
     * Serializes document into a byte array, which is than returned.
     * @param doc The document to serialize.
     * @return The byte array containing the serialized document.
     * @throws IOException If document data can not be serialized.
     */
    static byte[] serializeDocument(final Document doc) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
          TermDocumentDeSerializationUtil.fastSerializeDocument(doc, new DataOutputStream(bos));
          bos.flush();
          return bos.toByteArray();
        } finally {
            bos.close();
        }
    }

    /**
     * Deserializes document from byte array and returns new document instance.
     * @param bytes The byte array containing the serialized document data.
     * @return The new document instance.
     * @throws IOException If document can not be deserialzed from byte array.
     */
    static Document deserializedDocument(final byte[] bytes) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            return TermDocumentDeSerializationUtil.fastDeserializeDocument(dis);
        } finally {
          dis.close();
        }
    }
}

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
package org.knime.ext.textprocessing.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.LRUCache;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 * A {@link FileStoreCell} implementation holding a {@link org.knime.ext.textprocessing.data.Document}. It provides a
 * document value as well as a string value. The document data is serialized into the file of the assigned file store.
 * The address (offset), as well as the length and the uuid of the document in the file store file is serialized into
 * the cells data output.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public final class DocumentFileStoreCell extends FileStoreCell implements DocumentValue, StringValue {

    /**
     * SerialVersionID.
     */
    private static final long serialVersionUID = -8256678631254743854L;

    /* Logger */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentFileStoreCell.class);

    private static final int DEF_CACHE_SIZE = 1000;

    /* Document cache. */
    private static final LRUCache<UUID, Document> DOCUMENT_CACHE = new LRUCache<UUID, Document>(DEF_CACHE_SIZE);

    /**
     * Convenience access member for <code>DataType.getType(DocumentFileStoreCell.class)</code>.
     *
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(DocumentFileStoreCell.class);

    /* Document to store. */
    private Document m_document;

    /* Flag to specify whether cell was serialized or not, in order to avoid multiple writes. */
    private AtomicBoolean m_serialized = new AtomicBoolean(false);

    /* Offset marking the documents position in file. */
    private long m_offset;

    /* Length of the byte array storing the serialized document. */
    private int m_length;

    /* UUID as unique identifier of document. */
    private UUID m_docUuid;

    /**
     * @return The serializer of the {@link DocumentFileStoreCell}.
     */
    public static DataCellSerializer<DocumentFileStoreCell> getCellSerializer() {
        return new DataCellSerializer<DocumentFileStoreCell>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public DocumentFileStoreCell deserialize(final DataCellDataInput input) throws IOException {
                DocumentFileStoreCell docCell = new DocumentFileStoreCell();
                docCell.deserializeCell(input);
                return docCell;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void serialize(final DocumentFileStoreCell cell, final DataCellDataOutput output)
                    throws IOException {
                cell.serializeCell(output);
            }
        };
    }

    /**
     * Returns the preferred value class of this cell implementation.
     *
     * @return {@code DocumentValue.class};
     */
    public static final Class<? extends DataValue> getPreferredValueClass() {
        return DocumentValue.class;
    }

    /**
     * Constructor of {@link DocumentFileStoreCell}. Creates new instance with given document and file store to store
     * document at.
     * @param fileStore File store to store document at.
     * @param document Document to encapsulate and store in file store.
     * @throws IOException if document can not be serialized into file store file.
     */
    public DocumentFileStoreCell(final FileStore fileStore, final Document document) throws IOException {
        super(fileStore);
        m_document = document;
        m_docUuid = m_document.getUUID();

        // Write document only if it has not been already serialized.
        if (m_serialized.compareAndSet(false, true)) {
            // serialize document in byte array (no need to synchronize at this point)
            byte[] serializedDoc = serializeDocument(m_document);
            m_length = serializedDoc.length;
            final File file = getFileStore().getFile();

            // write synchronized to file to avoid parallel writing and mess up serialized document data
            synchronized (file) {
                m_offset = file.length();
                final OutputStream os = new BufferedOutputStream(new FileOutputStream(file, true), m_length);
                try {
                    os.write(serializedDoc);
                } catch (IOException e) {
                    LOGGER.error("Could not write serialized document to random access file.", e);
                    throw e;
                } finally {
                    os.close();
                }
            }
        }
    }

    /**
     * Empty constructor.
     */
    DocumentFileStoreCell() {
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
        return m_document.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        if (dc == null) {
            return false;
        }
        if (!m_document.equals(((DocumentFileStoreCell)dc).getDocument())) {
            return false;
        }

        return true;
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
    private void serializeCell(final DataCellDataOutput output) throws IOException {
        // write document to file store (if it has not been written bevore)
        flushToFileStore();

        // write meta data
        output.writeLong(m_offset);
        output.writeInt(m_length);
        output.writeUTF(m_docUuid.toString());
    }

    /**
     * Serializes document into a byte array, which is than returned.
     * @param doc The document to serialize.
     * @return The byte array containing the serialized document.
     * @throws IOException If document data can not be serialized.
     */
    private byte[] serializeDocument(final Document doc) throws IOException {
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
    private Document deserializedDocument(final byte[] bytes) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            return TermDocumentDeSerializationUtil.fastDeserializeDocument(dis);
        } finally {
          dis.close();
        }
    }

    /**
     * Deserializes document address information from given data input.The document itself is not deserialzed at this
     * point, only its offset, length and uuid information.
     * @param input The input to read the document address information from.
     * @throws IOException If document address information can not be deserialized from given data input.
     */
    private void deserializeCell(final DataCellDataInput input) throws IOException {
        // read offset, length, and uuid, set serialized flag true (since cell has obviously been serialized before)
        m_serialized = new AtomicBoolean(true);
        m_offset = input.readLong();
        m_length = input.readInt();
        m_docUuid = UUID.fromString(input.readUTF());
    }

    /**
     * If document has not been deserialized and is not already in cache it is deserialized from file store file and
     * than put to cache.
     */
    private synchronized void readDocumentData() {
        if (m_document == null && m_docUuid != null) {
            m_document = DOCUMENT_CACHE.get(m_docUuid);
            if (m_document == null) {
                InputStream is = null;
                try {
                    final File file = getFileStore().getFile();
                    final FileInputStream fileInput = new FileInputStream(file);
                    // jump to beginning of document data
                    fileInput.skip(m_offset);
                    is = new BufferedInputStream(fileInput, m_length);

                    byte[] serializedDoc = new byte[m_length];
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
     * {@inheritDoc}
     */
    @Override
    protected void flushToFileStore() throws IOException { }
}

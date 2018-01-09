/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;

/**
 * A {@link FileStoreCell} implementation holding a {@link org.knime.ext.textprocessing.data.Document}. It provides a
 * document value as well as a string value. The document data is serialized into the file of the assigned file store.
 * The address (offset), as well as the length and the uuid of the document in the file store file is serialized into
 * the cells data output.
 * The serialization of the document data into the file store file is called in the cells non empty constructor.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.9
 */
public final class DocumentFileStoreCell extends AbstractDocumentFileStoreCell {
    /**
     * Serializer for {@link DocumentFileStoreCell}s.
     *
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class Serializer implements DataCellSerializer<DocumentFileStoreCell> {
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
        public void serialize(final DocumentFileStoreCell cell, final DataCellDataOutput output) throws IOException {
            cell.serializeCell(output);
        }
    }

    /** SerialVersionID. */
    private static final long serialVersionUID = -8429008711201330855L;


    /* Logger */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentFileStoreCell.class);

    /**
     * Convenience access member for {@code DataType.getType(DocumentFileStoreCell.class)}.
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(DocumentFileStoreCell.class);

    /**
     * Constructor of {@link DocumentFileStoreCell}. Creates new instance with given document and file store to store
     * document at.
     * @param fileStore File store to store document at.
     * @param document Document to encapsulate and store in file store.
     * @throws IOException if document cannot be serialized into file store file.
     */
    public DocumentFileStoreCell(final FileStore fileStore, final Document document) throws IOException {
        super(fileStore, document);

        // Write document only if it has not been already serialized.
        if (m_serialized.compareAndSet(false, true)) {
            // serialize document in byte array (no need to synchronize at this point)
            byte[] serializedDoc = AbstractDocumentFileStoreCell.serializeDocument(m_document);
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
    protected void flushToFileStore() throws IOException { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareReadDocumentData() { }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

import java.io.IOException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStoreCell;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * A {@link FileStoreCell} implementation holding a {@link org.knime.ext.textprocessing.data.Document}. It provides a
 * document value as well as a string value. The document data is serialized into the file of the assigned file store.
 * The address (offset), as well as the length and the uuid of the document in the file store file is serialized into
 * the cells data output.
 * Documents are serialized in a buffered manner into file store files.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public final class DocumentBufferedFileStoreCell extends AbstractDocumentFileStoreCell {

    /** SerialVersionID. */
    private static final long serialVersionUID = -8256678631254743854L;


    /* Logger */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentBufferedFileStoreCell.class);

    /**
     * Convenience access member for {@code DataType.getType(DocumentFileStoreCell.class)}.
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(DocumentBufferedFileStoreCell.class);

    /* Buffered file store writer, writing document data to file store file. */
    private BufferedFileStoreWriter m_bufferedFileStoreWriter;


    /**
     * Returns the preferred value class of this cell implementation.
     *
     * @return {@code DocumentValue.class};
     */
    public static final Class<? extends DataValue> getPreferredValueClass() {
        return DocumentValue.class;
    }

    /**
     * @return The serializer of the {@link DocumentBufferedFileStoreCell}.
     */
    public static DataCellSerializer<DocumentBufferedFileStoreCell> getCellSerializer() {
        return new DataCellSerializer<DocumentBufferedFileStoreCell>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public DocumentBufferedFileStoreCell deserialize(final DataCellDataInput input) throws IOException {
                DocumentBufferedFileStoreCell docCell = new DocumentBufferedFileStoreCell();
                docCell.deserializeCell(input);
                return docCell;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void serialize(final DocumentBufferedFileStoreCell cell, final DataCellDataOutput output)
                    throws IOException {
                cell.serializeCell(output);
            }
        };
    }

    /**
     * Constructor of {@link DocumentBufferedFileStoreCell}. Creates new instance with given document and file store
     * to store document at.
     * @param fileStore File store to store document at.
     * @param bufferedFileStoreWriter The buffered file store writer to use for serialization of document data into
     * file store file.
     * @param document Document to encapsulate and store in file store.
     * @throws IOException if document can not be serialized into file store file.
     */
    public DocumentBufferedFileStoreCell(final FileStore fileStore,
        final BufferedFileStoreWriter bufferedFileStoreWriter, final Document document) throws IOException {
        super(fileStore, document);
        m_bufferedFileStoreWriter = bufferedFileStoreWriter;

        // Write document only if it has not been already serialized.
        if (m_serialized.compareAndSet(false, true)) {
            final DocumentAddress address = m_bufferedFileStoreWriter.write(m_document);
            m_docUuid = address.getUuid();
            m_offset = address.getOffset();
            m_length = address.getLength();
        }
    }

    /**
     * Empty constructor.
     */
    DocumentBufferedFileStoreCell() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        if (dc == null) {
            return false;
        }
        if (!m_document.equals(((DocumentBufferedFileStoreCell)dc).getDocument())) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void flushToFileStore() throws IOException {
        // if cell has been create with empty constructor the file store writer instance has been been initialized.
        // Thus it has to be requested at this point.
        if (m_bufferedFileStoreWriter == null) {
            m_bufferedFileStoreWriter =
                    BufferedFileStoreWriterFactory.instance().getBufferedFileStoreWriter(getFileStore());
        }

        // flush and hand over file store, which file may have been moved.
        m_bufferedFileStoreWriter.flush(getFileStore());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareReadDocumentData() {
        try {
            flushToFileStore();
        } catch (IOException e1) {
            LOGGER.warn("Could not flush to file store before read", e1);
        }
    }
}

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

import java.io.IOException;
import java.util.UUID;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.TextContainer;
import org.knime.ext.textprocessing.preferences.StoragePreferenceInitializer;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;

/**
 * A {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory}
 * creating {@link org.knime.ext.textprocessing.data.filestore.DocumentFileStoreCell}s containing given
 * {@link org.knime.ext.textprocessing.data.Document}s. It can be specified how many documents will be stored in one
 * file store file.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public final class DocumentFileStoreDataCellFactory implements TextContainerDataCellFactory {

    /* Logger */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentFileStoreDataCellFactory.class);

    private FileStore m_fileStore;

    private ExecutionContext m_exec;

    private int m_cellsInFileStore = 0;

    private int m_maxCellsInFileStore;

    /**
     * Creates new instance of {@link DocumentFileStoreDataCellFactory} with default number of maximal documents stored
     * in one file store file.
     */
    public DocumentFileStoreDataCellFactory() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        m_maxCellsInFileStore = pStore.getInt(StoragePreferenceInitializer.PREF_FILESTORE_CHUNKSIZE);
    }

    /**
     * Creates new instance of {@link DocumentFileStoreDataCellFactory} with given number of maximal documents stored
     * in one file store file.
     * @param maxCellsInFileStore The maximal number of documents to store in one file store file.
     */
    public DocumentFileStoreDataCellFactory(final int maxCellsInFileStore) {
        m_maxCellsInFileStore = maxCellsInFileStore;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare(final ExecutionContext exec) {
        m_exec = exec;
        createNewFileStore();
    }

    /**
     * Creates new file store with random uuid.
     */
    private void createNewFileStore() {
        try {
            final String fileStore = UUID.randomUUID().toString();
            LOGGER.debug("Creating file store: " + fileStore);
            m_fileStore = m_exec.createFileStore(fileStore);
        } catch (IOException e) {
            LOGGER.error("Could not create file store.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell createDataCell(final TextContainer tc) {
        DataCell dc = null;
        if (tc instanceof Document) {
            Document doc = (Document)tc;
            try {
                dc = new DocumentFileStoreCell(m_fileStore, doc);

                m_cellsInFileStore++;
                if (m_cellsInFileStore >= m_maxCellsInFileStore) {
                    createNewFileStore();
                    m_cellsInFileStore = 0;
                }
            } catch (IOException e) {
                LOGGER.error("Could not create DocumentFileStoreCell for document: " + doc.getUUID(), e);
            }
        }
        return dc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return DocumentFileStoreCell.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateCellType(final DataCell cell) {
        if (cell instanceof DocumentFileStoreCell) {
            return true;
        }
        return false;
    }
}

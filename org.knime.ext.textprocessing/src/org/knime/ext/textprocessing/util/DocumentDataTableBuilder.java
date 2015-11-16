/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * History
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;

/**
 * Provides convenient methods that create
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * {@link org.knime.ext.textprocessing.data.Document}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentDataTableBuilder implements DataTableBuilder {

    /**
     * The default document column name in document data tables.
     */
    public static final String DEF_DOCUMENT_COLNAME = "Document";

    /**
     * The default document vector column name in document data tables.
     */
    public static final String DEF_DOCUMENT_VECTOR_COLNAME = "Document Vector";

    private final TextContainerDataCellFactory m_documentCellFac;

    /**
     * Empty constructor of <code>DocumentDataTableBuilder</code>.
     */
    public DocumentDataTableBuilder() {
        m_documentCellFac =
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
    }

    /**
     * Creates and returns a {@link org.knime.core.node.BufferedDataTable}
     * containing a single column with the given documents as
     * {@link org.knime.ext.textprocessing.data.DocumentCell}s, one for each
     * row.
     *
     * @param exec The <code>ExecutionContext</code> to create the
     * <code>BufferedDataTable</code> with.
     * @param docs The list of
     * {@link org.knime.ext.textprocessing.data.Document}s to keep in the table.
     * @return The <code>BufferedDataTable</code> containing the given
     * documents.
     * @throws CanceledExecutionException If execution was canceled.
     *
     * @deprecated Use
     * {@link DocumentDataTableBuilder#openDataTable(ExecutionContext)},
     * {@link DocumentDataTableBuilder#addDocument(Document, RowKey)}, and
     * {@link DocumentDataTableBuilder#getAndCloseDataTable()} instead.
     */
    @Deprecated
    public BufferedDataTable createDataTable(final ExecutionContext exec, final List<Document> docs)
        throws CanceledExecutionException {
        // create cache
        m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
        DataCellCache cache = new LRUDataCellCache(m_documentCellFac);
        BufferedDataContainer dc = exec.createDataContainer(this.createDataTableSpec());

        try {
            int i = 1;
            for (Document d : docs) {
                exec.checkCanceled();
                RowKey rowKey = new RowKey(new Integer(i).toString());
                DocumentBlobCell docCell = (DocumentBlobCell)cache.getInstance(d);
                DataRow row = new DefaultRow(rowKey, docCell);
                dc.addRowToTable(row);
                i++;
            }
        } finally {
            cache.close();
            dc.close();
        }

        return dc.getTable();
    }


    private DataCellCache m_cache;

    private BufferedDataContainer m_dc;

    private boolean m_opened = false;

    private long m_rowRey = 0;

    /**
     * Creates new DataContainer to add rows to and create a
     * {@link org.knime.core.node.BufferedDataTable}, with one column containing
     * the added documents as
     * {@link org.knime.ext.textprocessing.data.DocumentBlobCell}s. Before
     * adding and closing this open method must be called.
     *
     * @param exec The <code>ExecutionContext</code> to create the
     * <code>BufferedDataTable</code> with.
     */
    public void openDataTable(final ExecutionContext exec) {
        // create cache
        //m_cache = new FullDataCellCache(m_documentCellFac);
        m_cache = new LRUDataCellCache(m_documentCellFac);

        m_dc = exec.createDataContainer(this.createDataTableSpec());
        m_opened = true;
        m_rowRey = 0;
        m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
    }

    /**
     * Adds a row with the given document as
     * {@link org.knime.ext.textprocessing.data.DocumentBlobCell} to the opened
     * data container. The method
     * {@link DocumentDataTableBuilder#openDataTable(ExecutionContext)} has to
     * called before adding and closing is possible, otherwise an
     * <code>IllegalStateException</code> is thrown.
     *
     * @param d The document to add.
     * @param rowKey The <code>RowKey</code> to set.
     * @throws IllegalStateException If the
     * <code>DocumentDataTableBuilder</code> has not been opened before.
     */
    public synchronized void addDocument(final Document d, final RowKey rowKey)
        throws IllegalStateException {
        if (m_opened) {
            final DataCell docCell = m_cache.getInstance(d);
            final DataRow row = new DefaultRow(rowKey, docCell);
            m_dc.addRowToTable(row);
        } else {
            throw new IllegalStateException("DocumentDataTableBuilder has "
                    + "not been opened! Open before add.");
        }
    }

    /**
     * Adds a row with the given document as
     * {@link org.knime.ext.textprocessing.data.DocumentBlobCell} to the opened
     * data container. The method
     * {@link DocumentDataTableBuilder#openDataTable(ExecutionContext)} has to
     * called before adding and closing is possible, otherwise an
     * <code>IllegalStateException</code> is thrown. As <code>RowKey</code>
     * a number is used, incremented by 1 each time this method is called.
     * The row key number starts at 0, when the data table builder is opened.
     *
     * @param d The document to add.
     * @throws IllegalStateException If the
     * <code>DocumentDataTableBuilder</code> has not been opened before.
     */
    public synchronized void addDocument(final Document d)
    throws IllegalStateException {
        RowKey rowKey = RowKey.createRowKey(m_rowRey++);
        addDocument(d, rowKey);
    }

    /**
     * Closes the data container and returns the data table. The method
     * {@link DocumentDataTableBuilder#openDataTable(ExecutionContext)} has to
     * called before adding and closing is possible, otherwise an
     * <code>IllegalStateException</code> is thrown.
     *
     * @return The <code>BufferedDataTable</code> containing the given
     * documents.
     * @throws IllegalStateException If the
     * <code>DocumentDataTableBuilder</code> has not been opened before.
     */
    public BufferedDataTable getAndCloseDataTable()
        throws IllegalStateException {
        closeCache();
        if (m_opened) {
            m_dc.close();
            m_rowRey = 0;
            m_opened = false;
            return m_dc.getTable();
        } else {
            throw new IllegalStateException("DocumentDataTableBuilder has "
                    + "not been opened! Open before close.");
        }
    }

    /**
     * Closes cache, removes its entries and unregisters listeners.
     * @since 2.8
     */
    public void closeCache() {
        if (m_cache != null) {
            m_cache.close();
        }
    }

    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing just one column of type <code>Document(Blob)Cell</code> to
     * store text documents.
     *
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with just one column of type <code>Document(Blob)Cell</code>.
     */
    @Override
    public DataTableSpec createDataTableSpec() {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator dcscDocs = new DataColumnSpecCreator(
                DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME,
                m_documentCellFac.getDataType());
        return new DataTableSpec(dcscDocs.createSpec());
    }
}

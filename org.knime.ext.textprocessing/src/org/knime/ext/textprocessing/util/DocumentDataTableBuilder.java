/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 * 
 * History
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.List;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

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
     * {@link DocumentDataTableBuilder#addDocument(Document, int)}, and 
     * {@link DocumentDataTableBuilder#getAndCloseDataTable()} instead.
     */
    @Deprecated
    public BufferedDataTable createDataTable(final ExecutionContext exec,
            final List<Document> docs) throws CanceledExecutionException {
      // create cache
      FullDataCellCache cache = new FullDataCellCache(m_documentCellFac);
      BufferedDataContainer dc =
              exec.createDataContainer(this.createDataTableSpec());

      int i = 1;
      for (Document d : docs) {
          exec.checkCanceled();
          RowKey rowKey = new RowKey(new Integer(i).toString());
          DocumentBlobCell docCell = (DocumentBlobCell)cache.getInstance(d);
          DataRow row = new DefaultRow(rowKey, docCell);
          dc.addRowToTable(row);
          i++;
      }
      dc.close();

      cache.reset();
      docs.clear();
      
      return dc.getTable();
    }
    
    
    private FullDataCellCache m_cache;
    
    private BufferedDataContainer m_dc;
    
    private boolean m_opened = false;
    
    private int m_rowRey = 0;
    
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
        m_cache = new FullDataCellCache(m_documentCellFac);
        m_dc = exec.createDataContainer(this.createDataTableSpec());
        m_opened = true;
        m_rowRey = 0;
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
            if (!TextprocessingPreferenceInitializer.useBlobCell()) {
                // Cast to regular cell
                DocumentCell docCell = 
                    (DocumentCell)m_cache.getInstance(d);
                DataRow row = new DefaultRow(rowKey, docCell);
                m_dc.addRowToTable(row);
            } else {
                // Cast to Blob cell
                DocumentBlobCell docCell = 
                    (DocumentBlobCell)m_cache.getInstance(d);
                DataRow row = new DefaultRow(rowKey, docCell);
                m_dc.addRowToTable(row);
            }
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
        m_rowRey++;
        RowKey rowKey = RowKey.createRowKey(m_rowRey);
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
        if (m_opened) {
            m_dc.close();
            m_cache.reset();
            m_rowRey = 0;
            m_opened = false;
            return m_dc.getTable();
        } else {
            throw new IllegalStateException("DocumentDataTableBuilder has " 
                    + "not been opened! Open before close.");
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

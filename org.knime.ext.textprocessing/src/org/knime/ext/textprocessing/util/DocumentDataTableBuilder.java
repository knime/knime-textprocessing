/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

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

import java.util.List;

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
     */
    public BufferedDataTable createDataTable(ExecutionContext exec,
            List<Document> docs) throws CanceledExecutionException {
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

    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing just one column of type <code>Document(Blob)Cell</code> to
     * store text documents.
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with just one column of type <code>Document(Blob)Cell</code>.
     */
    public DataTableSpec createDataTableSpec() {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator dcscDocs = new DataColumnSpecCreator(
                DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME, 
                m_documentCellFac.getDataType());
        return new DataTableSpec(dcscDocs.createSpec());
    } 
}

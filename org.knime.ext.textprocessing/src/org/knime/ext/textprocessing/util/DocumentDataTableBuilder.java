/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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

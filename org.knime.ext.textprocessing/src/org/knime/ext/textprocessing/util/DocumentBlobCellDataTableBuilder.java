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
 *   03.03.2008 (Kilian Thiel): created
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

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentBlobCellDataTableBuilder extends DocumentDataTableBuilder {

    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing just one column of type <code>DocumentCell</code> to
     * store text documents.
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with just one column of type <code>DocumentListCell</code>.
     */
    public DataTableSpec createDataTableSpec() {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator dcscDocs =
                new DataColumnSpecCreator("Document", DocumentBlobCell.TYPE);
        return new DataTableSpec(dcscDocs.createSpec());
    }     
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable createDataTable(ExecutionContext exec,
            List<Document> docs) throws CanceledExecutionException {
      // create cache
      FullDataCellCache cache = new FullDataCellCache(
              new DocumentBlobDataCellFactory());
      
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

      return dc.getTable();
    }
}

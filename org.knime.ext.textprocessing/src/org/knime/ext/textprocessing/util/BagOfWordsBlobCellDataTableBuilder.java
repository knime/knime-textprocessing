/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   03.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Hashtable;
import java.util.Set;

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
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class BagOfWordsBlobCellDataTableBuilder extends
        BagOfWordsDataTableBuilder {

    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing one column of type <code>DocumentCell</code> to
     * store text documents and one column of type <code>TermCell</code>
     * representing the terms contained by a certain document. 
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with one column of type <code>DocumentListCell</code> and one
     *         column of type <code>TermCell</code>.
     */
    public DataTableSpec createDataTableSpec() {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator docs =
                new DataColumnSpecCreator("Document", DocumentBlobCell.TYPE);
        DataColumnSpecCreator terms =
            new DataColumnSpecCreator("Term", TermCell.TYPE);
        return new DataTableSpec(terms.createSpec(), docs.createSpec());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable createDataTable(ExecutionContext exec,
            Hashtable<Document, Set<Term>> docTerms, boolean useTermCache) 
    throws CanceledExecutionException {
      // create cache
      FullDataCellCache docCache = new FullDataCellCache(
              new DocumentBlobDataCellFactory());
      FullDataCellCache termCache = new FullDataCellCache(
              new TermDataCellFactory());
      
      BufferedDataContainer dc =
              exec.createDataContainer(this.createDataTableSpec());

      int i = 1;
      Set<Document> keys = docTerms.keySet();
      int rowCount = keys.size();
      int currRow = 1;
      
      for (Document d : keys) {
          DocumentBlobCell docCell = (DocumentBlobCell)docCache.getInstance(d);
          
          Set<Term> terms = docTerms.get(d);
          for (Term t : terms) {
              exec.checkCanceled();
              RowKey rowKey = new RowKey(new Integer(i).toString());
              i++;
              
              TermCell termCell;
              if (!useTermCache) {
                  termCell = new TermCell(t);
              } else {
                  termCell = (TermCell)termCache.getInstance(t);
              }
              DataRow row = new DefaultRow(rowKey, termCell, docCell);
              dc.addRowToTable(row);
          }
          
          double progress = (double)currRow / (double)rowCount;
          exec.setProgress(progress, "Creating Bow of document " + currRow 
                  + " of " + rowCount);
          exec.checkCanceled();
          currRow++;           
      }
      dc.close();
      return dc.getTable();
    }
  
}

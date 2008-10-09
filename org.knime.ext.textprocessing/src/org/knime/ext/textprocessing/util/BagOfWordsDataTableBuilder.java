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

import java.util.Hashtable;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;

/**
 * Provides convenient methods that create 
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * a bag of words which consists one column containing  
 * {@link org.knime.ext.textprocessing.data.Document}s and one column containing
 * {@link org.knime.ext.textprocessing.data.Term}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class BagOfWordsDataTableBuilder implements DataTableBuilder {
    
    /**
     * The default document column name in bow data tables.
     */
    public static final String DEF_DOCUMENT_COLNAME = "Document";
    
    /**
     * The default original document column name in bow data tables.
     */
    public static final String DEF_ORIG_DOCUMENT_COLNAME = "Orig Document";
    
    /**
     * The default term column name in bow data tables.
     */
    public static final String DEF_TERM_COLNAME = "Term";
    
    /**
     * Empty constructor of <code>BagOfWordsDataTableBuilder</code>.
     */
    public BagOfWordsDataTableBuilder() { }    
    
    /**
     * @param appendExtraDocCol if <code>true</code> an extra document column 
     * will be appended.
     * @return The <code>DataTableSpec</code> of the data table build by the
     * underlying implementation.
     */
    public abstract DataTableSpec createDataTableSpec(
            final boolean appendExtraDocCol);    
    
    /**
     * @return The corresponding factory creating a certain kind of 
     * <code>DataCell</code>s containing documents. 
     */
    protected abstract TextContainerDataCellFactory 
        getDocumentCellDataFactory();
    
    /**
     * Validates the cell type of a document i.e. underlying implementations ca
     * validate for <code>BlobCell</code> or usual <code>DataCell</code>s etc. 
     * @param documentCell The <code>DataCell</code> to validate.
     * @return <code>false</code> if given <code>DataCell</code> has not a
     * valid type, otherwise <code>true</code>.
     */
    protected abstract boolean validateDocumentCellType(
            final DataCell documentCell);

    
    
    /**
     * Creates and returns a {@link org.knime.core.node.BufferedDataTable} 
     * containing a column with the given documents and one with the terms.
     * Each row consists of a {@link org.knime.ext.textprocessing.data.TermCell}
     * {@link org.knime.ext.textprocessing.data.DocumentCell} tupel meaning that
     * a term is contained in a document.
     * 
     * @param exec The <code>ExecutionContext</code> to create the 
     * <code>BufferedDataTable</code> with.
     * @param docTerms The  
     * {@link org.knime.ext.textprocessing.data.Document}s and the corresponding 
     * set of {@link org.knime.ext.textprocessing.data.Term}s.
     * @param useTermCache If set true the created <code>TermCell</code>s are
     * cached during creation of the data table. This means that a cell
     * holding a certain term exists only once. An other cell  holding the
     * same term is only a reference to the first cell.
     * @return The <code>BufferedDataTable</code> containing the given 
     * documents and terms
     * @throws CanceledExecutionException If execution was canceled.
     */    
    public BufferedDataTable createDataTable(final ExecutionContext exec,
            final Hashtable<Document, Set<Term>> docTerms, 
            final boolean useTermCache) throws CanceledExecutionException {
        return createDataTable(exec, docTerms, null, useTermCache);
    }
    
    
    /**
     * Creates and returns a {@link org.knime.core.node.BufferedDataTable} 
     * containing the given preprocessed documents the corresponding terms
     * as well as the original documents (provided as <code>DataCell</code>). 
     * The key-column is the column containing the documents of the hash table
     * <code>docTerms</code>, according to this key documents the given terms
     * are stored in a column too. Additional to that, the original documents
     * can be provided in the has table <code>docDocCell</code>, if that table
     * is not null, the given <code>DataCell</code>s are added in an additional
     * column according to their keys.
     * 
     * @param exec The <code>ExecutionContext</code> to create the 
     * <code>BufferedDataTable</code> with.
     * @param docTerms The  
     * {@link org.knime.ext.textprocessing.data.Document}s and the corresponding 
     * set of {@link org.knime.ext.textprocessing.data.Term}s.
     * @param docDocCell Containing the original documents as 
     * <code>DataCell</code>s. If it is not null, the original documents are
     * applied as additional column.
     * @param useTermCache If set true the created <code>TermCell</code>s are
     * cached during creation of the data table. This means that a cell
     * holding a certain term exists only once. An other cell  holding the
     * same term is only a reference to the first cell.
     * @return The <code>BufferedDataTable</code> containing the given 
     * documents and terms
     * @throws CanceledExecutionException If execution was canceled.
     */
    public BufferedDataTable createDataTable(final ExecutionContext exec,
            final Hashtable<Document, Set<Term>> docTerms, 
            final Hashtable<Document, DataCell> docDocCell,
            final boolean useTermCache) throws CanceledExecutionException {
      // create cache
      FullDataCellCache docCache = new FullDataCellCache(
              getDocumentCellDataFactory());
      FullDataCellCache termCache = new FullDataCellCache(
              new TermDataCellFactory());
      
      boolean appendExtraDocCol = false;
      if (docDocCell != null && docTerms.size() == docDocCell.size()) {
          appendExtraDocCol = true;
      }
      BufferedDataContainer dc = exec.createDataContainer(
              this.createDataTableSpec(appendExtraDocCol));

      int i = 1;
      Set<Document> keys = docTerms.keySet();
      int rowCount = keys.size();
      int currRow = 1;
      
      for (Document d : keys) {
          DataCell docCell = docCache.getInstance(d);
          
          // get data cell containing original document
          DataCell docCell2 = null;
          if (appendExtraDocCol) {
               docCell2 = docDocCell.get(d);
          }
          
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
                            
              DataRow row;
              if (appendExtraDocCol) {
                  if (docCell2 == null) {
                      docCell2 = DataType.getMissingCell();
                  }
                  row = new DefaultRow(rowKey, termCell, docCell, docCell2);
              } else {
                  row = new DefaultRow(rowKey, termCell, docCell);
              }
              
              dc.addRowToTable(row);
          }
          
          double progress = (double)currRow / (double)rowCount;
          exec.setProgress(progress, "Creating Bow of document " + currRow 
                  + " of " + rowCount);
          exec.checkCanceled();
          currRow++;           
      }
      dc.close();
      
      docTerms.clear();
      docCache.reset();
      termCache.reset();
      
      return dc.getTable();
    }
    

    /**
     * Creates and returns a new {@link org.knime.core.node.BufferedDataTable}
     * containing a column with the given data cells and one with the terms. 
     * The data cells have to be compatible with <code>DocumentValue</code>,
     * otherwise an <code>IllegalArgumentException</code> will be thrown.
     * The given data cells are reused when creating the new data table,
     * no new data cells are created to save memory and take full advantage
     * of the benefit of <code>BlobDataCell</code>s.
     * 
     * @param exec The context to create a new <code>BufferedDataTable</code>
     * and monitor the progress.
     * @param docTerms A hash table containing the <code>DataCell</code>s
     * with the documents and the terms to create a data table out of.
     * @param useTermCache If true the term cells will be cached and
     * only created once for each term. Equal term are represented by the
     * same <code>TermCell</code>.
     * @return The created <code>BufferedDataTable</code> containing the given
     * data cells with the documents and terms represented by 
     * <code>TermCell</code>s.
     * @throws CanceledExecutionException If execution was canceled.
     * @throws IllegalArgumentException If a data cell is not compatible with
     * <code>DocumentValue</code>.
     */
    public BufferedDataTable createReusedDataTable(final ExecutionContext exec,
            final Hashtable<DataCell, Set<Term>> docTerms, 
            final boolean useTermCache) throws CanceledExecutionException, 
            IllegalArgumentException {
        // create cache
        FullDataCellCache termCache =
                new FullDataCellCache(new TermDataCellFactory());
        BufferedDataContainer dc =
                exec.createDataContainer(this.createDataTableSpec());

        int i = 1;
        Set<DataCell> keys = docTerms.keySet();
        int rowCount = keys.size();
        int currRow = 1;
        
        for (DataCell d : keys) {
            if (d == null) {
                continue;
            }
            
            if (!d.getType().isCompatible(DocumentValue.class)) {
                throw new IllegalArgumentException("DataCell is not " 
                        + "compatible with DocumentValue!");
            }
            
            Set<Term> terms = docTerms.get(d);
            for (Term t : terms) {
                if (t == null) {
                    continue;
                }
                exec.checkCanceled();
                RowKey rowKey = new RowKey(new Integer(i).toString());
                i++;

                TermCell termCell;
                if (!useTermCache) {
                    termCell = new TermCell(t);
                } else {
                    termCell = (TermCell)termCache.getInstance(t);
                }
                
                DataRow row = new DefaultRow(rowKey, termCell, d);
                dc.addRowToTable(row);
            }
            
            double progress = (double)currRow / (double)rowCount;
            exec.setProgress(progress, "Creating Bow of document " + currRow 
                    + " of " + rowCount);
            exec.checkCanceled();
            currRow++;               
        }
        dc.close();
        
        docTerms.clear();
        
        return dc.getTable();
    }
}

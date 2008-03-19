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

import org.knime.core.data.DataRow;
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
 * Provides convenient methods that create 
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * a bag of words which consists one column containing  
 * {@link org.knime.ext.textprocessing.data.Document}s and one column containing
 * {@link org.knime.ext.textprocessing.data.Term}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class BagOfWordsDataTableBuilder implements DataTableFactory {

    public BagOfWordsDataTableBuilder() { } 
    
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
    public abstract BufferedDataTable createDataTable(
            final ExecutionContext exec, 
            final Hashtable<Document, Set<Term>> docTerms, 
            final boolean useTermCache) throws CanceledExecutionException;

    

    public BufferedDataTable createReusedCellDataTable(
            final ExecutionContext exec,
            final Hashtable<DocumentBlobCell, Set<Term>> docTerms,
            final boolean useTermCache) throws CanceledExecutionException {
        // create cache
        FullDataCellCache termCache =
                new FullDataCellCache(new TermDataCellFactory());

        BufferedDataContainer dc =
                exec.createDataContainer(this.createDataTableSpec());

        int i = 1;
        Set<DocumentBlobCell> keys = docTerms.keySet();
        for (DocumentBlobCell d : keys) {
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
                DataRow row = new DefaultRow(rowKey, termCell, d);
                dc.addRowToTable(row);
            }
        }
        dc.close();
        return dc.getTable();
    }
}

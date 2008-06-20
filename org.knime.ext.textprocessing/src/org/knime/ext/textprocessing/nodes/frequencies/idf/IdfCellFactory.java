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
 *   18.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.idf;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.frequencies.Frequencies;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory;

/**
 * The idf cell factory computes the inverse document frequency value of each
 * term and adds the value as a new double cell.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class IdfCellFactory extends FrequencyCellFactory {

    /**
     * The name of the column containing the idf value.
     */
    public static final String COLNAME = "IDF";
    
    /**
     * The flag specifying that the column containing the tf values is a double 
     * column.
     */
    public static final boolean INT_COL = false;
    
    private Hashtable<Term, Set<Integer>> m_termDocData;
    
    private Set<Integer> m_docs;

    
    /**
     * Creates new instance of <code>IdfCellFactory</code> which computes
     * the idf value for each row and adds new column containing the values.
     * 
     * @param documentCellIndex The column index containing the documents. 
     * @param termCellindex The column index containing the terms.
     * @param docData The data table containing the complete bag of words.
     * @param exec The execution context to monitor the progress and check it
     * user canceled the process.
     * @throws CanceledExecutionException If user canceled the process.
     */
    public IdfCellFactory(final int documentCellIndex,
            final int termCellindex, final BufferedDataTable docData, 
            final ExecutionContext exec) throws CanceledExecutionException {
        super(documentCellIndex, termCellindex, COLNAME, INT_COL);
        
        m_termDocData = new Hashtable<Term, Set<Integer>>();
        m_docs = new HashSet<Integer>();
        
        int maxRows = docData.getRowCount();
        int currRow = 1;
        
        RowIterator it = docData.iterator();
        while (it.hasNext()) {
            double prog = (double)currRow / (double)maxRows;
            exec.setProgress(prog, "Computing idf value of term " + currRow 
                    + " of " + maxRows);
            exec.checkCanceled();
            currRow++;
            
            DataRow row = it.next();
            Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
            Document d = ((DocumentValue)row.getCell(getDocumentColIndex()))
                        .getDocument();
            int dHash = d.hashCode();
            
            // save doc
            if (!m_docs.contains(dHash)) {
                m_docs.add(dHash);
            }
            
            // save term and doc
            Set<Integer> docs;
            if (!m_termDocData.containsKey(t)) {
                docs = new HashSet<Integer>();
                docs.add(dHash);
                m_termDocData.put(t, docs);
            } else {
                docs = m_termDocData.get(t);
                if (!docs.contains(dHash)) {
                    docs.add(dHash);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public DataCell[] getCells(final DataRow row) {
        Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
        double idf = 0;
        if (m_termDocData.containsKey(t)) {
            idf = Frequencies.inverseDocumentFrequency(m_docs.size(), 
                    m_termDocData.get(t).size()); 
        }
        return new DoubleCell[]{new DoubleCell(idf)};
    }

}

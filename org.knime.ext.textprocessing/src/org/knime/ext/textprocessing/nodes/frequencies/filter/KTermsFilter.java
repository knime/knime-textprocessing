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
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import java.util.ArrayList;
import java.util.List;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 * Keeps a specified number k of rows and filters out the rest. The k rows that
 * are kept are those with the highest value of a specified frequency.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class KTermsFilter extends FrequencyFilter {
    
    private int m_k;
    
    private int m_count = 0;
    
    /**
     * Creates a new instance of <code>KTermsFilter</code> with the given
     * index of the term column, of the frequency column to apply the filtering 
     * to and the number k of row to keep.
     * 
     * @param termColIndex The index of the term column.
     * @param colIndex The index of the frequency column to apply the filtering 
     * to.
     * @param k The number k of rows to keep.
     */
    public KTermsFilter(final int termColIndex, final int colIndex, 
            final int k) {
        super(colIndex, termColIndex);
        m_k = k;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalMatches(DataRow row, int rowIndex) {
        m_count++;
        if (m_count <= m_k) {
            return true;
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     * @throws CanceledExecutionException 
     */
    @Override
    public DataTable preprocessData(final BufferedDataTable data,
            final ExecutionContext exec) throws CanceledExecutionException {
        DataTableSpec spec = data.getDataTableSpec();
        
        List<String> colName = new ArrayList<String>();
        colName.add(spec.getColumnSpec(m_filterColIndex).getName());
        
        boolean[] sortAsc = new boolean[1];
        sortAsc[0] = false;
        
        return new SortedTable(data, colName, sortAsc, exec);
    }   
}

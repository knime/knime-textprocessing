/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import java.util.ArrayList;
import java.util.List;

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
     * @param modifyUnmodifiable if set <code>true</code>, unmodifiable terms 
     * are modified or filtered even if they are set unmodifiable, otherwise 
     * not.
     */
    public KTermsFilter(final int termColIndex, final int colIndex, 
            final int k, final boolean modifyUnmodifiable) {
        super(colIndex, termColIndex, modifyUnmodifiable);
        m_k = k;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalMatches(final DataRow row, final int rowIndex) {
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
    public BufferedDataTable preprocessData(final BufferedDataTable data,
            final ExecutionContext exec) throws CanceledExecutionException {
        DataTableSpec spec = data.getDataTableSpec();
        
        List<String> colName = new ArrayList<String>();
        colName.add(spec.getColumnSpec(m_filterColIndex).getName());
        
        boolean[] sortAsc = new boolean[1];
        sortAsc[0] = false;
        
        return new SortedTable(data, colName, sortAsc, exec).
                    getBufferedDataTable();
    }   
}

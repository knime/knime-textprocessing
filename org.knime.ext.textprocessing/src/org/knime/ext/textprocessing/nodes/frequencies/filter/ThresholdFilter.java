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
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 * Filters rows accordant to a specified frequency column. If the frequency 
 * value is less than the given minimum value or greater than the given
 * maximum value, the row is filtered out.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class ThresholdFilter extends FrequencyFilter {
    
    private double m_min;
    
    private double m_max;
    
    /**
     * Creates a new instance of <code>FrequencyFilter</code> with the given
     * index of the term column, of the frequency column to apply the filtering 
     * to and the min and max values.
     * 
     * @param termColIndex The index of the term column.
     * @param colIndex The index of the frequency column to apply the filtering 
     * to.
     * @param min The minimum value.
     * @param max The maximum value.
     * @param modifyUnmodifiable if set <code>true</code>, unmodifiable terms 
     * are modified or filtered even if they are set unmodifiable, otherwise 
     * not.
     */
    public ThresholdFilter(final int termColIndex, final int colIndex, 
            final double min, final double max, 
            final boolean modifyUnmodifiable) {
        super(colIndex, termColIndex, modifyUnmodifiable);
        m_min = min;
        m_max = max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean internalMatches(final DataRow row, final int rowIndex) {
        DataCell cell = row.getCell(m_filterColIndex);
        if (cell.getType().isCompatible(DoubleValue.class)) {
            double val = ((DoubleValue)cell).getDoubleValue();
            if (val >= m_min && val <= m_max) {
                return true;
            }
        } else if (cell.getType().isCompatible(IntValue.class)) {
            int val = ((IntValue)cell).getIntValue();
            if (val >= m_min && val <= m_max) {
                return true;
            }
        }
        return false;
    }    

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable preprocessData(final BufferedDataTable data,
            final ExecutionContext exec) throws CanceledExecutionException {
        return data;
    }
}

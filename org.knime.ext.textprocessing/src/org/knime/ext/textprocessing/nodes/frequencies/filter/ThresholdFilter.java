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

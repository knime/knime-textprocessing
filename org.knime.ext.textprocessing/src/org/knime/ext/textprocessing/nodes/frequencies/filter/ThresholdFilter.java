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

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.IntValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class ThresholdFilter extends FrequencyFilter {
    
    private double m_min;
    
    private double m_max;
    
    public ThresholdFilter(final int termColIndex, final int colIndex, 
            final double min, final double max) {
        super(colIndex, termColIndex);
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
    public DataTable preprocessData(final BufferedDataTable data,
            final ExecutionContext exec) throws CanceledExecutionException {
        return data;
    }
}

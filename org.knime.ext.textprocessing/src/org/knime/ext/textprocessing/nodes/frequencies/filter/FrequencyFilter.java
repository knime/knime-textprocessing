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

import org.knime.base.node.preproc.filter.row.rowfilter.EndOfTableException;
import org.knime.base.node.preproc.filter.row.rowfilter.IncludeFromNowOn;
import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyFilter extends RowFilter {

    protected int m_filterColIndex;
    
    private int m_termColIndex;
    
    public FrequencyFilter(final int filterColIndex, final int termColIndex) {
        m_filterColIndex = filterColIndex;
        m_termColIndex = termColIndex;
    }
    
    public abstract DataTable preprocessData(
            final BufferedDataTable data, final ExecutionContext exec) 
    throws CanceledExecutionException ;
    
    public abstract boolean internalMatches(final DataRow row, 
            final int rowIndex);
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean matches(final DataRow row, final int rowIndex)
            throws EndOfTableException, IncludeFromNowOn {
        DataCell cell = row.getCell(m_termColIndex);
        if (cell.getType().isCompatible(TermValue.class)) {
            Term t = ((TermValue)cell).getTermValue();
            if (t.isUnmodifiable()) {
                return true;
            }
        }
        return internalMatches(row, rowIndex);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveSettings(final NodeSettingsWO cfg) {
        throw new IllegalStateException("Not intended for permanent usage");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DataTableSpec configure(final DataTableSpec inSpec)
            throws InvalidSettingsException {
        throw new IllegalStateException("Not intended for permanent usage");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final void loadSettingsFrom(final NodeSettingsRO cfg)
            throws InvalidSettingsException {
        throw new IllegalStateException("Not intended for permanent usage");
    }
}

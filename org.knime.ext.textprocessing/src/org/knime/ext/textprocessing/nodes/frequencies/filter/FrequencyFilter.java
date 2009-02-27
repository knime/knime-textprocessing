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

import org.knime.base.node.preproc.filter.row.rowfilter.EndOfTableException;
import org.knime.base.node.preproc.filter.row.rowfilter.IncludeFromNowOn;
import org.knime.base.node.preproc.filter.row.rowfilter.RowFilter;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
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
 * This abstract class provides methods to filter rows, like a
 * {@link org.knime.base.node.preproc.filter.row.rowfilter.RowFilter} by
 * taking various kinds of term frequencies into account. The index of the
 * column containing the frequency has to be specified, as well as the index
 * of the column containing the terms. Underlying implementations has
 * to implement the abstract method 
 * {@link FrequencyFilter#internalMatches(DataRow, int)}. In this method the 
 * filtering strategy can be specified. Additionally the data table to filter
 * can be preprocessed, by implementing the 
 * {@link FrequencyFilter#preprocessData(BufferedDataTable, ExecutionContext)}
 * method in a certain way. Preprocessing can be necessary or useful i.e.
 * by sorting the data table before filtering.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyFilter extends RowFilter {

    /**
     * The index of the column containing the frequency to applie the filter.
     */
    protected int m_filterColIndex;
    
    private int m_termColIndex;
    
    private boolean m_modifyUnmodifiable = false;
    
    /**
     * Creates a new instance of <code>FrequencyFilter</code> with given indices
     * specifying the term column, the column to apply the filtering to and the
     * flag enabling the filtering of unmodifiable terms.
     * 
     * @param filterColIndex The column to apply the filtering to.
     * @param termColIndex The column containing the terms.
     * @param modifyUnmodifiable if set <code>true</code>, unmodifiable terms 
     * are modified or filtered even if they are set unmodifiable, otherwise 
     * not.
     */
    public FrequencyFilter(final int filterColIndex, final int termColIndex,
            final boolean modifyUnmodifiable) {
        m_filterColIndex = filterColIndex;
        m_termColIndex = termColIndex;
        m_modifyUnmodifiable = modifyUnmodifiable;
    }
    
    /**
     * Creates a new instance of <code>FrequencyFilter</code> with given indices
     * specifying the term column and the column to apply the filtering to.
     * 
     * @param filterColIndex The column to apply the filtering to.
     * @param termColIndex The column containing the terms.
     */
    public FrequencyFilter(final int filterColIndex, final int termColIndex) {
        this(filterColIndex, termColIndex, false);
    }
    
    /**
     * Preprocesses the given data table and returns it as a new data table.
     * 
     * @param data The data table to preprocess.
     * @param exec The <code>ExecutionContext</code> to use.
     * @return The preprocessed data table.
     * @throws CanceledExecutionException If user canceled the execution.
     */
    public abstract BufferedDataTable preprocessData(
            final BufferedDataTable data, final ExecutionContext exec) 
    throws CanceledExecutionException;
    
    /**
     * This method is called right after
     * {@link FrequencyFilter#matches(DataRow, int)}. By implementing this 
     * method the strategy of filtering can be specified. For rows that have 
     * to be filtered out, <code>false</code> has to be returned, otherwise
     * <code>true</code>. 
     * 
     * @param row The row to filter or not.
     * @param rowIndex The index of the row.
     * @return <code>true</code> if the row is not filtered <code>false</code>
     * if the row has to be filtered.
     */
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
            if (t.isUnmodifiable() && !m_modifyUnmodifiable) {
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

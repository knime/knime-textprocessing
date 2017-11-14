/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import org.knime.base.node.preproc.filter.row.rowfilter.AbstractRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.EndOfTableException;
import org.knime.base.node.preproc.filter.row.rowfilter.IRowFilter;
import org.knime.base.node.preproc.filter.row.rowfilter.IncludeFromNowOn;
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
 * {@link IRowFilter} by
 * taking various kinds of term frequencies into account. The index of the
 * column containing the frequency has to be specified, as well as the index
 * of the column containing the terms. Underlying implementations has
 * to implement the abstract method
 * {@link FrequencyFilter#internalMatches(DataRow, long)}. In this method the
 * filtering strategy can be specified. Additionally the data table to filter
 * can be preprocessed, by implementing the
 * {@link FrequencyFilter#preprocessData(BufferedDataTable, ExecutionContext)}
 * method in a certain way. Preprocessing can be necessary or useful i.e.
 * by sorting the data table before filtering.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyFilter extends AbstractRowFilter {

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
     * {@link FrequencyFilter#matches(DataRow, long)}. By implementing this
     * method the strategy of filtering can be specified. For rows that have
     * to be filtered out, <code>false</code> has to be returned, otherwise
     * <code>true</code>.
     *
     * @param row The row to filter or not.
     * @param rowIndex The index of the row.
     * @return <code>true</code> if the row is not filtered <code>false</code>
     * if the row has to be filtered.
     * @since 3.0
     */
    public abstract boolean internalMatches(final DataRow row, final long rowIndex);


    /**
     * {@inheritDoc}
     * @since 3.0
     */
    @Override
    public final boolean matches(final DataRow row, final long rowIndex) throws EndOfTableException, IncludeFromNowOn {
        DataCell cell = row.getCell(m_termColIndex);
        if (!cell.isMissing() && cell.getType().isCompatible(TermValue.class)) {
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

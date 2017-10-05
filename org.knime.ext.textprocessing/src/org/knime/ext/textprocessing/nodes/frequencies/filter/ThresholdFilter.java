/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
    public boolean internalMatches(final DataRow row, final long rowIndex) {
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

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


/**
 * A simple factory which creates concrete implementations of 
 * <code>FrequencyFilter</code>s conveniently.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class FilterFactory {

    private FilterFactory() { }
    
    /**
     * Creates and returns an instance of a concrete 
     * <code>FrequencyFilter</code> implementation. The given 
     * <code>filterOption</code> specifies which kind of 
     * <code>FrequencyFilter</code> is created. The additional parameter
     * specify the index of the column containing the terms, the column to apply
     * the filtering to, the number of rows to keep and the min and max value.
     * 
     * @param filterOption Specifies which filter is created
     * @param termColIndex The index of the column containing terms.
     * @param filterColIndex The index of the column to apply the filter method 
     * to.
     * @param number The number of rows to keep (the rest of the rows is 
     * filtered).
     * @param minVal The min value of the filter column's number to be not 
     * filtered.
     * @param maxVal The max value of the filter column's number to be not 
     * filtered.
     * @param modifyUnmodifiable if set <code>true</code>, unmodifiable terms 
     * are modified or filtered even if they are set unmodifiable, otherwise 
     * not.
     * @return A new instance of <code>FrequencyFilter</code>.
     */
    public static final FrequencyFilter createFilter(final String filterOption, 
            final int termColIndex, final int filterColIndex, final int number, 
            final double minVal, final double maxVal, 
            final boolean modifyUnmodifiable) {
        if (filterOption.equals(FilterNodeModel.SELECTION_NUMBER)) {
            return new KTermsFilter(termColIndex, filterColIndex, number, 
                    modifyUnmodifiable);
        } else if (filterOption.equals(FilterNodeModel.SELECTION_THRESHOLD)) {
            return new ThresholdFilter(termColIndex, filterColIndex, minVal, 
                    maxVal, modifyUnmodifiable);
        }
        return null;
    }
    
}

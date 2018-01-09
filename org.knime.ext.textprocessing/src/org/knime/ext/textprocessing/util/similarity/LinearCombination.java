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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Jul 22, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.similarity;

import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.node.NodeLogger;

/**
 * Linearly combines multiple similarity measures. In practice, this is quite
 * hard to achieve properly, as there is a risk of the measures adding up to
 * a very similar value for all pairs.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class LinearCombination<T> extends SimilarityMeasure<T> {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(LinearCombination.class);

    private Map<SimilarityMeasure<T>, Double> m_measures;

    private double m_lowerbound;
    private double m_higherbound;

    /**
     * @param measures the measures and their associated weight
     * @param threshold inclusive value for elements to be considered similar
     */
    public LinearCombination(final Map<SimilarityMeasure<T>, Double> measures,
            final double threshold) {
        super();

        if (measures.isEmpty()) {
            throw new IllegalArgumentException("At least one measure must be "
                    + "provided.");
        }

        // Infer the bounds from the measures
        m_lowerbound = m_higherbound = 0.0;
        for (Entry<SimilarityMeasure<T>, Double> e : measures.entrySet()) {
            SimilarityMeasure<T> m = e.getKey();
            double weight = e.getValue();

            m_lowerbound += m.getLowerBound();
            m_higherbound += weight * m.getHigherBound();
        }

        setThreshold(threshold);

        m_measures = measures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double s = 0.0;

        for (Entry<SimilarityMeasure<T>, Double> me : m_measures.entrySet()) {
            SimilarityMeasure<T> m = me.getKey();
            double weight = me.getValue();

            s += weight * m.getValue(e1, e2);
        }
        LOGGER.debug(e1 + "::" + e2 + "=" + s);
        return s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHigherBound() {
        return m_higherbound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLowerBound() {
        return m_lowerbound;
    }

}

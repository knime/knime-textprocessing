/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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

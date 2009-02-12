/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
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
 *   Jul 22, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.similarity;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Linearly combines multiple similarity measures. In practice, this is quite
 * hard to achieve properly, as there is a risk of the measures adding up to
 * a very similar value for all pairs.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class LinearCombination<T> extends SimilarityMeasure<T> {

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
            throw new IllegalArgumentException("At least one measure must be " +
            		"provided.");
        }

        // Infer the bounds from the measures
        m_lowerbound = m_higherbound = 0.0;
        for (Entry<SimilarityMeasure<T>,Double> e : measures.entrySet()) {
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
        System.out.println(e1 + "::" + e2 + "=" + s);
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

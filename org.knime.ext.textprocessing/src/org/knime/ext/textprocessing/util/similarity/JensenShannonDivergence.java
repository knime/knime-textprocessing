/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   Jul 21, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.similarity;

import org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare.Event;

/**
 * Calculates the Jensen-Shannon divergence between the conditional
 * probability distributions of e1 and e2. This is a normalized and smoothed
 * version of the Kullback-Leibler Divergence and effectively computes the
 * information gain achieved by using the average of t1 and t2 instead of
 * considering them as separate distributions.
 *
 * J(P,Q) = 1/2 [ D(P || (P+Q)/2) + D(Q || (P+Q)/2) ]
 * where D is the Kullback-Leibler divergence.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class JensenShannonDivergence<T> extends SimilarityMeasure<T> {

    private Event<T> m_event;

    /**
     * @param threshold inclusive threshold which, when exceeded, will imply
     * similarity between two elements.
     * @param event probability information for the outcomes
     */
    public JensenShannonDivergence(
            final double threshold, final Event<T> event) {
        super(threshold);
        m_event = event;
    }

    /**
     * @param e1 the first element
     * @param e2 the second element
     * @return J, a number in [0, log(2)] where log(2) is the highest similarity
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double j = 0.0;

        // Only consider outcomes with P(x|e1) and P(x|e2) properly defined
        for (T e : m_event.getDomain()) {
            double p1 = m_event.getConditionalProbability(e, e1);
            if (p1 > 0) {
                double p2 = m_event.getConditionalProbability(e, e2);
                if (p2 > 0) {
                    double sum = p1+p2;
                    j += (p1*Math.log(p1) + p2*Math.log(p2) - sum*Math.log(sum));
                }
            }
        }

        j = Math.log(2) + (j / 2.0);

        // Rounding errors may have occurred, leaving us with values outside
        // our domain.
        j = clamp(j, 0, Math.log(2));

        return j;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHigherBound() {
        return Math.log(2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLowerBound() {
        return 0.0;
    }
}

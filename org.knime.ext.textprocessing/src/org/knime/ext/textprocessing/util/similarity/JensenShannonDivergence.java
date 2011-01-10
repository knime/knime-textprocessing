/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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

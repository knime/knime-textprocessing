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
 *   Jul 21, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.similarity;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare.Event;

/**
 * L1 norm: if the conditional probability of all possible outcomes of a
 * distribution with regards to two outcomes are similar, then those two
 * outcomes are similar.
 *
 * A typical threshold value is 0.4.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class NormalizedL1<T> extends SimilarityMeasure<T> {
    private Event<T> m_event;

    private NodeLogger m_logger =
        org.knime.core.node.NodeLogger.getLogger(getClass());

    /**
     * @param threshold inclusive threshold above which two elements are
     * considered as similar.
     * @param event probability information for the outcomes
     */
    public NormalizedL1(final double threshold, final Event<T> event) {
        super(threshold);

        m_event = event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double totalp1 = 0.0;
        double totalp2 = 0.0;
        double totaldiff = 0.0;

        for (T e : m_event.getDomain()) {
            double p1 = m_event.getConditionalProbability(e, e1);
            double p2 = m_event.getConditionalProbability(e, e2);

            if (p1 != 0 && p2 != 0) {
                totalp1 += p1;
                totalp2 += p2;
                totaldiff += Math.abs(p1 - p2);
            }
        }

        // Usually between [0,2] with 0 as highest similarity.
        // Normalize to [0,1] with 1 as highest similarity
        double l = 1 - (2 + totaldiff - totalp1 - totalp2) / 2.0;

        // Rounding errors may have occurred
        l = clamp(l, 0, 1);

        if (m_logger.isDebugEnabled()) {
            m_logger.debug("L1(" + e1 + "," + e2 + ") = " + l);
        }

        return l;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHigherBound() {
        return 1.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLowerBound() {
        return 0.0;
    }
}

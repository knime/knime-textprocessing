/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 * Normalized pointwise mutual information. It is a measure of the discrepancy
 * between the probability of the coincidence of two events versus the
 * probability of their coincidence given their individual distributions
 * (assuming independence).
 *
 * See http://en.wikipedia.org/wiki/Pointwise_mutual_information
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of element to compare
 */
public class PointwiseMutualInformation<T> extends SimilarityMeasure<T> {
    private static double NORMALIZINGFACTOR = Math.PI / 2.0;

    private Event<T> m_event;

    private NodeLogger m_logger =
        org.knime.core.node.NodeLogger.getLogger(getClass());

    /**
     * @param threshold exclusive threshold above which two events are
     * considered as similar.
     * @param event probability distribution information for the events
     */
    public PointwiseMutualInformation(
            final double threshold, final Event<T> event) {
        super(threshold);

        m_event = event;
    }

    /**
     * Point-wise mutual information
     *
     * @param e1 the first event
     * @param e2 the second event
     * @return log( P(t1, t2) / P(t1)*P(t2)) normalized to [0,1],
     *     where 0 means total independence
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double p1 = m_event.getProbability(e1);
        double p2 = m_event.getProbability(e2);
        double joint = m_event.getJointProbability(e1, e2);

        if (joint == 0.0) {
            return 0.0;
        } else if (p1 == 0.0 || p2 == 0.0) {
            return 1.0;
        } else {
            // Normalize using arc tan. The loss of precision for larger values
            // is not dramatic, as it is rare that this will go over 8-10 and
            // this measure is typically used with a low threshold
            double mi = Math.log(joint / (p1 * p2));
            double normalized = clamp(Math.atan(mi) / NORMALIZINGFACTOR, 0.0, 1.0);

            if (m_logger.isDebugEnabled()) {
                m_logger.debug("PMI(" + e1 + "," + e2 + ") = " + normalized);
            }

            return normalized;
        }
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

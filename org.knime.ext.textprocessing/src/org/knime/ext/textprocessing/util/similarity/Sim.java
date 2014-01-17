/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

import org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare.Event;

/**
 * Compares the discrepancy between the sum of the lowest pointwise mutual
 * information (e1 to the whole domain vs e2 to the whole domain) with the
 * lowest.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class Sim<T> extends SimilarityMeasure<T> {

    private Event<T> m_event;
    private PointwiseMutualInformation<T> m_pmi;

    /**
     * @param threshold inclusive threshold over which two elements are
     * considered as similar
     * @param event probability information for the elements
     */
    public Sim(final double threshold, final Event<T> event) {
        super(threshold);

        m_event = event;
        m_pmi = new PointwiseMutualInformation<T>(0, event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double minsum = 0.0;
        double maxsum = 0.0;

        for (T e : m_event.getDomain()) {
            final double miE1 = m_pmi.getValue(e, e1);
            final double miE2 = m_pmi.getValue(e, e2);

            if (miE1 > miE2) {
                minsum += miE2;
                maxsum += miE1;
            } else {
                minsum += miE1;
                maxsum += miE2;
            }
        }

        double avg;

        if (maxsum == 0) {
            avg = 1;
        } else {
            avg = minsum / maxsum;
        }

        return clamp(avg, 0, 1);
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

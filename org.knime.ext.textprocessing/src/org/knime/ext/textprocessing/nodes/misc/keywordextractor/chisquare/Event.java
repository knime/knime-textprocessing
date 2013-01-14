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
package org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare;

import java.util.Set;

/**
 * Provides probability information for events. See TermEvent for an example.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the events
 */
public abstract class Event<T> {

    /**
     * @return all possible outcomes
     */
    public abstract Set<T> getDomain();

    /**
     * @param x the outcome
     * @return the probability that the outcome will happen (P(x))
     */
    public abstract double getProbability(final T x);

    /**
     * @param x the outcome
     * @param given the outcome to use as a given
     * @return P(x|given)
     */
    public abstract double getConditionalProbability(final T x, final T given);

    /**
     * @param x the first value
     * @param y the second value
     * @return P(x INTERSECT y)
     */
    public double getJointProbability(final T x, final T y) {
        return getConditionalProbability(y, x) * getProbability(x);
    }
}

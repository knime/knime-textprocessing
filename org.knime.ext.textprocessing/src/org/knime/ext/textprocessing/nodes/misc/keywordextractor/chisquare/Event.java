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

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
package org.knime.ext.textprocessing.util;


/**
 * Unordered pair where (a,b) == (b,a)
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of objects in the pair
 */
public class UnorderedPair<T> {
    private final T m_first;
    private final T m_second;
    private int m_hashCode = -1;

    /**
     * @param first
     * @param second
     */
    public UnorderedPair(final T first, final T second) {
        m_first = first;
        m_second = second;
    }

    /**
     * @return the first element
     */
    public T getFirst() {
        return m_first;
    }

    /**
     * @return the second element
     */
    public T getSecond() {
        return m_second;
    }

    /**
     * @param e the element to look for
     * @return the other element in the pair
     */
    public T getOther(final T e) {
        if (m_first.equals(e)) {
            return m_second;
        } else if (m_second.equals(e)) {
            return m_first;
        } else {
            throw new IllegalArgumentException(
                    "This element is not in the pair.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (m_hashCode == -1) {
            m_hashCode = m_first.hashCode() + m_second.hashCode();
        }

        return m_hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof UnorderedPair)) { return false; }

        UnorderedPair<?> p = (UnorderedPair<?>) o;

        if (m_first.equals(p.m_first)) {
            return m_second.equals(p.m_second);
        } else  if (m_first.equals(p.m_second)) {
            return m_second.equals(p.m_first);
        }

        return false;
    }

    /**
     * @param e the element to look for
     * @return true if the element is contained in the pair
     */
    public boolean contains(final T e) {
        return m_first.equals(e) || m_second.equals(e);
    }

    /**
     * Utility function to make code that uses UnorderedPairs a bit easier to
     * read by removing boilerplate instantiation code.
     *
     * @param <T> the type of the elements of the pair
     * @param first the first element
     * @param second the second element
     * @return a new UnorderedPair<T>
     */
    public static <T> UnorderedPair<T> makePair(final T first, final T second) {
        return new UnorderedPair<T>(first, second);
    }
}

/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Jul 21, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;


/**
 * Unordered pair where (a,b) == (b,a).
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

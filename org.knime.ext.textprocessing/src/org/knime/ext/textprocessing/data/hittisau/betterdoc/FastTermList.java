/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   11.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.betterdoc;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.knime.ext.textprocessing.data.hittisau.Term;

/**
 *
 * @author Alexander
 */
public class FastTermList implements List<Term> {

    private FastTerm[] m_terms;
    public FastTermList(final FastTerm[] terms) {
        m_terms = terms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return m_terms.length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return m_terms.length == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object o) {
        for (int i = 0; i < m_terms.length; i++) {
            if (o.equals(m_terms[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Term> iterator() {
        return new Iterator<Term>() {
            private int m_currentIdx = 0;
            /**
             * {@inheritDoc}
             */
            @Override
            public boolean hasNext() {
                return m_currentIdx < m_terms.length - 1;
            }
            /**
             * {@inheritDoc}
             */
            @Override
            public Term next() {
                return m_terms[m_currentIdx++];
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return m_terms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final Term e) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object o) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        for (Object o : c) {
            if (contains(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final Collection<? extends Term> c) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends Term> c) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term get(final int index) {
        return m_terms[index];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term set(final int index, final Term element) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final int index, final Term element) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term remove(final int index) {
        throw new InvalidOperationException("This list cannot be modified");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final Object o) {
        for (int i = 0; i < m_terms.length; i++) {
            if (m_terms[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(final Object o) {
        for (int i = m_terms.length - 1; i >= 0; i--) {
            if (m_terms[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Term> listIterator() {
        throw new InvalidOperationException("There is no list iterator for this list");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Term> listIterator(final int index) {
        throw new InvalidOperationException("There is no list iterator for this list");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Term> subList(final int fromIndex, final int toIndex) {
        throw new InvalidOperationException("Cannot create a sublist for this type of list");
    }

}

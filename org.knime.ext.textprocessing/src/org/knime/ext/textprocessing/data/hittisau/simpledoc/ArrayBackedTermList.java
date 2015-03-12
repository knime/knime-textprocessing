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
 *   12.03.2015 (Alexander): created
 */
package org.knime.ext.textprocessing.data.hittisau.simpledoc;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.data.hittisau.Term;
import org.knime.ext.textprocessing.data.hittisau.betterdoc.TagBuilderLookupTable;
import org.knime.ext.textprocessing.data.hittisau.betterdoc.TermLookupTable;

/**
 *
 * @author Alexander
 */
public class ArrayBackedTermList implements List<Term> {
    private int[] m_terms;
    private int[] m_tagValues;
    private int[] m_tagTypes;
    private BitSet m_immutable;
    private Term[] m_materialized;
    private TermLookupTable m_termLookup;
    private TagBuilderLookupTable m_tagBuilderLookup;
    private int m_numTerms;
    public ArrayBackedTermList(final int[] terms, final int[] tagValues,
        final int[] tagTypes, final BitSet immutable, final TermLookupTable tlt,
        final TagBuilderLookupTable tblt) {
        m_terms = terms;
        m_tagValues = tagValues;
        m_tagTypes = tagTypes;
        m_immutable = immutable;
        m_termLookup = tlt;
        m_tagBuilderLookup = tblt;
        m_numTerms = terms.length / 2;
        m_materialized = new Term[m_numTerms];
    }

    private void materialize(final int termIdx) {
        int realIndex = termIdx * 2;
        int wsIndex = realIndex + 1;
        Word w = new Word(m_termLookup.getTermAt(m_terms[realIndex]),
            m_terms[wsIndex] < 0 ? m_termLookup.getTermAt(m_terms[wsIndex]) : "");
        Tag tag = m_tagValues.length == 0 ? null : m_tagBuilderLookup.getTagBuilderAt(m_tagTypes[termIdx]).buildTag(m_tagValues[termIdx]);
        m_materialized[termIdx] = new org.knime.ext.textprocessing.data.Term(
                    new ArrayList<Word>(1) {{ add(w); }},
                    new ArrayList<Tag>(1) {{ add(tag); }},
                    m_immutable.get(termIdx)
                );
    }

    private Term getTerm(final int termIdx) {
        if (m_materialized[termIdx] == null) {
            materialize(termIdx);
        }
        return m_materialized[termIdx];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        // TODO Auto-generated method stub
        return m_numTerms;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return m_numTerms == 0;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(final Object o) {
        for (int i = 0; i < m_numTerms; i++) {
            Term t = getTerm(i);
            if (t.equals(o)) {
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
            int m_current = 0;
            @Override
            public Term next() {
                return getTerm(m_current++);
            }

            @Override
            public boolean hasNext() {
                return m_current < m_numTerms;
            }
        };
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        for (int i = 0; i < m_numTerms; i++) {
            getTerm(i);
        }
        return m_materialized;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        T[] out = a;
        if (a.length < m_numTerms) {
            out = (T[])new Object[m_numTerms];
        }
        for (int i = 0; i < m_numTerms; i++) {
            out[i] = (T)getTerm(i);
        }
        return out;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(final Term e) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(final Object o) {
        throw new InvalidOperationException("This list is not modifiable");
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
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(final int index, final Collection<? extends Term> c) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Term get(final int index) {
        return getTerm(index);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Term set(final int index, final Term element) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final int index, final Term element) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public Term remove(final int index) {
        throw new InvalidOperationException("This list is not modifiable");
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(final Object o) {
        for (int i = 0; i < m_numTerms; i++) {
            Term t = getTerm(i);
            if (t.equals(o)) {
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
        for (int i = m_numTerms - 1; i >= 0; i--) {
            Term t = getTerm(i);
            if (t.equals(o)) {
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
        return new ArrayBackedTermListIterator();
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Term> listIterator(final int index) {
        return new ArrayBackedTermListIterator(index);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Term> subList(final int fromIndex, final int toIndex) {
        ArrayList<Term> sublist = new ArrayList<Term>();
        for (int i = fromIndex; i < toIndex; i++) {
            sublist.add(getTerm(i));
        }
        return sublist;
    }

    private class ArrayBackedTermListIterator implements ListIterator<Term> {

        private int m_current = 0;

        public ArrayBackedTermListIterator() {
        }

        public ArrayBackedTermListIterator(final int index) {
            m_current = index;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return m_current < m_numTerms;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Term next() {
            return getTerm(m_current++);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasPrevious() {
            return m_current > 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Term previous() {
            return getTerm(m_current - 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int nextIndex() {
            return m_current + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int previousIndex() {
            return m_current + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new InvalidOperationException("The underlying list of this operator is not modifiable");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(final Term e) {
            throw new InvalidOperationException("The underlying list of this operator is not modifiable");
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(final Term e) {
            throw new InvalidOperationException("The underlying list of this operator is not modifiable");
        }

    }
}

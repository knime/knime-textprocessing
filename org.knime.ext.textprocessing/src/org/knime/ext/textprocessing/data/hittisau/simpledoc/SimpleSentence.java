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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;
import java.util.List;

import org.knime.ext.textprocessing.data.hittisau.Sentence;
import org.knime.ext.textprocessing.data.hittisau.Term;
import org.knime.ext.textprocessing.data.hittisau.betterdoc.TagBuilderLookupTable;
import org.knime.ext.textprocessing.data.hittisau.betterdoc.TermLookupTable;

/**
 *
 * @author Alexander
 */
public class SimpleSentence implements Sentence {

    public SimpleSentence(final int[] terms, final int[] tagValues,
                            final int[] tagTypes, final BitSet immutable, final TermLookupTable tlt,
                            final TagBuilderLookupTable tblt) {
        m_terms = terms;
        m_tagValues = tagValues;
        m_tagTypes = tagTypes;
        m_immutable = immutable;
        m_termLookup = tlt;
        m_tagBuilderLookup = tblt;
        m_numTags = terms.length / 2;
    }

    public SimpleSentence(final int[] terms, final int[] tagValues,
        final int[] tagTypes, final boolean[] immutable, final TermLookupTable tlt,
        final TagBuilderLookupTable tblt) {
        m_terms = terms;
        m_tagValues = tagValues;
        m_tagTypes = tagTypes;
        m_termLookup = tlt;
        m_tagBuilderLookup = tblt;
        m_numTags = terms.length / 2;
        m_immutable = new BitSet(m_numTags);
        for (int i = 0; i < immutable.length; i++) {
            if (immutable[i]) {
                m_immutable.set(i);
            }
        }
    }

    private int[] m_terms;
    private int[] m_tagValues;
    private int[] m_tagTypes;
    private BitSet m_immutable;
    private TermLookupTable m_termLookup;
    private TagBuilderLookupTable m_tagBuilderLookup;
    private int m_numTags;

    int[] getTermsArray()  {
        return m_terms;
    }

    int[] getTagValuesArray()  {
        return m_tagValues;
    }

    int[] getTagTypesArray() {
        return m_tagTypes;
    }

    BitSet getImmutable() {
        return m_immutable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Term> getTerms() {
        return new ArrayBackedTermList(m_terms, m_tagValues, m_tagTypes,
                        m_immutable, m_termLookup, m_tagBuilderLookup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_terms.length; i++) {
            if (i != m_terms.length - 1 || m_terms[i] >= 0) {
                if (!(i % 2 == 1 && m_terms[i] >= 0)) {
                    sb.append(m_termLookup.getTermAt(m_terms[i]));
                }
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTextWithWsSuffix() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_terms.length; i++) {
            if (!(i % 2 == 1 && m_terms[i] >= 0)) {
                sb.append(m_termLookup.getTermAt(m_terms[i]));
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }
}

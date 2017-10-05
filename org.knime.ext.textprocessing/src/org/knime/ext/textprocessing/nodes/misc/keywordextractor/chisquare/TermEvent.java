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
package org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare;

import java.util.Collections;
import java.util.Set;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.DocumentUtil;
import org.knime.ext.textprocessing.util.FrequencyMap;
import org.knime.ext.textprocessing.util.Maps;
import org.knime.ext.textprocessing.util.UnorderedPair;

/**
 * Analyses and provides term (co)occurrence frequency statistics for Documents.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public class TermEvent extends Event<Term> {

    private Set<Term> m_terms;
    private Set<Term> m_topFrequentTerms;
    private int m_documentLength;
    private FrequencyMap<Term> m_frequencies;
    private FrequencyMap<UnorderedPair<Term>> m_cooccurrences;
    private FrequencyMap<Term> m_totalcoocs;

    /**
     * @param doc the document to analyse
     * @param nrFrequentTerms the number of unique terms to consider
     * when building the cooccurrence table (for conditional probabilities)
     */
    public TermEvent(final Document doc, final int nrFrequentTerms) {
        if (nrFrequentTerms <= 0) {
            throw new IllegalArgumentException("The number of frequent terms "
                    + " must be strictly positive");
        }

        init(doc);
        cacheCoocs(doc, nrFrequentTerms);
    }

    /**
     * @param doc the document to analyse
     * @param frequentTermsProportion the proportion of the number of unique
     * terms to consider when building the cooccurrence table
     */
    public TermEvent(final Document doc, final double frequentTermsProportion) {
        if (frequentTermsProportion < 0 || frequentTermsProportion > 1) {
            throw new IllegalArgumentException("frequentTermsProportion must "
                    + "be between 0 and 1");
        }

        init(doc);
        int n = (int)Math.floor(m_terms.size() * frequentTermsProportion);
        cacheCoocs(doc, n);
    }

    private void init(final Document doc) {
        m_documentLength = doc.getLength();
        m_frequencies = DocumentUtil.getTermFrequencies(doc);
        m_terms = Collections.unmodifiableSet(m_frequencies.keySet());
    }

    private void cacheCoocs(final Document doc, final int nrFrequentTerms) {
        m_topFrequentTerms =  Collections.unmodifiableSet(
                Maps.getTopValues(m_frequencies, nrFrequentTerms).keySet());

        m_cooccurrences = DocumentUtil.getTermCooccurrences(doc, m_topFrequentTerms);
        m_totalcoocs = DocumentUtil.getTotalCoocs(doc, m_topFrequentTerms);
    }

    /**
     * @return the top frequentTermsProportion% terms based on their frequency
     */
    public Set<Term> getTopFrequentTerms() {
        return m_topFrequentTerms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Term> getDomain() {
        return m_terms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getProbability(final Term t) {
        if (m_documentLength > 0) {
            return (double)m_frequencies.get(t) / m_documentLength;
        }
        return 0.0;
    }

    /**
     * P(t|given) = number of cooccurrences of t and given / total number of
     * possible cooccurrences for given.
     *
     * {@inheritDoc}
     */
    @Override
    public double getConditionalProbability(final Term t, final Term given) {
        int totalcoocs = m_totalcoocs.get(given);

        if (totalcoocs == 0) {
            return 0.0;
        }

        double p = (double)getNrCooccurrences(t, given) / totalcoocs;
        assert p >= 0.0 && p <= 1.0;
        return p;
    }

    /**
     * @param t1 the first term
     * @param t2 the second term
     * @return the number of time that t1 and t2 cooccur
     */
    public int getNrCooccurrences(final Term t1, final Term t2) {
        return m_cooccurrences.get(UnorderedPair.makePair(t1, t2));
    }

    /**
     * @return all terms contained in the document
     */
    public Set<Term> getTerms() {
        return m_terms;
    }
}

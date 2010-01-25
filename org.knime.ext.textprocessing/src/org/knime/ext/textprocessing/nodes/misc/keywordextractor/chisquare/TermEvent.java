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
            throw new IllegalArgumentException("The number of frequent terms " +
            		" must be strictly positive");
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
            throw new IllegalArgumentException("frequentTermsProportion must " +
            		"be between 0 and 1");
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

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
package org.knime.ext.textprocessing.nodes.misc.categorytoclass.keywordextractor;

import java.util.Collections;
import java.util.Set;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.DocumentUtil;
import org.knime.ext.textprocessing.util.FrequencyMap;
import org.knime.ext.textprocessing.util.Maps;
import org.knime.ext.textprocessing.util.UnorderedPair;

/**
 * Analyses and provides statistics for Document objects.
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
     * Longer documents make holding the complete cooccurrence table in memory
     * impossible.
     * @param doc the document to analyse
     * @param frequentTermsProportion the percentage of unique terms to consider
     * when building the cooccurrence table (for conditional probabilities)
     */
    public TermEvent(final Document doc, final int frequentTermsProportion) {
        if (frequentTermsProportion < 0 || frequentTermsProportion > 100) {
            throw new IllegalArgumentException("The frequent terms proportion" +
            		" must be between 0 and 100 inclusively");
        }

        m_documentLength = doc.getLength();
        m_frequencies = DocumentUtil.getTermFrequencies(doc);
        m_terms = Collections.unmodifiableSet(m_frequencies.keySet());

        int n = (int)Math.floor(
                (double)m_terms.size() * frequentTermsProportion / 100.00);
        m_topFrequentTerms =  Collections.unmodifiableSet(
                Maps.getTopValues(m_frequencies, n).keySet());

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
}

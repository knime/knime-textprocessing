/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Jun 18, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.IndexedTerm;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.clustering.Cluster;

/**
 * Provides various Document analysis methods.
 *
 * TODO: some of these should be integrated into Document
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public final class DocumentUtil {

    private DocumentUtil() {
    }

    /**
     * @param doc the document to analyse
     * @return the total number of possible term cooccurrences in the document
     */
    public static int getNrTotalCoocs(final Document doc) {
        int n = 0;

        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            int senlength = sentence.getTerms().size();

            // The total number of possible coocurrences in a sentence is:
            // |s|^2 - [sum of i from 1 to |s|] = (|s|^2 - |s|)/2
            n += (Math.pow(senlength, 2) - senlength) / 2;
        }

        return n;
    }

    /**
     * Creates a new document based on the one passed by parameter but without any term tags.
     *
     * @param doc the document to copy
     * @return a tagless copy of the document.
     */
    public static Document stripTermTags(final Document doc) {
        DocumentBuilder builder = new DocumentBuilder(doc);
        for (Section s : doc.getSections()) {
            for (Paragraph p : s.getParagraphs()) {
                for (Sentence sen : p.getSentences()) {
                    List<Term> senTerms = sen.getTerms();
                    for (Term t : senTerms) {
                        builder.addTerm(new Term(t.getWords(), null, t.isUnmodifiable()));
                    }
                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        return builder.createDocument();
    }

    /**
     * Calculates the frequency of all terms in a given document.
     *
     * @param doc the document to analyse
     * @return a frequency count for each term
     */
    public static FrequencyMap<Term> getTermFrequencies(final Document doc) {
        FrequencyMap<Term> frequencies = new FrequencyMap<Term>();

        addTermFrequencies(doc, frequencies);

        return frequencies;
    }

    /**
     * Adds the term frequencies of 'doc' to a given frequency map.
     *
     * @param doc the document to analyse
     * @param frequencies the frequency map to update
     */
    public static void addTermFrequencies(final Document doc, final FrequencyMap<Term> frequencies) {
        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            for (Term term : sentence.getTerms()) {
                frequencies.increment(term);
            }
        }
    }

    /**
     * @param sentence the sentence to analyse
     * @return a frequency count for each term
     */
    public static FrequencyMap<Term> getTermFrequencies(final Sentence sentence) {
        FrequencyMap<Term> frequencies = new FrequencyMap<Term>();

        for (Term t : sentence.getTerms()) {
            frequencies.increment(t);
        }

        return frequencies;
    }

    /**
     * Calculates the individual cooccurrence frequency of the terms in a given set with the terms in a document.
     *
     * Example: "a a b b b" -> cooc(a,b) = 6, cooc(a,a) = 2, cooc(b,b) = 3
     *
     * @param doc the document to analyse
     * @param terms the terms to look for
     * @return cooccurrence frequencies for each possible [term in doc]-[term in terms] pair
     */
    public static FrequencyMap<UnorderedPair<Term>> getTermCooccurrences(final Document doc, final Set<Term> terms) {
        FrequencyMap<UnorderedPair<Term>> coocs = new FrequencyMap<UnorderedPair<Term>>();

        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            List<Term> sterms = new ArrayList<Term>(sentence.getTerms());

            for (int i = 0; i < sterms.size(); i++) {
                Term t1 = sterms.get(i);

                if (terms.contains(t1)) {
                    for (int j = i + 1; j < sterms.size(); j++) {
                        Term t2 = sterms.get(j);

                        coocs.increment(UnorderedPair.makePair(t1, t2));
                    }
                }
            }
        }

        return coocs;
    }

    /**
     * Calculates the total number of possible cooccurrences for each given term.
     *
     * @param doc the document to analyse
     * @param terms the terms to calculate the number of cooccurrences for
     * @return the total number of possible cooccurrences for each given term
     */
    public static FrequencyMap<Term> getTotalCoocs(final Document doc, final Set<Term> terms) {
        FrequencyMap<Term> totalcoocs = new FrequencyMap<Term>();

        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            List<Term> senterms = sentence.getTerms();

            FrequencyMap<Term> termfreqs = DocumentUtil.getTermFrequencies(sentence);
            for (Term t : termfreqs.keySet()) {
                if (terms.contains(t)) {
                    int freq = termfreqs.get(t);
                    totalcoocs.increment(t, senterms.size() * freq - ((freq + 1) * freq) / 2);
                }
            }
        }

        return totalcoocs;
    }

    /**
     * Calculates for each term the total length of all sentences in which it occurs.
     *
     * @param doc the document to analyse
     * @return total sentence length for each term present in the document
     */
    public static FrequencyMap<Term> getSentenceLengths(final Document doc) {
        FrequencyMap<Term> sentenceLengths = new FrequencyMap<Term>();
        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            List<Term> senterms = sentence.getTerms();

            Set<Term> uniqueterms = new HashSet<Term>(senterms);

            for (Term t : uniqueterms) {
                sentenceLengths.increment(t, senterms.size());
            }
        }

        return sentenceLengths;
    }

    /**
     * Calculates the total number of term cooccurrences for each cluster.
     *
     * Example: "a b c. a b d e c." and cluster (a,c) has a total of 10 possible cooccurrences, 3 in the first sentence,
     * 7 in the second.
     *
     * @param clusters the clusters
     * @param doc the document to analyse
     * @return the total number of term cooccurrences for each cluster
     */
    public static FrequencyMap<Cluster<Term>> getClusterNbCoocs(final Set<Cluster<Term>> clusters, final Document doc) {
        FrequencyMap<Cluster<Term>> nrcoocs = new FrequencyMap<Cluster<Term>>();

        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();
            int senlength = sentence.getTerms().size();

            FrequencyMap<Term> termfreqs = getTermFrequencies(sentence);
            Set<Term> uniqueterms = termfreqs.keySet();

            for (Cluster<Term> c : clusters) {
                if (!Collections.disjoint(uniqueterms, c)) {
                    int clusterfreqs = 0;
                    for (Term clusterterm : c) {
                        clusterfreqs += termfreqs.get(clusterterm);
                    }

                    // the number of cooccurrences in a sentence s is
                    // |s|*freq(c in s) - [summation from i = 1 to freq(c in s)]
                    nrcoocs.increment(c, senlength * clusterfreqs - (sumOverOne(clusterfreqs)));
                }
            }
        }

        return nrcoocs;
    }

    /**
     * Calculates the cooccurrence frequency of terms with clusters.
     *
     * Example: "a b c. a b d e c." and cluster (a,c) has a total of 10 possible cooccurrences, 3 in the first sentence,
     * 7 in the second.
     *
     * @param clusters the clusters to look for
     * @param doc the document to analyse
     * @return cooccurrence frequencies for each term with each cluster
     */
    public static Map<Cluster<Term>, FrequencyMap<Term>> getClusterCoocs(final Set<Cluster<Term>> clusters,
        final Document doc) {
        Map<Cluster<Term>, FrequencyMap<Term>> coocs = new HashMap<Cluster<Term>, FrequencyMap<Term>>();

        // Initialise
        for (Cluster<Term> c : clusters) {
            coocs.put(c, new FrequencyMap<Term>());
        }

        Iterator<Sentence> sentences = doc.sentenceIterator();
        while (sentences.hasNext()) {
            Sentence sentence = sentences.next();

            FrequencyMap<Term> termfreqs = getTermFrequencies(sentence);
            Set<Term> uniqueterms = termfreqs.keySet();

            for (Cluster<Term> c : clusters) {
                Set<Term> inC = new HashSet<Term>(uniqueterms);
                inC.retainAll(c);

                // is there at least one term from c in the sentence?
                if (!inC.isEmpty()) {
                    int clusterfreqs = 0;
                    for (Term clusterterm : c) {
                        clusterfreqs += termfreqs.get(clusterterm);
                    }

                    FrequencyMap<Term> cCoocs = coocs.get(c);

                    for (Term t : uniqueterms) {
                        int freqt = termfreqs.get(t);

                        if (c.contains(t)) {
                            cCoocs.increment(t, (clusterfreqs - 1) * freqt - sumOverOne(freqt - 1));
                        } else {
                            cCoocs.increment(t, freqt * clusterfreqs);
                        }
                    }
                }
            }
        }

        return coocs;
    }

    /**
     * @param n
     * @return summation of 1 for i from 1 to n
     */
    private static int sumOverOne(final int n) {
        return (n * (n + 1)) / 2;
    }

    /**
     * @param terms a list of terms
     * @return the same terms but without tags
     */
    public static List<Term> stripTermTags(final List<Term> terms) {
        List<Term> tagless = new ArrayList<Term>(terms.size());

        for (Term t : terms) {
            tagless.add(new Term(t.getWords(), null, t.isUnmodifiable()));
        }

        return tagless;
    }

    /**
     * @param data the data table
     * @param exec an execution monitor to report on the progress
     * @param documentColumnName the name of the document column. If it does not exist, this function will attempt to
     *            use the first Document column cell it can find.
     * @return a set of unique documents extracted from the data table
     * @throws CanceledExecutionException if the execution is cancelled
     */
    public static Set<Document> extractUniqueDocuments(final BufferedDataTable data, final ExecutionMonitor exec,
        final String documentColumnName) throws CanceledExecutionException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(data.getDataTableSpec());

        int documentColIndex = data.getDataTableSpec().findColumnIndex(documentColumnName);

        if (documentColIndex == -1) {
            documentColIndex = verifier.getDocumentCellIndex();
            if (documentColIndex == -1) {
                throw new IllegalStateException("No document cell was found.");
            }
        }

        Set<Document> docs = new LinkedHashSet<>();
        long n = data.size();
        int i = 1;
        try (final CloseableRowIterator it = data.iterator()) {
            while (it.hasNext()) {
                exec.checkCanceled();
                double progress = (double)i / (double)n;
                exec.setProgress(progress, "Processing row " + i + " of " + n);

                DataRow row = it.next();

                if (!row.getCell(documentColIndex).isMissing()) {
                    docs.add(((DocumentValue)row.getCell(documentColIndex)).getDocument());
                }
                ++i;
            }
            return docs;
        }
    }

    /**
     * Get a list of terms from a document.
     *
     * @param doc the document
     * @return the list of terms, or <code>Null</code> if no terms are found
     * @since 3.8
     */
    public static List<Term> getTerms(final Document doc) {
        List<Term> terms = null;
        if (doc != null) {
            terms = new ArrayList<Term>();
            final Iterator<Sentence> it = doc.sentenceIterator();
            while (it.hasNext()) {
                terms.addAll(it.next().getTerms());
            }
        }
        return terms;
    }

    /**
     * Get terms from the input documents and find their position in the document text (start and stop indexes). The
     * document text is fetched from <code>Document.getText()</code> method. Only terms that have at least one tag are
     * included in the result.
     *
     * @param doc the document
     * @return a list of indexed terms where all terms have at least one tag
     * @since 3.8
     */
    public static List<IndexedTerm> getIndexedTerms(final Document doc) {
        final List<IndexedTerm> result = new ArrayList<IndexedTerm>();
        // include doc title
        final String text = doc.getText();
        // mark the start position to search a term in the text
        int stopIndex = 0;
        final Iterator<Sentence> it = doc.sentenceIterator();
        while (it.hasNext()) {
            for (Term t : it.next().getTerms()) {
                // only include terms with a least one tag
                if (!t.getTags().isEmpty()) {
                    final String term = t.getText();
                    // get the start and stop index of the term in the text
                    final int startIndex = text.indexOf(term, stopIndex);
                    stopIndex = startIndex + term.length();
                    result.add(new IndexedTerm(t, startIndex, stopIndex));
                }
            }
        }
        return result;
    }
}

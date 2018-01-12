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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies;

import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;

/**
 * Utility class providing various frequency computation methods, like relative term frequency or inverse document
 * frequency.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public final class Frequencies {

    private Frequencies() {
    }

    /**
     * Computes and returns the inverse document frequency (idf) out of the given parameters.
     *
     * @param noAllDocs The number of all documents of the bag of words.
     * @param noTDocs The number of documents containing a certain term.
     * @return The inverse document frequency.
     */
    public static double inverseDocumentFrequency(final int noAllDocs, final int noTDocs) {
        return Math.log10(1 + ((double)noAllDocs / (double)noTDocs));
    }

    /**
     * Computes and returns the normalized inverse document frequency out of the given parameters
     *
     * @param noAllDocs The number of all documents of the bag of words.
     * @param noTDocs The number of documents containing a certain term.
     * @return The normalized inverse document frequency
     * @since 3.3
     */
    public static double normalizedInverseDocumentFrequency(final int noAllDocs, final int noTDocs) {
        if (noTDocs == 0) {
            return Double.NaN;
        } else {
            return Math.log10((double)noAllDocs / (double)noTDocs);
        }
    }

    /**
     * Computes and returns the normalized inverse document frequency out of the given parameters
     *
     * @param noAllDocs The number of all documents of the bag of words.
     * @param noTDocs The number of documents containing a certain term.
     * @return The probabilistic inverse document frequency
     * @since 3.3
     */

    public static double probabilisticInverseDocumentFrequency(final int noAllDocs, final int noTDocs) {
        double numerator = (double)noAllDocs - (double)noTDocs;
        if (noTDocs == 0) {
            return Double.NaN;
        } else if (numerator == 0) {
            return Double.NaN;
        } else {
            return Math.log10((numerator) / noTDocs);
        }

    }

    /**
     * Computes and returns the absolute term frequency of the given term and the document.
     *
     * @param term The term to compute the tf value for.
     * @param doc The document to compute the tf value with.
     * @return The absolute term frequency value of the given term according to the given document.
     */
    public static int absoluteTermFrequency(final Term term, final Document doc) {
        int termCount = 0;
        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            List<Paragraph> paragraphs = s.getParagraphs();
            for (Paragraph p : paragraphs) {
                List<Sentence> sentences = p.getSentences();
                for (Sentence sen : sentences) {
                    List<Term> senTerms = sen.getTerms();
                    for (Term t : senTerms) {
                        if (t != null) {
                            if (t.equals(term)) {
                                termCount++;
                            }
                        }
                    }
                }
            }
        }
        return termCount;
    }

    /**
     * Computes and returns the relative term frequency (tf) of the given term and the document. The tf value is
     * computed by dividing the number of occurrences of the given term in the given document by the number of all words
     * in the document.
     *
     * @param term The term to compute the tf value for.
     * @param doc The document to compute the tf value with.
     * @return The tf value of the given term according to the given document.
     */
    public static double relativeTermFrequency(final Term term, final Document doc) {
        int termCount = 0;
        int completeTermCount = 0;

        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            List<Paragraph> paragraphs = s.getParagraphs();
            for (Paragraph p : paragraphs) {
                List<Sentence> sentences = p.getSentences();
                for (Sentence sen : sentences) {
                    List<Term> senTerms = sen.getTerms();
                    for (Term t : senTerms) {
                        if (t != null) {
                            completeTermCount++;
                            if (t.equals(term)) {
                                termCount++;
                            }
                        }
                    }
                }
            }
        }

        return (double)termCount / (double)completeTermCount;
    }
}

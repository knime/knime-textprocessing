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
 * Utility class providing various frequency computation methods, like
 * relative term frequency or inverse document frequency.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class Frequencies {

    private Frequencies() { }
    
    
    /**
     * Computes and returns the inverse document frequency (idf) out of the 
     * given parameters.
     * 
     * @param noAllDocs The number of all documents of the bag of words.
     * @param noTDocs The number of documents containing a certain term.
     * @return The inverse document frequency.
     */
    public static double inverseDocumentFrequency(final int noAllDocs, 
            final int noTDocs) {
        return Math.log10(1 + ((double)noAllDocs / (double)noTDocs));
    }
    
    /**
     * Computes and returns the absolute term frequency of the given term 
     * and the document.
     * 
     * @param term The term to compute the tf value for.
     * @param doc The document to compute the tf value with.
     * @return The absolute term frequency value of the given term according 
     * to the given document.
     */
    public static int absoluteTermFrequency(final Term term, 
            final Document doc) {
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
                            if (t.equalsWordsOnly(term)) {
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
     * Computes and returns the relative term frequency (tf) of the given term 
     * and the document. The tf value is computed by dividing the number of
     * occurrences of the given term in the given document by the number of
     * all words in the document.
     * 
     * @param term The term to compute the tf value for.
     * @param doc The document to compute the tf value with.
     * @return The tf value of the given term according to the given document.
     */
    public static double relativeTermFrequency(final Term term, 
            final Document doc) {
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
                            if (t.equalsWordsOnly(term)) {
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

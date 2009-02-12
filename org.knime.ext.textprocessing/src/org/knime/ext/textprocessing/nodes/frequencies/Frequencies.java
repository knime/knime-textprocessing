/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
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

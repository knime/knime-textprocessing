/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 * -------------------------------------------------------------------
 * 
 * History
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.puncterasure;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PunctuationErasure implements Preprocessing {

    private static String punctMarks = 
        "[!#$%&'\"()*+,./\\:;<=>?@^_`{|}~\\[\\]]+";
    private static String replacement = "";
    
    /**
     * Creates new instance of <code>PunctuationErasure</code>.
     */
    public PunctuationErasure() { }
    
    /**
     * {@inheritDoc}
     */
    public Term preprocess(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(
                    PunctuationErasure.punctuationFilter(w.getWord())));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    } 
    
    /**
     * Filters all punctuation marks and replaces them with "". The filtered
     * String is returend.
     * @param str String to filter punctuation marks from.
     * @return Filtered String.
     */
    public static String punctuationFilter(final String str) {
        return str.replaceAll(punctMarks, replacement);
    }
}

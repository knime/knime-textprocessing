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
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import java.util.HashSet;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * A stop word filter, filtering strings contained in the given set of stop 
 * words. See {@link StopWordFilter#preprocess(Term)} for details to filter 
 * terms.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StopWordFilter implements Preprocessing {

    private HashSet < String > m_wordList;
    
    private boolean m_caseSensitive;
    
    /**
     * Creates new instance of <code>StopWordFilter</code> with given 
     * list containing the stop words to filter.
     *
     * @param stopWordList List with stop word to filter.
     * @param caseSensitive If set <code>true</code> the case matters when
     * filtering given string, otherwise not.
     */
    public StopWordFilter(final HashSet < String > stopWordList, 
            final boolean caseSensitive) {
        
        m_caseSensitive = caseSensitive;
        if (caseSensitive) {
            m_wordList = stopWordList;
        } else {
            m_wordList = convert(stopWordList);
        }
    }
    
    private HashSet<String> convert(final HashSet<String> set) {
        HashSet<String> convertedSet = new HashSet<String>();
        for (String s : set) {
            convertedSet.add(s.toLowerCase());
        }
        return convertedSet;
    }
    
    /**
     * @param stopwords The stop words to set.
     */
    public void setStopwords(final HashSet <String> stopwords) {
        m_wordList = stopwords;
    }
    
    /**
     * Returns true if given String is a stop word, false if not.
     * @param word String to check if it is a stop word.
     * @return True if given String is a stop word.
     */
    public boolean isStopWord(final String word) {
        return m_wordList.contains(word);
    }

    /**
     * {@inheritDoc}
     */
    public Term preprocess(Term term) {
        String t;
        if (m_caseSensitive) {
            t = term.getText();
        } else {
            t = term.getText().toLowerCase();
        }
        
        if (isStopWord(t)) {
            return null;
        }
        return term;
    }
}

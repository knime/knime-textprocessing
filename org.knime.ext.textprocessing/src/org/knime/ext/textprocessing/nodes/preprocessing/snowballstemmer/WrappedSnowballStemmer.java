/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class WrappedSnowballStemmer implements TermPreprocessing,
        StringPreprocessing {

    private SnowballStemmer m_stemmer;
    
    /**
     * Creates new instance of <code>WrappedSnowballStemmer</code> with given
     * stemmer to use.
     * 
     * @param stemmer The stemmer to use.
     */
    public WrappedSnowballStemmer(final SnowballStemmer stemmer) {
        m_stemmer = stemmer;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            m_stemmer.setCurrent(w.getText());
            m_stemmer.stem();
            newWords.add(new Word(m_stemmer.getCurrent()));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        m_stemmer.setCurrent(str);
        m_stemmer.stem();
        return m_stemmer.getCurrent();
    }
}

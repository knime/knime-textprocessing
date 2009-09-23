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
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacer implements Preprocessing, StringPreprocessing {

    private Hashtable<String, String> m_replaceDict;
    
    /**
     * Creates new instance of <code>DictionaryReplacer</code> with give 
     * dictionary, containing key value pairs for replacement.
     * 
     * @param replaceDict The dictionary consisting of key value pairs for
     * replacement (keys will be replaced by their corresponding values).
     */
    public DictionaryReplacer(final Hashtable<String, String> replaceDict) {
        super();
        m_replaceDict = replaceDict;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocess(final Term term) {
        String word = term.getText();
        String newWord = m_replaceDict.get(word);
        if (newWord != null) {
            List<String> tokenizedWords = 
                DefaultTokenization.getWordTokenizer().tokenize(newWord);
            
            List<Word> newWords = new ArrayList<Word>();
            for (String s : tokenizedWords) {
                newWords.add(new Word(s));
            }
            return new Term(newWords, term.getTags(), term.isUnmodifiable());
        }
        return term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        String newStr = m_replaceDict.get(str);
        if (newStr != null) {
            return newStr;
        }
        return str;
    }

}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacer implements TermPreprocessing, StringPreprocessing {

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
    public Term preprocessTerm(final Term term) {
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

/* @(#)$RCSfile$ 
 * $Revision$ $Date$ $Author$
 * 
========================================================================
 *
 *  Copyright (C) 2003 - 2011
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
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.puncterasure;

import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PunctuationErasure implements TermPreprocessing, StringPreprocessing {

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
    @Override
    public Term preprocessTerm(final Term term) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return PunctuationErasure.punctuationFilter(str);
    }
}

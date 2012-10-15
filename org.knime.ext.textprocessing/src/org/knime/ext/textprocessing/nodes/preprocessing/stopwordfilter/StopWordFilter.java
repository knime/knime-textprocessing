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
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

import java.util.HashSet;

/**
 * A stop word filter, filtering strings contained in the given set of stop 
 * words. See {@link StopWordFilter#preprocessTerm(Term)} for details to filter 
 * terms.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StopWordFilter implements TermPreprocessing, StringPreprocessing {

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
    @Override
    public Term preprocessTerm(final Term term) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        String t;
        if (m_caseSensitive) {
            t = str;
        } else {
            t = str.toLowerCase();
        }
        
        if (isStopWord(t)) {
            return null;
        }
        return str;
    }
}

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
package org.knime.ext.textprocessing.nodes.preprocessing.replacer;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class RegExReplacer implements TermPreprocessing, StringPreprocessing {

    private String m_regEx;
    private String m_replacement;

    /**
     * Creates new instance of <code>RegExReplacer</code> with the given
     * regular expression to find patterns to replace with the given
     * replacement.
     * @param regEx The regular expression to find pattern.
     * @param replacement The replacement pattern.
     */
    public RegExReplacer(final String regEx, final String replacement) {
        m_regEx = regEx;
        m_replacement = replacement;
    }

    /**
     * {@inheritDoc}
     */
    public Term preprocessTerm(final Term term) {
        List<Word> words = term.getWords();
        List<Word> newWords = new ArrayList<Word>();
        for (Word w : words) {
            newWords.add(new Word(
                    RegExReplacer.replaceAll(w.getWord(), m_regEx,
                            m_replacement)));
        }
        return new Term(newWords, term.getTags(), term.isUnmodifiable());
    }

    /**
     * Replaces all pattern in <code>str</code> matching given regular
     * expression with the specified replacement.
     * @param str String to replace patterns.
     * @param regEx The regular expression specifying the pattern to replace.
     * @param replacement The String to replace matching pattern with.
     * @return replaced String.
     */
    public static String replaceAll(final String str, final String regEx,
            final String replacement) {
        return str.replaceAll(regEx, replacement);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return RegExReplacer.replaceAll(str, m_regEx, m_replacement);
    }
}

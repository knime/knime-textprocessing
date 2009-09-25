/* @(#)$RCSfile$
 * $Revision$ $Date$ $Author$
 *
 * -------------------------------------------------------------------
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

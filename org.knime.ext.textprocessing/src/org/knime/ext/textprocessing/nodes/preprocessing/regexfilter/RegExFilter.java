/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2011
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
 *   19.02.2010 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.regexfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class RegExFilter implements TermPreprocessing, StringPreprocessing {

    private String m_regEx;
    
    private Pattern m_pattern;
    
    /**
     * Creates new instance of <code>RegExFilter</code> with the given
     * regular expression to find patterns in terms which have to be filtered.
     * @param regEx The regular expression to find pattern.
     */
    public RegExFilter(final String regEx) {
        m_regEx = regEx;
        m_pattern = Pattern.compile(m_regEx);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        String words = term.getText();
        Matcher m = m_pattern.matcher(words);
        if (m.find()) {
            return null;
        }
        return term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        Matcher m = m_pattern.matcher(str);
        if (m.find()) {
            return null;
        }
        return str;
    }
}

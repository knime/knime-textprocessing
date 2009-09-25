/* ------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.08.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.ncharsfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

/**
 * Filters terms with less than the specified number N chars. If a given term
 * has less than N characters <code>null</code> is returned by the
 * {@link NCharsFilter#preprocessTerm(Term)} method, otherwise unmodified term.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NCharsFilter implements TermPreprocessing, StringPreprocessing {

    private int m_n = 1;
    
    /**
     * Creates new instance of <code>NCharsFilter</code> with given N as the 
     * number of minimum chars.
     * 
     * @param n The number n of minimum chars of a term.
     */
    public NCharsFilter(final int n) {
        m_n = n;
    }

    /**
     * {@inheritDoc}
     */
    public Term preprocessTerm(final Term term) {
        if (term.getText().length() >= m_n) {
            return term;
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        if (str.length() >= m_n) {
            return str;
        }
        return null;
    }
}

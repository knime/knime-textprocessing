/* ------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.08.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.ncharsfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * Filters terms with less than the specified number N chars. If a given term
 * has less than N characters <code>null</code> is returned by the
 * {@link NCharsFilter#preprocess(Term)} method, otherwise unmodified term.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NCharsFilter implements Preprocessing {

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
    public Term preprocess(Term term) {
        if (term.getText().length() >= m_n) {
            return term;
        }
        return null;
    }
}

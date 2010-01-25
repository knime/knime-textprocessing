/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

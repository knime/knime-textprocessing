/*
 * ------------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 *
 * History
 *   24.10.2008 (kilian): created
 */
package org.knime.ext.textprocessing.util;


import org.knime.core.data.RowKey;
import org.knime.ext.textprocessing.data.Term;

/**
 *
 * @author kilian, University of Konstanz
 */
public class TermRowKeyTuple {

    private Term m_term;

    private RowKey m_key;

    /**
     * Creates a new instance of <code>TermRowKeyTuple</code> with given
     * term and row key to store.
     * @param t The term to set.
     * @param r The RowKey to set.
     */
    public TermRowKeyTuple(final Term t, final RowKey r) {
        if (t == null) {
            throw new IllegalArgumentException("Term may not be null!");
        }
        if (r == null) {
            throw new IllegalArgumentException("RowKey may not be null!");
        }

        m_term = t;
        m_key = r;
    }

    /**
     * @return The term.
     */
    public Term getTerm() {
        return m_term;
    }

    /**
     * @return The RowKey.
     */
    public RowKey getKey() {
        return m_key;
    }
}

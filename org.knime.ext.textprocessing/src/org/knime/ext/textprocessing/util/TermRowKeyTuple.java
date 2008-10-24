/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
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

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
 *   29.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

/**
 * Provides start and stop indices for terms and words.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
class IndexRange {
    
    private int m_startTermIndex = -1;
    
    private int m_stopTermIndex = -1;
    
    private int m_startWordIndex = -1;
    
    private int m_stopWordIndex = -1;
    
    /**
     * Creates a new instance of <code>IndexRange</code> with given start and
     * stop indices of terms and words.
     * 
     * @param startTermIndex A term's start index.
     * @param stopTermIndex A term's stop index.
     * @param startWordIndex A word's start index.
     * @param stopIndex A word's stop index.
     */
    IndexRange(final int startTermIndex, final int stopTermIndex,
            final int startWordIndex, final int stopIndex) {
        m_startTermIndex = startTermIndex;
        m_stopTermIndex = stopTermIndex;
        m_startWordIndex = startWordIndex;
        m_stopWordIndex = stopIndex;
    }

    /**
     * @return the startTermIndex
     */
    public int getStartTermIndex() {
        return m_startTermIndex;
    }

    /**
     * @return the stopTermIndex
     */
    public int getStopTermIndex() {
        return m_stopTermIndex;
    }

    /**
     * @return the startWordIndex
     */
    public int getStartWordIndex() {
        return m_startWordIndex;
    }

    /**
     * @return the stopWordIndex
     */
    public int getStopWordIndex() {
        return m_stopWordIndex;
    }
}

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

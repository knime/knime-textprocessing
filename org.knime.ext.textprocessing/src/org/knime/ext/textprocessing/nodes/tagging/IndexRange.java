/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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
final class IndexRange {

    private final int m_startTermIndex;

    private final int m_stopTermIndex;

    private final int m_startWordIndex;

    private final int m_stopWordIndex;

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

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_startTermIndex;
        result = prime * result + m_startWordIndex;
        result = prime * result + m_stopTermIndex;
        result = prime * result + m_stopWordIndex;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IndexRange other = (IndexRange)obj;
        if (m_startTermIndex != other.m_startTermIndex) {
            return false;
        }
        if (m_startWordIndex != other.m_startWordIndex) {
            return false;
        }
        if (m_stopTermIndex != other.m_stopTermIndex) {
            return false;
        }
        if (m_stopWordIndex != other.m_stopWordIndex) {
            return false;
        }
        return true;
    }

}

/*
 * ------------------------------------------------------------------------
 *
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
 *   Apr 4, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.uniquetermextractor;

import org.knime.ext.textprocessing.nodes.frequencies.Frequencies;

/**
 * A class to store pair of term and document frequencies.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class FrequencyPair {

    /**
     * Number of documents in total.
     */
    private final long m_noOfDocs;

    /**
     * Term frequency.
     */
    private final long m_tf;

    /**
     * Document frequency.
     */
    private final long m_df;

    /**
     * Creates a new instance of {@link FrequencyPair}.
     *
     * @param noOfDocs Number of documents in total.
     * @param tf Term frequency.
     * @param df Document frequency.
     */
    FrequencyPair(final long noOfDocs, final long tf, final long df) {
        m_noOfDocs = noOfDocs;
        m_tf = tf;
        m_df = df;
    }

    /**
     * Static method to merge two {@link FrequencyPair FrequencyPairs}.
     *
     * @param a First {@code FrequencyPair.}
     * @param b Second {@code FrequencyPair.}
     * @return Returns a new instance of {@code FrequencyPair} with merged/added frequencies.
     */
    static final FrequencyPair sum(final FrequencyPair a, final FrequencyPair b) {
        return new FrequencyPair(a.getNoOfDocs(), a.getTF() + b.getTF(), a.getDF() + b.getDF());
    }

    /**
     * Returns the total number of documents.
     *
     * @return Returns the total number of documents.
     */
    final long getNoOfDocs() {
        return m_noOfDocs;
    }

    /**
     * Returns the term frequency.
     *
     * @return Returns the term frequency.
     */
    final long getTF() {
        return m_tf;
    }

    /**
     * Returns the document frequency.
     *
     * @return Returns the term frequency.
     */
    final long getDF() {
        return m_df;
    }

    /**
     * Calculates and returns the inverse document frequency.
     *
     * @return Returns the inverse document frequency.
     */
    final double getIDF() {
        return Frequencies.normalizedInverseDocumentFrequency((int)m_noOfDocs, (int)m_df);
    }
}

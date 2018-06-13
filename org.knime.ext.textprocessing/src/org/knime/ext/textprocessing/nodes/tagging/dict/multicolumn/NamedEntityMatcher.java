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
 *   Jun 6, 2018 (Julian Bunzel, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

/**
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class NamedEntityMatcher {

    private final boolean m_caseSensitivity;

    private final boolean m_exactMatch;

    /**
     * @param caseSensitivity
     * @param exactMatch
     *
     */
    public NamedEntityMatcher(final boolean caseSensitivity, final boolean exactMatch) {
        m_caseSensitivity = caseSensitivity;
        m_exactMatch = exactMatch;
    }

    /**
     * @param entity
     * @param sentence
     * @return
     */
    public boolean matchWithSentence(String entity, String sentence) {
        if (!m_caseSensitivity) {
            sentence = sentence.toLowerCase();
            entity = entity.toLowerCase();
        }

        return sentence.contains(entity);
    }

    /**
     * @param entity
     * @param word
     * @return
     */
    public boolean matchWithWord(String entity, String word) {
        if (!m_caseSensitivity) {
            word = word.toLowerCase();
            entity = entity.toLowerCase();
        }

        return (m_exactMatch && word.equals(entity)) || (!m_exactMatch && word.contains(entity));
    }
}

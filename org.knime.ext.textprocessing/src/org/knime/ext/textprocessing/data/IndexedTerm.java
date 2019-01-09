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
 *   Nov 9, 2018 (dewi): created
 */
package org.knime.ext.textprocessing.data;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * This class stores a term name, its list of tags, and the position of the term in a document text.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 * @since 3.8
 */
public class IndexedTerm {

    private final int m_startIndex;

    private final int m_stopIndex;

    private final List<Tag> m_tags;

    private final String m_termName;

    /**
     * The constructor for IndexedTerm.
     *
     * @param term the term
     * @param startIdx the start index of the term
     * @param stopIdx the stop index of the term
     */
    public IndexedTerm(final Term term, final int startIdx, final int stopIdx) {
        m_tags = term.getTags();
        m_startIndex = startIdx;
        m_stopIndex = stopIdx;
        m_termName = term.getText();
    }

    /**
     * Get the tags of this term.
     *
     * @return a list of tags
     */
    public List<Tag> getTags() {
        return m_tags;
    }

    /**
     * Get the values of the tags. The chars, except the first, are in lower case.
     *
     * @return a list of tags in String
     */
    public List<String> getTagValues() {
        return m_tags.stream()//
            .map(tag -> StringUtils.capitalize(tag.getTagValue().toLowerCase()))//
            .collect(Collectors.toList());
    }

    /**
     * Get the start index of the term.
     *
     * @return the startIndex
     */
    public int getStartIndex() {
        return m_startIndex;
    }

    /**
     * Get the stop index of the term.
     *
     * @return the stopIndex
     */
    public int getStopIndex() {
        return m_stopIndex;
    }

    /**
     * Get the term name.
     *
     * @return the term name in String
     */
    public String getTermValue() {
        return m_termName;
    }

}

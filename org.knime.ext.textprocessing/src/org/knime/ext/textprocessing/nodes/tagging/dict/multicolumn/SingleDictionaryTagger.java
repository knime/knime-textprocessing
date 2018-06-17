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
 *   Jun 15, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import java.util.Set;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.NamedEntityMatcher;

/**
 * The {@code SingleDictionaryTagger} contains the {@code Tag} and dictionary used for tagging and a
 * {@code NamedEntityMatcher} which provides matching behavior (case sensitive and exact matching).
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.6
 */
final class SingleDictionaryTagger {

    /**
     * A set of entities.
     */
    private final Set<String> m_entities;

    /**
     * The {@code NamedEntityMatcher} used for word matching.
     */
    private final NamedEntityMatcher m_matcher;

    /**
     * The {@code Tag} to be used.
     */
    private final Tag m_tag;

    /**
     * Creates a new instance of {@code SingleDictionaryTagger}.
     *
     * @param caseSensitivity The case sensitivity behavior.
     * @param exactMatch The exact matching behavior.
     * @param entities A set of entities.
     */
    SingleDictionaryTagger(final boolean caseSensitivity, final boolean exactMatch, final Tag tag,
        final Set<String> entities) {
        m_entities = entities;
        m_tag = tag;
        m_matcher = new NamedEntityMatcher(caseSensitivity, exactMatch);
    }

    /**
     * Creates a new instance of {@code SingleDictionaryTagger}.
     *
     * @param config The {@code DictionaryTaggerConfiguration}.
     * @param entities A set of entities.
     */
    SingleDictionaryTagger(final DictionaryTaggerConfiguration config, final Set<String> entities) {
        this(config.getCaseSensitivityOption(), config.getExactMatchOption(), config.getTag(), entities);
    }

    /**
     * Returns the set of entities used for tagging.
     *
     * @return Returns a set of entities used for tagging.
     */
    Set<String> getEntities() {
        return m_entities;
    }

    /**
     * Returns the {@code NamedEntityMatcher}.
     *
     * @return Returns the {@code NamedEntityMatcher}.
     */
    NamedEntityMatcher getMatcher() {
        return m_matcher;
    }

    /**
     * Returns the assigned {@code Tag}.
     *
     * @return The assigned {@code Tag}.
     */
    Tag getTag() {
        return m_tag;
    }

}

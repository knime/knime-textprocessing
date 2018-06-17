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
 *   Apr 25, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.HashMap;
import java.util.Map;

import org.knime.ext.textprocessing.data.Tag;

/**
 * The {@code MultipleTaggedEntity} contains the name of the entity as a {@code String} as well as a Map from {@link Tag
 * Tags} to {@link NamedEntityMatcher NamedEntityMatchers}. The map contains information about how the entity has to be
 * found and tagged.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
public final class MultipleTaggedEntity {

    /**
     * The name of the entity.
     */
    private final String m_entity;

    /**
     * The map of containing all necessary properties for tagging.
     */
    private final Map<Tag, NamedEntityMatcher> m_tagMatcherMap;

    /**
     * Creates an new instance of {@code MultipleTaggedEntity} based on a given entity and initializes an empty map for
     * tag and matching behavior.
     *
     * @param entity The named entity.
     */
    public MultipleTaggedEntity(final String entity) {
        m_entity = entity;
        m_tagMatcherMap = new HashMap<>();
    }

    /**
     * Adds a combination of a {@code Tag} and {@code NamedEntityMatcher} to the current {@code MultipleTaggedEntity}
     * instance. The combination will later be used for looking up the entity within terms and applying the specific
     * tag.
     *
     * @param tag The tag to be used for tagging the entity.
     * @param matcher The matcher to be used to find the entity.
     */
    public final void addTagMatcherCombination(final Tag tag, final NamedEntityMatcher matcher) {
        m_tagMatcherMap.put(tag, matcher);
    }

    /**
     * Returns the entity as a String.
     *
     * @return Returns the entity as a String.
     */
    final String getEntity() {
        return m_entity;
    }

    /**
     * Returns a map of {@code Tags} and the specific {@code NamedEntityMatcher} that is used to find the entity within
     * a sentence or word.
     *
     * @return Returns a map of {@code Tags} and {@code NamedEntityMatcher}.
     */
    final Map<Tag, NamedEntityMatcher> getTagMatcherMap() {
        return m_tagMatcherMap;
    }
}

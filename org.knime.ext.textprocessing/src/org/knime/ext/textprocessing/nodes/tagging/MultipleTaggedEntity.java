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

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code MultipleTaggedEntity} contains the name of the entity as a {@code String} as well as a List of
 * {@link DocumentTaggerConfiguration}s. The configurations contain information about how the entity has to be tagged.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
public class MultipleTaggedEntity {

    /**
     * The name of the entity.
     */
    private final String m_entity;

    /**
     * The list of {@code DocumentTaggerConfiguration}s containing all necessary properties for tagging.
     */
    private final List<DocumentTaggerConfiguration> m_configs;

    /**
     * Creates an new instance of {@code MultipleTaggedEntity} based on a given entity and initializes an empty list for
     * {@link DocumentTaggerConfiguration}s.
     *
     * @param entity The named entity.
     */
    public MultipleTaggedEntity(final String entity) {
        m_entity = entity;
        m_configs = new ArrayList<>();
    }

    /**
     * Adds a {@link DocumentTaggerConfiguration} to the current {@code MultipleTaggedEntity} instance.
     *
     * @param config The {@code DocumentTaggerConfiguration} to add.
     */
    public final void addConfig(final DocumentTaggerConfiguration config) {
        m_configs.add(config);
    }

    /**
     * Returns the entity as a String.
     *
     * @return Returns the entity as a String.
     */
    public final String getEntity() {
        return m_entity;
    }

    /**
     * Returns a list of {@link DocumentTaggerConfiguration}s which contain information about how the entity has to be
     * tagged.
     *
     * @return Returns a list of {@code DocumentTaggerConfiguration}s.
     */
    public final List<DocumentTaggerConfiguration> getConfigs() {
        return m_configs;
    }
}

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
 *   Apr 11, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTaggerSettings;

/**
 * Stores {@link DocumentTaggerSettings} and their identifiers in a map.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
final class MultipleDictionaryTaggerSettings {

    /**
     * List of {@code DictionaryTaggerSettings} containing the settings for all dictionaries used for
     * tagging.
     */
    private final List<DictionaryTaggerSettings> m_settings = new ArrayList<>();

    /**
     * Config identifier for the NodeSettings object contained in the NodeSettings which contains the settings.
     */
    private static final String CFG_SUB_CONFIG = "all_columns";

    /**
     * Config identifier for the column names.
     */
    private static final String CFG_KEY_DICT_TAGGER_SUB = "dict-tagger-sub";

    /**
     * Creates an instance of {@code MultipleDocumentTaggerSettings}.
     *
     * @param settings The settings to store.
     * @throws InvalidSettingsException, if node settings could not be retrieved.
     */
    MultipleDictionaryTaggerSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        if (settings.containsKey(CFG_SUB_CONFIG)) {
            NodeSettingsRO subSettings = settings.getNodeSettings(CFG_SUB_CONFIG);
            if (subSettings != null) {
                for (String identifier : subSettings) {
                    NodeSettingsRO col = subSettings.getNodeSettings(identifier);
                    m_settings.add(DictionaryTaggerSettings.createFrom(col));
                }
            }
        }
    }

    /**
     * Creates an instance of {@code MultipleDocumentTaggerSettings}.
     *
     * @param settings The list of {@code DictionaryTaggerSettings}.
     */
    MultipleDictionaryTaggerSettings(final List<DictionaryTaggerSettings> settings) {
        m_settings.addAll(settings);
    }

    /**
     * Writes the settings to a {@code NodeSettingsWO} object.
     *
     * @param settings The {@code NodeSettingsWO} to write to.
     * @throws InvalidSettingsException
     */
    final void save(final NodeSettingsWO settings) {
        if (settings != null) {
            final NodeSettingsWO subSettings = settings.addNodeSettings(CFG_SUB_CONFIG);
            int index = 0;
            for (DictionaryTaggerSettings entry : m_settings) {
                NodeSettingsWO subSub =
                    subSettings.addNodeSettings(CFG_KEY_DICT_TAGGER_SUB + "_" + Integer.toString(index));
                entry.saveSettingsTo(subSub);
                index++;
            }
        }
    }

    /**
     * Validates the settings of a {@code NodeSettingsRO} object.
     *
     * @param settings The {@code NodeSettingsRO} to validate.
     * @throws InvalidSettingsException
     */
    static final void validate(final NodeSettingsRO settings) throws InvalidSettingsException {
        if (settings.containsKey(CFG_SUB_CONFIG)) {
            NodeSettingsRO subSettings = settings.getNodeSettings(CFG_SUB_CONFIG);
            if (subSettings != null) {
                for (String identifier : subSettings) {
                    NodeSettingsRO col = subSettings.getNodeSettings(identifier);
                    DictionaryTaggerSettings.createFrom(col);
                }
            }
        }
    }

    /**
     * Returns list of {@code DictionaryTaggerSettings} containing the specific settings for each dictionary
     * column.
     *
     * @return Returns the settings for each dictionary column.
     */
    final List<DictionaryTaggerSettings> getSettings() {
        return m_settings;
    }

    /**
     * Adds a {@code DictionaryTaggerSettings}.
     *
     * @param dictTaggerColumnSetting The {@code DictionaryTaggerSettings} to add.
     */
    final void add(final DictionaryTaggerSettings dictTaggerColumnSetting) {
        m_settings.add(dictTaggerColumnSetting);
    }

    /**
     * Removes a {@code DictionaryTaggerSettings}.
     *
     * @param dictTaggerColumnSetting The {@code DictionaryTaggerSettings} to remove.
     */
    final void remove(final DictionaryTaggerSettings dictTaggerColumnSetting) {
        m_settings.remove(dictTaggerColumnSetting);
    }

    /**
     * Retrieves a {@code DictionaryTaggerSettings} based on an index.
     *
     * @param settingsIndex The index.
     * @return Returns a {@code DictionaryTaggerSettings}.
     */
    final DictionaryTaggerSettings get(final int settingsIndex) {
        return m_settings.get(settingsIndex);
    }
}

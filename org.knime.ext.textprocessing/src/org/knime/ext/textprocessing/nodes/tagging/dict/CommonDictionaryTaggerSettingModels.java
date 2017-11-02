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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 *   17.07.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Provides shared SettingsModels used by different DictionaryTagger implementations. Does not provide the specific
 * dialog itself.
 *
 * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.5
 */
public class CommonDictionaryTaggerSettingModels {

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the name of
     * the column to use as dictionary column.
     *
     * @return A {@code SettingsModelString} containing the name of the column to use as dictionary column.
     */
    public static final SettingsModelString createColumnModel() {
        return new SettingsModelString(CommonDictionaryTaggerConfigKeys.CFGKEY_DICT_COL, "");
    }

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} containing the user
     * settings whether terms representing named entities have to be set unmodifiable or not.
     *
     * @return A {@code SettingsModelBoolean} containing the terms unmodifiable flag.
     */
    public static final SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(CommonDictionaryTaggerConfigKeys.CFGKEY_UNMODIFIABLE,
            AbstractDictionaryTaggerModel2.DEFAULT_UNMODIFIABLE);
    }

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} containing the flag
     * specifying whether the search for named entities have to be done case sensitive or not.
     *
     * @return A {@code SettingsModelBoolean} containing the value of the case sensitivity flag.
     */
    public static final SettingsModelBoolean createCaseSensitiveModel() {
        return new SettingsModelBoolean(CommonDictionaryTaggerConfigKeys.CFGKEY_CASE_SENSITIVE,
            AbstractDictionaryTaggerModel2.DEFAULT_CASE_SENSITIVE);
    }

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the tag type
     * to assign to each found named entity.
     *
     * @return A {@code SettingsModelString} containing the tag type to assign to each found named entity.
     */
    public static final SettingsModelString createTagTypeModel() {
        return new SettingsModelString(CommonDictionaryTaggerConfigKeys.CFGKEY_TAG_TYPE,
            AbstractDictionaryTaggerModel2.DEFAULT_TAG_TYPE);
    }

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the tag to
     * assign to each found named entity.
     *
     * @return A {@code SettingsModelString} containing the tag to assign to each found name entity.
     */
    public static final SettingsModelString createTagModel() {
        return new SettingsModelString(CommonDictionaryTaggerConfigKeys.CFGKEY_TAG,
            AbstractDictionaryTaggerModel2.DEFAULT_TAG);
    }

}

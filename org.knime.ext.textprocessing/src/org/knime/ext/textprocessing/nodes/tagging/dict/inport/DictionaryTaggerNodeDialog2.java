/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.inport;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane2;
import org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel2;
import org.knime.ext.textprocessing.nodes.tagging.dict.CommonDictionaryTaggerSettingModels;

/**
 * The node dialog class of the dictionary tagger node. This class extends the {@link TaggerNodeSettingsPane2}.
 *
 * @author Kilian Thiel, KNIME.com GmbH, Berlin, Germany
 * @since 3.5
 */
class DictionaryTaggerNodeDialog2 extends TaggerNodeSettingsPane2 {

    /**
     * Creates and returns a {@link SettingsModelBoolean} containing the flag specifying whether the search for named
     * entities have to be via exact match or contains match or not.
     *
     * @return A {@code SettingsModelBoolean} containing the flag specifying whether the search for named entities have
     *         to be via exact match or contains match or not.
     */
    static final SettingsModelBoolean createExactMatchModel() {
        return new SettingsModelBoolean(DictionaryTaggerConfigKeys2.CFGKEY_EXACTMATCH,
            DictionaryTaggerNodeModel2.DEFAULT_EXACTMATCH);
    }

    private final DialogComponentStringSelection m_tagSelection;

    private final SettingsModelString m_tagtypemodel;

    /**
     * Creates a new instance of {@code DictionaryTaggerNodeDialog2}.
     */
    @SuppressWarnings("unchecked")
    DictionaryTaggerNodeDialog2() {
        super();
        createNewTab("Tagger options");
        setSelected("Tagger options");

        addDialogComponent(
            new DialogComponentColumnNameSelection(CommonDictionaryTaggerSettingModels.createColumnModel(),
                "Dictionary column", AbstractDictionaryTaggerModel2.DICT_TABLE_INDEX, StringValue.class));

        addDialogComponent(new DialogComponentBoolean(CommonDictionaryTaggerSettingModels.createSetUnmodifiableModel(),
            "Set named entities unmodifiable"));

        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentBoolean(CommonDictionaryTaggerSettingModels.createCaseSensitiveModel(),
            "Case sensitive"));

        addDialogComponent(new DialogComponentBoolean(createExactMatchModel(), "Exact match"));

        setHorizontalPlacement(false);

        // tag type model & tag list
        m_tagtypemodel = CommonDictionaryTaggerSettingModels.createTagTypeModel();
        m_tagSelection = new DialogComponentStringSelection(CommonDictionaryTaggerSettingModels.createTagModel(),
            "Tag value", TagFactory.getInstance().getTagSetByType(m_tagtypemodel.getStringValue()).asStringList());
        m_tagtypemodel.addChangeListener(e -> m_tagSelection.replaceListItems(
            TagFactory.getInstance().getTagSetByType(m_tagtypemodel.getStringValue()).asStringList(), ""));

        this.setHorizontalPlacement(true);
        addDialogComponent(
            new DialogComponentStringSelection(m_tagtypemodel, "Tag type", TagFactory.getInstance().getTagTypes()));

        addDialogComponent(m_tagSelection);
    }
}

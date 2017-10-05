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

import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane;
import org.knime.ext.textprocessing.nodes.tagging.dict.AbstractDictionaryTaggerModel;

/**
 * The dialog class of the dictionary named entity recognizer node.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated Use custom node dialog instead.
 */
@Deprecated
public class DictionaryTaggerNodeDialog extends TaggerNodeSettingsPane {

    /**
     * Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     *
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static final SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_UNMODIFIABLE,
                AbstractDictionaryTaggerModel.DEFAULT_UNMODIFIABLE);
    }

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the flag specifying whether the search for named entities
     * have to be done case sensitive or not.
     */
    public static final SettingsModelBoolean createCaseSensitiveModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_CASE_SENSITIVE,
                AbstractDictionaryTaggerModel.DEFAULT_CASE_SENSITIVE);
    }

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag type to assign to each found named entity.
     */
    public static final SettingsModelString createTagTypeModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_TAG_TYPE,
                AbstractDictionaryTaggerModel.DEFAULT_TAG_TYPE);
    }

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag to assign to each found named entity.
     */
    public static final SettingsModelString createTagModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_TAG,
                AbstractDictionaryTaggerModel.DEFAULT_TAG);
    }

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the name of the column to use as dictionary column.
     */
    public static final SettingsModelString createColumnModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_DICT_COL, "");
    }

    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the flag specifying whether the search for named entities
     * have to be via exact match or contains match or not.
     * @since 2.8
     */
    public static final SettingsModelBoolean createExactMatchModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_EXACTMATCH,
                DictionaryTaggerNodeModel.DEFAULT_EXACTMATCH);
    }

    /**
     * Creates a new instance of <code>DictionaryTaggerNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public DictionaryTaggerNodeDialog() {
        super();
        createNewTab("Tagger options");
        setSelected("Tagger options");

        addDialogComponent(new DialogComponentColumnNameSelection(
                createColumnModel(), "Dictionary column",
                AbstractDictionaryTaggerModel.DICT_TABLE_INDEX,
                StringValue.class));

        addDialogComponent(new DialogComponentBoolean(
                        createSetUnmodifiableModel(),
                        "Set named entities unmodifiable"));

        setHorizontalPlacement(true);

        addDialogComponent(new DialogComponentBoolean(
                createCaseSensitiveModel(),
                "Case sensitive"));

        addDialogComponent(new DialogComponentBoolean(
                createExactMatchModel(),
                "Exact match"));

        setHorizontalPlacement(false);

        // tag type model
        m_tagtypemodel = createTagTypeModel();
        m_tagtypemodel.addChangeListener(new InternalChangeListener());

        // tag list
        String selectedTagType = m_tagtypemodel.getStringValue();
        List<String> tags = TagFactory.getInstance()
                .getTagSetByType(selectedTagType).asStringList();
        m_tagSelection = new DialogComponentStringSelection(
                    createTagModel(), "Tag value", tags);

        this.setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentStringSelection(
                m_tagtypemodel, "Tag type",
                TagFactory.getInstance().getTagTypes()));

        addDialogComponent(m_tagSelection);
    }

    private DialogComponentStringSelection m_tagSelection;

    private SettingsModelString m_tagtypemodel;

    /**
     *
     * @author thiel, University of Konstanz
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedTagType = m_tagtypemodel.getStringValue();
            List<String> tags = TagFactory.getInstance()
                    .getTagSetByType(selectedTagType).asStringList();
            m_tagSelection.replaceListItems(tags, "");
        }
    }
}

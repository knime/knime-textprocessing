/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import java.util.stream.Collectors;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.data.tag.TagSet;
import org.knime.ext.textprocessing.data.tag.TagSets;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return A new instance of <code>SettingsModelStringArray</code> containing the specified tag types to use.
     */
    public static SettingsModelStringArray getTagTypesModel() {
        return new SettingsModelStringArray(TagToStringConfigKeys.CFG_KEY_TAG_TYPES,
            new String[]{TagToStringNodeModel.DEFAULT_TAG_TYPE});
    }

    /**
     * @return A new instance of <code>SettingsModelString</code> containing the specified term column.
     */
    public static SettingsModelString getTermColModel() {
        return new SettingsModelString(TagToStringConfigKeys.CFG_KEY_TERM_COL, "");
    }

    /**
     * @return A new instance of <code>SettingsModelString</code> containing the specified missinn tag value.
     */
    public static SettingsModelString getMissingTagModel() {
        return new SettingsModelString(TagToStringConfigKeys.CFG_KEY_MISSING_TAG_STRING,
            TagToStringNodeModel.DEFAULT_MISSING_VALUE);
    }

    private final SettingsModelString m_termCol = getTermColModel();

    private final SettingsModelStringArray m_tagTypesModel = getTagTypesModel();

    private final DialogComponentStringListSelection m_tagTypesList;

    private DataTableSpec m_lastTableSpec;

    /**
     * Creates a new instance of <code>TagToStringNodeDialog</code>, providing the dialog components.
     */
    @SuppressWarnings("unchecked")
    public TagToStringNodeDialog() {
        addDialogComponent(
            new DialogComponentColumnNameSelection(m_termCol, "Term column", 0, TermValue.class));

        m_termCol.addChangeListener(c -> updateTagModels());

        String[] allTagTypes = TagSets.getInstalledTagSets().stream().map(TagSet::getType).toArray(String[]::new);
        m_tagTypesList = new DialogComponentStringListSelection(m_tagTypesModel, "Tag types", allTagTypes);
        addDialogComponent(m_tagTypesList);

        addDialogComponent(new DialogComponentString(getMissingTagModel(), "Missing tag value", false, 15));
    }

    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        m_lastTableSpec = specs[0];
        updateTagModels();
    }

    private void updateTagModels() {
        if (m_lastTableSpec != null) {
            var availableTagTypes = TagSets.getTagSets(getSelectedColumn()).stream()//
                    .map(TagSet::getType)//
                    .collect(Collectors.toList());
            // null means that the previous selection remains
            m_tagTypesList.replaceListItems(availableTagTypes, (String[])null);
        }
    }

    private DataColumnSpec getSelectedColumn() {
        return m_lastTableSpec.getColumnSpec(m_termCol.getStringValue());
    }
}

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
 *   May 3, 2016 (hermann): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictionaryfilter;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 * Node dialog for the "DictionaryFilter" node.
 *
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows creation of a simple dialog with standard
 * components. If you need a more complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class DictionaryFilterNodeDialog extends PreprocessingNodeSettingsPane2 {

    /**
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the name of the column with
     *         the strings to filter.
     */
    public static final SettingsModelString getColumnToReadModel() {
        return new SettingsModelString(DictionaryFilterConfigKeys.CFGKEY_KEYCOLUMN, "");

    }

    /**
     * @return Creates and returns a new instance of {@SettingsModelBoolean} containing the flag if filtering has to be
     *         done case sensitive or not.
     */
    public static final SettingsModelBoolean getCaseSensitiveModel() {
        return new SettingsModelBoolean(DictionaryFilterConfigKeys.CFGKEY_CASE_SENSITIVE,
            DictionaryFilterNodeModel.DEF_CASE_SENSITIVE);
    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    public DictionaryFilterNodeDialog() {
        super();

        createNewTab("Dictionary options");
        setSelected("Dictionary options");

        addDialogComponent(new DialogComponentBoolean(getCaseSensitiveModel(), "Case sensitive"));

        addDialogComponent(new DialogComponentColumnNameSelection(getColumnToReadModel(),
            "Column containing the strings to filter", 1, true, StringValue.class));

    }

}

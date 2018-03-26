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
 *   02.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 * The {@code NodeDialog} for the Stop Word Filter node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
final class StopWordFilterNodeDialog3 extends PreprocessingNodeSettingsPane2 {

    static final SettingsModelBoolean getCaseSensitiveModel() {
        return new SettingsModelBoolean(StopWordFilterConfigKeys3.CFGKEY_CASE_SENSITIVE,
            StopWordFilterNodeModel3.DEF_CASE_SENSITIVE);
    }

    static final SettingsModelBoolean getUseBuiltInListModel() {
        return new SettingsModelBoolean(StopWordFilterConfigKeys3.CFGKEY_USE_BUILTIN_LIST,
            StopWordFilterNodeModel3.DEF_USE_BUILTIN_LIST);
    }

    static final SettingsModelString getBuiltInListModel() {
        return new SettingsModelString(StopWordFilterConfigKeys3.CFGKEY_BUILTIN_LIST,
            BuildInStopwordListFactory.getInstance().getDefaultName());
    }

    static final SettingsModelString getStopWordColumnModel() {
        return new SettingsModelString(StopWordFilterConfigKeys3.CFGKEY_COL_NAME, "");
    }

    static final SettingsModelBoolean getUseCustomListModel() {
        return new SettingsModelBoolean(StopWordFilterConfigKeys3.CFGKEY_USE_CUSTOM_LIST,
            StopWordFilterNodeModel3.DEF_USE_CUSTOM_LIST);
    }

    private SettingsModelString m_builtInListModel;

    private SettingsModelBoolean m_useBuiltInListModel;

    private SettingsModelString m_stopWordColumnModel;

    private SettingsModelBoolean m_useCustomListModel;

    private boolean m_hasStopWordInput = true;

    /**
     * Creates new instance of {@code StopwordFilterNodeDialog3}.
     */
    @SuppressWarnings("unchecked")
    StopWordFilterNodeDialog3() {
        super();

        createNewTab("Filter options");
        setSelected("Filter options");

        setHorizontalPlacement(true);
        m_useBuiltInListModel = getUseBuiltInListModel();
        m_useBuiltInListModel.addChangeListener(e -> updateModelsBuiltIn());
        addDialogComponent(new DialogComponentBoolean(m_useBuiltInListModel, "Use built-in list"));

        m_builtInListModel = getBuiltInListModel();
        addDialogComponent(new DialogComponentStringSelection(m_builtInListModel, "Stopword lists",
            BuildInStopwordListFactory.getInstance().getNames()));
        setHorizontalPlacement(false);

        setHorizontalPlacement(true);
        m_useCustomListModel = getUseCustomListModel();
        m_useCustomListModel.addChangeListener(e -> updateModelsCustom());
        addDialogComponent(new DialogComponentBoolean(m_useCustomListModel, "Use custom list"));

        m_stopWordColumnModel = getStopWordColumnModel();
        addDialogComponent(new DialogComponentColumnNameSelection(m_stopWordColumnModel, "Stopword column ", 1, false,
            StringValue.class));

        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentBoolean(getCaseSensitiveModel(), "Case sensitive"));

        updateModelsCustom();
        updateModelsBuiltIn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        if (specs[1] == null || !((DataTableSpec)specs[1]).containsCompatibleType(StringValue.class)) {
            m_hasStopWordInput = false;
        } else {
            m_hasStopWordInput = true;
        }

        updateModelsCustom();
        updateModelsBuiltIn();
    }

    // used as ChangeListener method for m_useCustomListModel
    private void updateModelsCustom() {
        if (!m_hasStopWordInput) {
            m_useBuiltInListModel.setEnabled(false);
            m_useCustomListModel.setBooleanValue(false);
        }
        if (!m_useCustomListModel.getBooleanValue()) {
            m_useBuiltInListModel.setBooleanValue(true);
        } else {
            m_useBuiltInListModel.setEnabled(m_hasStopWordInput);
        }

        m_stopWordColumnModel.setEnabled(m_useCustomListModel.getBooleanValue());
    }

    // used as ChangeListener method for m_useBuiltInListModel
    private void updateModelsBuiltIn() {
        if (!m_useBuiltInListModel.getBooleanValue()) {
            m_useCustomListModel.setBooleanValue(true);
        } else {
            m_useCustomListModel.setEnabled(m_hasStopWordInput);
        }

        m_builtInListModel.setEnabled(m_useBuiltInListModel.getBooleanValue());
    }

}

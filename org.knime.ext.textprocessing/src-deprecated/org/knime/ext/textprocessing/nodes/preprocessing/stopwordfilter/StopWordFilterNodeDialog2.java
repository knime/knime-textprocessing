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

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
@Deprecated
public final class StopWordFilterNodeDialog2 extends PreprocessingNodeSettingsPane2 {

    /**
     * @return Creates and returns a new instance of <code>SettingsModelString</code> containing the pat to the stop
     *         word file. By default the users home is set as path.
     */
    public static final SettingsModelString getFileModel() {
        return new SettingsModelString(StopWordFilterConfigKeys2.CFGKEY_FILE, System.getProperty("user.home"));
    }

    /**
     * @return Creates and returns a new instance of <code>SettingsModelBoolean</code> containing the flag if filtering
     *         has to be done case sensitive or not.
     */
    public static final SettingsModelBoolean getCaseSensitiveModel() {
        return new SettingsModelBoolean(StopWordFilterConfigKeys2.CFGKEY_CASE_SENSITIVE,
            StopWordFilterNodeModel2.DEF_CASE_SENSITIVE);
    }

    /**
     * @return Creates and returns a new instance of <code>SettingsModelBoolean</code> containing the flag if a build in
     *         list will be used or not.
     */
    public static final SettingsModelBoolean getUseBuildInListModel() {
        return new SettingsModelBoolean(StopWordFilterConfigKeys2.CFGKEY_USE_BUILDIN_LIST,
            StopWordFilterNodeModel2.DEF_USE_BUILIN_LIST);
    }

    /**
     * @return Creates and returns a new instance of <code>SettingsModelString</code> containing the name of the
     *         selected stopword list to use.
     */
    public static final SettingsModelString getBuildInListModel() {
        return new SettingsModelString(StopWordFilterConfigKeys2.CFGKEY_BUILDIN_LIST,
            BuildInStopwordListFactory.getInstance().getDefaultName());
    }

    private SettingsModelString m_buildinListModel;

    private SettingsModelString m_fileModel;

    private SettingsModelBoolean m_useBuilinListModel;

    /**
     * Creates new instance of <code>StopwordFilterNodeDialog</code>.
     */
    public StopWordFilterNodeDialog2() {
        super();

        createNewTab("Filter options");
        setSelected("Filter options");

        addDialogComponent(new DialogComponentBoolean(getCaseSensitiveModel(), "Case sensitive"));

        setHorizontalPlacement(true);
        m_useBuilinListModel = getUseBuildInListModel();
        m_useBuilinListModel.addChangeListener(new StopwordChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_useBuilinListModel, "Use built-in list"));

        m_buildinListModel = getBuildInListModel();
        addDialogComponent(new DialogComponentStringSelection(m_buildinListModel, "Stopword lists",
            BuildInStopwordListFactory.getInstance().getNames()));
        setHorizontalPlacement(false);

        m_fileModel = getFileModel();
        DialogComponentFileChooser fileChooser = new DialogComponentFileChooser(m_fileModel,
            StopWordFilterNodeDialog2.class.toString(), JFileChooser.FILES_ONLY);
        addDialogComponent(fileChooser);

        updateModels();
    }

    private class StopwordChangeListener implements ChangeListener {
        @Override
        public void stateChanged(final ChangeEvent arg0) {
            updateModels();
        }
    }

    private void updateModels() {
        if (m_useBuilinListModel.getBooleanValue()) {
            m_buildinListModel.setEnabled(true);
            m_fileModel.setEnabled(false);
        } else {
            m_buildinListModel.setEnabled(false);
            m_fileModel.setEnabled(true);
        }
    }
}

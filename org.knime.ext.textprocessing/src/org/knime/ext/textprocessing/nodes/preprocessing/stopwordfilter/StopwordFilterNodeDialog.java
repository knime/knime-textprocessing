/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 * 
 * History
 *   16.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StopwordFilterNodeDialog extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelString</code> containing the pat to the stop word 
     * file. By default the users home is set as path.
     */
    public static final SettingsModelString getFileModel() {
        return new SettingsModelString(StopwordFilterConfigKeys.CFGKEY_FILE,
                System.getProperty("user.home"));
    }
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelBoolean</code> containing the flag if filtering has
     * to be done case sensitive or not.
     */
    public static final SettingsModelBoolean getCaseSensitiveModel() {
        return new SettingsModelBoolean(
                StopwordFilterConfigKeys.CFGKEY_CASE_SENSITIVE,
                StopwordFilterNodeModel.DEF_CASE_SENSITIVE);
    }
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelBoolean</code> containing the flag if a build in list
     * will be used or not.
     */
    public static final SettingsModelBoolean getUseBuildInListModel() {
        return new SettingsModelBoolean(
                StopwordFilterConfigKeys.CFGKEY_USE_BUILDIN_LIST,
                StopwordFilterNodeModel.DEF_USE_BUILIN_LIST);
    }
    
    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelString</code> containing the name of the selected
     * stopword list to use.
     */
    public static final SettingsModelString getBuildInListModel() {
        return new SettingsModelString(
                StopwordFilterConfigKeys.CFGKEY_BUILDIN_LIST,
                BuildInStopwordListFactory.getInstance().getDefaultName());
    }
    
    private SettingsModelString m_buildinListModel;
    
    private SettingsModelString m_fileModel;
    
    private SettingsModelBoolean m_useBuilinListModel;
    
    /**
     * Creates new instance of <code>StopwordFilterNodeDialog</code>.
     */
    public StopwordFilterNodeDialog() {
        super();
        
        createNewTab("File options");
        setSelected("File options");
        
        addDialogComponent(new DialogComponentBoolean(
                getCaseSensitiveModel(), "Case sensitive"));
        
        setHorizontalPlacement(true);
        m_useBuilinListModel = getUseBuildInListModel();
        m_useBuilinListModel.addChangeListener(new StopwordChangeListener());
        addDialogComponent(new DialogComponentBoolean(
                m_useBuilinListModel, "Use build in list"));
        
        m_buildinListModel = getBuildInListModel();
        addDialogComponent(new DialogComponentStringSelection(
                m_buildinListModel, "Stopword lists", 
                BuildInStopwordListFactory.getInstance().getNames()));
        setHorizontalPlacement(false);
        
        m_fileModel = getFileModel();
        addDialogComponent(new DialogComponentFileChooser(
                m_fileModel, StopwordFilterNodeDialog.class.toString(),
                JFileChooser.FILES_ONLY));
        
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

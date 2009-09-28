/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpenNlpNerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} 
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     * 
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                OpenNlpTaggerConfigKeys.CFGKEY_UNMODIFIABLE, 
                OpennlpNerTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString} 
     * containing the name of the OPNENLP tagging model to use.
     * 
     * @return A <code>SettingsModelString</code> containing the the name of 
     * the ABNER tagging model to use.
     */
    public static SettingsModelString createOpenNlpModelModel() {
        return new SettingsModelString(
                OpenNlpTaggerConfigKeys.CFGKEY_MODEL,
                OpennlpNerTaggerNodeModel.DEF_OPENNLPMODEL);
    }
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} 
     * containing the user settings whether a dictionary is used or not.
     * 
     * @return A <code>SettingsModelBoolean</code> containing the dictionary 
     * flag.
     */
    public static SettingsModelBoolean createUseDictModel() {
        return new SettingsModelBoolean(
                OpenNlpTaggerConfigKeys.CFGKEY_USE_DICT, 
                OpennlpNerTaggerNodeModel.DEFAULT_USE_DICT);
    }
    
    /**
     * @return Creates and returns a <code>SettingsModelString</code> containing
     * the file name of the dictionary file.
     */
    public static SettingsModelString createDictFileModel() {
        return new SettingsModelString(
                OpenNlpTaggerConfigKeys.CFGKEY_DICTFILE,
                OpennlpNerTaggerNodeModel.DEFAULT_DICT_FILENAME);
    }
    
    private SettingsModelBoolean m_useDictFileModel;
    
    private SettingsModelString m_dictFileModel;
    
    /**
     * Creates a new instance of <code>OpenNlpNerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not. 
     */
    public OpenNlpNerNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(
                createSetUnmodifiableModel(), 
                "Set named entities unmodifiable"));
        
        List<String> modelNames = new ArrayList<String>();
        for (String name : OpenNlpModelFactory.getInstance().getModelNames()) {
            modelNames.add(name);
        }
        addDialogComponent(new DialogComponentStringSelection(
                createOpenNlpModelModel(), "OpenNlp model", modelNames));
        
        
        m_useDictFileModel = createUseDictModel();
        m_useDictFileModel.addChangeListener(new SettingsChangeListener());
        addDialogComponent(new DialogComponentBoolean(
                m_useDictFileModel, "Use additional dictionary file"));
                
        m_dictFileModel = createDictFileModel();
        addDialogComponent(new DialogComponentFileChooser(
                m_dictFileModel, OpenNlpNerNodeDialog.class.toString(), "txt"));
        
        checkSettings();
    }
    
    private class SettingsChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent arg0) {
            checkSettings();
        }
        
    }
    
    private void checkSettings() {
        if (m_useDictFileModel.getBooleanValue()) {
            m_dictFileModel.setEnabled(true);
        } else {
            m_dictFileModel.setEnabled(false);
        }
    }
}

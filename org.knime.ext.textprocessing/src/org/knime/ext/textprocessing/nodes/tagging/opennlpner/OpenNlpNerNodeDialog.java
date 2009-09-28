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
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;

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
    }
    
}

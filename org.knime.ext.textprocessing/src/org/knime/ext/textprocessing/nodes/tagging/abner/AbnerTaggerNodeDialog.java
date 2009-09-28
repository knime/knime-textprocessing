/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the dialog of the AbnerTaggerNode with a checkbox component,
 * to specify whether recognized named entity terms should be set unmodifiable
 * or not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTaggerNodeDialog extends DefaultNodeSettingsPane {

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
                AbnerTaggerConfigKeys.CFGKEY_UNMODIFIABLE, 
                AbnerTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString} 
     * containing the name of the ABNER tagging model to use.
     * 
     * @return A <code>SettingsModelString</code> containing the the name of 
     * the ABNER tagging model to use.
     */
    public static SettingsModelString createAbnerModelModel() {
        return new SettingsModelString(
                AbnerTaggerConfigKeys.CFGKEY_MODEL,
                AbnerTaggerNodeModel.DEF_ABNERMODEL);
    }
    
    /**
     * Creates a new instance of <code>AbnerTaggerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not. 
     */
    public AbnerTaggerNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(
                        createSetUnmodifiableModel(), 
                        "Set named entities unmodifiable"));
        
        List<String> modelNames = new ArrayList<String>();
        modelNames.add(AbnerDocumentTagger.MODEL_BIOCREATIVE);
        modelNames.add(AbnerDocumentTagger.MODEL_NLPBA);
        addDialogComponent(new DialogComponentStringSelection(
                createAbnerModelModel(), "ABNER model", modelNames));
    }
}

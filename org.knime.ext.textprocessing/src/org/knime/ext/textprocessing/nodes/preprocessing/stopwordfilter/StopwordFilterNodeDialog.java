/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   16.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

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
     * Creates new instance of <code>StopwordFilterNodeDialog</code>.
     */
    public StopwordFilterNodeDialog() {
        super();
        
        createNewTab("File options");
        setSelected("File options");
        
        addDialogComponent(new DialogComponentBoolean(
                getCaseSensitiveModel(), "Case sensitive"));
        
        addDialogComponent(new DialogComponentFileChooser(
                getFileModel(), StopwordFilterNodeDialog.class.toString(),
                JFileChooser.FILES_ONLY));
    }
}

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
 *   01.04.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * A panel that can be used as a dialog tab for preprocessing node dialogs, 
 * providing a checkbox to specify if deep preprocessing have to be applied 
 * or not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DeepPreprocessingDialogTab extends JPanel {
    
    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if deep preprocessing have to be applied or not.
     */
    public static SettingsModelBoolean getDeepPrepressingModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_DEEP_PREPRO,
                PreprocessingNodeModel.DEF_DEEP_PREPRO);
    }
    
    private SettingsModelBoolean m_deepPreproModel;
    
    /**
     * Creates a new instance of <code>DeepPreprocessingDialogTab</code>
     * with a checkbox to specify if deep preprocessing have to be applied.
     */
    public DeepPreprocessingDialogTab() {
        m_deepPreproModel = getDeepPrepressingModel();
        DialogComponentBoolean comp = new DialogComponentBoolean(
                m_deepPreproModel, "Deep preprocessing: ");
        
        add(comp.getComponentPanel());
    }
    
    /**
     * Save the internal settings to the given settings instance.
     * @param settings To save the settings to.
     */
    public void saveSettings(final NodeSettingsWO settings) {
        m_deepPreproModel.saveSettingsTo(settings);
    }
    
    /**
     * Loads the internal settings from the given settings object.
     * @param settings The instance to load the settings form.
     * @throws InvalidSettingsException If settings are invalid and can not
     * be loaded.
     */
    public void loadSettings(final NodeSettingsRO settings) 
    throws InvalidSettingsException {
        m_deepPreproModel.loadSettingsFrom(settings);
    }
    
    /**
     * Validates the given settings.
     * @param settings The settings to validate.
     * @throws InvalidSettingsException If settings are invalid and can not be 
     * validated.
     */
    public void validateSettings(final NodeSettingsRO settings) 
    throws InvalidSettingsException {
        m_deepPreproModel.validateSettings(settings);
    }
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   01.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * A {@link org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane}
 * which provides additionally a tab that contains a checkbox to specify
 * if deep preprocessing have to be applied or not.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PreprocessingNodeSettingsPane extends DefaultNodeSettingsPane {
    
    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if deep preprocessing have to be applied or not.
     */
    public static SettingsModelBoolean getDeepPrepressingModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_DEEP_PREPRO,
                PreprocessingNodeModel.DEF_DEEP_PREPRO);
    }
    
    /**
     * Creates new instance of <code>PreprocessingNodeSettingsPane</code>.
     */
    public PreprocessingNodeSettingsPane() {
        removeTab("Options");
        createNewTabAt("Deep Preprocessing", 1);
        
        DialogComponentBoolean comp = new DialogComponentBoolean(
                getDeepPrepressingModel(), "Deep preprocessing");
        comp.setToolTipText(
                "Be aware that deep preprocessing is more time consuming!");
        
        addDialogComponent(comp);
    }
}

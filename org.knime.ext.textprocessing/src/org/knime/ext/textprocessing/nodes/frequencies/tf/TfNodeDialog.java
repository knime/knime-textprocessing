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
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TfNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns new instance of a 
     * <code>SettingsModelBoolean</code> containing the setting whether the
     * term frequency has to be computed relative or absolute.
     */
    public static SettingsModelBoolean getRelativeModel() {
        return new SettingsModelBoolean(TfConfigKeys.CFG_KEY_RELATIVE,
                TfNodeModel.DEF_RELATIVE);
    }
    
    /**
     * Creates new instance of <code>TfNodeDialog</code>.
     */
    public TfNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(getRelativeModel(), 
                "Relative frequency"));
    }
}

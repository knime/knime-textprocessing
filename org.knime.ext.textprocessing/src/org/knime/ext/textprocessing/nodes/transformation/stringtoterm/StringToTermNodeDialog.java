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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringToTermNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns the settings model, storing the column name of the
     * string column.
     * @return The settings model with the column name of the string column. 
     */
    static final SettingsModelString getStringColModel() {
        return new SettingsModelString(StringToTermConfigKeys.STRING_COLNAME,
                "");
    }
    
    /**
     * Creates new instance of <code>StringToTermNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public StringToTermNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getStringColModel(), "String column", 0, 
                StringValue.class));
    }
}

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
 *   25.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termtostring;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermValue;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStringNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns an instance of 
     * <code>SettingsModelString</code> specifying the column which has to
     * be used as title column.
     */
    public static final SettingsModelString getTermColModel() {
        return new SettingsModelString(
                TermToStringConfigKeys.CFGKEY_TERMCOL, "");
    }
    
    /**
     * Creates a new instance of <code>TermToStringNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public TermToStringNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getTermColModel(), "Term Column", 0, TermValue.class));
    }
}

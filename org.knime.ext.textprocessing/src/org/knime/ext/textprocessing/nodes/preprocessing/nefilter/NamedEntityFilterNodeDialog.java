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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.nefilter;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 * The dialog of the named entity filter node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NamedEntityFilterNodeDialog extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelBoolean</code> which contains the settings if
     * modifiable or unmodifiable terms have o be filtered.
     */
    public static final SettingsModelBoolean getFilterModifiableModel() {
        return new SettingsModelBoolean(
                NamedEntityConfigKeys.CFGKEY_FILTERMODIFIABLE, 
                NamedEntityFilterNodeModel.DEF_FILTER_MODIFIABLE);
    }
    
    /**
     * Creates a new instance of <code>NamedEntityFilterNodeDialog</code>.
     */
    public NamedEntityFilterNodeDialog() {
        super();
        
        createNewTab("Filter options");
        setSelected("Filter options");
        
        addDialogComponent(new DialogComponentBoolean(
                getFilterModifiableModel(), "Filter modifiable terms"));
    }
}

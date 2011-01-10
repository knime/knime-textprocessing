/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2011
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
 *   19.02.2010 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.regexfilter;

import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class RegExFilterNodeDialog extends PreprocessingNodeSettingsPane {
    
    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the regular expression.
     */
    public static final SettingsModelString getRegExModel() {
        return new SettingsModelString(
                RegExFilterConfigKeys.CFGKEY_REGEX,
                RegExFilterNodeModel.DEFAULT_REGEX);
    }

    /**
     * Creates new instance of <code>StopwordFilterNodeDialog</code>.
     */
    public RegExFilterNodeDialog() {
        super();

        createNewTab("Filter options");
        setSelected("Filter options");

        addDialogComponent(new DialogComponentString(
                getRegExModel(), "Regular expression"));
    }

}

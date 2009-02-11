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
 *   16.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.replacer;

import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class RegExReplacerNodeDialog extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the regular expression.
     */
    public static final SettingsModelString getRegExModel() {
        return new SettingsModelString(
                RegExReplacerFilterConfigKeys.CFGKEY_REGEX,
                RegExReplacerNodeModel.DEFAULT_REGEX);
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the replacement.
     */
    public static final SettingsModelString getReplacementModel() {
        return new SettingsModelString(
                RegExReplacerFilterConfigKeys.CFGKEY_REPLACEMENT,
                RegExReplacerNodeModel.DEFAULT_REPLACEMENT);
    }

    /**
     * Creates new instance of <code>StopwordFilterNodeDialog</code>.
     */
    public RegExReplacerNodeDialog() {
        super();

        createNewTab("Replacement options");
        setSelected("Replacement options");

        addDialogComponent(new DialogComponentString(
                getRegExModel(), "Regular expression"));

        addDialogComponent(new DialogComponentString(
                getReplacementModel(), "Replacement"));
    }
}

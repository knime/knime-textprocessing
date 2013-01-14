/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
                RegExReplacerConfigKeys.CFGKEY_REGEX,
                RegExReplacerNodeModel.DEFAULT_REGEX);
    }

    /**
     * @return Creates and returns a new instance of
     * <code>SettingsModelString</code> containing the replacement.
     */
    public static final SettingsModelString getReplacementModel() {
        return new SettingsModelString(
                RegExReplacerConfigKeys.CFGKEY_REPLACEMENT,
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

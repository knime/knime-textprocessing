/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.frequencies.FrequenciesNodeSettingsPane;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TfNodeDialog extends FrequenciesNodeSettingsPane {

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
     * Creates new instance of <code>IdfNodeDialog</code>.
     */
    public TfNodeDialog() {
        super();
        
        createNewTab("TF options");
        setSelected("TF options");
        
        addDialogComponent(new DialogComponentBoolean(getRelativeModel(), 
                "Relative frequency"));
    }
}

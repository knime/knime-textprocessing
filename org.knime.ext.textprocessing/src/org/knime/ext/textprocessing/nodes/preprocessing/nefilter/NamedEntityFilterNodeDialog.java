/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

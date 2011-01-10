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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates the dialog of the AbnerTaggerNode with a checkbox component,
 * to specify whether recognized named entity terms should be set unmodifiable
 * or not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTaggerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} 
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     * 
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                AbnerTaggerConfigKeys.CFGKEY_UNMODIFIABLE, 
                AbnerTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString} 
     * containing the name of the ABNER tagging model to use.
     * 
     * @return A <code>SettingsModelString</code> containing the the name of 
     * the ABNER tagging model to use.
     */
    public static SettingsModelString createAbnerModelModel() {
        return new SettingsModelString(
                AbnerTaggerConfigKeys.CFGKEY_MODEL,
                AbnerTaggerNodeModel.DEF_ABNERMODEL);
    }
    
    /**
     * Creates a new instance of <code>AbnerTaggerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not. 
     */
    public AbnerTaggerNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(
                        createSetUnmodifiableModel(), 
                        "Set named entities unmodifiable"));
        
        List<String> modelNames = new ArrayList<String>();
        modelNames.add(AbnerDocumentTagger.MODEL_BIOCREATIVE);
        modelNames.add(AbnerDocumentTagger.MODEL_NLPBA);
        addDialogComponent(new DialogComponentStringSelection(
                createAbnerModelModel(), "ABNER model", modelNames));
    }
}

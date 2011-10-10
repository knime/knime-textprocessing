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
package org.knime.ext.textprocessing.nodes.tagging.stanford;

import java.util.Set;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Creates the dialog of the OscarTaggerNode with a checkbox component,
 * to specify whether recognized named entity terms should be set unmodifiable
 * or not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StanfordTaggerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString} 
     * containing the user settings of the specified tagger model to use.
     * 
     * @return A <code>SettingsModelString</code> containing the tagger model 
     * to use.
     */
    public static SettingsModelString createTaggerModelModel() {
        return new SettingsModelString(
                StanfordTaggerConfigKeys.CFGKEY_MODEL, 
                StanfordTaggerNodeModel.DEF_MODEL);
    }
    
    /**
     * Creates a new instance of <code>StanfordTaggerNodeDialog</code> a drop 
     * down box to choose a tagger model to use.
     */
    public StanfordTaggerNodeDialog() {
        Set<String> models = StanfordDocumentTagger.TAGGERMODELS.keySet();
        addDialogComponent(new DialogComponentStringSelection(
                createTaggerModelModel(), "Tagger model", models));
    }
}

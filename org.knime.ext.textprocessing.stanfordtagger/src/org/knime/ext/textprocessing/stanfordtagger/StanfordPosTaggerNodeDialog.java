/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 * -------------------------------------------------------------------
 * 
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.stanfordtagger;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class StanfordPosTaggerNodeDialog extends DefaultNodeSettingsPane {
    
    public static SettingsModelString createStanfordModelModel() {
        return new SettingsModelString(
        		StanfordPosTaggerConfigKeys.CFGKEY_MODEL, "");
    }

    public static SettingsModelString createMappingFileModel() {
        return new SettingsModelString(
        		StanfordPosTaggerConfigKeys.CFGKEY_MAPPINGFILE, "");
    }
    
    public static SettingsModelString createSeparatorModel() {
        return new SettingsModelString(
        		StanfordPosTaggerConfigKeys.CFGKEY_SEPARATOR, "");
    }    
    
    public StanfordPosTaggerNodeDialog() {
    	
    	createNewGroup("Tagging Model");
    	addDialogComponent(new DialogComponentFileChooser(
    			createStanfordModelModel(), 
    			StanfordPosTaggerNodeDialog.class.toString(), "tagger"));
    	closeCurrentGroup();
    	
    	createNewGroup("Tagset Mapping");
    	addDialogComponent(new DialogComponentFileChooser(
    			createMappingFileModel(), 
    			StanfordPosTaggerNodeDialog.class.toString()));
    	
    	addDialogComponent(new DialogComponentString(createSeparatorModel(), 
    			"Tagset mapping separator"));
    	closeCurrentGroup();
    }
}

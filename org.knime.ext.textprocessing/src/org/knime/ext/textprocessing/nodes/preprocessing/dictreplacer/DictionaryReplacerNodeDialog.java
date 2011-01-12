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
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;

import javax.swing.JFileChooser;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacerNodeDialog 
extends PreprocessingNodeSettingsPane {

    /**
     * @return Creates and returns a new instance of 
     * <code>SettingsModelString</code> containing the path to the 
     * dictionary file.
     */
    public static final SettingsModelString getDictionaryFileModel() {
        return new SettingsModelString(
                DictionaryReplacerConfigKeys.CFGKEY_DICTFILE, 
                DictionaryReplacerNodeModel.DEF_DICTFILE);
    }
    
    /**
     * Creates new instance of <code>DictionaryReplacerNodeDialog</code>.
     */
    public DictionaryReplacerNodeDialog() {
        super();

        createNewTab("Dictionary");
        setSelected("Dictionary");

        addDialogComponent(new DialogComponentFileChooser(
                getDictionaryFileModel(), 
                DictionaryReplacerNodeDialog.class.toString(),
                JFileChooser.FILES_ONLY, 
                DictionaryReplacerNodeModel.VALID_DICTFILE_EXTENIONS));
    }
}

/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
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

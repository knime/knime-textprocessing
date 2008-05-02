/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.NamedEntityTag;

/**
 * The dialog class of the dictionary named entity recognizer node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryTaggerNodeDialog extends DefaultNodeSettingsPane {
    
    /**
     * Creates and returns a 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} 
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     * 
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static final SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_UNMODIFIABLE, 
                DictionaryTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the flag specifying whether the search for named entities
     * have to be done case sensitive or not.
     */
    public static final SettingsModelBoolean createCaseSensitiveModel() {
        return new SettingsModelBoolean(
                DictionaryTaggerConfigKeys.CFGKEY_CASE_SENSITIVE,
                DictionaryTaggerNodeModel.DEFAULT_CASE_SENSITIVE);
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the file with the named entities to watch out for.
     */
    public static final SettingsModelString createFileModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_FILE, "");
    }
    
    /**
     * @return Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the tag to assign to each found named entity.
     */
    public static final SettingsModelString createTagModel() {
        return new SettingsModelString(
                DictionaryTaggerConfigKeys.CFGKEY_TAG, 
                DictionaryTaggerNodeModel.DEFAULT_TAG);
    }
    
    /**
     * Creates a new instance of <code>AbnerTaggerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not. 
     */
    public DictionaryTaggerNodeDialog() {
        addDialogComponent(new DialogComponentBoolean(
                        createSetUnmodifiableModel(), 
                        "Set named entities unmodifiable"));
        
        addDialogComponent(new DialogComponentBoolean(
                createCaseSensitiveModel(), 
                "Case sensitive"));
        
        addDialogComponent(new DialogComponentFileChooser(
                createFileModel(), 
                DictionaryTaggerNodeDialog.class.toString()));
        
        addDialogComponent(new DialogComponentStringSelection(
                createTagModel(), "Named entity tag", 
                NamedEntityTag.asStringList()));
    }
}

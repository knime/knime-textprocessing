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
 *   19.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * Creates the dialog of the DocumentParserNode with a file chooser component,
 * to specify the directory containing the files to parse, and a checkbox
 * component to specify if the directory is searched recursively for files to 
 * parse.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentParserNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return The 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the path to the directory containing the files to parse.
     */
    static SettingsModelString getPathModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_PATH, 
                DocumentParserNodeModel.DEFAULT_PATH);
    }

    /**
     * @return The 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the <code>boolean</code> value of the recursive flag (if
     * set <code>true</code> the specified directory will be search 
     * recursively).
     */
    static SettingsModelBoolean getRecursiveModel() {
        return new SettingsModelBoolean(
                DocumentParserConfigKeys.CFGKEY_RECURSIVE, 
                DocumentParserNodeModel.DEFAULT_RECURSIVE);
    }
    
    /**
     * @return The 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the category to set to the documents. 
     */
    static SettingsModelString getCategoryModel() {
        return new SettingsModelString(
                DocumentParserConfigKeys.CFGKEY_CATEGORY, 
                DocumentParserNodeModel.DEFAULT_CATEGORY);
    }
    
    /**
     * @return The 
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the document's source to set. 
     */
    static SettingsModelString getSourceModel() {
        return new SettingsModelString(
                DocumentParserConfigKeys.CFGKEY_SOURCE, 
                DocumentParserNodeModel.DEFAULT_SOURCE);
    }    
    
    
    /**
     * Creates a new instance of <code>DocumentParserNodeDialog</code> which 
     * displays a file chooser component, to specify the directory containing 
     * the files to parse, and a checkbox component to specify if the 
     * directory is searched recursively for files to parse.
     */
    public DocumentParserNodeDialog() {
        addDialogComponent(new DialogComponentFileChooser(
                getPathModel(), DocumentParserNodeDialog.class.toString(), 
                JFileChooser.OPEN_DIALOG, true));
        
        addDialogComponent(new DialogComponentBoolean(
                getRecursiveModel(), "Search recursively"));
        
        addDialogComponent(new DialogComponentString(
                getCategoryModel(), "Document category"));
        
        addDialogComponent(new DialogComponentString(
                getSourceModel(), "Document source"));        
    }
}

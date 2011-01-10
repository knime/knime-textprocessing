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
 *   01.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

import javax.swing.event.ChangeListener;

/**
 * A {@link org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane}
 * which provides additionally a tab that contains a checkbox to specify
 * if deep preprocessing have to be applied or not.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PreprocessingNodeSettingsPane extends DefaultNodeSettingsPane {
    
    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if deep preprocessing have to be applied or not.
     */
    public static SettingsModelBoolean getDeepPrepressingModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_DEEP_PREPRO,
                PreprocessingNodeModel.DEF_DEEP_PREPRO);
    }
    
    /**
     * @return Creates and returns the boolean settings model which contains
     * the flag if the incoming original documents have to be applied in an
     * extra column.
     */
    public static SettingsModelBoolean getAppendIncomingDocument() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_APPEND_INCOMING,
                PreprocessingNodeModel.DEF_APPEND_INCOMING);
    }
    
    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the documents to preprocess.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                PreprocessingConfigKeys.CFG_KEY_DOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }
    
    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the original documents to append unchanged.
     */
    public static SettingsModelString getOrigDocumentColumnModel() {
        return new SettingsModelString(
                PreprocessingConfigKeys.CFG_KEY_ORIGDOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME);
    }
    
    /**
     * @return Creates and returns a <code>SettingsModelBoolean</code> which
     * stores the flag specifying whether unmodifiable terms will be 
     * preprocessed was well or not.
     */
    public static SettingsModelBoolean getPreprocessUnmodifiableModel() {
        return new SettingsModelBoolean(
                PreprocessingConfigKeys.CFG_KEY_PREPRO_UNMODIFIABLE,
                PreprocessingNodeModel.DEF_PREPRO_UNMODIFIABLE);
    }
    
    /**
     * Creates new instance of <code>PreprocessingNodeSettingsPane</code>.
     */
    @SuppressWarnings("unchecked")
    public PreprocessingNodeSettingsPane() {
        removeTab("Options");
        createNewTabAt("Preprocessing", 1);
                
        //
        // document to preprocess and deep preprocessing
        //
        createNewGroup("Deep preprocessing");
        
        SettingsModelBoolean deepPreproModel = getDeepPrepressingModel();
        DialogComponentBoolean comp1 = new DialogComponentBoolean(
                deepPreproModel, "Deep preprocessing");
        comp1.setToolTipText(
                "Be aware that deep preprocessing is more time consuming!");
        addDialogComponent(comp1);

        SettingsModelString documentColModel = getDocumentColumnModel();
        DialogComponentColumnNameSelection comp3 = 
            new DialogComponentColumnNameSelection(documentColModel, 
                    "Document column", 0, DocumentValue.class);
        comp3.setToolTipText(
                "Column has to contain documents to preprocess!");
        addDialogComponent(comp3);

        ChangeListener cl1 = new DefaultSwitchEventListener(
                documentColModel, deepPreproModel);
        deepPreproModel.addChangeListener(cl1);
        
        closeCurrentGroup();
        
        //
        // original document to append and append setting
        //
        createNewGroup("Appending");
        
        SettingsModelBoolean appendOrigDocModel = getAppendIncomingDocument(); 
        DialogComponentBoolean comp2 = new DialogComponentBoolean(
                appendOrigDocModel, "Append unchanged documents");
        comp2.setToolTipText(
                "The unchanged incoming documents will be appended!");
        addDialogComponent(comp2);
        
        SettingsModelString origDocColModel = getOrigDocumentColumnModel();
        DialogComponentColumnNameSelection comp4 = 
            new DialogComponentColumnNameSelection(origDocColModel, 
                    "Original Document column", 0, DocumentValue.class);
        comp4.setToolTipText("Column has to contain the original documents " 
                + "to append unchanged!");
        addDialogComponent(comp4);
        
        ChangeListener cl2 = new DefaultSwitchEventListener(
                origDocColModel, appendOrigDocModel);
        appendOrigDocModel.addChangeListener(cl2);
        
        cl1.stateChanged(null);
        cl2.stateChanged(null);
        
        closeCurrentGroup();
        
        //
        // chunk size
        //
//        createNewGroup("Chunking");
//        
//        addDialogComponent(new DialogComponentNumber(
//                getChunkSizeModel(), "Chunk size", 1000));
//        
//        closeCurrentGroup();
        
        //
        // preprocess unmodifiable terms
        //
        createNewGroup("Unmodifiable policy");
        
        addDialogComponent(new DialogComponentBoolean(
                getPreprocessUnmodifiableModel(), 
                "Ignore unmodifiable flag"));
        
        closeCurrentGroup();
    }
}

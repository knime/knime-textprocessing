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
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return A new instance of <code>SettingsModelStringArray</code>
     * containing the specified tag types to use.
     */
    public static SettingsModelStringArray getTagTypesModel() {
        return new SettingsModelStringArray(
                TagToStringConfigKeys.CFG_KEY_TAG_TYPES,
                new String[]{TagToStringNodeModel.DEFAULT_TAG_TYPE});
    }
    
    /**
     * @return A new instance of <code>SettingsModelString</code>
     * containing the specified term column.
     */
    public static SettingsModelString getTermColModel() {
        return new SettingsModelString(TagToStringConfigKeys.CFG_KEY_TERM_COL,
                BagOfWordsDataTableBuilder.DEF_TERM_COLNAME);
    }
    
    /**
     * @return A new instance of <code>SettingsModelString</code>
     * containing the specified missinn tag value.
     */
    public static SettingsModelString getMissingTagModel() {
        return new SettingsModelString(
                TagToStringConfigKeys.CFG_KEY_MISSING_TAG_STRING,
                TagToStringNodeModel.DEFAULT_MISSING_VALUE);
    }
    
    /**
     * Creates a new instance of <code>TagToStringNodeDialog</code>, providing
     * the dialog components.
     */
    @SuppressWarnings("unchecked")
    public TagToStringNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getTermColModel(), "Term column", 0, TermValue.class));
        
        String[] allTagTypes = 
            TagToStringNodeModel.ALL_TAG_TYPES.toArray(new String[0]);
        addDialogComponent(new DialogComponentStringListSelection(
                getTagTypesModel(), "Tag types", allTagTypes));
        
        addDialogComponent(new DialogComponentString(
                getMissingTagModel(), "Missing tag value", false, 15));
    }
}

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

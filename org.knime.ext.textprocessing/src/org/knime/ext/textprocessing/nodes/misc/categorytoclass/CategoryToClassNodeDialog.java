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
 * ---------------------------------------------------------------------
 * 
 * History
 *   18.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.misc.categorytoclass;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class CategoryToClassNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns new instance of 
     * <code>SettingsModelString</code> containing the name of the document
     * column to use.
     */
    public static final SettingsModelString getDocumentColModel() {
        return new SettingsModelString(
                CategoryToClassConfigKeys.CFG_KEY_DOCUMENT_COL, 
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }
    
    /**
     * Creates new instance if <code>CategoryToClassNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public CategoryToClassNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getDocumentColModel(), "Document column", 0, 
                DocumentValue.class));
    }
}

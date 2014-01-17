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
 * ---------------------------------------------------------------------
 * 
 * History
 *   08.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns the string settings model containing
     * the name of the column with the documents to preprocess.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                DocumentViewerConfigKeys.CFG_KEY_DOCUMENT_COL,
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }
    
    /**
     * Creates new instance of <code>DocumentViewerNodeDialog</code>.
     */
    @SuppressWarnings("unchecked")
    public DocumentViewerNodeDialog() {
        removeTab("Options");
        createNewTabAt("Preprocessing", 1);
        
        DialogComponentColumnNameSelection comp1 = 
            new DialogComponentColumnNameSelection(getDocumentColumnModel(), 
                    "Document column", 0, DocumentValue.class);
        comp1.setToolTipText(
                "Column has to contain documents to preprocess!");
        addDialogComponent(comp1);
    }
}

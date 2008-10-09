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
 *   08.08.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsBlobCellDataTableBuilder;

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
                BagOfWordsBlobCellDataTableBuilder.DEF_DOCUMENT_COLNAME);
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

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   18.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.misc.categorytoclass;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsBlobCellDataTableBuilder;

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
                BagOfWordsBlobCellDataTableBuilder.DEF_DOCUMENT_COLNAME);
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

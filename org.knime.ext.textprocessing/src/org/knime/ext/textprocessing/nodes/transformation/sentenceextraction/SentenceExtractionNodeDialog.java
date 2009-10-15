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
 *   20.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.sentenceextraction;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class SentenceExtractionNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return Creates a new instance of <code>SettingsModelString</code>
     * containing the name of the document column.
     */
    public static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(
                SentenceExtractionConfigKeys.CFG_KEY_DOCUMENT_COLUMN,
                DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME);
    }
    
    /**
     * Creates a new instance of <code>SentenceExtractionNodeDialog</code>.
     */
    public SentenceExtractionNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(
                getDocumentColumnModel(), "Document column", 0, 
                DocumentValue.class));
    }
}

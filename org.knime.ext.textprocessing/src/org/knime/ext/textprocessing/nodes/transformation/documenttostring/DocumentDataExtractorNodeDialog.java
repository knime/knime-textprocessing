/*
 * -------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.DocumentValue;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorNodeDialog extends DefaultNodeSettingsPane {

    private final SettingsModelString m_documentCol =
        DocumentDataExtractorNodeModel.getDocumentColConfigObj();

    private final SettingsModelStringArray m_extractorNames =
        DocumentDataExtractorNodeModel.getExtractorNamesConfigObj();

    /**Constructor for class DocumentDataExtractorNodeDialog.
     */
    @SuppressWarnings("unchecked")
    public DocumentDataExtractorNodeDialog() {
        final DialogComponent colName = new DialogComponentColumnNameSelection(
                m_documentCol, "Document column: ", 0, true,
                DocumentValue.class);
        final DialogComponent extractors =
            new DialogComponentStringListSelection(m_extractorNames,
                    "Data extractors: ", 
                    DocumentDataExtractor.getExtractorNames());
        addDialogComponent(colName);
        addDialogComponent(extractors);
    }
}

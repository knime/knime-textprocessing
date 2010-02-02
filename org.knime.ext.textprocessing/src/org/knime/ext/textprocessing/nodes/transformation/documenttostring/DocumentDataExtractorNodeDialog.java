/*
========================================================================
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
 * The NodeDialog implementation of the DocumentDataExtractor node.
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

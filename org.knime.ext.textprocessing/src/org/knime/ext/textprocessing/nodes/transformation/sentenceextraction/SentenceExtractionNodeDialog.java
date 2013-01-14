/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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

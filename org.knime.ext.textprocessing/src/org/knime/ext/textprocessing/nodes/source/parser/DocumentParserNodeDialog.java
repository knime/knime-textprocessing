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
 *   19.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import javax.swing.JFileChooser;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentType;

/**
 * Creates the dialog of the DocumentParserNode with a file chooser component, to specify the directory containing the
 * files to parse, and a checkbox component to specify if the directory is searched recursively for files to parse.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentParserNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the path to the
     *         directory containing the files to parse.
     */
    static SettingsModelString getPathModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_PATH, DocumentParserNodeModel.DEFAULT_PATH);
    }

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} containing the
     *         <code>boolean</code> value of the recursive flag (if set <code>true</code> the specified directory will
     *         be search recursively).
     */
    static SettingsModelBoolean getRecursiveModel() {
        return new SettingsModelBoolean(DocumentParserConfigKeys.CFGKEY_RECURSIVE,
            DocumentParserNodeModel.DEFAULT_RECURSIVE);
    }

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the category to set to
     *         the documents.
     */
    static SettingsModelString getCategoryModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_CATEGORY,
            DocumentParserNodeModel.DEFAULT_CATEGORY);
    }

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the document's source
     *         to set.
     */
    static SettingsModelString getSourceModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_SOURCE, DocumentParserNodeModel.DEFAULT_SOURCE);
    }

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the document's type to
     *         set.
     */
    static SettingsModelString getTypeModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_DOCTYPE,
            DocumentParserNodeModel.DEFAULT_DOCTYPE.toString());
    }

    /**
     * @return The {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} containing the
     *         <code>boolean</code> value of the ignore hidden files flag (if set <code>true</code> hidden files will be
     *         not considered for parsing.
     */
    static SettingsModelBoolean getIgnoreHiddenFilesModel() {
        return new SettingsModelBoolean(DocumentParserConfigKeys.CFGKEY_IGNORE_HIDDENFILES,
            DocumentParserNodeModel.DEFAULT_IGNORE_HIDDENFILES);
    }

    /**
     * Creates a new instance of <code>DocumentParserNodeDialog</code> which displays a file chooser component, to
     * specify the directory containing the files to parse, and a checkbox component to specify if the directory is
     * searched recursively for files to parse.
     */
    public DocumentParserNodeDialog() {
        addDialogComponent(new DialogComponentFileChooser(getPathModel(), DocumentParserNodeDialog.class.toString(),
            JFileChooser.OPEN_DIALOG, true));

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(getRecursiveModel(), "Search recursively"));

        addDialogComponent(new DialogComponentBoolean(getIgnoreHiddenFilesModel(), "Ignore hidden files"));
        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentString(getCategoryModel(), "Document category"));

        addDialogComponent(new DialogComponentString(getSourceModel(), "Document source"));

        String[] types = DocumentType.asStringList().toArray(new String[0]);
        addDialogComponent(new DialogComponentStringSelection(getTypeModel(), "Document Type", types));
    }
}

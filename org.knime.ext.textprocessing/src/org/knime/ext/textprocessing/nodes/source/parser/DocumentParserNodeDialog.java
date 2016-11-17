/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   19.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactory;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

import com.google.common.collect.ImmutableMap;

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

    static SettingsModelString getTokenizerModel() {
        return new SettingsModelString(DocumentParserConfigKeys.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
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

        Set<String> tokenizerList = new TreeSet<String>();
        for (ImmutableMap.Entry<String, TokenizerFactory> entry : TokenizerFactoryRegistry.getTokenizerFactoryMap().entrySet()) {
            tokenizerList.add(entry.getKey());
        }
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));

    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Oct 19, 2018 (dewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.bratdocumentwriter;

import javax.swing.JFileChooser;

import org.knime.core.node.NodeDialog;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The {@link NodeDialog} for the Brat Document Writer node.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
public class BratDocumentWriterNodeDialog extends DefaultNodeSettingsPane {

    static final SettingsModelString getDocColModel() {
        return new SettingsModelString(BratDocumentWriterConfigKeys.DOC_COLNAME, "");
    }

    static final SettingsModelString getDirectoryModel() {
        return new SettingsModelString(BratDocumentWriterConfigKeys.DIR_PATH, BratDocumentWriterNodeModel.DEF_DIR);
    }

    static final SettingsModelBoolean getOverwriteModel() {
        return new SettingsModelBoolean(BratDocumentWriterConfigKeys.OVERWRITE_FILES, false);
    }

    /**
     * The node dialog for Brat Document Writer. It contains a directory chooser, an overwrite option checkbox, and a
     * column name selection to choose a document column.
     */
    @SuppressWarnings("unchecked")
    public BratDocumentWriterNodeDialog() {
        DialogComponentFileChooser selectDir = new DialogComponentFileChooser(getDirectoryModel(),
            BratDocumentWriterNodeDialog.class.toString(), JFileChooser.SAVE_DIALOG, true,
            createFlowVariableModel(BratDocumentWriterConfigKeys.DIR_PATH, Type.STRING));
        selectDir.setBorderTitle("Output directory");
        addDialogComponent(selectDir);

        addDialogComponent(new DialogComponentBoolean(getOverwriteModel(), "Overwrite existing files"));

        addDialogComponent(
            new DialogComponentColumnNameSelection(getDocColModel(), "Document column", 0, DocumentValue.class));
    }
}

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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialog;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The {@link NodeDialog} for the Brat Document Writer node.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
final class BratDocumentWriterNodeDialog extends DefaultNodeSettingsPane {

    /**
     * The default target directory.
     */
    private static final String DEF_DIR = System.getProperty("user.home");

    /**
     * The configuration key for the column name of the doc column.
     */
    private static final String CFG_DOC_COLNAME = "DocColName";

    /**
     * The configuration key for the path to directory.
     */
    private static final String CFG_DIR_PATH = "DirPath";

    /**
     * The configuration key for overwrite flag.
     */
    private static final String CFG_OVERWRITE_FILES = "OverwriteFiles";

    /**
     * The configuration key for file name prefix.
     */
    private static final String CFG_PREFIX = "PrefixFilename";

    /**
     * The configuration key for file name suffix.
     */
    private static final String CFG_SUFFIX = "SuffixFilename";

    /**
     * Get the document column SettingsModel.
     *
     * @return the SettingsModelString for document column
     */
    static final SettingsModelString getDocColModel() {
        return new SettingsModelString(CFG_DOC_COLNAME, "");
    }

    /**
     * Get the directory path SettingsModel.
     *
     * @return the SettingsModelString for the directory path
     */
    static final SettingsModelString getDirectoryModel() {
        return new SettingsModelString(CFG_DIR_PATH, DEF_DIR);
    }

    /**
     * Get the overwrite flag SettingsModel.
     *
     * @return the SettingsModelBoolean for the overwrite flag
     */
    static final SettingsModelBoolean getOverwriteModel() {
        return new SettingsModelBoolean(CFG_OVERWRITE_FILES, false);
    }

    /**
     * Get the file name prefix.
     *
     * @return the SettingsModelString for the file name prefix
     */
    static final SettingsModelString getPrefixModel() {
        return new SettingsModelString(CFG_PREFIX, "");
    }

    /**
     * Get the file name suffix.
     *
     * @return the SettingsModelString for the file name suffix
     */
    static final SettingsModelString getSuffixModel() {
        return new SettingsModelString(CFG_SUFFIX, "");
    }

    final private SettingsModelString m_prefixModel;

    final private SettingsModelString m_suffixModel;

    final private DialogComponentLabel m_errorLabel;

    /**
     * The node dialog for Brat Document Writer. It contains a directory chooser, two dialog component strings for the
     * optional prefix and suffix with an error label, an overwrite option checkbox, and a column name selection to
     * choose a document column.
     */
    @SuppressWarnings("unchecked")
    public BratDocumentWriterNodeDialog() {
        final DialogComponentFileChooser selectDir =
            new DialogComponentFileChooser(getDirectoryModel(), BratDocumentWriterNodeDialog.class.toString(),
                JFileChooser.SAVE_DIALOG, true, createFlowVariableModel(CFG_DIR_PATH, Type.STRING));
        selectDir.setBorderTitle("Output directory");
        addDialogComponent(selectDir);

        createNewGroup("Filename settings (optional)");
        setHorizontalPlacement(true);

        m_prefixModel = getPrefixModel();
        // validate the value every time the model is updated
        m_prefixModel.addChangeListener(new ValidateTextListener());
        addDialogComponent(new DialogComponentString(m_prefixModel, "Prefix"));

        m_suffixModel = getSuffixModel();
        m_suffixModel.addChangeListener(new ValidateTextListener());
        addDialogComponent(new DialogComponentString(m_suffixModel, "Suffix"));

        setHorizontalPlacement(false);

        // label to place the prefix and suffix error validation message
        m_errorLabel = new DialogComponentLabel("");
        setErrorColor(m_errorLabel);
        addDialogComponent(m_errorLabel);

        closeCurrentGroup();

        addDialogComponent(new DialogComponentBoolean(getOverwriteModel(), "Overwrite existing files"));

        addDialogComponent(
            new DialogComponentColumnNameSelection(getDocColModel(), "Document column", 0, DocumentValue.class));
    }

    /**
     * Set the error message to red color.
     *
     * @param errorLabel the label to be set to red
     */
    private static void setErrorColor(final DialogComponentLabel errorLabel) {
        // find the JLabel in the list of components and set its color to red
        for (Component c : errorLabel.getComponentPanel().getComponents()) {
            if (c instanceof JLabel) {
                ((JLabel)c).setForeground(Color.RED);
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        super.saveAdditionalSettingsTo(settings);

        // verify prefix and suffix again before closing the dialog
        BratDocumentWriterNodeModel.checkForInvalidChars(m_prefixModel.getStringValue());
        BratDocumentWriterNodeModel.checkForInvalidChars(m_suffixModel.getStringValue());
    }

    private class ValidateTextListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            // validate both prefix and suffix models every time a change occurs
            // If an invalid char is found, show the error message in the node
            // dialog.
            String errorMsg = "";

            try {
                BratDocumentWriterNodeModel.checkForInvalidChars(m_prefixModel.getStringValue());
            } catch (InvalidSettingsException ex) {
                errorMsg = "Error: prefix contains invalid char(s)";
            }
            try {
                BratDocumentWriterNodeModel.checkForInvalidChars(m_suffixModel.getStringValue());
            } catch (InvalidSettingsException ex) {
                if (errorMsg.isEmpty()) {
                    errorMsg = "Error: suffix contains invalid char(s)";
                } else {
                    errorMsg = "Error: prefix and suffix contain invalid char(s)";
                }
            }
            m_errorLabel.setText(errorMsg);
        }
    }
}

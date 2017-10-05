/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   08.11.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentPasswordField;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public abstract class AbstractTikaNodeDialog extends DefaultNodeSettingsPane {

    private static final String PDF_EXT = "pdf";

    private static final String PDF_MIME = "application/pdf";

    private SettingsModelString m_typeModel;

    private SettingsModelString m_errorColNameModel;

    private SettingsModelBoolean m_errorColModel;

    private SettingsModelBoolean m_extractBooleanModel;

    private SettingsModelString m_extractPathModel;

    private SettingsModelBoolean m_extractInlineImagesModel;

    private SettingsModelBoolean m_authBooleanModel;

    private SettingsModelString m_authModel;

    private TikaDialogComponentStringFilter m_filterComponent;

    private SettingsModelFilterString m_filterModel;

    /**
     * An abstract Tika node dialog class which displays an input group (either a file chooser component, to specify the
     * directory containing the files to parse, or a column list to specify the input document column), then two
     * checkbox components to specify if the directory is searched recursively for files to parse and whether to ignore
     * hidden files, button and list components to specify which file types are to be parsed (through file extension or
     * MIME-Type), and the last list component is to specify which meta data information are to be extracted. A tick box
     * for creating an additional error column is provided. Another boolean button is added to specify whether embedded
     * files should be extracted as well to a specific directory using a file chooser component. For encrypted files,
     * there is a boolean button to specify whether any detected encrypted files should be parsed. If set to true, a
     * password has to be given in the authentication component.
     *
     */
    public AbstractTikaNodeDialog() {

        createInputGroup();

        createNewGroup("File type settings");
        m_typeModel = TikaParserConfig.getTypeModel();

        ButtonGroupEnumInterface[] options = new ButtonGroupEnumInterface[2];
        options[0] = new TypeButtonGroup("File Extension", true, "Choose which file to parse through its extension",
            TikaParserConfig.EXT_TYPE);
        options[1] = new TypeButtonGroup("MIME-Type", false, "Choose which file to parse through its MIME-Type",
            TikaParserConfig.MIME_TYPE);

        addDialogComponent(new DialogComponentButtonGroup(m_typeModel, "Choose which type to parse", false, options));
        m_typeModel.addChangeListener(new ButtonChangeListener());

        m_filterModel = TikaParserConfig.getFilterModel();
        m_filterModel.addChangeListener(new FilterChangeListener());
        m_filterComponent =
            new TikaDialogComponentStringFilter(m_filterModel, "EXT", TikaParserConfig.DEFAULT_TYPE_LIST);
        addDialogComponent(m_filterComponent);

        closeCurrentGroup();

        createNewGroup("Output settings");
        addDialogComponent(new DialogComponentStringListSelection(TikaParserConfig.getColumnModel(), "Metadata",
            new ArrayList<String>(Arrays.asList(TikaParserConfig.DEFAULT_COLUMNS_LIST)), true, 5));
        setHorizontalPlacement(true);

        m_errorColModel = TikaParserConfig.getErrorColumnModel();
        m_errorColNameModel = TikaParserConfig.getErrorColumnNameModel(m_errorColModel);

        DialogComponentBoolean errorColBooleanModel =
            new DialogComponentBoolean(m_errorColModel, "Create error column");
        errorColBooleanModel.setToolTipText(
            "Create an additional String column to show any error messages if they appear while parsing the files.");
        addDialogComponent(errorColBooleanModel);
        addDialogComponent(new DialogComponentString(m_errorColNameModel, "New error output column"));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Extract embedded files to a directory");
        m_extractBooleanModel = TikaParserConfig.getExtractAttachmentModel();
        m_extractPathModel = TikaParserConfig.getExtractPathModel(m_extractBooleanModel);
        m_extractInlineImagesModel = TikaParserConfig.getExtractInlineImagesModel();

        addDialogComponent(new DialogComponentBoolean(m_extractBooleanModel, "Extract attachments and embedded files"));
        m_extractBooleanModel.addChangeListener(new CheckboxChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_extractInlineImagesModel, "Extract inline images from PDFs"));
        addDialogComponent(new DialogComponentFileChooser(m_extractPathModel, TikaParserNodeDialog.class.toString(),
            JFileChooser.OPEN_DIALOG, true));
        closeCurrentGroup();

        createNewGroup("Encrypted files settings");
        setHorizontalPlacement(true);
        m_authBooleanModel = TikaParserConfig.getAuthBooleanModel();
        m_authModel = TikaParserConfig.getCredentials(m_authBooleanModel);
        addDialogComponent(new DialogComponentBoolean(m_authBooleanModel, "Parse encrypted files"));
        addDialogComponent(new DialogComponentPasswordField(m_authModel, "Enter password"));
        setHorizontalPlacement(false);

        closeCurrentGroup();
    }

    class FilterChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedButton = m_typeModel.getStringValue();
            if (selectedButton.equals(TikaParserConfig.EXT_TYPE) && m_filterModel.getExcludeList().contains(PDF_EXT)) {
                m_extractInlineImagesModel.setEnabled(false);
            } else if (selectedButton.equals(TikaParserConfig.MIME_TYPE)
                && m_filterModel.getExcludeList().contains(PDF_MIME)) {
                m_extractInlineImagesModel.setEnabled(false);
            } else {
                m_extractInlineImagesModel.setEnabled(true);
            }
        }
    }

    class CheckboxChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedButton = m_typeModel.getStringValue();
            if (m_extractBooleanModel.getBooleanValue() && (selectedButton.equals(TikaParserConfig.EXT_TYPE)
                && !m_filterComponent.getExtExcList().contains(PDF_EXT))) {
                m_extractInlineImagesModel.setEnabled(true);
            } else if (m_extractBooleanModel.getBooleanValue() && (selectedButton.equals(TikaParserConfig.MIME_TYPE)
                && !m_filterComponent.getMimeExcList().contains(PDF_MIME))) {
                m_extractInlineImagesModel.setEnabled(true);
            } else {
                m_extractInlineImagesModel.setEnabled(false);
            }

        }

    }

    class ButtonChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            String selectedButton = m_typeModel.getStringValue();
            boolean extractCheckbox = m_extractBooleanModel.getBooleanValue();
            if (selectedButton.equals(TikaParserConfig.EXT_TYPE)) {
                m_filterComponent.setAllTypes(TikaParserConfig.EXTENSION_LIST);
                m_filterComponent.setType("EXT");
                m_filterComponent.updateLists();
                if (extractCheckbox && !m_filterComponent.getExtExcList().contains(PDF_EXT)) {
                    m_extractInlineImagesModel.setEnabled(true);
                } else {
                    m_extractInlineImagesModel.setEnabled(false);
                }
            } else if (selectedButton.equals(TikaParserConfig.MIME_TYPE)) {
                m_filterComponent.setAllTypes(TikaParserConfig.MIMETYPE_LIST);
                m_filterComponent.setType("MIME");
                m_filterComponent.updateLists();
                if (extractCheckbox && !m_filterComponent.getMimeExcList().contains(PDF_MIME)) {
                    m_extractInlineImagesModel.setEnabled(true);
                } else {
                    m_extractInlineImagesModel.setEnabled(false);
                }
            }

        }
    }

    private final class TypeButtonGroup implements ButtonGroupEnumInterface {

        private String m_text;

        private String m_tooltip;

        private boolean m_default;

        private String m_command;

        private TypeButtonGroup(final String text, final boolean isDefault, final String toolTip,
            final String command) {
            m_text = text;
            m_tooltip = toolTip;
            m_default = isDefault;
            m_command = command;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getActionCommand() {
            return m_command;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return m_text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getToolTip() {
            return m_tooltip;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefault() {
            return m_default;
        }
    }

    /**
     * Create a group for the input column/directory. Should be implemented by subclasses.
     */
    protected abstract void createInputGroup();
}

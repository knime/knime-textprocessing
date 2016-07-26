/*
 * ------------------------------------------------------------------------
 *
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
 *   08.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaParserNodeDialog extends DefaultNodeSettingsPane {

    static SettingsModelString getPathModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_PATH, TikaParserNodeModel.DEFAULT_PATH);
    }

    static SettingsModelBoolean getRecursiveModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_RECURSIVE, TikaParserNodeModel.DEFAULT_RECURSIVE);
    }

    static SettingsModelBoolean getIgnoreHiddenFilesModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_IGNORE_HIDDENFILES,
            TikaParserNodeModel.DEFAULT_IGNORE_HIDDENFILES);
    }

    static SettingsModelStringArray getTypeListModel() {
        return new SettingsModelStringArray(TikaParserConfigKeys.CFGKEY_TYPE_LIST,
            TikaParserNodeModel.DEFAULT_TYPE_LIST);
    }

    static SettingsModelStringArray getColumnModel() {
        return new SettingsModelStringArray(TikaParserConfigKeys.CFGKEY_COLUMNS_LIST,
            TikaParserNodeModel.DEFAULT_COLUMNS_LIST);
    }

    static SettingsModelString getTypeModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_TYPE, TikaParserNodeModel.DEFAULT_TYPE);
    }

    static SettingsModelBoolean getExtractBooleanModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_EXTRACT_BOOLEAN,
            TikaParserNodeModel.DEFAULT_EXTRACT);
    }

    static SettingsModelString getExtractPathModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_EXTRACT_PATH,
            TikaParserNodeModel.DEFAULT_EXTRACT_PATH);
    }

    private SettingsModelString m_typeModel;

    private DialogComponentStringListSelection m_typeListModel;

    private SettingsModelBoolean m_extractBooleanModel;

    private SettingsModelString m_extractPathModel;

    /**
     * Creates a new instance of {@code TikaParserNodeDialog} which displays a file chooser component, to specify the
     * directory containing the files to parse, two checkbox components to specify if the directory is searched
     * recursively for files to parse and whether to ignore hidden files, button and list components to specify which
     * file types are to be parsed (through file extension or MIME-Type), and the last list component is to specify
     * which meta data information are to be extracted. Another boolean button is added to specify whether embedded
     * files should be extracted as well to a specific directory using a file chooser component.
     */
    public TikaParserNodeDialog() {
        createNewGroup("Directory settings");
        addDialogComponent(new DialogComponentFileChooser(getPathModel(), TikaParserNodeDialog.class.toString(),
            JFileChooser.OPEN_DIALOG, true));

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(getRecursiveModel(), "Search recursively"));

        addDialogComponent(new DialogComponentBoolean(getIgnoreHiddenFilesModel(), "Ignore hidden files"));
        setHorizontalPlacement(false);
        closeCurrentGroup();

        m_typeModel = getTypeModel();

        ButtonGroupEnumInterface[] options = new ButtonGroupEnumInterface[2];
        options[0] = new TypeButtonGroup("File Extension", true, "Choose which file to parse through its extension",
            TikaParserNodeModel.EXT_TYPE);
        options[1] = new TypeButtonGroup("MIME-Type", false, "Choose which file to parse through its MIME-Type",
            TikaParserNodeModel.MIME_TYPE);

        addDialogComponent(new DialogComponentButtonGroup(m_typeModel, "Choose which type to parse", false, options));

        m_typeModel.addChangeListener(new ButtonChangeListener());

        m_typeListModel = new DialogComponentStringListSelection(getTypeListModel(), "Type",
            new ArrayList<String>(Arrays.asList(TikaParserNodeModel.DEFAULT_TYPE_LIST)), true, 10);
        addDialogComponent(m_typeListModel);

        addDialogComponent(new DialogComponentStringListSelection(getColumnModel(), "Metadata",
            new ArrayList<String>(Arrays.asList(TikaParserNodeModel.DEFAULT_COLUMNS_LIST)), true, 5));

        createNewGroup("Extract embedded files to a directory");
        m_extractBooleanModel = getExtractBooleanModel();
        m_extractPathModel = getExtractPathModel();
        checkState();
        m_extractBooleanModel.addChangeListener(new InternalChangeListener());

        addDialogComponent(new DialogComponentBoolean(m_extractBooleanModel, "Extract attachments and embedded files"));
        addDialogComponent(new DialogComponentFileChooser(m_extractPathModel, TikaParserNodeDialog.class.toString(),
            JFileChooser.OPEN_DIALOG, true));
        closeCurrentGroup();
    }

    private void checkState() {
        if (m_extractBooleanModel.getBooleanValue()) {
            m_extractPathModel.setEnabled(true);
        } else {
            m_extractPathModel.setEnabled(false);
        }
    }

    /**
     * Listens to state change and enables / disables the model of the extracted path model
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            checkState();
        }
    }

    class ButtonChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            List<String> newList;
            String selectedButton = m_typeModel.getStringValue();
            if (selectedButton.equals(TikaParserNodeModel.EXT_TYPE)) {
                newList = new ArrayList<String>(Arrays.asList(TikaParserNodeModel.EXTENSION_LIST));
                m_typeListModel.replaceListItems(newList, TikaParserNodeModel.EXTENSION_LIST);
            } else if (selectedButton.equals(TikaParserNodeModel.MIME_TYPE)) {
                newList = new ArrayList<String>(Arrays.asList(TikaParserNodeModel.MIMETYPE_LIST));
                m_typeListModel.replaceListItems(newList, TikaParserNodeModel.MIMETYPE_LIST);
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

}

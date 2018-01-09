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
 *   28.10.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public class PreprocessingNodeSettingsPane2 extends DefaultNodeSettingsPane {

    /**
     * @return Creates and returns the boolean settings model which contains the flag if the olde documents have to be
     *         replaced by the preprocessed documents or if they will be appended.
     */
    public static SettingsModelBoolean getReplaceDocumentModel() {
        return new SettingsModelBoolean(PreprocessingConfigKeys2.CFG_KEY_REPLACE_DOC,
            StreamablePreprocessingNodeModel.DEF_REPLACE);
    }

    /**
     * @return Creates and returns the string settings model containing the name of the column with the documents to
     *         preprocess.
     */
    public static SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(PreprocessingConfigKeys2.CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * @return Creates and returns the string settings model containing the name of the column with the new,
     *         preprocessed documents.
     */
    public static SettingsModelString getNewDocumentColumnModel() {
        return new SettingsModelString(PreprocessingConfigKeys2.CFG_KEY_NEW_DOCUMENT_COL,
            StreamablePreprocessingNodeModel.DEF_NEW_DOCUMENT_COL);
    }

    /**
     * @return Creates and returns boolean settings model which stores the flag specifying whether unmodifiable terms
     *         will be preprocessed was well or not.
     */
    public static SettingsModelBoolean getPreprocessUnmodifiableModel() {
        return new SettingsModelBoolean(PreprocessingConfigKeys2.CFG_KEY_PREPRO_UNMODIFIABLE,
            StreamablePreprocessingNodeModel.DEF_PREPRO_UNMODIFIABLE);
    }

    private final SettingsModelBoolean m_replaceDocModel = getReplaceDocumentModel();

    private final SettingsModelString m_newDocumentColModel =  getNewDocumentColumnModel();

    /**
     * Creates new instance of {@code PreprocessingNodeSettingsPane}.
     */
    @SuppressWarnings("unchecked")
    public PreprocessingNodeSettingsPane2() {
        removeTab("Options");
        createNewTabAt("Preprocessing", 1);

        SettingsModelString documentColModel = getDocumentColumnModel();
        DialogComponentColumnNameSelection docComp =
            new DialogComponentColumnNameSelection(documentColModel, "Document column", 0, DocumentValue.class);
        docComp.setToolTipText("The documents to preprocess.");
        addDialogComponent(docComp);

        setHorizontalPlacement(true);

        DialogComponentBoolean replaceComp = new DialogComponentBoolean(m_replaceDocModel, "Replace column");
        replaceComp.setToolTipText("Replace selected document column.");
        m_replaceDocModel.addChangeListener(new ColumnHandlingListener());
        addDialogComponent(replaceComp);

        DialogComponentString newDocColNameComp =
            new DialogComponentString(m_newDocumentColModel, "Append column:", true, 20);
        newDocColNameComp.setToolTipText("Name of the new new document column");
        addDialogComponent(newDocColNameComp);

        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentBoolean(getPreprocessUnmodifiableModel(), "Ignore unmodifiable flag"));
    }

    private final class ColumnHandlingListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            if (m_replaceDocModel.getBooleanValue()) {
                m_newDocumentColModel.setEnabled(false);
            } else {
                m_newDocumentColModel.setEnabled(true);
            }
        }
    }
}

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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * Provides the dialog of the document vector node.
 *
 * @author Kilian Thiel & Julian Bunzel, KNIME.com GmbH, Berlin, Germany
 * @since 3.5
 */
class DocumentVectorNodeDialog2 extends DefaultNodeSettingsPane {

    /**
     * Creates and returns an instance of {@link SettingsModelBoolean} specifying if a bitvector has to be created or
     * not.
     *
     * @return A {@code SettingsModelBoolean} specifying if a bitvector has to be created or not.
     */
    static final SettingsModelBoolean getBooleanModel() {
        return new SettingsModelBoolean(DocumentVectorConfigKeys2.CFGKEY_BOOLEAN,
            DocumentVectorNodeModel2.DEFAULT_BOOLEAN);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelBoolean} specifying if a collection cell has to be created
     * or not.
     *
     * @return A {@code SettingsModelBoolean} specifying if a collection cell has to be created or not.
     */
    static final SettingsModelBoolean getAsCollectionModel() {
        return new SettingsModelBoolean(DocumentVectorConfigKeys2.CFGKEY_ASCOLLECTION,
            DocumentVectorNodeModel2.DEFAULT_ASCOLLECTION);
    }

    /**
     * Creates and returns an instance of {@link SettingsModelString} containing the name of the column with the vector
     * values to use.
     *
     * @return A {@code SettingsModelString} containing the name of the column with the vector values to use.
     */
    static final SettingsModelString getColumnModel() {
        return new SettingsModelString(DocumentVectorConfigKeys2.CFGKEY_VALUE_COL,
            DocumentVectorNodeModel2.DEFAULT_COL);
    }

    /**
     * Creates and returns a new instance of {@link SettingsModelString} containing the name of the document column.
     *
     * @return A {@code SettingsModelString} containing the name of the document column.
     */
    static final SettingsModelString getDocumentColModel() {
        return new SettingsModelString(DocumentVectorConfigKeys2.CFGKEY_DOC_COL, "");
    }

    /**
     * Creates and returns a new instance of {@link SettingsModelBoolean} specifying whether tags will be ignored or
     * not.
     *
     * @return A {@code SettingsModelBoolean} specifying whether tags will be ignored or not.
     */
    static final SettingsModelBoolean getIgnoreTagsModel() {
        return new SettingsModelBoolean(DocumentVectorConfigKeys2.CFGKEY_IGNORE_TAGS,
            DocumentVectorNodeModel2.DEFAULT_IGNORE_TAGS);
    }

    private SettingsModelString m_columnModel;

    private SettingsModelBoolean m_booleanModel;

    /**
     * Creates a new instance of {@code DocumentVectorNodeDialog2}.
     */
    @SuppressWarnings("unchecked")
    DocumentVectorNodeDialog2() {
        addDialogComponent(
            new DialogComponentColumnNameSelection(getDocumentColModel(), "Document column", 0, DocumentValue.class));

        addDialogComponent(new DialogComponentBoolean(getIgnoreTagsModel(), "Ignore tags"));

        m_columnModel = getColumnModel();
        m_booleanModel = getBooleanModel();
        m_booleanModel.addChangeListener(e -> checkUncheck());

        addDialogComponent(new DialogComponentBoolean(m_booleanModel, "Bitvector"));

        addDialogComponent(new DialogComponentColumnNameSelection(m_columnModel, "Vector value", 0, DoubleValue.class));

        addDialogComponent(new DialogComponentBoolean(getAsCollectionModel(), "As collection cell"));

        checkUncheck();
    }

    private void checkUncheck() {
        if (m_booleanModel.getBooleanValue()) {
            m_columnModel.setEnabled(false);
        } else {
            m_columnModel.setEnabled(true);
        }
    }
}

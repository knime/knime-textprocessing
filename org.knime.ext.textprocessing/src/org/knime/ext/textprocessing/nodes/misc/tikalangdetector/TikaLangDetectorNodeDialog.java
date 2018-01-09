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
 *   05.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikalangdetector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaLangDetectorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * @return The settings model containing the default column.
     */
    public static SettingsModelString getColModel() {
        return new SettingsModelString(TikaLangDetectorConfigKeys.CFGKEY_COL,
            TikaLangDetectorNodeModel.DEFAULT_COLNAME);
    }

    /**
     * @return The settings model containing the name of the language column.
     */
    public static SettingsModelString getLangColNameModel() {
        return new SettingsModelString(TikaLangDetectorConfigKeys.CFGKEY_LANG_COL_NAME,
            TikaLangDetectorNodeModel.DEFAULT_LANG_COLNAME);
    }

    /**
     * @return The settings model specifying if the confidence value has to be calculated or not.
     */
    public static final SettingsModelBoolean getConfidenceBooleanModel() {
        return new SettingsModelBoolean(TikaLangDetectorConfigKeys.CFGKEY_CONFIDENCE_VALUE,
            TikaLangDetectorNodeModel.DEFAULT_CONFIDENCE);
    }

    /**
     * @return The settings model containing the name of the confidence value column.
     */
    public static SettingsModelString getConfidenceColNameModel() {
        return new SettingsModelString(TikaLangDetectorConfigKeys.CFGKEY_CONFIDENCE_COL_NAME,
            TikaLangDetectorNodeModel.DEFAULT_CONFIDENCE_COLNAME);
    }

    /**
     * @return The settings model specifying if all detected languages have to be shown or not.
     */
    public static final SettingsModelBoolean getAllLangsBooleanModel() {
        return new SettingsModelBoolean(TikaLangDetectorConfigKeys.CFGKEY_ALL_LANGS,
            TikaLangDetectorNodeModel.DEFAULT_ALL_LANGS);
    }

    private SettingsModelBoolean m_confidenceBooleanModel;

    private SettingsModelString m_confidenceColModel;

    /**
     * Creates a new instance of {@code TikaLangDetectorNodeDialog} which displays a column chooser component, to
     * specify the column containing the text to parse. User can also choose to name the appended language column. There
     * is also a tickbox component to specify whether to show the confidence value, and a string component to name the
     * confidence value column if ticked. Another tickbox component specifies whether all detected languages should be
     * shown in output table.
     */
    @SuppressWarnings("unchecked")
    public TikaLangDetectorNodeDialog() {
        addDialogComponent(new DialogComponentColumnNameSelection(getColModel(), "String or Document column", 0,
            DocumentValue.class, StringValue.class));

        addDialogComponent(new DialogComponentString(getLangColNameModel(), "New language column"));

        m_confidenceColModel = getConfidenceColNameModel();
        m_confidenceBooleanModel = getConfidenceBooleanModel();
        checkState();
        m_confidenceBooleanModel.addChangeListener(new InternalChangeListener());

        addDialogComponent(new DialogComponentBoolean(m_confidenceBooleanModel, "Show Confidence value"));

        addDialogComponent(new DialogComponentString(m_confidenceColModel, "New confidence value column"));

        addDialogComponent(new DialogComponentBoolean(getAllLangsBooleanModel(), "Show all detected languages"));

    }

    private void checkState() {
        if (m_confidenceBooleanModel.getBooleanValue()) {
            m_confidenceColModel.setEnabled(true);
        } else {
            m_confidenceColModel.setEnabled(false);
        }
    }

    /**
     * Listens to state change and enables / disables the model of the confidence value column
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
}

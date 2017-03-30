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
 *   24.05.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.misc.markuptagfilter;

import java.util.Collection;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentLabel;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class MarkupTagFilterNodeDialog extends DefaultNodeSettingsPane {

    private static final String WARNING_MESSAGE =
        "<html><font color='red'>Attention: Documents will be retokenized after filtering. During this process,"
        + " all tag information will get lost.</font></html>";

    /**
     * Creates and returns the settings model, storing the selected columns.
     *
     * @return The settings model with the selected columns.
     */
    @SuppressWarnings("unchecked")
    static final SettingsModelColumnFilter2 getFilterColModel() {
        return new SettingsModelColumnFilter2(MarkupTagFilterConfigKeys.COLUMN_NAMES,
            new Class[]{StringValue.class, DocumentValue.class});
    }

    /**
     * Creates and returns the settings model, storing the "append column" flag.
     *
     * @return The settings model with the "append column" flag.
     */
    static final SettingsModelBoolean getAppendColumnModel() {
        return new SettingsModelBoolean(MarkupTagFilterConfigKeys.APPEND_COLUMNS,
            MarkupTagFilterNodeModel.DEF_APPEND_COLUMNS);
    }

    /**
     * Creates and returns the settings model, storing the column suffix.
     *
     * @return The settings model with the column suffix.
     */
    static final SettingsModelString getColumnSuffixModel() {
        return new SettingsModelString(MarkupTagFilterConfigKeys.COLUMN_SUFFIX,
            MarkupTagFilterNodeModel.DEF_COLUMN_SUFFIX);
    }

    /**
     * Creates and returns the settings model, storing the name of the tokenizer.
     *
     * @return The settings model with the name of the word tokenizer.
     */
    static SettingsModelString getTokenizerNameModel() {
        return new SettingsModelString(MarkupTagFilterConfigKeys.TOKENIZER_NAME,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    private SettingsModelString m_suffixModel;

    private SettingsModelColumnFilter2 m_filterModel;

    private SettingsModelBoolean m_appendColumnModel;

    private SettingsModelString m_tokenizerNameModel;

    private DataTableSpec m_inSpecs;

    private DialogComponentLabel m_warningLabel;

    /**
     * Creates new instance of {@code MarkupTagFilterNodeDialog}
     */
    public MarkupTagFilterNodeDialog() {
        // COLUMN SELECTION
        createNewGroup("Column Selection");
        m_filterModel = getFilterColModel();
        m_filterModel.addChangeListener(new FilteredColumnsChangeListener());
        addDialogComponent(new DialogComponentColumnFilter2(m_filterModel, 0));
        closeCurrentGroup();

        // COLUMN SETTINGS
        createNewGroup("Column settings");
        setHorizontalPlacement(true);
        m_appendColumnModel = getAppendColumnModel();
        m_suffixModel = getColumnSuffixModel();

        m_appendColumnModel.addChangeListener(new AppendColumnChangeListener());
        addDialogComponent(new DialogComponentBoolean(m_appendColumnModel, "Append as new columns (specify suffix)"));
        addDialogComponent(new DialogComponentString(m_suffixModel, "", true, 8));

        setHorizontalPlacement(false);
        closeCurrentGroup();

        createNewGroup("Tokenizer settings");
        Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().keySet();
        m_tokenizerNameModel = getTokenizerNameModel();
        addDialogComponent(new DialogComponentStringSelection(m_tokenizerNameModel, "Word tokenizer", tokenizerList));
        m_warningLabel = new DialogComponentLabel(WARNING_MESSAGE);
        addDialogComponent(m_warningLabel);
        closeCurrentGroup();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        m_inSpecs = specs[0];
        checkState(m_filterModel);
    }

    /**
     * Checks the type of the included columns and enables/disables the tokenizer selection & the related warning
     * message depending on availability of document columns.
     *
     * @param filterModel The SettingsModelColumnFilter2 to check the inSpecs with
     */
    private void checkState(final SettingsModelColumnFilter2 filterModel) {
        if (m_inSpecs != null) {
            m_tokenizerNameModel.setEnabled(false);
            m_warningLabel.setText("");
            FilterResult result = filterModel.applyTo(m_inSpecs);
            for (String columnName : result.getIncludes()) {
                if (m_inSpecs.getColumnSpec(columnName).getType().equals(DocumentCell.TYPE)) {
                    m_tokenizerNameModel.setEnabled(true);
                    m_warningLabel.setText(WARNING_MESSAGE);
                }
            }
        }
    }

    private class AppendColumnChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_suffixModel.setEnabled(m_appendColumnModel.getBooleanValue());
        }
    }

    private class FilteredColumnsChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            SettingsModelColumnFilter2 filterModel = (SettingsModelColumnFilter2)e.getSource();
            checkState(filterModel);
        }
    }

}

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
 *   Jan 25, 2019 (Julian Bunzel, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ext.textprocessing.nodes.transformation.dictionaryextractor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialog;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The {@link NodeDialog} for the Dictionary Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class DictionaryExtractorNodeDialog extends NodeDialogPane {

    /** Dialog containing the document column to process. */
    private final DialogComponentColumnNameSelection m_colNameSelectionDialog;

    /** Dialog indicating whether the node should only keep the top k most frequent terms. */
    private final DialogComponentBoolean m_enableFilteringDialog;

    /** Dialog holding the k value used to filter the most frequent terms. */
    private final DialogComponentNumber m_topKTermsDialog;

    /** Dialog storing three different frequency options used to filter the top k most terms.*/
    private final DialogComponentButtonGroup m_filterByDialog;

    /** Dialog holding the number of threads. */
    private final DialogComponentNumber m_noOfThreadsDialog;

    /** Array holding all dialog components. */
    private final DialogComponent[] m_diagComps;

    /**
     * Constructor for class {@link DictionaryExtractorNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    DictionaryExtractorNodeDialog() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;

        // document column selection
        m_colNameSelectionDialog = new DialogComponentColumnNameSelection(
            DictionaryExtractorNodeModel.getDocumentColumnModel(), "Document column", 0, DocumentValue.class);
        m_colNameSelectionDialog.setToolTipText("Column containing the documents to create dictionary from");
        panel.add(m_colNameSelectionDialog.getComponentPanel(), gbc);
        ++gbc.gridy;

        // enable top k filtering and top k b
        final JPanel topKPanel = new JPanel(new GridBagLayout());
        m_enableFilteringDialog =
            new DialogComponentBoolean(DictionaryExtractorNodeModel.getFilterTermsModel(), "Most frequent terms (k)");
        m_enableFilteringDialog.getModel().addChangeListener(e -> updateModel());
        m_topKTermsDialog = new DialogComponentNumber(DictionaryExtractorNodeModel.getTopKTermsModel(), "", 1, 6);
        m_topKTermsDialog
            .setToolTipText("Keep only the top k frequent terms based on the set value and filter method.");

        topKPanel.add(m_enableFilteringDialog.getComponentPanel());
        topKPanel.add(m_topKTermsDialog.getComponentPanel());
        panel.add(topKPanel, gbc);
        ++gbc.gridy;

        // filter method
        m_filterByDialog = new DialogComponentButtonGroup(DictionaryExtractorNodeModel.getFilterByModel(), false,
            "Filter terms by", MultiThreadDictionaryExtractor.TF, MultiThreadDictionaryExtractor.DF,
            MultiThreadDictionaryExtractor.IDF);
        m_filterByDialog.setToolTipText("Keep the k top terms regarding the selected frequency method.");
        panel.add(m_filterByDialog.getComponentPanel(), gbc);
        ++gbc.gridy;

        // Number of threads component to select output columns
        m_noOfThreadsDialog = new DialogComponentNumber(DictionaryExtractorNodeModel.getNumberOfThreadsModel(),
            "Number of threads", 1, 5);
        panel.add(m_noOfThreadsDialog.getComponentPanel(), gbc);

        m_diagComps = new DialogComponent[]{m_colNameSelectionDialog, m_enableFilteringDialog, m_topKTermsDialog,
            m_filterByDialog, m_noOfThreadsDialog};

        addTab("Options", panel);
        updateModel();
    }

    /**
     * Enables/disables the filtering model option in case that the value of the 'keep top k most frequent terms' option
     * changes.
     */
    private void updateModel() {
        m_filterByDialog.getModel()
            .setEnabled(((SettingsModelBoolean)m_enableFilteringDialog.getModel()).getBooleanValue());
        m_topKTermsDialog.getModel()
            .setEnabled(((SettingsModelBoolean)m_enableFilteringDialog.getModel()).getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        for (final DialogComponent dia : m_diagComps) {
            dia.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        for (final DialogComponent dia : m_diagComps) {
            dia.loadSettingsFrom(settings, specs);
        }
        updateModel();
    }

}

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

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeDialog;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * The {@link NodeDialog} for the Dictionary Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class DictionaryExtractorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * A {@link SettingsModelIntegerBounded} to store the threshold to keep the top X frequent terms.
     */
    private final SettingsModelIntegerBounded m_keepXTopFreqTermsModel =
        DictionaryExtractorNodeModel.getTopXTermsModel();

    /**
     * A {@link SettingsModelIntegerBounded} to store the number of threads used to process the documents.
     */
    private final SettingsModelIntegerBounded m_numberOfThreadsModel =
        DictionaryExtractorNodeModel.getNumberOfThreadsModel();

    /**
     * A {@link SettingsModelString} to store the frequency method used to filter the terms.
     */
    private final SettingsModelString m_filterByModel = DictionaryExtractorNodeModel.getFilterByModel();

    /**
     * Constructor for class {@link DictionaryExtractorNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    DictionaryExtractorNodeDialog() {
        // document col to create bow from
        final DialogComponentColumnNameSelection docColSelectionComp = new DialogComponentColumnNameSelection(
            DictionaryExtractorNodeModel.getDocumentColumnModel(), "Document column", 0, DocumentValue.class);
        docColSelectionComp.setToolTipText("Column containing the documents to create dictionary from");
        addDialogComponent(docColSelectionComp);
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);

        // keep top x most frequent terms
        m_keepXTopFreqTermsModel.addChangeListener(e -> updateModel());
        final DialogComponentNumber keepXTerms =
            new DialogComponentNumber(m_keepXTopFreqTermsModel, "Keep top X most frequent terms", 1);
        keepXTerms.setToolTipText("Keep only the top frequent terms based on the set value and filter method.");
        addDialogComponent(keepXTerms);
        setHorizontalPlacement(false);

        // filter method
        final DialogComponentButtonGroup filterBy = new DialogComponentButtonGroup(m_filterByModel, false,
            "Filter terms by", MultiThreadDictionaryExtractor.TF, MultiThreadDictionaryExtractor.DF,
            MultiThreadDictionaryExtractor.IDF);
        filterBy.setToolTipText("Keep the X top terms regarding the selected frequency method.");
        addDialogComponent(filterBy);

        // Number of threads component to select output columns
        addDialogComponent(new DialogComponentNumber(m_numberOfThreadsModel, "Number of threads", 1));
        updateModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        updateModel();
    }

    /**
     * Enables/disables the filtering model option in case that the value of the 'keep top X most frequent terms' option
     * changes.
     */
    private void updateModel() {
        m_filterByModel.setEnabled(m_keepXTopFreqTermsModel.getIntValue() > 0);
    }

}

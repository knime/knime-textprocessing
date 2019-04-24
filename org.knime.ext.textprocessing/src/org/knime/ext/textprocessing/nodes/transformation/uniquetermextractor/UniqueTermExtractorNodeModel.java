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
package org.knime.ext.textprocessing.nodes.transformation.uniquetermextractor;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The {@link NodeModel} for the Unique Term Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class UniqueTermExtractorNodeModel extends NodeModel {

    /**
     * Configuration key for the document column to select.
     */
    private static final String CFG_KEY_DOCUMENT_COL = "document_column";

    /**
     * Configuration key for the number of threads.
     */
    private static final String CFG_KEY_NUMBER_OF_THREADS = "number_of_threads";

    /**
     * Configuration key for the top k terms option.
     */
    private static final String CFG_KEY_TOP_K_TERMS = "top_k_terms";

    /**
     * Configuration key for the filter by option.
     */
    private static final String CFG_KEY_FILTER_BY = "filter_by";

    /**
     * Configuration key for the option to append frequency columns.
     */
    private static final String CFG_KEY_APPEND_FREQ_COL = "append_freq_cols";

    /**
     * Configuration key for the option to append indices column.
     */
    private static final String CFG_KEY_APPEND_IDX_COL = "append_idx_col";

    /**
     * Configuration key for the filter option.
     */
    private static final String CFG_KEY_ENABLE_FILTERING = "enable_filtering";

    /**
     * Default value for the top k most frequent terms.
     */
    private static final int DEF_TOP_K_TERMS = 20000;

    /**
     * Default value for enabling the term filter.
     */
    private static final boolean DEF_ENABLE_FILTERING = false;

    /**
     * Default value for appending index column.
     */
    private static final boolean DEF_APPEND_IDX_COLUMN = false;

    /**
     * Default value for appending frequency columns.
     */
    private static final boolean DEF_APPEND_FREQ_COLUMNS = false;

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the column with the documents to create
     * the set of terms from.
     *
     * @return {@code SettingsModelString} containing the name of the document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * Creates and returns a {@link SettingsModelIntegerBounded} containing the number of threads.
     *
     * @return {@code SettingsModelIntegerBounded} containing the number of threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(CFG_KEY_NUMBER_OF_THREADS, 1, 1, Integer.MAX_VALUE);
    }

    /**
     * Creates and returns a {@link SettingsModelBoolean} containing the boolean value to enable/disable the 'top k
     * terms' option.
     *
     * @return {@code SettingsModelBoolean} containing the boolean value to enable/disable the 'top k terms' option.
     */
    static final SettingsModelBoolean getFilterTermsModel() {
        return new SettingsModelBoolean(CFG_KEY_ENABLE_FILTERING, DEF_ENABLE_FILTERING);
    }

    /**
     * Creates and returns a {@link SettingsModelIntegerBounded} containing the value for the 'top k terms' option.
     *
     * @return {@code SettingsModelIntegerBounded} containing the value for the 'top k terms' option.
     */
    static final SettingsModelIntegerBounded getTopKTermsModel() {
        return new SettingsModelIntegerBounded(CFG_KEY_TOP_K_TERMS, DEF_TOP_K_TERMS, 1, Integer.MAX_VALUE);
    }

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the frequency method used to filter the
     * terms to get the top k most frequent terms only.
     *
     * @return Creates and returns a {@link SettingsModelString} containing the name of the frequency method.
     */
    static final SettingsModelString getFilterByModel() {
        return new SettingsModelString(CFG_KEY_FILTER_BY, MultiThreadTermExtractor.TF);
    }

    /**
     * Creates and returns a {@link SettingsModelBoolean} containing the boolean value that indicates whether frequency
     * columns should be appended or not.
     *
     * @return Creates and returns a {@link SettingsModelBoolean} containing the boolean value that indicates whether
     *         frequency columns should be appended or not.
     */
    static final SettingsModelBoolean getAppendFreqColModel() {
        return new SettingsModelBoolean(CFG_KEY_APPEND_FREQ_COL, DEF_APPEND_FREQ_COLUMNS);
    }

    /**
     * Creates and returns a {@link SettingsModelBoolean} containing the boolean value that indicates whether an index
     * column should be appended or not.
     *
     * @return Creates and returns a {@link SettingsModelBoolean} containing the boolean value that indicates whether an
     *         index column should be appended or not.
     */
    static final SettingsModelBoolean getAppendIdxColModel() {
        return new SettingsModelBoolean(CFG_KEY_APPEND_IDX_COL, DEF_APPEND_IDX_COLUMN);
    }

    /**
     * The {@link SettingsModelString} containing the name of the document column.
     */
    private final SettingsModelString m_docColModel = getDocumentColumnModel();

    /**
     * The {@link SettingsModelIntegerBounded} containing the number of threads.
     */
    private final SettingsModelIntegerBounded m_numberOfThreadsModel = getNumberOfThreadsModel();

    /**
     * The {@link SettingsModelIntegerBounded} containing the value for the top k terms option.
     */
    private final SettingsModelIntegerBounded m_topKTermsModel = getTopKTermsModel();

    /**
     * The {@link SettingsModelBoolean} containing the boolean value to enable/disable the 'top k terms' option.
     */
    private final SettingsModelBoolean m_enableFilteringModel = getFilterTermsModel();

    /**
     * The {@link SettingsModelString} containing the name of the frequency method used to filter the terms to get the
     * top k most frequent terms only.
     */
    private final SettingsModelString m_filterByModel = getFilterByModel();

    /**
     * The {@link SettingsModelBoolean} containing the boolean value to enable/disable appending frequency columns.
     */
    private final SettingsModelBoolean m_appendFreqColsModel = getAppendFreqColModel();

    /**
     * The {@link SettingsModelBoolean} containing the boolean value to enable/disable appending a unique index column.
     */
    private final SettingsModelBoolean m_appendIdxColModel = getAppendIdxColModel();

    /**
     * Creates a new instance of {@code UniqueTermExtractorNodeModel}.
     */
    protected UniqueTermExtractorNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{MultiThreadTermExtractor
            .createDataTableSpec(m_appendFreqColsModel.getBooleanValue(), m_appendIdxColModel.getBooleanValue())};
    }

    /**
     * Checks if the {@link DataTableSpec} contains a {@link DocumentValue} column and looks up its column index.
     *
     * @param spec The {@code DataTableSpec} to check.
     * @throws InvalidSettingsException Throws an {@code InvalidSettingsException} if no document column could be found.
     */
    private void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // verify that the incoming DataTableSpec contains at least one Document column
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        // Auto-guessing the correct document column
        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        // Get table and specs
        final BufferedDataTable dataTable = inData[0];
        final DataTableSpec inputSpec = dataTable.getDataTableSpec();
        checkDataTableSpec(inputSpec);
        final int documentColIndex = inputSpec.findColumnIndex(m_docColModel.getStringValue());
        final long numberOfRows = dataTable.size();

        // Create extractor
        final int numberOfThreads = numberOfRows > m_numberOfThreadsModel.getIntValue()
            ? m_numberOfThreadsModel.getIntValue() : (int)numberOfRows;
        final MultiThreadTermExtractor extractor =
            new MultiThreadTermExtractor(documentColIndex, m_enableFilteringModel.getBooleanValue(),
                m_topKTermsModel.getIntValue(), numberOfRows, m_filterByModel.getStringValue(),
                m_appendIdxColModel.getBooleanValue(), m_appendFreqColsModel.getBooleanValue(), numberOfThreads, exec);

        // Only run if table is not empty
        if (numberOfRows > 0) {
            extractor.run(dataTable);
        }

        // Set node messages if needed
        exec.setMessage("Creating output table.");
        if (extractor.getMissingRowCount() > 0) {
            this.setWarningMessage("Skipped " + extractor.getMissingRowCount() + " of " + numberOfRows
                + " documents due to missing values.");
        }

        return new BufferedDataTable[]{extractor.createDataTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_numberOfThreadsModel.saveSettingsTo(settings);
        m_topKTermsModel.saveSettingsTo(settings);
        m_filterByModel.saveSettingsTo(settings);
        m_appendFreqColsModel.saveSettingsTo(settings);
        m_appendIdxColModel.saveSettingsTo(settings);
        m_enableFilteringModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_numberOfThreadsModel.validateSettings(settings);
        m_topKTermsModel.validateSettings(settings);
        m_filterByModel.validateSettings(settings);
        m_appendFreqColsModel.validateSettings(settings);
        m_appendIdxColModel.validateSettings(settings);
        m_enableFilteringModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_numberOfThreadsModel.loadSettingsFrom(settings);
        m_topKTermsModel.loadSettingsFrom(settings);
        m_enableFilteringModel.loadSettingsFrom(settings);
        m_appendFreqColsModel.loadSettingsFrom(settings);
        m_appendIdxColModel.loadSettingsFrom(settings);
        m_filterByModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

}

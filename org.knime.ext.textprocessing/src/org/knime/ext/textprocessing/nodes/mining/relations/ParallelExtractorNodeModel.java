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
 *   Feb 12, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * The abstract {@link NodeModel} for the StanfordNLP Extractor nodes.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public abstract class ParallelExtractorNodeModel extends NodeModel {

    /**
     * Configuration key for the document column selection.
     */
    private static final String CFG_KEY_DOCUMENT_COLUMN = "document_column";

    /**
     * Configuration key for the lemmatized document column selection.
     */
    private static final String CFG_KEY_LEMMATIZED_DOCUMENT_COLUMN = "lemma_document_column";

    /**
     * Configuration key for apply required preprocessing option.
     */
    private static final String CFG_KEY_APPLY_REQUIRED_PREPROCESSING = "apply_required_preprocessing";

    /**
     * Configuration key for the number of threads.
     */
    private static final String CFG_KEY_NUMBER_OF_THREADS = "number_of_threads";

    /**
     * Default number of threads.
     */
    private static final int DEF_NUMBER_OF_THREADS = 4;

    /**
     * Default value for the apply required preprocessing option.
     */
    private static final boolean DEF_APPLY_REQUIRED_PREPROCESSING = true;

    /**
     * Creates and returns a new {@link SettingsModelString} containing the selected lemmatized document column.
     *
     * @return {@code SettingsModelString} containing the selected lemmatized document column.
     */
    static final SettingsModelString getLemmatizedDocumentColumnModel() {
        return new SettingsModelString(CFG_KEY_LEMMATIZED_DOCUMENT_COLUMN, "");
    }

    /**
     * Creates and returns a new {@link SettingsModelString} containing the selected document column.
     *
     * @return {@code SettingsModelString} containing the selected document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(CFG_KEY_DOCUMENT_COLUMN, "");
    }

    /**
     * Creates and returns a new {@link SettingsModelBoolean} containing the value for the option to apply required
     * preprocessing.
     *
     * @return {@code SettingsModelBoolean} containing the value for the option to apply required preprocessing.
     */
    static final SettingsModelBoolean getApplyReqPreprocModel() {
        return new SettingsModelBoolean(CFG_KEY_APPLY_REQUIRED_PREPROCESSING, DEF_APPLY_REQUIRED_PREPROCESSING);
    }

    /**
     * Creates and returns a new {@link SettingsModelIntegerBounded} containing the selected number of threads.
     *
     * @return {@code SettingsModelIntegerBounded} containing the selected number of threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(CFG_KEY_NUMBER_OF_THREADS, DEF_NUMBER_OF_THREADS, 1, Integer.MAX_VALUE);
    }

    /**
     * The path to the part-of-speech tagging model.
     */
    protected static final String POS_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/pos/english-left3words-distsim.tagger").getAbsolutePath();

    /**
     * The path to the part-of-speech tagging model.
     */
    protected static final String NER_MODEL_PATH = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/nermodels/english.all.3class.distsim.crf.ser.gz").getAbsolutePath();

    /**
     * The {@link SettingsModelString} containing the selected document column.
     */
    private final SettingsModelString m_docColModel = getDocumentColumnModel();

    /**
     * The {@link SettingsModelString} containing the selected document column.
     */
    private final SettingsModelString m_lemmaDocColModel = getLemmatizedDocumentColumnModel();

    /**
     * The {@link SettingsModelBoolean} containing the value for to apply required preprocessing.
     */
    private final SettingsModelBoolean m_applyReqPreprocModel = getApplyReqPreprocModel();

    /**
     * The {@link SettingsModelIntegerBounded} containing the number of threads..
     */
    private final SettingsModelIntegerBounded m_noOfThreadsModel = getNumberOfThreadsModel();

    /**
     * Creates a new instance of {@code ParallelExtractorNodeModel}.
     */
    public ParallelExtractorNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec spec = inSpecs[0];
        checkDataTableSpec(spec);
        return new DataTableSpec[]{createDataTableSpec(spec)};
    }

    /**
     * Creates and returns the {@link DataTableSpec} of the table to be created.
     *
     * @param spec The {@code DataTableSpec} of the incoming table.
     * @return Returns the {@link DataTableSpec} of the table to be created.
     */
    protected abstract DataTableSpec createDataTableSpec(final DataTableSpec spec);

    /**
     * Checks the {@link DataTableSpec} for document columns.
     *
     * @param dataTableSpec The {@code DataTableSpec} to verify.
     * @throws InvalidSettingsException Thrown if no document columns can be found.
     */
    private void checkDataTableSpec(final DataTableSpec dataTableSpec) throws InvalidSettingsException {
        // verify that the incoming DataTableSpec contains at least one Document column
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(dataTableSpec);
        if (m_applyReqPreprocModel.getBooleanValue()) {
            verifier.verifyMinimumDocumentCells(1, true);
        } else {
            verifier.verifyMinimumDocumentCells(2, true);
        }

        ColumnSelectionVerifier.verifyColumn(m_docColModel, dataTableSpec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));
        if (m_lemmaDocColModel.isEnabled()) {
            ColumnSelectionVerifier.verifyColumn(m_lemmaDocColModel, dataTableSpec, DocumentValue.class, null)
                .ifPresent(msg -> setWarningMessage(msg));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        // get table specific data
        final BufferedDataTable inputData = inData[0];
        final DataTableSpec dataTableSpec = inputData.getDataTableSpec();
        final long totalNoOfRows = inputData.size();
        if (totalNoOfRows == 0) {
            return new BufferedDataTable[]{
                MultiThreadRelationExtractor.createEmptyTable(createDataTableSpec(dataTableSpec), exec)};
        }

        final int docColIdx = dataTableSpec.findColumnIndex(m_docColModel.getStringValue());
        final int lemmaDocColIdx =
            m_lemmaDocColModel.isEnabled() ? dataTableSpec.findColumnIndex(m_lemmaDocColModel.getStringValue()) : -1;

        // create annotation pipeline and a data table creator instance, which collects the results
        exec.setProgress(0.01, "Load models...");
        StanfordCoreNLP.clearAnnotatorPool();
        final StanfordCoreNLP annotationPipeline = createAnnotationPipeline(m_applyReqPreprocModel.getBooleanValue());

        // Open data container
        final BufferedDataContainer dataContainer = exec.createDataContainer(createDataTableSpec(dataTableSpec));
        final MultiThreadRelationExtractor extractor = createExtractor(dataContainer, docColIdx, lemmaDocColIdx,
            annotationPipeline, (int)totalNoOfRows, m_noOfThreadsModel.getIntValue(), exec);
        extractor.run(inputData);
        dataContainer.close();

        return new BufferedDataTable[]{dataContainer.getTable()};
    }

    /**
     * Creates and returns a new instance of {@link MultiThreadRelationExtractor}.
     *
     * @param container The {@link BufferedDataContainer} used to create a data table.
     * @param docColIdx The document column index.
     * @param lemmaDocColIdx The lemmatized document column index.
     * @param annotationPipeline The {@link AnnotationPipeline}.
     * @param maxQueueSize Maximum queue size of finished jobs (finished computations might be cached in order to ensure
     *            the proper output ordering). If this queue is full (because the next-to-be-processed computation is
     *            still ongoing), no further tasks are submitted.
     * @param maxActiveInstanceSize The maximum number of simultaneously running computations (unless otherwise bound by
     *            the used executor).
     * @param exec ExecutionContext
     *
     * @return Returns a new instance of {@link MultiThreadRelationExtractor}.
     */
    protected abstract MultiThreadRelationExtractor createExtractor(final BufferedDataContainer container,
        final int docColIdx, final int lemmaDocColIdx, final AnnotationPipeline annotationPipeline,
        final int maxQueueSize, final int maxActiveInstanceSize, final ExecutionContext exec);

    /**
     * Creates and returns an {@link AnnotationPipeline} for the specified tasks.
     *
     * @param applyPreprocessing Set true, if pos, ne tagging and lemmatizing should be done beforehand.
     * @return An {@code AnnotationPipeline} for the specified tasks
     * @throws IOException Thrown if model could not be load.
     */
    protected abstract StanfordCoreNLP createAnnotationPipeline(final boolean applyPreprocessing) throws IOException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_lemmaDocColModel.saveSettingsTo(settings);
        m_applyReqPreprocModel.saveSettingsTo(settings);
        m_noOfThreadsModel.saveSettingsTo(settings);
        saveAdditionalSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_lemmaDocColModel.validateSettings(settings);
        m_applyReqPreprocModel.validateSettings(settings);
        m_noOfThreadsModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_lemmaDocColModel.loadSettingsFrom(settings);
        m_applyReqPreprocModel.loadSettingsFrom(settings);
        m_noOfThreadsModel.loadSettingsFrom(settings);
        loadAdditionalSettingsFrom(settings);
    }

    /**
     * Load additional settings. Override this method if needed.
     *
     * @param settings A settings object.
     * @throws InvalidSettingsException Thrown if invalid settings are loaded.
     */
    protected void loadAdditionalSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
    }

    /**
     * Saves additional settings. Override this method if needed.
     *
     * @param settings A settings object.
     */
    protected void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
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
    protected void reset() {
        // Nothing to do here...
    }

}

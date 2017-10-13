/*
 * ------------------------------------------------------------------------
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
 * Created on 20.05.2013 by koetter
 */
package org.knime.ext.textprocessing.nodes.mining.topic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.mallet.Document2FeatureSequencePipe;
import org.knime.ext.textprocessing.util.mallet.DocumentInstanceIterator;

import cc.mallet.pipe.Pipe;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 *
 * @author Tobias Koetter, KNIME.com, Zurich, Switzerland
 */
public class ParallelTopicExtractorNodeModel extends NodeModel {

    private final class LogTranslator extends Handler {
        /**
         * This constant is extracted from {@link LogRecord} where it is used to determine if the given
         * thread id fits into int. "The default value of threadID will be the current thread's
         * thread id, for ease of correlation, unless it is greater than MIN_SEQUENTIAL_THREAD_ID, in which case we
         * try harder to keep our promise to keep threadIDs unique by avoiding collisions due to 32-bit wraparound.
         * Unfortunately, LogRecord.getThreadID() returns int, while Thread.getId() returns long."
         */
        private static final int MIN_SEQUENTIAL_THREAD_ID = Integer.MAX_VALUE / 2;

        private final Pattern m_progressPattern = Pattern.compile("<([0-9]+)> .* (-?[0-9]*[,\\.]?[0-9]+)");
        private final int m_maxNoOfIterations;
        private final ExecutionMonitor m_exec;
        private final long m_threadId;
        private int m_rowidCounter = 0;

        private BufferedDataContainer m_dc;

        private final ParallelTopicModel m_model;

        /**
         * @param exec {@link ExecutionMonitor} to provide progress and to check for cancel
         * @param noOfIterations the number of iterations to perform
         * @param model
         * @param threadId
         */
        private LogTranslator(final ExecutionContext exec, final int noOfIterations, final ParallelTopicModel model) {
            m_maxNoOfIterations = noOfIterations;
            m_exec = exec;
            m_model = model;
            //use the thread id to distinguish between parallel executed threads
            //this code is copied from LogRecord#defaultThreadID()
            long tid = Thread.currentThread().getId();
            if (tid < MIN_SEQUENTIAL_THREAD_ID) {
                m_threadId = (int) tid;
            } else {
                m_threadId = -1;
            }
            m_dc = exec.createDataContainer(createDetailedTableSpec());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void publish(final LogRecord record) {
            final int threadID = record.getThreadID();
            if (m_threadId < 0 || m_threadId != threadID) {
                //this is either not the thread we are listen to or we couldn't determine the right thread id at all
                return;
            }
            final Level level = record.getLevel();
            final String msg = record.getMessage();
            if (Level.INFO.equals(level)) {
                LOGGER.debug(msg);
            } else if (Level.WARNING.equals(level)) {
                LOGGER.warn(msg);
            } else if (Level.SEVERE.equals(level)) {
                LOGGER.error(msg);
            }
            //analyze only every 5th log message
            final Matcher matcher = m_progressPattern.matcher(msg);
            if (matcher.matches()) {
                final String strIter = matcher.group(1);
                final String strLl = matcher.group(2).replace(",", ".");
                try {
                    final int currentIteration = Integer.parseInt(strIter);
                    final RowKey key = RowKey.createRowKey(m_rowidCounter++);
                    final DataCell iterCell = new IntCell(currentIteration);
                    double logLikelihood = 0;
                    try {
                        logLikelihood = Double.parseDouble(strLl);
                    } catch (NumberFormatException e) {
                        logLikelihood = m_model.modelLogLikelihood();
                    }
                    final DataCell llCell = new DoubleCell(logLikelihood);
                    m_dc.addRowToTable(new DefaultRow(key, iterCell, llCell));
                    m_exec.setProgress(currentIteration / (double) m_maxNoOfIterations,
                        "Processing iteration " + currentIteration + " of " + m_maxNoOfIterations);
                } catch (NumberFormatException e) {
                    // ignore it
                }
            }
            try {
                m_exec.checkCanceled();
            } catch (CanceledExecutionException e) {
                throw new RuntimeException();
            }
        }

        /**
         * @return the details table
         */
        private BufferedDataTable getDetailsTable() {
            if (m_dc.isOpen()) {
                m_dc.close();
            }
            return m_dc.getTable();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void flush() {
            //nothing to do
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() throws SecurityException {
            //nothing to do
        }
    }

    private static final NodeLogger LOGGER = NodeLogger.getLogger(ParallelTopicExtractorNodeModel.class);

    private static final Random RANDOM = new Random();

    private final SettingsModelString m_docCol = createDocColModel();

    private final SettingsModelInteger m_noOfThreads = createNoOfThreadsModel();

    private final SettingsModelInteger m_noOfTopics = createNoOfTopicsModel();

    private final SettingsModelInteger m_topKWords = createTopKWordsModel();

    private final SettingsModelInteger m_noOfIterations = createNoOfIterationsModel();

    private final SettingsModelDouble m_alpha = createAlphaModel();

    private final SettingsModelDouble m_beta = createBetaModel();

    private final SettingsModelInteger m_seed = createSeedModel();

    private final SettingsModel[] m_models = new SettingsModel[] {
        m_docCol, m_noOfThreads, m_noOfTopics, m_topKWords, m_noOfIterations, m_alpha, m_beta, m_seed};

    /**
     * Constructor.
     */
    ParallelTopicExtractorNodeModel() {
        super(1, 3);
    }

    /**
     * @return the seed model
     */
    static SettingsModelInteger createSeedModel() {
        return new SettingsModelInteger("seed", RANDOM.nextInt());
    }

    /**
     * @return the beta model
     */
    static SettingsModelDouble createBetaModel() {
        return new SettingsModelDoubleBounded("beta", ParallelTopicModel.DEFAULT_BETA, 0, Integer.MAX_VALUE);
    }

    /**
     * @return the alpha sum model
     */
    static SettingsModelDouble createAlphaModel() {
        return new SettingsModelDoubleBounded("alpha", 0.1, 0, Integer.MAX_VALUE);
    }

    /**
     * @return the number of topics model
     */
    static SettingsModelInteger createNoOfIterationsModel() {
        return new SettingsModelIntegerBounded("noOfIterations", 1000, 1, Integer.MAX_VALUE);
    }

    /**
     * @return the number of topics model
     */
    static SettingsModelInteger createTopKWordsModel() {
        return new SettingsModelIntegerBounded("topKWords", 10, 1, Integer.MAX_VALUE);
    }

    /**
     * @return the number of topics model
     */
    static SettingsModelInteger createNoOfTopicsModel() {
        return new SettingsModelIntegerBounded("noOfTopics", 10, 1, Integer.MAX_VALUE);
    }

    /**
     * @return the number of threads model
     */
    static SettingsModelInteger createNoOfThreadsModel() {
        return new SettingsModelIntegerBounded("noOfThreads", KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads(),
            1, KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads());
    }

    /**
     * @return the document column model
     */
    static SettingsModelString createDocColModel() {
        return new SettingsModelString("docCol", null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec spec = inSpecs[0];
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        if (spec == null) {
            return null;
        }
        if (m_docCol.getStringValue() == null) {
            //preset the first column with documents
            for (DataColumnSpec colSpec : spec) {
                if (colSpec.getType().isCompatible(DocumentValue.class)) {
                    m_docCol.setStringValue(colSpec.getName());
                    setWarningMessage("Preset document column to " + m_docCol.getStringValue());
                    break;
                }
            }
        }
        final DataColumnSpec docColSpec = spec.getColumnSpec(m_docCol.getStringValue());
        if (docColSpec == null) {
            throw new IllegalArgumentException("Selected column with name "
                    + m_docCol.getStringValue() + " not found in input table");
        }
        if (!docColSpec.getType().isCompatible(DocumentValue.class)) {
            throw new IllegalArgumentException("Selected column with name "
                        + m_docCol.getStringValue() + " does not contain documents");
        }
        final ColumnRearranger docTopCR = createDocumentTopicColumnRearranger(spec, m_noOfTopics.getIntValue(), null);
        return new DataTableSpec[] {docTopCR.createSpec(), createTopicTableSpec(), createDetailedTableSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {
        final BufferedDataTable table = inData[0];
        final int noOfTopics = m_noOfTopics.getIntValue();
        final int colIdx = table.getSpec().findColumnIndex(m_docCol.getStringValue());
        final int noOfThreads = m_noOfTopics.getIntValue();
        final int noOfIterations = m_noOfIterations.getIntValue();
        final Iterator<Instance> docsIter =
                new DocumentInstanceIterator(exec.createSubProgress(0.05), table, colIdx, false);
        final Pipe docPipe = new Document2FeatureSequencePipe();
        exec.setMessage("Preprocessing documents");
        // Begin by importing documents from text to feature sequences
        final InstanceList instances = new InstanceList(docPipe);
        exec.checkCanceled();
        instances.addThruPipe(docsIter);
        exec.checkCanceled();
        final ParallelTopicModel model =
                new ParallelTopicModel(noOfTopics, m_alpha.getDoubleValue() * noOfTopics, m_beta.getDoubleValue());
        model.setRandomSeed(m_seed.getIntValue());
        model.addInstances(instances);
        model.setNumThreads(noOfThreads);
        model.setNumIterations(noOfIterations);
        exec.checkCanceled();
        exec.setMessage("Extracting topics");
        //redirect the logger output to our node logger
        final LogTranslator myLogHandler =
                new LogTranslator(exec.createSubExecutionContext(0.9), noOfIterations, model);
        ParallelTopicModel.logger.addHandler(myLogHandler);
        try {
            model.estimate();
        } catch (RuntimeException e) {
            ParallelTopicModel.logger.removeHandler(myLogHandler);
            exec.checkCanceled();
            throw e;
        }
        ParallelTopicModel.logger.removeHandler(myLogHandler);
        exec.setMessage("Writing tables");
        exec.checkCanceled();
        final ColumnRearranger dtcr = createDocumentTopicColumnRearranger(table.getDataTableSpec(), noOfTopics, model);
        final BufferedDataTable docTopicTable =
                exec.createColumnRearrangeTable(table, dtcr, exec.createSubProgress(0.025));
        // The data alphabet maps word IDs to strings
        final Alphabet dataAlphabet = instances.getDataAlphabet();
        // Get an array of sorted sets of word ID/count pairs
        final BufferedDataTable topicTable =
                createTopicTable(exec.createSubExecutionContext(0.025), dataAlphabet, model, m_topKWords.getIntValue());
        return new BufferedDataTable[] {docTopicTable, topicTable, myLogHandler.getDetailsTable()};
    }

    private ColumnRearranger createDocumentTopicColumnRearranger(final DataTableSpec inputSpec, final int noOfTopics,
        final ParallelTopicModel model) {
        final DocumentTopicCellFactory dtcf = new DocumentTopicCellFactory(noOfTopics);
        if (model != null) {
            dtcf.setTopicModel(model);
        }
        final ColumnRearranger columnRearranger = new ColumnRearranger(inputSpec);
        columnRearranger.append(dtcf);
        return columnRearranger;
    }

    private DataTableSpec createTopicTableSpec() {
        final List<DataColumnSpec> specs = new LinkedList<>();
        final DataColumnSpecCreator creator =
                new DataColumnSpecCreator("Topic id", StringCell.TYPE);
        specs.add(creator.createSpec());
        creator.setName("Term");
        specs.add(creator.createSpec());
        creator.setName("Weight");
        creator.setType(DoubleCell.TYPE);
        specs.add(creator.createSpec());
        return new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
    }

    private BufferedDataTable createTopicTable(final ExecutionContext exec, final Alphabet dataAlphabet,
        final ParallelTopicModel model, final int topKWords) throws CanceledExecutionException {
        final BufferedDataContainer dc = exec.createDataContainer(createTopicTableSpec());
        final ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        int rowId = 0;
        int wordCounter = 0;
        for (int topicId = 0; topicId < model.getNumTopics(); topicId++) {
            exec.setProgress(topicId / (double) model.getNumTopics(),
                "create rows for topic " + topicId + " of " + model.getNumTopics());
            final DataCell topicIdCell = new StringCell(DocumentTopicCellFactory.TOPIC_PREFIX + topicId);
            wordCounter = 0;
            final Iterator<IDSorter> sortedWordsIter = topicSortedWords.get(topicId).iterator();
            while (sortedWordsIter.hasNext() && wordCounter < topKWords) {
                final IDSorter idCountPair = sortedWordsIter.next();
                exec.checkCanceled();
                final DataCell termCell = new StringCell((String)dataAlphabet.lookupObject(idCountPair.getID()));
                final DataCell termWeightCell = new DoubleCell(idCountPair.getWeight());
                final RowKey rowKey = RowKey.createRowKey(rowId++);
                final DefaultRow row =
                        new DefaultRow(rowKey, topicIdCell, termCell, termWeightCell);
                dc.addRowToTable(row);
                wordCounter++;
            }
        }
        dc.close();
        return dc.getTable();
    }

    private static final DataTableSpec createDetailedTableSpec() {
        final List<DataColumnSpec> specs = new LinkedList<>();
        final DataColumnSpecCreator creator =
                new DataColumnSpecCreator("Iteration", IntCell.TYPE);
        specs.add(creator.createSpec());
        creator.setName("Log likelihood");
        creator.setType(DoubleCell.TYPE);
        specs.add(creator.createSpec());
        return new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // nothig to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        for (SettingsModel model : m_models) {
            model.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (SettingsModel model : m_models) {
            model.validateSettings(settings);
        }
        final String docCol = ((SettingsModelString)m_docCol.createCloneWithValidatedValue(settings)).getStringValue();
        if (docCol == null || docCol.trim().isEmpty()) {
            throw new InvalidSettingsException("Please select the document column");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (SettingsModel model : m_models) {
            model.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        //nothing to do
    }
}

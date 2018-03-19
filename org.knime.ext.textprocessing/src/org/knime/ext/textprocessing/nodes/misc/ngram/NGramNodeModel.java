/*
 * ------------------------------------------------------------------------
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
 * Created on 28.01.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.ThreadPool;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
public class NGramNodeModel extends NodeModel {

    /**
     * The default N values for ngram creation.
     */
    static final int DEF_N = 3;

    /**
     * The min N values for ngram creation.
     */
    static final int MIN_N = 2;

    /**
     * The max N values for ngram creation.
     */
    static final int MAX_N = Integer.MAX_VALUE;

    /**
     * The default number of threads value.
     */
    static final int DEF_THREADS = 3;

    /**
     * The min number of threads.
     */
    static final int MIN_THREADS = 1;

    /**
     * The max number of threads.
     */
    static final int MAX_THREADS = Integer.MAX_VALUE;

    /**
     * The default chunk size.
     */
    static final int DEF_CHUNK_SIZE = 500;

    /**
     * The min chunk size.
     */
    static final int MIN_CHUNK_SIZE = 1;

    /**
     * The max chunk size.
     */
    static final int MAX_CHUNK_SIZE = Integer.MAX_VALUE;

    /**
     * The word based n gram type.
     */
    static final String WORD_NGRAM_TYPE = "Word";

    /**
     * The character based n gram type.
     */
    static final String CHAR_NGRAM_TYPE = "Character";

    /**
     * The bag of words output table flag.
     */
    static final String BOW_NGRAM_OUTPUT = "NGram bag of words";

    /**
     * The frequency output table flag.
     */
    static final String FREQUENCY_NGRAM_OUTPUT = "NGram frequencies";

    /**
     * The name of the ngram column.
     */
    static final String NGRAM_OUTPUT_COLNAME = "Ngram";

    /**
     * The name of the corpus freqeuncy column.
     */
    static final String CORPUS_FREQ_OUTPUT_COLNAME = "Corpus frequency";

    /**
     * The name of the document frequency column.
     */
    static final String DOC_FREQ_OUTPUT_COLNAME = "Document frequency";

    /**
     * The name of the sentence frequency column.
     */
    static final String SENT_FREQ_OUTPUT_COLNAME = "Sentence frequency";

    /**
     * The name of the word frequency column.
     */
    static final String WORD_FREQ_OUTPUT_COLNAME = "Word frequency";

    /**
     * The name of the document column.
     */
    static final String DOCUMENT_OUTPUT_COLNAME = "Document";

    private SettingsModelIntegerBounded m_nModel = NGramNodeDialog.getNModel();

    private SettingsModelString m_nGramTypeModel = NGramNodeDialog.getNGramTypeModel();

    private SettingsModelString m_nGramOutputTableModel = NGramNodeDialog.getNGramOutputTableModel();

    private SettingsModelString m_documentColumnModel = NGramNodeDialog.getDocumentColumnModel();

    private SettingsModelIntegerBounded m_numberOfThreadsModel = NGramNodeDialog.getNumberOfThreadsModel();

    private SettingsModelIntegerBounded m_chunkSizeModel = NGramNodeDialog.getChunkSizeModel();

    private int m_documentColIndex = -1;

    private NGramDataTableCreator m_nGramDataTableCreator;

    /**
     * Creates new instance of <code>NGramNodeModel</code>.
     */
    public NGramNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec spec = inSpecs[0];
        checkDataTableSpec(spec);

        m_nGramDataTableCreator = createNGramCreator(null);
        return new DataTableSpec[]{m_nGramDataTableCreator.createDataTableSpec()};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_documentColumnModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        m_documentColIndex = spec.findColumnIndex(m_documentColumnModel.getStringValue());

    }

    private NGramDataTableCreator createNGramCreator(final ExecutionContext exec) {
        int n = m_nModel.getIntValue();

        NGramIterator nGramIterator;
        if (m_nGramTypeModel.getStringValue().equals(CHAR_NGRAM_TYPE)) {
            nGramIterator = new NGramCharacterIterator(n);
        } else {
            nGramIterator = new NGramWordIterator(n, NGramWordIterator.DEFAULT_WORD_SEPARATOR);
        }

        NGramDataTableCreator nGramCreator;

        String outputTableType = m_nGramOutputTableModel.getStringValue();
        if (outputTableType.equals(FREQUENCY_NGRAM_OUTPUT)) {
            nGramCreator = new NGramFrequencyDataTableCreator(nGramIterator, true);
        } else {
            nGramCreator = new NGramBoWDataTableCreator(nGramIterator);
        }

        return nGramCreator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        BufferedDataTable inputTable = inData[0];
        final long inputTableSize = inputTable.size();
        checkDataTableSpec(inputTable.getDataTableSpec());

        m_nGramDataTableCreator = createNGramCreator(exec);

        final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
        //The semaphore restricts the number of concurrent processes
        final Semaphore semaphore = new Semaphore(m_numberOfThreadsModel.getIntValue());
        final int chunkSize = m_chunkSizeModel.getIntValue();
        int count = 0;

        NGramDataTableCreator joiner = createNGramCreator(exec);
        List<Document> documentChunk = null;

        AtomicInteger docCount = new AtomicInteger(0);
        RowIterator it = inputTable.iterator();
        List<Future<?>> futures = new ArrayList<>();
        while (it.hasNext()) {
            exec.checkCanceled();
            DataRow row = it.next();
            if (row.getCell(m_documentColIndex).isMissing()) {
                continue;
            }

            count++;

            if (documentChunk == null) {
                documentChunk = new ArrayList<Document>(chunkSize);
            }

            // add to chunk
            if (count < chunkSize) {
                documentChunk.add(((DocumentValue)row.getCell(m_documentColIndex)).getDocument());

                // chunk is full, process and clear
            } else {
                documentChunk.add(((DocumentValue)row.getCell(m_documentColIndex)).getDocument());
                futures
                    .add(pool.enqueue(processChunk(documentChunk, joiner, exec, semaphore, docCount, inputTableSize)));
                documentChunk = null;
                count = 0;
            }
        }

        // enqueue the last chunk and wait
        if (documentChunk != null && documentChunk.size() > 0) {
            futures.add(pool.enqueue(processChunk(documentChunk, joiner, exec, semaphore, docCount, inputTableSize)));
        }

        for (Future<?> f : futures) {
            f.get(); // this call allows an additional thread from pool to run
        }

        exec.setMessage("Creating output table.");
        return new BufferedDataTable[]{joiner.createDataTable(exec)};
    }

    private Runnable processChunk(final List<Document> documents, final NGramDataTableCreator joiner,
        final ExecutionContext exec, final Semaphore semaphore, final AtomicInteger docCount, final long inputTableSize)
        throws CanceledExecutionException {
        exec.checkCanceled();
        return new Runnable() {

            @Override
            public void run() {
                NGramDataTableCreator nGramTC = null;
                try {
                    semaphore.acquire();
                    nGramTC = createNGramCreator(exec);
                    for (Document d : documents) {
                        exec.checkCanceled();
                        nGramTC.addDocument(d);
                    }

                    exec.checkCanceled();
                    joiner.joinResults(nGramTC, exec);

                    int docs = docCount.addAndGet(documents.size());
                    double progress = (double)docs / (double)inputTableSize;
                    exec.setProgress(progress,
                        "Created ngrams for documents " + docs + " of " + inputTableSize + " ...");
                } catch (final CanceledExecutionException e) {
                    // handeled in main executor thread
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } finally {
                    semaphore.release();
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_nModel.saveSettingsTo(settings);
        m_nGramTypeModel.saveSettingsTo(settings);
        m_nGramOutputTableModel.saveSettingsTo(settings);
        m_documentColumnModel.saveSettingsTo(settings);
        m_numberOfThreadsModel.saveSettingsTo(settings);
        m_chunkSizeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_nModel.validateSettings(settings);
        m_nGramTypeModel.validateSettings(settings);
        m_nGramOutputTableModel.validateSettings(settings);
        m_documentColumnModel.validateSettings(settings);
        m_numberOfThreadsModel.validateSettings(settings);
        m_chunkSizeModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_nModel.loadSettingsFrom(settings);
        m_nGramTypeModel.loadSettingsFrom(settings);
        m_nGramOutputTableModel.loadSettingsFrom(settings);
        m_documentColumnModel.loadSettingsFrom(settings);
        m_numberOfThreadsModel.loadSettingsFrom(settings);
        m_chunkSizeModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }
}

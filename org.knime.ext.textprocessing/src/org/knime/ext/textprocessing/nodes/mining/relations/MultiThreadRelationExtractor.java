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
 *   Mar 29, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.MultiThreadWorker;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreLabel.OutputFormat;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * Abstract class that provides functionality to run multi-threaded relation extraction.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public abstract class MultiThreadRelationExtractor extends MultiThreadWorker<DataRow, List<ExtractionResult>> {

    /**
     * The {@link BufferedDataContainer} to add rows and create the data table from.
     */
    private final BufferedDataContainer m_dataContainer;

    /**
     * The {@link AnnotationPipeline} to process the documents.
     */
    private final AnnotationPipeline m_annotationPipeline;

    /**
     * The document column index.
     */
    private final int m_docColIdx;

    /**
     * The lemmatized document column index.
     */
    private final int m_lemmaDocColIdx;

    /**
     * The {@link ExecutionContext} used to set the execution progress message.
     */
    private final ExecutionContext m_exec;

    /**
     * Maximum queue size.
     */
    private final int m_maxQueueSize;

    /**
     * AtomicLong to count ignored missing values.
     */
    private final AtomicLong m_missingValueCount = new AtomicLong(0);

    /**
     * Creates a new instance of {@link MultiThreadRelationExtractor}.
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
     */
    protected MultiThreadRelationExtractor(final BufferedDataContainer container, final int docColIdx,
        final int lemmaDocColIdx, final AnnotationPipeline annotationPipeline, final int maxQueueSize,
        final int maxActiveInstanceSize, final ExecutionContext exec) {
        super(maxQueueSize >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)maxQueueSize, maxActiveInstanceSize);
        m_dataContainer = container;
        m_docColIdx = docColIdx;
        m_lemmaDocColIdx = lemmaDocColIdx;
        m_annotationPipeline = annotationPipeline;
        m_exec = exec;
        m_maxQueueSize = maxQueueSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<ExtractionResult> compute(final DataRow in, final long index) throws Exception {
        final DataCell dataCell = in.getCell(m_docColIdx);
        final DataCell lemmaDataCell = m_lemmaDocColIdx >= 0 ? in.getCell(m_lemmaDocColIdx) : DataType.getMissingCell();
        List<ExtractionResult> extractionResults;
        if (!dataCell.isMissing()) {
            final Document doc = ((DocumentValue)in.getCell(m_docColIdx)).getDocument();
            final Annotation annotation = !lemmaDataCell.isMissing()
                ? DocumentToAnnotationConverter.convert(doc, ((DocumentValue)lemmaDataCell).getDocument())
                : DocumentToAnnotationConverter.convert(doc);
            try {
                m_annotationPipeline.annotate(annotation);
                List<CoreLabel> labels = annotation.get(CoreAnnotations.TokensAnnotation.class);
                labels.stream().forEach(cl -> System.out.println(cl.toString(OutputFormat.ALL)));
                System.out
                    .print(((Annotation)annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0)).toShortString('\n'));
                extractionResults = extractRelations(annotation);
            } catch (final AssertionError | NullPointerException e) {
                extractionResults = Arrays.asList(ExtractionResult.getEmptyResult());
            }
        } else {
            extractionResults = Arrays.asList(ExtractionResult.getEmptyResult());
            m_missingValueCount.addAndGet(1);
        }
        m_exec.setProgress(index / (double)m_maxQueueSize,
            () -> "Extracted relations for " + index + "/" + m_maxQueueSize + "documents.");
        return extractionResults;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processFinished(final MultiThreadWorker<DataRow, List<ExtractionResult>>.ComputationTask task)
        throws ExecutionException, CancellationException, InterruptedException {
        final List<DataCell> dataCells = Arrays.asList(task.getInput().stream().toArray(DataCell[]::new));
        int rowCount = 0;
        for (final ExtractionResult result : task.get()) {
            final List<DataCell> combined = new ArrayList<>(dataCells);
            combined.addAll(result.getDataCells());
            final RowKey key = new RowKey("Row" + task.getIndex() + "_" + rowCount);
            final DataRow newRow = new DefaultRow(key, combined);
            m_dataContainer.addRowToTable(newRow);
            rowCount++;
        }
    }

    /**
     * Creates the {@link BufferedDataTable}.
     *
     * @param exec The {@link ExecutionContext}.
     * @return A {@code BufferedDataTable}.
     */
    BufferedDataTable createDataTable(final ExecutionContext exec) {
        m_dataContainer.close();
        return m_dataContainer.getTable();
    }

    /**
     * Creates an empty {@link BufferedDataTable}.
     *
     * @return Creates an empty {@code BufferedDataTable}.
     */
    static final BufferedDataTable createEmptyTable(final DataTableSpec spec, final ExecutionContext exec) {
        final BufferedDataContainer dataContainer = exec.createDataContainer(spec);
        dataContainer.close();
        return dataContainer.getTable();
    }

    /**
     * Creates a list of {@link ExtractionResult ExtractionResults} for an {@link Annotation}.
     *
     * @param annotation The {@code Annotation} to extract the results from.
     * @return A list of {@code ExtractionResults}.
     */
    protected abstract List<ExtractionResult> extractRelations(final Annotation annotation);

    /**
     * Returns the number of processed documents cells which contained missing values.
     *
     * @return Returns the number of processed documents cells which contained missing values.
     */
    final long getMissingValueCount() {
        return m_missingValueCount.get();
    }

}

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
 *   Feb 7, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.DocumentToAnnotationConverter;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;

/**
 * This class provides functionality to extract relations from data rows, collect results and create a data table based
 * on the results.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public abstract class ExtractorDataTableCreator {

    /**
     * The {@link BufferedDataContainer} to add rows and create the data table from.
     */
    private BufferedDataContainer m_dataContainer;

    /**
     * The {@link AnnotationPipeline} to process the documents.
     */
    private final AnnotationPipeline m_annotationPipeline;

    /**
     * The {@link DataTableSpec} of the input data table.
     */
    private final DataTableSpec m_inputSpec;

    /**
     * The document column index.
     */
    private final int m_docColIdx;

    /**
     * The lemmatized document column index.
     */
    private final int m_lemmaDocColIdx;

    /**
     * The row count to generate {@link RowKey RowKeys} from.
     */
    private long m_rowCount = 0;

    /**
     * The number of the thread to create unique {@link RowKey Rowkeys}.
     */
    private final long m_threadNo;

    /**
     * Creates and returns a new instance of {@code ExtratorDataTableCreator}.
     *
     * @param inputSpec The {@link DataTableSpec} of the input data table.
     * @param docColIdx The index of the document column.
     * @param lemmaDocColIdx The index of the lemmatized document column.
     * @param annotationPipeline The {@link AnnotationPipeline} to process documents.
     * @param queueIdx The queue index used to create unique row keys.
     * @param exec The {@link ExecutionContext}.
     */
    public ExtractorDataTableCreator(final DataTableSpec inputSpec, final int docColIdx, final int lemmaDocColIdx,
        final AnnotationPipeline annotationPipeline, final long queueIdx, final ExecutionContext exec) {
        m_inputSpec = inputSpec;
        m_docColIdx = docColIdx;
        m_lemmaDocColIdx = lemmaDocColIdx;
        m_annotationPipeline = annotationPipeline;
        m_threadNo = queueIdx;

        // Open data container
        if (exec != null && m_dataContainer == null) {
            m_dataContainer = exec.createDataContainer(createDataTableSpec());
        }
    }

    /**
     * Returns the input spec.
     *
     * @return The input {@code DataTableSpec}.
     */
    protected DataTableSpec getInputSpec() {
        return m_inputSpec;
    }

    /**
     * Returns the index of the document column.
     *
     * @return The index of the document column.
     */
    protected int getDocColIdx() {
        return m_docColIdx;
    }

    /**
     * Returns the index of the lemmatized document column.
     *
     * @return The index of the lemmatized document column.
     */
    protected int getLemmaDocColIdx() {
        return m_lemmaDocColIdx;
    }

    /**
     * Returns the {@link AnnotationPipeline} used for annotating the documents.
     *
     * @return An {@code AnnotationPipeline}.
     */
    protected AnnotationPipeline getAnnotationPipeline() {
        return m_annotationPipeline;
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
     * Processes a {@link DataRow} and stores the results.
     *
     * @param dataRow The {@code DataRow} to process.
     */
    void processDataRow(final DataRow dataRow) {
        final DataCell dataCell = dataRow.getCell(m_docColIdx);
        final DataCell lemmaDataCell =
            m_lemmaDocColIdx >= 0 ? dataRow.getCell(m_lemmaDocColIdx) : DataType.getMissingCell();
        if (!dataCell.isMissing()) {
            final Document doc = ((DocumentValue)dataRow.getCell(m_docColIdx)).getDocument();
            final Annotation annotation = !lemmaDataCell.isMissing()
                ? DocumentToAnnotationConverter.convert(doc, ((DocumentValue)lemmaDataCell).getDocument(), true, true)
                : DocumentToAnnotationConverter.convert(doc);
            try {
                m_annotationPipeline.annotate(annotation);
                addResultToDataContainer(dataRow, extractRelations(annotation));
            } catch (final AssertionError | NullPointerException e) {
                addResultToDataContainer(dataRow, Arrays.asList(ExtractionResult.getEmptyResult()));
            }
        } else {
            addResultToDataContainer(dataRow, Arrays.asList(ExtractionResult.getEmptyResult()));
        }
    }

    /**
     * Adds new {@link DataRow DataRows} to the {@code BufferedDataContainer} based on the input {@code DataRow} and
     * related {@link ExtractionResult ExtractionResults}.
     */
    private void addResultToDataContainer(final DataRow dataRow, final List<ExtractionResult> results) {
        final List<DataCell> dataCells = Arrays.asList(dataRow.stream().toArray(DataCell[]::new));
        for (final ExtractionResult result : results) {
            final List<DataCell> combined = new ArrayList<>(dataCells);
            combined.addAll(result.getDataCells());
            final RowKey key = new RowKey("Row" + m_threadNo + "_" + m_rowCount);
            final DataRow newRow = new DefaultRow(key, combined);
            m_dataContainer.addRowToTable(newRow);
            m_rowCount++;
        }
    }

    /**
     * Creates a new {@link DataTableSpec}.
     *
     * @return A {@code DataTableSpec}.
     */
    protected abstract DataTableSpec createDataTableSpec();

    /**
     * Creates a list of {@link ExtractionResult ExtractionResults} for an {@link Annotation}.
     *
     * @param annotation The {@code Annotation} to extract the results from.
     * @return A list of {@code ExtractionResults}.
     */
    protected abstract List<ExtractionResult> extractRelations(final Annotation annotation);
}

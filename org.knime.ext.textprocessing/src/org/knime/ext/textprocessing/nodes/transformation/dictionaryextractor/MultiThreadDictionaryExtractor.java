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
package org.knime.ext.textprocessing.nodes.transformation.dictionaryextractor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.LongCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.MultiThreadWorker;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;

/**
 * Multi-threaded dictionary extractor. Used to extract terms from documents, count frequencies and return a data table
 * containing either all or the top X most frequent terms.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MultiThreadDictionaryExtractor extends MultiThreadWorker<DataRow, Map<String, FrequencyPair>> {

    /**
     * The name of the term column to create.
     */
    private static final String TERM_COL_NAME = "Term";

    /**
     * The name of the index column to create;
     */
    private static final String IDX = "Index";

    /**
     * The name of the term frequency column. Also used as one of the options for the 'filter by' option.
     */
    static final String TF = "TF";

    /**
     * The name of the document frequency column. Also used as one of the options for the 'filter by' option.
     */
    static final String DF = "DF";

    /**
     * The name of the inverse document frequency column. Also used as one of the options for the 'filter by' option.
     */
    static final String IDF = "IDF";

    /**
     * The document column index.
     */
    private final int m_docColIdx;

    /**
     * A map to keeptrack of all terms and their frequencies.
     */
    private Map<String, FrequencyPair> m_frequencyMap = new HashMap<>();

    /**
     * True, if only the top k terms should be returned.
     */
    private final boolean m_enableFiltering;

    /**
     * Number of top frequent terms to be displayed in the output table.
     */
    private final int m_numberOfTerms;

    /**
     * Total number of documents in the data table to process.
     */
    private final long m_totalNoOfRows;

    /**
     * The name of the frequency method which is used to sort the data to get the top X most frequent terms.
     */
    private final String m_filterBy;

    /**
     * True, if frequency columns should be appended.
     */
    private final boolean m_appendFreqColumns;

    /**
     * True, if indices columns should be appended.
     */
    private final boolean m_appendIdxColumn;

    /**
     * Number of processed rows.
     */
    private final AtomicLong m_processedRowCount = new AtomicLong(0);

    /**
     * Number of processed missing rows.
     */
    private final AtomicLong m_missingRowCount = new AtomicLong(0);

    /**
     * The {@link ExecutionContext} used to set messages and create the {@link BufferedDataTable}.
     */
    private final ExecutionContext m_exec;

    /**
     * Creates a new instance of {@link MultiThreadDictionaryExtractor}.
     *
     * @param docColIdx The document column index.
     * @param numberOfTerms Number of top frequent terms to be displayed in the output table.
     * @param totalNoOfRows Total number of documents.
     * @param filterBy Name of the frequency method which is used to sort the data to get the top X most frequent terms.
     * @param appendIdxCol Set true to append a unique indices column.
     * @param appendFreqCols Set true to append frequency columns.
     * @param maxActiveInstanceSize Number of threads.
     * @param exec The ExecutionContext.
     */
    MultiThreadDictionaryExtractor(final int docColIdx, final boolean filterTerms, final int numberOfTerms,
        final long totalNoOfRows, final String filterBy, final boolean appendIdxCol, final boolean appendFreqCols,
        final int maxActiveInstanceSize, final ExecutionContext exec) {
        super(totalNoOfRows >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)totalNoOfRows, maxActiveInstanceSize);
        m_docColIdx = docColIdx;
        m_numberOfTerms = numberOfTerms;
        m_enableFiltering = filterTerms;
        m_totalNoOfRows = totalNoOfRows;
        m_filterBy = filterBy;
        m_appendFreqColumns = appendFreqCols;
        m_appendIdxColumn = appendIdxCol;
        m_exec = exec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, FrequencyPair> compute(final DataRow in, final long index) throws Exception {
        final Map<String, FrequencyPair> counts = new HashMap<>();
        final DataCell inputCell = in.getCell(m_docColIdx);
        if (!inputCell.isMissing()) {
            final Document doc = ((DocumentValue)inputCell).getDocument();
            final Iterator<Sentence> sentenceIterator = doc.sentenceIterator();
            while (sentenceIterator.hasNext()) {
                sentenceIterator.next().getTerms().stream()//
                    .collect(Collectors.groupingBy(Term::getText, Collectors.counting()))//
                    .forEach((k, v) -> counts.merge(k, new FrequencyPair(m_totalNoOfRows, v, 1), FrequencyPair::sum));
            }
        } else {
            m_missingRowCount.incrementAndGet();
        }
        m_exec.setProgress(m_processedRowCount.incrementAndGet() / (double)m_totalNoOfRows,
            () -> "Processed " + m_processedRowCount.get() + " of " + m_totalNoOfRows + " documents.");
        return counts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processFinished(final MultiThreadWorker<DataRow, Map<String, FrequencyPair>>.ComputationTask task)
        throws ExecutionException, CancellationException, InterruptedException {
        task.get().forEach((k, v) -> m_frequencyMap.merge(k, v, FrequencyPair::sum));
    }

    /**
     * Creates and returns a new {@link BufferedDataTable} based on the processed data.
     *
     * @return Returns a new {@link BufferedDataTable} based on the processed data.
     */
    BufferedDataTable createDataTable() {
        final BufferedDataContainer dataContainer =
            m_exec.createDataContainer(createDataTableSpec(m_appendFreqColumns, m_appendIdxColumn));
        final AtomicLong rowCount = new AtomicLong(0);

        if (m_enableFiltering) {
            filterTerms(rowCount, dataContainer);
        } else {
            m_frequencyMap.entrySet().stream()//
                .forEach(
                    e -> addRowToDataContainer(e.getKey(), e.getValue(), rowCount.getAndIncrement(), dataContainer));
        }

        dataContainer.close();
        return dataContainer.getTable();
    }

    /**
     * Adds a new {@link DataRow} to a {@link BufferedDataContainer}.
     *
     * @param term The term/word.
     * @param freqPair The {@link FrequencyPair} storing TF, DF and IDF values.
     * @param rowCount The row count of the {@link DataRow} to create.
     * @param dataContainer A {@link BufferedDataContainer} to add the data row to.
     */
    private final void addRowToDataContainer(final String term, final FrequencyPair freqPair,
        final long rowCount, final BufferedDataContainer dataContainer) {
        final RowKey key = RowKey.createRowKey(rowCount);
        final DataRow row;
        if (m_appendFreqColumns && m_appendIdxColumn) {
            row = new DefaultRow(key, new StringCell(term), new IntCell((int)rowCount + 1),
                new LongCell(freqPair.getTF()), new LongCell(freqPair.getDF()), new DoubleCell(freqPair.getIDF()));
        } else if (m_appendIdxColumn) {
            row = new DefaultRow(key, new StringCell(term), new IntCell((int)rowCount + 1));
        } else if (m_appendFreqColumns) {
            row = new DefaultRow(key, new StringCell(term), new LongCell(freqPair.getTF()),
                new LongCell(freqPair.getDF()), new DoubleCell(freqPair.getIDF()));
        } else {
            row = new DefaultRow(key, new StringCell(term));
        }

        dataContainer.addRowToTable(row);
    }

    /**
     * Filters and sorts the map to keep only the top X most frequent terms.
     *
     * @param rowCount An {@link AtomicLong} taking care of the row count.
     * @param container A {@link BufferedDataContainer} which stores the data.
     */
    private final void filterTerms(final AtomicLong rowCount, final BufferedDataContainer container) {
        m_frequencyMap.entrySet().stream()//
            .sorted(Entry.<String, FrequencyPair> comparingByValue(new FrequencyPairComparator(m_filterBy)).reversed())//
            .limit(m_numberOfTerms)//
            .forEachOrdered(
                e -> addRowToDataContainer(e.getKey(), e.getValue(), rowCount.getAndIncrement(), container));
    }

    /**
     * Creates and returns the {@link DataTableSpec}.
     *
     * @return Returns the {@link DataTableSpec}.
     */
    static final DataTableSpec createDataTableSpec(final boolean appendFreqCols, final boolean appendIdxCol) {
        // create dict and occurrence column spec
        final DataColumnSpecCreator dictionaryColumn = new DataColumnSpecCreator(TERM_COL_NAME, StringCell.TYPE);
        final DataColumnSpecCreator indexColumn;
        final DataColumnSpecCreator tfColumn;
        final DataColumnSpecCreator dfColumn;
        final DataColumnSpecCreator idfColumn;
        if (appendIdxCol && appendFreqCols) {
            indexColumn = new DataColumnSpecCreator(IDX, IntCell.TYPE);
            tfColumn = new DataColumnSpecCreator(TF, LongCell.TYPE);
            dfColumn = new DataColumnSpecCreator(DF, LongCell.TYPE);
            idfColumn = new DataColumnSpecCreator(IDF, DoubleCell.TYPE);
            return new DataTableSpec(dictionaryColumn.createSpec(), indexColumn.createSpec(), tfColumn.createSpec(),
                dfColumn.createSpec(), idfColumn.createSpec());
        } else if (appendIdxCol) {
            indexColumn = new DataColumnSpecCreator(IDX, IntCell.TYPE);
            return new DataTableSpec(dictionaryColumn.createSpec(), indexColumn.createSpec());
        } else if (appendFreqCols) {
            tfColumn = new DataColumnSpecCreator(TF, LongCell.TYPE);
            dfColumn = new DataColumnSpecCreator(DF, LongCell.TYPE);
            idfColumn = new DataColumnSpecCreator(IDF, DoubleCell.TYPE);
            return new DataTableSpec(dictionaryColumn.createSpec(), tfColumn.createSpec(), dfColumn.createSpec(),
                idfColumn.createSpec());
        } // create new data table with selected columns and term column

        return new DataTableSpec(dictionaryColumn.createSpec());
    }

    /**
     * Returns the number of processed missing rows.
     *
     * @return Returns the number of processed missing rows.
     */
    final long getMissingRowCount() {
        return m_missingRowCount.get();
    }

    /**
     * A comparator to compare {@link FrequencyPair FrequencyPairs} based on the selected frequency.
     *
     * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
     */
    private static final class FrequencyPairComparator implements Comparator<FrequencyPair> {

        /**
         * The name of the frequencies to be compared.
         */
        private final String m_freqType;

        /**
         * Creates a new instance of {@link FrequencyPairComparator} with the name of the frequencies to be compared.
         *
         * @param freqType Name of the frequencies to be compared.
         */
        FrequencyPairComparator(final String freqType) {
            m_freqType = freqType;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final FrequencyPair o1, final FrequencyPair o2) {
            if (m_freqType.equals(DF)) {
                return Long.compare(o1.getDF(), o2.getDF());
            } else if (m_freqType.equals(IDF)) {
                return Double.compare(o1.getIDF(), o2.getIDF());
            } else {
                return Long.compare(o1.getTF(), o2.getTF());
            }
        }
    }

}

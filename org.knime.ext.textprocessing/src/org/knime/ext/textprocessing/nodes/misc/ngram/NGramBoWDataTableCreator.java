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
 * Created on 03.02.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * A {@link NGramDataTableCreator} which creates bag of words like data tables.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public class NGramBoWDataTableCreator implements NGramDataTableCreator {

    private final NGramIterator m_nGramIterator;

    private BufferedDataContainer m_dataContainer;

    private int m_rowCount = 0;

    private final TextContainerDataCellFactory m_documentCellFac;

    private final Map<Document, Map<String, Integer>> m_nGramFrequencies = new HashMap<Document, Map<String, Integer>>(
        1000);

    /**
     * Creates a new instance of {@link NGramBoWDataTableCreator} with given ngram iterator.
     *
     * @param nGramIterator The ngram iterator.
     */
    public NGramBoWDataTableCreator(final NGramIterator nGramIterator) {
        if (nGramIterator == null) {
            throw new IllegalArgumentException("N gram iterator must not be null!");
        }

        m_nGramIterator = nGramIterator;
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDocument(final Document doc) {
        m_nGramIterator.setDocument(doc);
        final Map<String, Integer> nGramFrequencies = new HashMap<String, Integer>(1000);

        // iterate over all blocks
        while (m_nGramIterator.hasNextBlock()) {
            m_nGramIterator.nextBlock();

            // generate all ngrams of current block
            while (m_nGramIterator.hasNextNGram()) {
                final String nGram = m_nGramIterator.nextNGram();
                Integer freq = nGramFrequencies.get(nGram);
                if (freq == null) {
                    freq = 0;
                }
                freq++;
                nGramFrequencies.put(nGram, freq);
            }
        }

        m_nGramFrequencies.put(doc, nGramFrequencies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized BufferedDataTable createDataTable(final ExecutionContext exec) {
        openDataContainer(exec);

        for (Entry<Document, Map<String, Integer>> entry : m_nGramFrequencies.entrySet()) {
            addNGramsToDataContainer(entry.getValue(), entry.getKey());
        }

        if (m_dataContainer != null && m_dataContainer.isOpen()) {
            m_dataContainer.close();
            return m_dataContainer.getTable();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void joinResults(final NGramDataTableCreator nGramCreator, final ExecutionContext exec) {
        if (nGramCreator != null && exec != null && nGramCreator instanceof NGramBoWDataTableCreator) {
            openDataContainer(exec);

            final Map<Document, Map<String, Integer>> nGramFrequencies =
                ((NGramBoWDataTableCreator)nGramCreator).getResults();

            for (Entry<Document, Map<String, Integer>> entry : nGramFrequencies.entrySet()) {
                addNGramsToDataContainer(entry.getValue(), entry.getKey());
            }
        }
    }

    /**
     * @return Returns the ngrams and their frequencies, where ngrams are keys and frequencies the corresponding values.
     */
    public Map<Document, Map<String, Integer>> getResults() {
        return m_nGramFrequencies;
    }

    private synchronized void openDataContainer(final ExecutionContext exec) {
        if (exec != null) {
            if (m_dataContainer == null) {
                m_dataContainer = exec.createDataContainer(createDataTableSpec());
            }
        }
        m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
    }

    private synchronized void addNGramsToDataContainer(final Map<String, Integer> nGrams, final Document doc) {
        if (nGrams != null && doc != null && m_dataContainer != null && m_dataContainer.isOpen()) {
            DataCell documentCell = m_documentCellFac.createDataCell(doc);

            for (Entry<String, Integer> nGramEntry : nGrams.entrySet()) {
                final String nGram = nGramEntry.getKey();
                final Integer freq = nGramEntry.getValue();

                final RowKey rowKey = new RowKey(Integer.valueOf(m_rowCount).toString());
                final StringCell nGramCell = new StringCell(nGram);
                final IntCell freqCell = new IntCell(freq);

                final DataRow newRow = new DefaultRow(rowKey, nGramCell, documentCell, freqCell);
                m_dataContainer.addRowToTable(newRow);

                m_rowCount++;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec createDataTableSpec() {
        final List<DataColumnSpec> dcscList = new ArrayList<DataColumnSpec>();

        dcscList.add(new DataColumnSpecCreator(NGramNodeModel.NGRAM_OUTPUT_COLNAME, StringCell.TYPE).createSpec());
        dcscList.add(new DataColumnSpecCreator(NGramNodeModel.DOCUMENT_OUTPUT_COLNAME, m_documentCellFac.getDataType())
            .createSpec());
        dcscList.add(new DataColumnSpecCreator(NGramNodeModel.DOC_FREQ_OUTPUT_COLNAME, IntCell.TYPE).createSpec());

        return new DataTableSpec(dcscList.toArray(new DataColumnSpec[dcscList.size()]));
    }

}

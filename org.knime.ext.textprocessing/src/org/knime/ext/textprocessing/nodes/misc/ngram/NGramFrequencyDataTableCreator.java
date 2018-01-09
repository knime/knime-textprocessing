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
 * Created on 28.01.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * A {@link NGramDataTableCreator} which creates data tables containing the ngrams and their corresponding frequencies
 * in the corpus and documents.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
public final class NGramFrequencyDataTableCreator implements NGramDataTableCreator {

    private static final int DEFAULT_MAP_SIZE = 1000;

    private final NGramIterator m_nGramIterator;

    private final boolean m_countDocumentFreqs;

    private final Map<String, Integer> m_nGramFreqs;

    private final Map<String, Set<UUID>> m_nGramDocumentFreqs;

    private final Map<String, Set<TextContainer>> m_nGramBlockFreq;

    /**
     * Creates an instance of <code>NGramCreator</code> with given ngram iterator, and a flag specifying whether
     * frequencies for documents are counted or not.
     *
     * @param nGramIterator the n gram iterator to use for n gram creation.
     * @param countDocumentFrequencies a flag specifying whether frequencies for documents are counted or not.
     */
    public NGramFrequencyDataTableCreator(final NGramIterator nGramIterator, final boolean countDocumentFrequencies) {
        if (nGramIterator == null) {
            throw new IllegalArgumentException("N gram iterator must not be null!");
        }

        m_nGramIterator = nGramIterator;

        m_countDocumentFreqs = countDocumentFrequencies;

        m_nGramFreqs = Collections.synchronizedMap(new LinkedHashMap<String, Integer>(DEFAULT_MAP_SIZE));
        m_nGramDocumentFreqs = Collections.synchronizedMap(new LinkedHashMap<String, Set<UUID>>(DEFAULT_MAP_SIZE));
        m_nGramBlockFreq = Collections.synchronizedMap(new LinkedHashMap<String, Set<TextContainer>>(DEFAULT_MAP_SIZE));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDocument(final Document doc) {
        m_nGramIterator.setDocument(doc);

        // iterate over all blocks
        while (m_nGramIterator.hasNextBlock()) {
            final TextContainer currentBlock = m_nGramIterator.nextBlock();

            // generate all ngrams of current block
            while (m_nGramIterator.hasNextNGram()) {
                final String nGram = m_nGramIterator.nextNGram();

                countCorpusFreqs(nGram);
                countDocumentFreqs(nGram, doc);
                countBlockFreqs(nGram, currentBlock);
            }
        }
    }

    /**
     * Resets all frequency counts.
     */
    public void reset() {
        m_nGramFreqs.clear();
        m_nGramDocumentFreqs.clear();
        m_nGramBlockFreq.clear();
    }

    private void countCorpusFreqs(final String nGram) {
        Integer freq = m_nGramFreqs.get(nGram);
        if (freq == null) {
            freq = 0;
        }
        freq++;
        m_nGramFreqs.put(nGram, freq);
    }

    private synchronized void addCorpusFreqs(final Map<String, Integer> corpusFreqs) {
        if (corpusFreqs != null) {

            for (Entry<String, Integer> entry : corpusFreqs.entrySet()) {
                final Integer freqToAdd = entry.getValue();
                Integer freq = m_nGramFreqs.get(entry.getKey());

                if (freqToAdd != null) {
                    if (freq == null) {
                        freq = 0;
                    }
                    freq += freqToAdd;
                    m_nGramFreqs.put(entry.getKey(), freq);
                }
            }
        }
    }

    private void countDocumentFreqs(final String nGram, final Document doc) {
        if (m_countDocumentFreqs) {
            Set<UUID> docUUIDs = m_nGramDocumentFreqs.get(nGram);
            if (docUUIDs == null) {
                docUUIDs = new LinkedHashSet<UUID>();
            }
            docUUIDs.add(doc.getUUID());
            m_nGramDocumentFreqs.put(nGram, docUUIDs);
        }
    }

    private synchronized void addDocumentFrequencies(final Map<String, Set<UUID>> ngramDocs) {
        if (ngramDocs != null) {
            for (Entry<String, Set<UUID>> entry : ngramDocs.entrySet()) {
                final Set<UUID> docsToAdd = entry.getValue();
                Set<UUID> docs = m_nGramDocumentFreqs.get(entry.getKey());

                if (docsToAdd != null) {
                    if (docs == null) {
                        docs = new LinkedHashSet<UUID>();
                    }
                    docs.addAll(docsToAdd);
                    m_nGramDocumentFreqs.put(entry.getKey(), docs);
                }
            }
        }
    }

    private void countBlockFreqs(final String nGram, final TextContainer block) {
        if (m_countDocumentFreqs) {
            Set<TextContainer> blocks = m_nGramBlockFreq.get(nGram);
            if (blocks == null) {
                blocks = new LinkedHashSet<TextContainer>();
            }
            blocks.add(block);
            m_nGramBlockFreq.put(nGram, blocks);
        }
    }

    private synchronized void addBlockFrequencies(final Map<String, Set<TextContainer>> nGramBlocks) {
        if (nGramBlocks != null) {
            for (Entry<String, Set<TextContainer>> entry : nGramBlocks.entrySet()) {
                final Set<TextContainer> blocksToAdd = entry.getValue();
                Set<TextContainer> blocks = m_nGramBlockFreq.get(entry.getKey());

                if (blocksToAdd != null) {
                    if (blocks == null) {
                        blocks = new LinkedHashSet<TextContainer>();
                    }
                    blocks.addAll(blocksToAdd);
                    m_nGramBlockFreq.put(entry.getKey(), blocks);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void joinResults(final NGramDataTableCreator nGramCreator, final ExecutionContext exec) {
        if (nGramCreator != null && exec != null && nGramCreator instanceof NGramFrequencyDataTableCreator) {

            final NGramFrequencyDataTableCreator freqNGramCreator = (NGramFrequencyDataTableCreator)nGramCreator;

            addCorpusFreqs(freqNGramCreator.getCorpusFrequencies());

            if (m_countDocumentFreqs) {
                addDocumentFrequencies(freqNGramCreator.getDocumentFrequencies());
                addBlockFrequencies(freqNGramCreator.getBlockFrequencies());
            }
        }
    }

    /**
     * @return Returns the ngrams and their frequencies in the corpus, where ngrams are the keys and the frequencies the
     *         corresponding values.
     */
    public Map<String, Integer> getCorpusFrequencies() {
        return m_nGramFreqs;
    }

    /**
     * @return Returns the ngrams and their frequencies in the documents, where ngrams are the keys and the frequencies
     *         the corresponding values.
     */
    public Map<String, Set<UUID>> getDocumentFrequencies() {
        return m_nGramDocumentFreqs;
    }

    /**
     * @return Returns the ngrams and their frequencies in the blocks, where ngrams are the keys and the frequencies the
     *         corresponding values.
     */
    public Map<String, Set<TextContainer>> getBlockFrequencies() {
        return m_nGramBlockFreq;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized BufferedDataTable createDataTable(final ExecutionContext exec) {
        BufferedDataContainer dc = exec.createDataContainer(createDataTableSpec());

        int rowCount = -1;
        for (String ngram : m_nGramFreqs.keySet()) {
            rowCount++;
            final int totalFreq = m_nGramFreqs.get(ngram);

            final RowKey rowKey = new RowKey(Integer.valueOf(rowCount).toString());

            final StringCell nGramCell = new StringCell(ngram);
            final IntCell totalFreqCell = new IntCell(totalFreq);

            DataRow row;
            if (m_countDocumentFreqs) {
                final Set<UUID> docUUIDs = m_nGramDocumentFreqs.get(ngram);
                Integer decFreq = 0;
                if (docUUIDs != null) {
                    decFreq = docUUIDs.size();
                }

                final Set<TextContainer> blocks = m_nGramBlockFreq.get(ngram);
                Integer blockFreq = 0;
                if (blocks != null) {
                    blockFreq = blocks.size();
                }

                final IntCell docFreqCell = new IntCell(decFreq);
                final IntCell blockFreqCell = new IntCell(blockFreq);

                row = new DefaultRow(rowKey, nGramCell, totalFreqCell, docFreqCell, blockFreqCell);
            } else {
                row = new DefaultRow(rowKey, nGramCell, totalFreqCell);
            }

            dc.addRowToTable(row);
        }

        dc.close();

        return dc.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec createDataTableSpec() {
        // which n gram type is created
        boolean wordNGramType = true;
        if (m_nGramIterator instanceof NGramCharacterIterator) {
            wordNGramType = false;
        }

        final List<DataColumnSpec> dcscList = new ArrayList<DataColumnSpec>();

        dcscList.add(new DataColumnSpecCreator(NGramNodeModel.NGRAM_OUTPUT_COLNAME, StringCell.TYPE).createSpec());
        dcscList.add(new DataColumnSpecCreator(NGramNodeModel.CORPUS_FREQ_OUTPUT_COLNAME, IntCell.TYPE).createSpec());

        if (m_countDocumentFreqs) {
            dcscList.add(new DataColumnSpecCreator(NGramNodeModel.DOC_FREQ_OUTPUT_COLNAME, IntCell.TYPE).createSpec());

            if (wordNGramType) {
                dcscList.add(new DataColumnSpecCreator(NGramNodeModel.SENT_FREQ_OUTPUT_COLNAME, IntCell.TYPE)
                    .createSpec());
            } else {
                dcscList.add(new DataColumnSpecCreator(NGramNodeModel.WORD_FREQ_OUTPUT_COLNAME, IntCell.TYPE)
                    .createSpec());
            }
        }

        return new DataTableSpec(dcscList.toArray(new DataColumnSpec[dcscList.size()]));
    }
}

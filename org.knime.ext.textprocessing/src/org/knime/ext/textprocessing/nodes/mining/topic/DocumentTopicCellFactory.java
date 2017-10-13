/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   26.06.2014 (koetter): created
 */
package org.knime.ext.textprocessing.nodes.mining.topic;

import org.knime.base.node.io.filereader.DataCellFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;

import cc.mallet.topics.ParallelTopicModel;

/**
 * {@link DataCellFactory} that uses a given {@link ParallelTopicModel} to append the probability of a topic
 * to belong to a given document.
 *
 * @author Tobias Koetter, KNIME.com, Zurich, Switzerland
 */
public class DocumentTopicCellFactory implements CellFactory {

    /**Prefix for the topic id.*/
    public static final String TOPIC_PREFIX = "topic_";
    private final int m_noOfTopics;
    private ParallelTopicModel m_model;
    private int m_docIdx = 0;
    private final int m_colIdx;

    /**
     * @param noOfTopics the number of topic columns to create
     * @param colIdx the index of the document column
     */
    public DocumentTopicCellFactory(final int noOfTopics, final int colIdx) {
        m_noOfTopics = noOfTopics;
        m_colIdx = colIdx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        if (m_model == null) {
            throw new IllegalStateException("Topic model must not be null. Call setTopicModel first.");
        }
        final DataCell[] cells = new DataCell[m_noOfTopics + 1];
        double maxProb = 0;
        int maxTopicIdx = 0;
        for (int idx = 0; idx < m_noOfTopics; idx++) {
            if (row.getCell(m_colIdx).isMissing()) {
                cells[idx] = DataType.getMissingCell();
            } else {
                // Estimate the topic distribution of the first document, given the current Gibbs state.
                final double[] topicDistribution = m_model.getTopicProbabilities(m_docIdx);
                final double topicProb = topicDistribution[idx];
                if (topicProb > maxProb) {
                    maxProb = topicProb;
                    maxTopicIdx = idx;
                }
                cells[idx] = new DoubleCell(topicProb);
            }
        }
        if (row.getCell(m_colIdx).isMissing()) {
            cells[cells.length - 1] = DataType.getMissingCell();
        } else {
            cells[cells.length - 1] = new StringCell(TOPIC_PREFIX + maxTopicIdx);
            m_docIdx++;
        }
        return cells;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        DataColumnSpec[] specs = new DataColumnSpec[m_noOfTopics + 1];
        final DataColumnSpecCreator creator = new DataColumnSpecCreator("Dummy", DoubleCell.TYPE);
        for (int idx = 0; idx < m_noOfTopics; idx++) {
            creator.setName(TOPIC_PREFIX + idx);
            specs[idx] = creator.createSpec();
        }
        creator.setName("Assigned topic");
        creator.setType(StringCell.TYPE);
        specs[specs.length - 1] = creator.createSpec();
        return specs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount, final RowKey lastKey, final ExecutionMonitor exec) {
        exec.setProgress(curRowNr / (double) rowCount, "processing row " + lastKey);
    }



    /**
     * @param model {@link ParallelTopicModel} to use
     */
    public void setTopicModel(final ParallelTopicModel model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null");
        }
        m_model = model;
    }
}

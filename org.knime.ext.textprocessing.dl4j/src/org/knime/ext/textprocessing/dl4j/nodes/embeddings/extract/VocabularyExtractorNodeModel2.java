/*******************************************************************************
 * Copyright by KNIME GmbH, Konstanz, Germany
 * Website: http://www.knime.org; Email: contact@knime.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.extract;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.inactive.InactiveBranchPortObject;
import org.knime.core.node.port.inactive.InactiveBranchPortObjectSpec;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Node to extract a vocabulary with corresponding word vectors from a {@link WordVectors} model.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class VocabularyExtractorNodeModel2 extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(VocabularyExtractorNodeModel2.class);

    private WordVectorTrainingMode m_trainingMode;

    private PortObjectSpec[] m_outputSpec;

    private double m_progressCounter = 0;

    private double m_maxProgress;

    public VocabularyExtractorNodeModel2() {
        super(new PortType[]{WordVectorPortObject.TYPE},
            new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final WordVectorPortObject portObject = (WordVectorPortObject)inObjects[0];
        WordVectors wordVec = portObject.getWordVectors();

        final List<String> vocabulary = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        switch (m_trainingMode) {
            case DOC2VEC:
                ParagraphVectors pv = (ParagraphVectors)wordVec;
                labels.addAll(pv.getLabelsSource().getLabels());

                vocabulary.addAll(pv.vocab().words());
                vocabulary.removeAll(labels);
            case WORD2VEC:
                vocabulary.addAll(wordVec.vocab().words());
        }

        m_maxProgress = vocabulary.size();

        PortObject[] outputTables = new PortObject[2];
        outputTables[0] = createWordVectorTableFromWordList((DataTableSpec)m_outputSpec[0], vocabulary, wordVec, exec);

        if (labels.isEmpty()) {
            outputTables[1] = InactiveBranchPortObject.INSTANCE;
        } else {
            outputTables[1] = createWordVectorTableFromWordList((DataTableSpec)m_outputSpec[1], labels, wordVec, exec);
        }

        return outputTables;
    }

    private void incrementProgess(final ExecutionContext exec) {
        exec.setProgress((m_progressCounter + 1) / m_maxProgress);
        m_progressCounter++;
    }

    private BufferedDataTable createWordVectorTableFromWordList(final DataTableSpec tableSpec, final List<String> words,
        final WordVectors wv, final ExecutionContext exec) {
        final BufferedDataContainer container = exec.createDataContainer(tableSpec);
        int i = 0;
        for (final String word : words) {
            incrementProgess(exec);
            final List<DataCell> cells = new ArrayList<>();

            cells.add(new StringCell(word));

            final INDArray vector = wv.getWordVectorMatrix(word);
            final ListCell wordVectorollectionCell =
                CollectionCellFactory.createListCell(NDArrayUtils.toListOfDoubleCells(vector));
            cells.add(wordVectorollectionCell);

            container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
            i++;
        }
        container.close();
        return container.getTable();
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        WordVectorPortObjectSpec port = (WordVectorPortObjectSpec)inSpecs[0];
        m_trainingMode = port.getWordVectorTrainingsMode();
        m_outputSpec = createOutputSpec(m_trainingMode);
        return m_outputSpec;
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        // no parameter for this node
        return new ArrayList<SettingsModel>();
    }

    /**
     * Create DataTableSpec containing two columns. First column: StringCell Second column: ListCell containing
     * DoubleCell
     *
     * @return the DataTableSpec containing both columns
     * @throws InvalidSettingsException
     */
    private PortObjectSpec[] createOutputSpec(final WordVectorTrainingMode mode) throws InvalidSettingsException {

        PortObjectSpec[] portSpecs = new PortObjectSpec[2];

        DataColumnSpec[] colSpecs = new DataColumnSpec[2];
        DataColumnSpecCreator stringSpecCreator = new DataColumnSpecCreator("set_me", DataType.getType(StringCell.class));
        DataColumnSpecCreator doubleListSpecCreator =
            new DataColumnSpecCreator("set_me", DataType.getType(ListCell.class, DoubleCell.TYPE));

        stringSpecCreator.setName("word");
        colSpecs[0] = stringSpecCreator.createSpec();
        doubleListSpecCreator.setName("vector");
        colSpecs[1] = doubleListSpecCreator.createSpec();
        portSpecs[0] = new DataTableSpec(colSpecs);

        switch (mode) {
            case DOC2VEC:
                stringSpecCreator.setName("label");
                colSpecs[0] = stringSpecCreator.createSpec();
                doubleListSpecCreator.setName("vector");
                colSpecs[1] = doubleListSpecCreator.createSpec();
                portSpecs[1] = new DataTableSpec(colSpecs);
                break;
            case WORD2VEC:
                portSpecs[1] = InactiveBranchPortObjectSpec.INSTANCE;
                break;
            default:
                throw new InvalidSettingsException("No case for WordVectorTrainingMode " + mode + " defined");
        }

        return portSpecs;
    }
}

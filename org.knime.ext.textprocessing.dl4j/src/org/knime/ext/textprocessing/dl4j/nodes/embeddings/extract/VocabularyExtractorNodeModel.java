/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
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
 * KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
import org.deeplearning4j.models.word2vec.Word2Vec;
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
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.knime.ext.textprocessing.dl4j.util.WordVectorPortObjectUtils;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Node to extract a vocabulary with corresponding word vectors from a {@link WordVectors} model.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class VocabularyExtractorNodeModel extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(VocabularyExtractorNodeModel.class);

    public VocabularyExtractorNodeModel() {
        super(new PortType[]{WordVectorPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final WordVectorPortObject portObject = (WordVectorPortObject)inObjects[0];
        final Word2Vec wordVec = WordVectorPortObjectUtils.wordVectorsToWord2Vec(portObject.getWordVectors());
        final BufferedDataContainer container = exec.createDataContainer(createOutputSpec());

        final List<String> voc = new ArrayList<>(wordVec.vocab().words());

        int i = 0;
        for (final String word : voc) {
            exec.setProgress(((double)(i + 1)) / ((double)voc.size()));

            final List<DataCell> cells = new ArrayList<>();

            cells.add(new StringCell(word));

            final INDArray vector = wordVec.getWordVectorMatrix(word);
            final ListCell wordVectorollectionCell =
                CollectionCellFactory.createListCell(NDArrayUtils.toListOfDoubleCells(vector));
            cells.add(wordVectorollectionCell);

            container.addRowToTable(new DefaultRow(new RowKey("Row" + i), cells));
            i++;
        }

        container.close();
        final BufferedDataTable outputTable = container.getTable();

        return new PortObject[]{outputTable};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{createOutputSpec()};
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
     */
    private DataTableSpec createOutputSpec() {
        final DataColumnSpec[] colSpecs = new DataColumnSpec[2];

        DataColumnSpecCreator specCreator = new DataColumnSpecCreator("word", DataType.getType(StringCell.class));
        colSpecs[0] = specCreator.createSpec();

        specCreator = new DataColumnSpecCreator("output_vector", DataType.getType(ListCell.class, DoubleCell.TYPE));
        colSpecs[1] = specCreator.createSpec();

        return new DataTableSpec(colSpecs);
    }
}

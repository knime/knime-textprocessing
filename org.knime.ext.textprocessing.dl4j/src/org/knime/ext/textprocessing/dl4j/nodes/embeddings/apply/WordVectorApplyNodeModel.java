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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.apply;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.text.tokenization.tokenizer.Tokenizer;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels2;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.NDArrayUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Node to apply a {@link WordVectors} model to documents, meaning to replace all words contained in the document with
 * the corresponding word vector. Has option to calculate the mean vector for a document.
 *
 * @author David Kolb, KNIME.com GmbH
 */
final class WordVectorApplyNodeModel extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(WordVectorApplyNodeModel.class);

    private DataParameterSettingsModels2 m_dataParameterSettings;

    private final SettingsModelBoolean m_calculateMean = createCalculateMeanSettings();

    private DataTableSpec m_outputSpec;

    private final Set<String> m_unknownWords = new HashSet<>();

    WordVectorApplyNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE, WordVectorPortObject.TYPE},
            new PortType[]{BufferedDataTable.TYPE});
        addToSettingsModels(m_calculateMean);
    }

    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final BufferedDataTable table = (BufferedDataTable)inObjects[0];
        final WordVectorPortObject portObject = (WordVectorPortObject)inObjects[1];
        final WordVectors wordVectors = portObject.getWordVectors();

        final int documentColumnIndex =
            table.getDataTableSpec().findColumnIndex(m_dataParameterSettings.getString(DataParameter.DOCUMENT_COLUMN));

        final BufferedDataContainer container = exec.createDataContainer(m_outputSpec);
        final CloseableRowIterator tableIterator = table.iterator();

        long i = 0;
        while (tableIterator.hasNext()) {
            exec.setProgress(((double)(i + 1)) / ((double)table.size()));

            container.addRowToTable(processRow(tableIterator.next(), documentColumnIndex, wordVectors));
            i++;
        }

        logUnkownWords();

        container.close();
        final BufferedDataTable outputTable = container.getTable();

        return new PortObject[]{outputTable};
    }

    private DataRow processRow(final DataRow row, final int documentColumnIndex, final WordVectors wordVectors)
        throws DataCellConversionException {

        final List<DataCell> cells = TableUtils.toListOfCells(row);
        final DataCell cell = row.getCell(documentColumnIndex);

        final String document = ConverterUtils.convertDataCellToJava(cell, String.class);
        ListCell convertedDocument;

        if (m_calculateMean.getBooleanValue()) {
            final INDArray documentMeanVector = calculateDocumentMean(wordVectors, document);
            convertedDocument =
                CollectionCellFactory.createListCell(NDArrayUtils.toListOfDoubleCells(documentMeanVector));
        } else {
            convertedDocument = replaceWordsByWordVector(wordVectors, document);
        }

        cells.add(convertedDocument);

        return new DefaultRow(row.getKey(), cells);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        return new StreamableOperator() {
            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
                throws Exception {
                DataTableSpec tableSpec = (DataTableSpec)inSpecs[0];
                WordVectorPortObject wordVectorModel =
                    (WordVectorPortObject)((PortObjectInput)inputs[1]).getPortObject();
                WordVectors wordVectors = wordVectorModel.getWordVectors();
                RowInput rowInput = (RowInput)inputs[0];
                RowOutput rowOutput = (RowOutput)outputs[0];

                final int documentColumnIndex =
                    tableSpec.findColumnIndex(m_dataParameterSettings.getString(DataParameter.DOCUMENT_COLUMN));

                DataRow row;
                while ((row = rowInput.poll()) != null) {
                    rowOutput.push(processRow(row, documentColumnIndex, wordVectors));
                }

                rowOutput.close();
                rowInput.close();
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return new InputPortRole[]{InputPortRole.DISTRIBUTED_STREAMABLE, InputPortRole.NONDISTRIBUTED_NONSTREAMABLE};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        return new OutputPortRole[]{OutputPortRole.DISTRIBUTED};
    }

    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[0];
        final String documentColumnName = m_dataParameterSettings.getString(DataParameter.DOCUMENT_COLUMN);
        ConfigurationUtils.validateColumnSelection(tableSpec, documentColumnName);
        checkDocumentColumnType(tableSpec);

        if (m_calculateMean.getBooleanValue()) {
            m_outputSpec = TableUtils.appendColumnSpec(tableSpec, "converted_document",
                DataType.getType(ListCell.class, DoubleCell.TYPE));
        } else {
            m_outputSpec = TableUtils.appendColumnSpec(tableSpec, "converted_document",
                DataType.getType(ListCell.class, DataType.getType(ListCell.class, DoubleCell.TYPE)));
        }
        return new DataTableSpec[]{m_outputSpec};
    }

    private void checkDocumentColumnType(final DataTableSpec spec) throws InvalidSettingsException {
        final String documentColumnName = m_dataParameterSettings.getString(DataParameter.DOCUMENT_COLUMN);
        DataColumnSpec documentColumnSpec = spec.getColumnSpec(documentColumnName);
        DataType type = documentColumnSpec.getType();

        if (!(type.isCompatible(DocumentValue.class) || type.isCompatible(StringValue.class))) {
            throw new InvalidSettingsException(
                "Data Type " + documentColumnSpec.getType().getName() + " is not supported.");
        }
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_dataParameterSettings = new DataParameterSettingsModels2();
        m_dataParameterSettings.setParameter(DataParameter.DOCUMENT_COLUMN);

        final List<SettingsModel> settings = new ArrayList<SettingsModel>();
        settings.addAll(m_dataParameterSettings.getAllInitializedSettings());

        return settings;
    }

    /**
     * Converts the word vector corresponding to a specific word to a {@link ListCell} containing {@link DoubleCell}s
     * containing the elements of the word vector.
     *
     * @param wordVec the {@link WordVectors} model to use
     * @param word the word for which we want to retrieve the word vector
     * @return the {@link ListCell} containing the word vector as {@link DoubleCell}s
     */
    private ListCell wordToListCell(final WordVectors wordVec, final String word) {
        final List<DoubleCell> cells = NDArrayUtils.toListOfDoubleCells(wordVec.getWordVectorMatrix(word));
        return CollectionCellFactory.createListCell(cells);
    }

    /**
     * Replaces each word contained in a document with its corresponding word vector. If a word from the document is not
     * contained in the used {@link WordVectors} model it will be skipped. The output is a {@link ListCell} containing
     * {@link ListCell}s containing the word vectors as {@link DoubleCell}s.
     *
     * @param wordVec the {@link WordVectors} model to use
     * @param document the document to use
     * @return {@link ListCell} of {@link ListCell}c of {@link DoubleCell}s containing converted words
     */
    private ListCell replaceWordsByWordVector(final WordVectors wordVec, final String document) {
        final TokenizerFactory tokenizerFac = new DefaultTokenizerFactory();
        tokenizerFac.setTokenPreProcessor(new CommonPreprocessor());

        final Tokenizer t = tokenizerFac.create(document);
        final List<ListCell> listCells = new ArrayList<ListCell>();

        while (t.hasMoreTokens()) {
            final String word = t.nextToken();
            if (!word.isEmpty()) {
                if (wordVec.hasWord(word)) {
                    listCells.add(wordToListCell(wordVec, word));
                } else {
                    m_unknownWords.add(word);
                }
            }
        }
        return CollectionCellFactory.createListCell(listCells);
    }

    /**
     * Calculates the mean vector of all word vectors of all words contained in a document.
     *
     * @param wordVec the {@link WordVectors} model to use
     * @param document the document for which the mean should be calculated
     * @return {@link INDArray} containing the mean vector of the document
     */
    private INDArray calculateDocumentMean(final WordVectors wordVec, final String document) {
        final TokenizerFactory tokenizerFac = new DefaultTokenizerFactory();
        tokenizerFac.setTokenPreProcessor(new CommonPreprocessor());

        final Tokenizer t = tokenizerFac.create(document);
        final List<String> tokens = t.getTokens();

        int numberOfWordsMatchingWithVoc = 0;
        for (final String token : tokens) {
            if (wordVec.hasWord(token)) {
                numberOfWordsMatchingWithVoc++;
            }
        }

        final INDArray documentWordVectors =
            Nd4j.create(numberOfWordsMatchingWithVoc, wordVec.lookupTable().layerSize());

        int i = 0;
        for (final String token : tokens) {
            if (!token.isEmpty()) {
                if (wordVec.hasWord(token)) {
                    documentWordVectors.putRow(i, wordVec.getWordVectorMatrix(token));
                    i++;
                } else {
                    m_unknownWords.add(token);
                }
            }
        }
        final INDArray documentMeanVector = documentWordVectors.mean(0);
        return documentMeanVector;
    }

    @Override
    protected void reset() {
        m_unknownWords.clear();
    }

    private void logUnkownWords() {
        if (!m_unknownWords.isEmpty()) {
            logger.warn(m_unknownWords.size() + " words are not contained in the WordVector vocabulary.");
        }
    }

    public static SettingsModelBoolean createCalculateMeanSettings() {
        return new SettingsModelBoolean("do_calculate_mean", false);
    }
}

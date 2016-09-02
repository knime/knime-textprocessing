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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.learn;

import java.util.ArrayList;
import java.util.List;

import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.models.paragraphvectors.ParagraphVectors;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.documentiterator.LabelAwareIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.settings.enumerate.DataParameter;
import org.knime.ext.dl4j.base.settings.enumerate.LearnerParameter;
import org.knime.ext.dl4j.base.settings.impl.DataParameterSettingsModels;
import org.knime.ext.dl4j.base.settings.impl.LearnerParameterSettingsModels;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.knime.ext.textprocessing.dl4j.data.BufferedDataTableLabelledDocumentIterator;
import org.knime.ext.textprocessing.dl4j.data.BufferedDataTableSentenceIterator;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorLearnerParameter;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.textprocessing.dl4j.settings.impl.WordVectorParameterSettingsModels;

/**
 * Node to learn a {@link WordVectors} model using either Wor2Vec or Doc2Vec using DL4J implementation. For details see:
 * http://deeplearning4j.org/word2vec
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class WordVectorLearnerNodeModel extends AbstractDLNodeModel {

    // the logger instance
    private static final NodeLogger logger = NodeLogger.getLogger(WordVectorLearnerNodeModel.class);

    /* SettingsModels */
    private LearnerParameterSettingsModels m_learnerParameterSettings;

    private DataParameterSettingsModels m_dataParameterSettings;

    private WordVectorParameterSettingsModels m_wordVecParameterSettings;

    private WordVectorPortObjectSpec m_outputSpec;

    public WordVectorLearnerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{WordVectorPortObject.TYPE});
    }

    @Override
    protected WordVectorPortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
        throws Exception {
        final BufferedDataTable table = (BufferedDataTable)inObjects[0];

        TableUtils.checkForEmptyTable(table);

        final WordVectorTrainingMode mode =
            WordVectorTrainingMode.valueOf(m_wordVecParameterSettings.getWordVectorTrainingsMode().getStringValue());
        final String labelColumnName = m_dataParameterSettings.getLabelColumn().getStringValue();
        final String documentColumnName = m_dataParameterSettings.getDocumentColumn().getStringValue();
        WordVectors wordVectors = null;

        // training parameters
        final int trainingIterations = m_learnerParameterSettings.getTrainingIterations().getIntValue();
        final int minWordFrequency = m_wordVecParameterSettings.getMinWordFrequency().getIntValue();
        final int layerSize = m_wordVecParameterSettings.getLayerSize().getIntValue();
        final int seed = m_learnerParameterSettings.getSeed().getIntValue();
        final double learningRate = m_learnerParameterSettings.getGlobalLearningRate().getDoubleValue();
        final double minLearningRate = m_wordVecParameterSettings.getMinimumLearningRate().getDoubleValue();
        final int windowSize = m_wordVecParameterSettings.getWindowSize().getIntValue();
        final int epochs = m_dataParameterSettings.getEpochs().getIntValue();
        final int batchSize = m_dataParameterSettings.getBatchSize().getIntValue();

        // sentence tokenizer and preprocessing
        final boolean usePreproc = m_wordVecParameterSettings.getUseBasicPreprocessing().getBooleanValue();
        final TokenizerFactory t = new DefaultTokenizerFactory();
        if (usePreproc) {
            t.setTokenPreProcessor(new CommonPreprocessor());
        }

        switch (mode) {
            case DOC2VEC:
                final LabelAwareIterator docIter =
                    new BufferedDataTableLabelledDocumentIterator(table, documentColumnName, labelColumnName);

                // build doc2vec model
                final ParagraphVectors d2v = new ParagraphVectors.Builder().learningRate(learningRate)
                    .minLearningRate(minLearningRate).seed(seed).layerSize(layerSize).batchSize(batchSize)
                    .windowSize(windowSize).minWordFrequency(minWordFrequency).iterations(trainingIterations)
                    .epochs(epochs).iterate(docIter).trainWordVectors(true).tokenizerFactory(t).build();

                d2v.fit();
                wordVectors = d2v;

                break;

            case WORD2VEC:
                final SentenceIterator sentenceIter = new BufferedDataTableSentenceIterator(table, documentColumnName);

                // build word2vec model
                final Word2Vec w2v = new Word2Vec.Builder().learningRate(learningRate).learningRate(minLearningRate)
                    .seed(seed).layerSize(layerSize).batchSize(batchSize).windowSize(windowSize)
                    .minWordFrequency(minWordFrequency).iterations(trainingIterations).epochs(epochs)
                    .iterate(sentenceIter).tokenizerFactory(t).build();

                w2v.fit();
                wordVectors = w2v;

                break;

            default:
                throw new InvalidSettingsException("No case defined for WordVectorTrainingMode: " + mode);
        }

        final WordVectorPortObject outPortObject = new WordVectorPortObject(wordVectors, m_outputSpec);
        return new WordVectorPortObject[]{outPortObject};
    }

    @Override
    protected WordVectorPortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec tableSpec = (DataTableSpec)inSpecs[0];

        final WordVectorTrainingMode mode =
            WordVectorTrainingMode.valueOf(m_wordVecParameterSettings.getWordVectorTrainingsMode().getStringValue());
        final String labelColumnName = m_dataParameterSettings.getLabelColumn().getStringValue();
        final String documentColumnName = m_dataParameterSettings.getDocumentColumn().getStringValue();

        switch (mode) {
            case DOC2VEC:
                try {
                    ConfigurationUtils.validateColumnSelection(tableSpec,
                        new String[]{labelColumnName, documentColumnName});
                } catch (final InvalidSettingsException e) {
                    throw new InvalidSettingsException("Need to specify document and label column for " + mode);
                }
                break;
            case WORD2VEC:
                try {
                    ConfigurationUtils.validateColumnSelection(tableSpec, documentColumnName);
                } catch (final InvalidSettingsException e) {
                    throw new InvalidSettingsException("Need to specify document column for " + mode);
                }
                break;
            default:
                throw new InvalidSettingsException("No case defined for WordVectorTrainingsMode: " + mode);
        }

        m_outputSpec = new WordVectorPortObjectSpec(mode);
        return new WordVectorPortObjectSpec[]{m_outputSpec};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_learnerParameterSettings = new LearnerParameterSettingsModels();
        m_learnerParameterSettings.setParameter(LearnerParameter.GLOBAL_LEARNING_RATE);
        m_learnerParameterSettings.setParameter(LearnerParameter.TRAINING_ITERATIONS);
        m_learnerParameterSettings.setParameter(LearnerParameter.SEED);

        m_dataParameterSettings = new DataParameterSettingsModels();
        m_dataParameterSettings.setParameter(DataParameter.BATCH_SIZE);
        m_dataParameterSettings.setParameter(DataParameter.EPOCHS);
        m_dataParameterSettings.setParameter(DataParameter.LABEL_COLUMN);
        // default training mode is Word2Vec so labels are not required by
        // default
        m_dataParameterSettings.getLabelColumn().setEnabled(false);

        m_dataParameterSettings.setParameter(DataParameter.DOCUMENT_COLUMN);

        m_wordVecParameterSettings = new WordVectorParameterSettingsModels();
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.LAYER_SIZE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.MIN_WORD_FREQUENCY);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.WINDOW_SIZE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.WORD_VECTOR_TRAINING_MODE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.MIN_LEARNING_RATE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.USE_BASIC_PREPROCESSING);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_learnerParameterSettings.getAllInitializedSettings());
        settings.addAll(m_dataParameterSettings.getAllInitializedSettings());
        settings.addAll(m_wordVecParameterSettings.getAllInitializedSettings());

        return settings;
    }

}

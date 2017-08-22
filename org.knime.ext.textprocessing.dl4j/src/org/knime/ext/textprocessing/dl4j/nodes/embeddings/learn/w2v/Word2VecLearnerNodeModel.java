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
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.learn.w2v;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.deeplearning4j.models.embeddings.learning.ElementsLearningAlgorithm;
import org.deeplearning4j.models.embeddings.learning.impl.elements.CBOW;
import org.deeplearning4j.models.embeddings.learning.impl.elements.SkipGram;
import org.deeplearning4j.models.word2vec.VocabWord;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.ext.dl4j.base.AbstractDLNodeModel;
import org.knime.ext.dl4j.base.util.ConfigurationUtils;
import org.knime.ext.dl4j.base.util.TableUtils;
import org.knime.ext.textprocessing.dl4j.data.BufferedDataTableSentenceIterator;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorFileStorePortObject;
import org.knime.ext.textprocessing.dl4j.nodes.embeddings.WordVectorPortObjectSpec;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorLearnerParameter;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorTrainingMode;
import org.knime.ext.textprocessing.dl4j.settings.impl.WordVectorParameterSettingsModels2;

/**
 * Learner node for Word2Vec models.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class Word2VecLearnerNodeModel extends AbstractDLNodeModel {

    /* SettingsModels */
    private WordVectorParameterSettingsModels2 m_wordVecParameterSettings;

    /**
     * Constructor for the node model.
     */
    public Word2VecLearnerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{WordVectorFileStorePortObject.TYPE});
    }

    @Override
    protected WordVectorFileStorePortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec)
        throws Exception {
        final BufferedDataTable table = (BufferedDataTable)inObjects[0];

        TableUtils.checkForEmptyTable(table);

        final String documentColumnName =
            m_wordVecParameterSettings.getString(WordVectorLearnerParameter.DOCUMENT_COLUMN);
        final String elementsAlgo =
            m_wordVecParameterSettings.getString(WordVectorLearnerParameter.ELEMENTS_LEARNING_ALGO);

        // training parameters
        final int trainingIterations =
            m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.TRAINING_ITERATIONS);
        final int minWordFrequency =
            m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.MIN_WORD_FREQUENCY);
        final int layerSize = m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.LAYER_SIZE);
        final int seed = m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.SEED);
        final double learningRate = m_wordVecParameterSettings.getDouble(WordVectorLearnerParameter.LEARNING_RATE);
        final double sampling = m_wordVecParameterSettings.getDouble(WordVectorLearnerParameter.SAMPLING);
        double negativeSampling =
            m_wordVecParameterSettings.getDouble(WordVectorLearnerParameter.NEGATIVE_SAMPLING);
        final double minLearningRate =
            m_wordVecParameterSettings.getDouble(WordVectorLearnerParameter.MIN_LEARNING_RATE);
        final int windowSize = m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.WINDOW_SIZE);
        final int epochs = m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.EPOCHS);
        final int batchSize = m_wordVecParameterSettings.getInteger(WordVectorLearnerParameter.BATCH_SIZE);

        final boolean skipMissing =
            m_wordVecParameterSettings.getBoolean(WordVectorLearnerParameter.SKIP_MISSING_CELLS);
        final boolean useHS =
            m_wordVecParameterSettings.getBoolean(WordVectorLearnerParameter.USE_HIERARCHICAL_SOFTMAX);

        final TokenizerFactory t = new DefaultTokenizerFactory();

        final BufferedDataTableSentenceIterator sentenceIter =
            new BufferedDataTableSentenceIterator(table, documentColumnName, skipMissing);

        // Either hierarchical softmax or negative sampling should be used at the same time.
        if (useHS) {
            negativeSampling = 0.0;
        }

        // build word2vec model
        final Word2Vec w2v =
            new Word2Vec.Builder().learningRate(learningRate).minLearningRate(minLearningRate).seed(seed)
                .layerSize(layerSize).batchSize(batchSize).windowSize(windowSize).minWordFrequency(minWordFrequency)
                .iterations(trainingIterations).epochs(epochs).iterate(sentenceIter).tokenizerFactory(t)
                .allowParallelTokenization(false).elementsLearningAlgorithm(parseElementsAlgo(elementsAlgo))
                .useHierarchicSoftmax(useHS).negativeSample(negativeSampling).sampling(sampling).build();

        w2v.fit();
        sentenceIter.close();

        final WordVectorFileStorePortObject outPortObject =
            WordVectorFileStorePortObject.create(w2v, new WordVectorPortObjectSpec(WordVectorTrainingMode.WORD2VEC),
                exec.createFileStore(UUID.randomUUID().toString()));
        return new WordVectorFileStorePortObject[]{outPortObject};
    }

    @Override
    protected WordVectorPortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        ConfigurationUtils.validateColumnSelection((DataTableSpec)inSpecs[0],
            m_wordVecParameterSettings.getString(WordVectorLearnerParameter.DOCUMENT_COLUMN));

        return new WordVectorPortObjectSpec[]{new WordVectorPortObjectSpec(WordVectorTrainingMode.WORD2VEC)};
    }

    @Override
    protected List<SettingsModel> initSettingsModels() {
        m_wordVecParameterSettings = new WordVectorParameterSettingsModels2();
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.LEARNING_RATE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.TRAINING_ITERATIONS);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.SEED);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.BATCH_SIZE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.EPOCHS);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.DOCUMENT_COLUMN);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.LAYER_SIZE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.MIN_WORD_FREQUENCY);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.WINDOW_SIZE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.MIN_LEARNING_RATE);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.SKIP_MISSING_CELLS);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.NEGATIVE_SAMPLING);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.SAMPLING);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.ELEMENTS_LEARNING_ALGO);
        m_wordVecParameterSettings.setParameter(WordVectorLearnerParameter.USE_HIERARCHICAL_SOFTMAX);

        final List<SettingsModel> settings = new ArrayList<>();
        settings.addAll(m_wordVecParameterSettings.getAllInitializedSettings());

        return settings;
    }

    /**
     * Parse the string representation of an
     * {@link org.knime.ext.textprocessing.dl4j.settings.enumerate.ElementsLearningAlgorithm} and returns the
     * corresponding DL4J object.
     *
     * @param rep
     * @return DL4J object of elements algo corresponding to specified string representation
     */
    private ElementsLearningAlgorithm<VocabWord> parseElementsAlgo(final String rep) {
        switch (org.knime.ext.textprocessing.dl4j.settings.enumerate.ElementsLearningAlgorithm.valueOf(rep)) {
            case CBOW:
                return new CBOW<VocabWord>();
            case SKIP_GRAM:
                return new SkipGram<VocabWord>();
            default:
                throw new IllegalArgumentException("No case defined for ElementsLearningAlgorithm: " + rep);
        }
    }
}

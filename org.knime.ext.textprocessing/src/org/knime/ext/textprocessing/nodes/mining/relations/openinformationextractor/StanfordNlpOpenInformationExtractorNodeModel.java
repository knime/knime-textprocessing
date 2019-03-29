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
 *   Nov 30, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations.openinformationextractor;

import java.io.IOException;
import java.util.Properties;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.nodes.mining.relations.MultiThreadRelationExtractor;
import org.knime.ext.textprocessing.nodes.mining.relations.ParallelExtractorNodeModel;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * The {@link NodeModel} for the Stanford NLP Open Information Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class StanfordNlpOpenInformationExtractorNodeModel extends ParallelExtractorNodeModel {

    /**
     * Configuration key for lemmatized results option.
     */
    private static final String CFG_KEY_LEMMATIZED_RESULTS = "lemmatized_results";

    /**
     * Configuration key for the coreference option.
     */
    private static final String CFG_KEY_RESOLVE_COREFERENCE = "resolve_coref";

    /**
     * Configuration key for the affinity probability cap.
     */
    private static final String CFG_KEY_AFFINITY_PROB_CAP = "affinity_probability_cap";

    /**
     * Configuration key for the triple strict option.
     */
    private static final String CFG_KEY_TRIPLE_STRICT = "triple_strict";

    /**
     * Configuration key for the all nominals option.
     */
    private static final String CFG_KEY_ALL_NOMINALS = "all_nominals";

    /**
     * Default value for lemmatized results option.
     */
    private static final boolean DEF_LEMMATIZED_RESULTS = false;

    /**
     * Default value for the coreference option.
     */
    private static final boolean DEF_RESOLVE_COREFERENCE = false;

    /**
     * Default value for the affinity probability cap.
     */
    private static final double DEF_AFFINITY_PROB_CAP = 1.0 / 3.0;

    /**
     * Default value for the triple strict option.
     */
    private static final boolean DEF_TRIPLE_STRICT = true;

    /**
     * Default value for the all nominals option.
     */
    private static final boolean DEF_ALL_NOMINALS = false;

    /**
     * Creates and returns a new {@link SettingsModelBoolean} containing the value for the option to show results as
     * lemma.
     *
     * @return {@code SettingsModelBoolean} containing the value for the option to show results as lemma.
     */
    static final SettingsModelBoolean getLemmatizedResultsModel() {
        return new SettingsModelBoolean(CFG_KEY_LEMMATIZED_RESULTS, DEF_LEMMATIZED_RESULTS);
    }

    /**
     * Creates and returns a new {@link SettingsModelBoolean} containing the value for the coreference solution option.
     *
     * @return {@code SettingsModelBoolean} containing the value for the coreference solution option.
     */
    static final SettingsModelBoolean getResolveCorefModel() {
        return new SettingsModelBoolean(CFG_KEY_RESOLVE_COREFERENCE, DEF_RESOLVE_COREFERENCE);
    }

    /**
     * Creates and returns a new {@link SettingsModelDoubleBounded} containing the cap for the affinity probability.
     *
     * @return {@code SettingsModelSettingsModelDoubleBounded} containing the cap for the affinity probability.
     */
    static final SettingsModelDoubleBounded getAffinityProbCapModel() {
        return new SettingsModelDoubleBounded(CFG_KEY_AFFINITY_PROB_CAP, DEF_AFFINITY_PROB_CAP, 0.0, 1.0);
    }

    /**
     * Creates and returns a new {@link SettingsModelBoolean} containing the value for the triple strict option.
     *
     * @return {@code SettingsModelBoolean} containing the value for the triple strict option.
     */
    static final SettingsModelBoolean getTripleStrictModel() {
        return new SettingsModelBoolean(CFG_KEY_TRIPLE_STRICT, DEF_TRIPLE_STRICT);
    }

    /**
     * Creates and returns a new {@link SettingsModelBoolean} containing the value for the all nominals option.
     *
     * @return {@code SettingsModelBoolean} containing the value for the all nominals option.
     */
    static final SettingsModelBoolean getAllNominalsModel() {
        return new SettingsModelBoolean(CFG_KEY_ALL_NOMINALS, DEF_ALL_NOMINALS);
    }

    /**
     * The path to the dependecy parsing model.
     */
    private static final String DEP_PARSE_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dependecyparsing/english_UD.gz").getAbsolutePath();

    /**
     * The path to the clause splitting model.
     */
    private static final String CLAUSES_SPLIT_MODEL_PATH = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/clausesplitting/clauseSearcherModel.ser.gz").getAbsolutePath();

    /**
     * The path to the affinity model.
     */
    private static final String AFFINITY_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/affinity_models").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_ANIMATE_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/animate.unigrams.txt").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_INANIMATE_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/inanimate.unigrams.txt").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_DEMONYMS_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/demonyms.txt").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATES_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/state-abbreviations.txt").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_COUNTRIES_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/countries").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATESANDPROVINCES_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/statesandprovinces").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_DICT1_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/coref.dict1.tsv").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_DICT2_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/coref.dict2.tsv").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_DICT3_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/coref.dict3.tsv").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_DICT4_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/coref.dict4.tsv").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_GENDER_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/dcoref/gender.data.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATISTICAL_CLASSIFICATION_MODEL_PATH = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/coref/statistical/classification_model.ser.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATISTICAL_RANKING_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/coref/statistical/ranking_model.ser.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATISTICAL_ANAPHORICITY_MODEL_PATH = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/coref/statistical/anaphoricity_model.ser.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATISTICAL_CLUSTERING_MODEL_PATH = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/coref/statistical/clustering_model.ser.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_STATISTICAL_WORD_COUNTS_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/coref/statistical/word_counts.ser.gz").getAbsolutePath();

    /**
     * The path to the dependency parsing model.
     */
    private static final String COREF_MD_DEP_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/coref/md-model-dep.ser.gz").getAbsolutePath();

    /**
     * OpenIE property prefix.
     */
    private static final String OPENIE_PREFIX = "openie.";

    /**
     * The {@link SettingsModelBoolean} containing the value for option to show results as lemma.
     */
    private final SettingsModelBoolean m_lemmatizedResultsModel = getLemmatizedResultsModel();

    /**
     * The {@link SettingsModelBoolean} containing the value for the coreference solution option.
     */
    private final SettingsModelBoolean m_resolveCorefModel = getResolveCorefModel();

    /**
     * The {@link SettingsModelIntegerBounded} containing the cap for the affinity probability.
     */
    private final SettingsModelDoubleBounded m_affinityProbCapModel = getAffinityProbCapModel();

    /**
     * The {@link SettingsModelBoolean} containing the value for the triple strict option.
     */
    private final SettingsModelBoolean m_tripleStrictModel = getTripleStrictModel();

    /**
     * The {@link SettingsModelBoolean} containing the value for the all nominals option.
     */
    private final SettingsModelBoolean m_allNominalsModel = getAllNominalsModel();

    /**
     * Creates a new instance of {@code StanfordNlpOpenInformationExtractorNodeModel}.
     */
    StanfordNlpOpenInformationExtractorNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec createDataTableSpec(final DataTableSpec spec) {
        return StanfordOpenInformationExtractor.createDataTableSpec(spec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final StanfordCoreNLP createAnnotationPipeline(final boolean applyPreprocessing) throws IOException {
        // create properties
        final String annotators = "annotators";
        final boolean resolveCoref = m_resolveCorefModel.getBooleanValue();
        final Properties props = new Properties();
        if (applyPreprocessing && resolveCoref) {
            props.setProperty(annotators, "pos, lemma, ner, entitymentions, depparse, coref, natlog, openie");
            setPreprocessingProps(props);
            setCorefProps(props);
        } else if (!applyPreprocessing && resolveCoref) {
            props.setProperty(annotators, "entitymentions, depparse, coref, natlog, openie");
            setCorefProps(props);
        } else if (applyPreprocessing) {
            props.setProperty(annotators, "pos, lemma, ner, entitymentions, depparse, natlog, openie");
            setPreprocessingProps(props);
        } else {
            props.setProperty(annotators, "entitymentions, depparse, natlog, openie");
        }

        setNecessaryProps(props, m_resolveCorefModel.getBooleanValue(), m_affinityProbCapModel.getDoubleValue(),
            m_tripleStrictModel.getBooleanValue(), m_allNominalsModel.getBooleanValue());

        // create pipeline
        return new StanfordCoreNLP(props, false);
    }

    /**
     * Sets pos/ne tagging properties to a {@link Properties} instance.
     *
     * @param props The properties object.
     */
    private static final void setPreprocessingProps(final Properties props) {
        final String falseFlag = "false";
        props.setProperty("pos.model", POS_MODEL_PATH);
        props.setProperty("ner.model", NER_MODEL_PATH);
        props.setProperty("ner.applyNumericClassifiers", falseFlag);
        props.setProperty("ner.useSUTime", falseFlag);
        props.setProperty("ner.applyFineGrained", falseFlag);
    }

    /**
     * Sets coreference solution properties to a {@link Properties} instance.
     *
     * @param props The properties object.
     */
    private static final void setCorefProps(final Properties props) {
        props.setProperty("coref.demonym", COREF_DEMONYMS_PATH);
        props.setProperty("coref.animate", COREF_ANIMATE_PATH);
        props.setProperty("coref.inanimate", COREF_INANIMATE_PATH);
        props.setProperty("coref.states", COREF_STATES_PATH);
        props.setProperty("coref.countries", COREF_COUNTRIES_PATH);
        props.setProperty("coref.states.provinces", COREF_STATESANDPROVINCES_PATH);
        props.setProperty("coref.dictlist",
            COREF_DICT1_PATH + "," + COREF_DICT2_PATH + "," + COREF_DICT3_PATH + "," + COREF_DICT4_PATH);
        props.setProperty("coref.big.gender.number", COREF_GENDER_PATH);
        props.setProperty("coref.statistical.classificationModel", COREF_STATISTICAL_CLASSIFICATION_MODEL_PATH);
        props.setProperty("coref.statistical.rankingModel", COREF_STATISTICAL_RANKING_MODEL_PATH);
        props.setProperty("coref.statistical.anaphoricityModel", COREF_STATISTICAL_ANAPHORICITY_MODEL_PATH);
        props.setProperty("coref.statistical.clusteringModel", COREF_STATISTICAL_CLUSTERING_MODEL_PATH);
        props.setProperty("coref.statistical.wordCounts", COREF_STATISTICAL_WORD_COUNTS_PATH);
        props.setProperty("coref.md.type", "dep");
        props.setProperty("coref.md.model", COREF_MD_DEP_MODEL_PATH);
    }

    /**
     * Sets open information extraction properties to a {@link Properties} instance.
     *
     * @param props The properties object.
     */
    private static final void setNecessaryProps(final Properties props, final boolean resolveCoref,
        final double affinityProbCap, final boolean tripleStrict, final boolean allNominals) {
        props.setProperty("depparse.model", DEP_PARSE_MODEL_PATH);
        props.setProperty(OPENIE_PREFIX + "splitter.model", CLAUSES_SPLIT_MODEL_PATH);
        props.setProperty(OPENIE_PREFIX + "affinity_models", AFFINITY_MODEL_PATH);
        props.setProperty(OPENIE_PREFIX + CFG_KEY_RESOLVE_COREFERENCE, Boolean.toString(resolveCoref));
        props.setProperty(OPENIE_PREFIX + CFG_KEY_AFFINITY_PROB_CAP, Double.toString(affinityProbCap));
        props.setProperty(OPENIE_PREFIX + CFG_KEY_TRIPLE_STRICT, Boolean.toString(tripleStrict));
        props.setProperty(OPENIE_PREFIX + CFG_KEY_ALL_NOMINALS, Boolean.toString(allNominals));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final MultiThreadRelationExtractor createExtractor(final BufferedDataContainer container,
        final int docColIdx, final int lemmaDocColIdx, final AnnotationPipeline annotationPipeline,
        final int maxQueueSize, final int maxActiveInstanceSize, final ExecutionContext exec) {
        return new StanfordOpenInformationExtractor(container, docColIdx, lemmaDocColIdx,
            m_lemmatizedResultsModel.getBooleanValue(), annotationPipeline, maxQueueSize, maxActiveInstanceSize, exec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
        m_lemmatizedResultsModel.saveSettingsTo(settings);
        m_allNominalsModel.saveSettingsTo(settings);
        m_resolveCorefModel.saveSettingsTo(settings);
        m_tripleStrictModel.saveSettingsTo(settings);
        m_affinityProbCapModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_lemmatizedResultsModel.validateSettings(settings);
        m_allNominalsModel.validateSettings(settings);
        m_resolveCorefModel.validateSettings(settings);
        m_tripleStrictModel.validateSettings(settings);
        m_affinityProbCapModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadAdditionalSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_lemmatizedResultsModel.loadSettingsFrom(settings);
        m_allNominalsModel.loadSettingsFrom(settings);
        m_resolveCorefModel.loadSettingsFrom(settings);
        m_tripleStrictModel.loadSettingsFrom(settings);
        m_affinityProbCapModel.loadSettingsFrom(settings);
    }
}

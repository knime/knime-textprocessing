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
package org.knime.ext.textprocessing.nodes.mining.relations.relationextractor;

import java.io.IOException;
import java.util.Properties;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeModel;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.nodes.mining.relations.ExtractorDataTableCreator;
import org.knime.ext.textprocessing.nodes.mining.relations.ParallelExtractorNodeModel;

import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

/**
 * The {@link NodeModel} for the Stanford NLP Relation Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class StanfordNlpRelationExtractorNodeModel extends ParallelExtractorNodeModel {

    /**
     * The path to the parser model.
     */
    private static final String PARSER_MODEL_PATH =
        TextprocessingCorePlugin.resolvePath("stanfordmodels/parser_model/englishPCFG.ser.gz").getAbsolutePath();

    /**
     * The path to the relation extractor model.
     */
    private static final String RELATION_EXTRACTOR_MODEL = TextprocessingCorePlugin
        .resolvePath("stanfordmodels/relationextraction/roth_relation_model_pipelineNER.ser").getAbsolutePath();

    /**
     * Creates a new instance of {@code StanfordNlpRelationExtractorNodeModel}.
     */
    StanfordNlpRelationExtractorNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec createDataTableSpec(final DataTableSpec spec) {
        return new RelationExtractorDataTableCreator(spec, 0, 0, null, 0, null).createDataTableSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final StanfordCoreNLP createAnnotationPipeline(final boolean applyPreprocessing) throws IOException {
        // create properties
        final Properties props = new Properties();
        if (applyPreprocessing) {
            final String falseFlag = "false";
            props.setProperty("annotators", "pos, lemma, ner, parse, relation");
            props.setProperty("ner.model", NER_MODEL_PATH);
            props.setProperty("ner.applyNumericClassifiers", falseFlag);
            props.setProperty("ner.useSUTime", falseFlag);
            props.setProperty("ner.applyFineGrained", falseFlag);
            props.setProperty("pos.model", POS_MODEL_PATH);
        } else {
            props.setProperty("annotators", "parse, relation");
        }
        props.setProperty("sup.relation.model", RELATION_EXTRACTOR_MODEL);
        props.setProperty("parse.model", PARSER_MODEL_PATH);

        // create pipeline
        return new StanfordCoreNLP(props, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final ExtractorDataTableCreator createDataTableCreator(final DataTableSpec inSpec, final int docColIdx,
        final int lemmaDocColIdx, final AnnotationPipeline annotationPipeline, final long queueIdx,
        final ExecutionContext exec) {
        return new RelationExtractorDataTableCreator(inSpec, docColIdx, lemmaDocColIdx, annotationPipeline, queueIdx,
            exec);
    }
}

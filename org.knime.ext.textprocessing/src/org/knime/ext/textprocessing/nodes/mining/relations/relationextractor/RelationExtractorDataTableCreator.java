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
 *   Feb 8, 2019 (julian): created
 */
package org.knime.ext.textprocessing.nodes.mining.relations.relationextractor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.ext.textprocessing.nodes.mining.relations.ExtractionResult;
import org.knime.ext.textprocessing.nodes.mining.relations.ExtractorDataTableCreator;

import edu.stanford.nlp.ie.machinereading.structure.EntityMention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class provides functionality to extract relations from data rows, collect results and create a data table based
 * on the results.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class RelationExtractorDataTableCreator extends ExtractorDataTableCreator {

    /**
     * The name of the subject column to create.
     */
    private static final String OBJECT1_COL_NAME = "Object 1";

    /**
     * The name of the object column to create.
     */
    private static final String OBJECT2_COL_NAME = "Object 2";

    /**
     * The name of the predicate column to create.
     */
    private static final String BEST_RELATION_COL_NAME = "Best relation";

    /**
     * The name of the confidence column to create.
     */
    private static final String CONFIDENCE_COL_NAME = "Confidence";

    /**
     * Creates and returns a new instance of {@code RelationExtractorDataTableCreator}.
     *
     * @param inputSpec The {@link DataTableSpec} of the input data table.
     * @param docColIdx The index of the document column.
     * @param lemmaDocColIdx The index of the lemmatized document column.
     * @param annotationPipeline The {@link AnnotationPipeline} to process documents.
     * @param queueIdx The queue index used to generate unique {@link RowKey RowKeys}.
     */
    RelationExtractorDataTableCreator(final DataTableSpec inputSpec, final int docColIdx, final int lemmaDocColIdx,
        final AnnotationPipeline annotationPipeline, final long queueIdx) {
        super(inputSpec, docColIdx, lemmaDocColIdx, annotationPipeline, queueIdx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec createDataTableSpec() {
        final DataColumnSpecCreator value1 = new DataColumnSpecCreator(OBJECT1_COL_NAME, StringCell.TYPE);
        final DataColumnSpecCreator value2 = new DataColumnSpecCreator(BEST_RELATION_COL_NAME, StringCell.TYPE);
        final DataColumnSpecCreator bestRelation = new DataColumnSpecCreator(OBJECT2_COL_NAME, StringCell.TYPE);
        final DataColumnSpecCreator confidence = new DataColumnSpecCreator(CONFIDENCE_COL_NAME, DoubleCell.TYPE);

        // create new data table with selected columns and term column
        return new DataTableSpec(getInputSpec(), new DataTableSpec(value1.createSpec(), value2.createSpec(),
            bestRelation.createSpec(), confidence.createSpec()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final List<ExtractionResult> extractRelations(final Annotation annotation) {
        final List<ExtractionResult> results = new LinkedList<>();
        boolean noResults = true;
        for (final CoreMap sentenceCM : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {

            // Get the relation extraction triples for the sentence
            final List<RelationMention> triples =
                sentenceCM.get(MachineReadingAnnotations.RelationMentionsAnnotation.class);

            for (final RelationMention triple : triples) {
                final List<EntityMention> mentions = triple.getEntityMentionArgs();
                final String object1 = mentions.get(0).getFullValue();
                final String object2 = mentions.get(1).getFullValue();
                final String relation = triple.getType();
                final Counter<String> probas = triple.getTypeProbabilities();
                Double confidence = 0.0;
                for (final Entry<String, Double> entry : probas.entrySet()) {
                    if (entry.getValue() >= confidence) {
                        confidence = entry.getValue();
                    }
                }
                results.add(new ExtractionResult(object1, relation, object2, confidence));
                noResults = false;
            }
        }
        if (noResults) {
            results.add(ExtractionResult.getEmptyResult());
        }

        return results;
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * Created on 28.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.Map;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;

/**
 * Provides utility methods for preprocessing of documents, terms, chunks and more.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
final class PreprocessingUtils {

    private PreprocessingUtils() { }

    /**
     * Applies deep preprocessing to the given document by using the given {@link TermPreprocessing} instance. All terms
     * of the document that are not unmodifieable will be replaced by the preprocessed version of the term, created by
     * the given {@link TermPreprocessing}. If preprocessUnmodifieable flag is {@code true} the unmodifieable setting of
     * the term is ignored. The new preprocessed document instance is returned.
     *
     * @param document The document to preprocess.
     * @param preprocessing The preprocessing instruction.
     * @param preprocessUnmodifieable If set {@code true} unmodifieable terms are processed as well, if {@code false}
     *            they will be not replaced.
     * @return The preprocessed document.
     * @since 2.9
     */
    public static final Document deepPPWithPreprocessing(final Document document, final TermPreprocessing preprocessing,
        final boolean preprocessUnmodifieable) {
        final DocumentBuilder builder = new DocumentBuilder(document);
        for (final Section s : document.getSections()) {
            for (final Paragraph p : s.getParagraphs()) {
                for (final Sentence sen : p.getSentences()) {
                    for (Term t : sen.getTerms()) {
                        if (!t.isUnmodifiable() || preprocessUnmodifieable) {
                            t = preprocessing.preprocessTerm(t);
                        }
                        if (t != null && t.getText().length() > 0) {
                            builder.addTerm(t);
                        }
                    }
                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        return builder.createDocument();
    }

    /**
     * Applies deep preprocessing of given document based on given term mapping. All term in the document will be
     * replaced accoring to the given mapping. Unmodifieable flags will be ignored if preprocessUnmodifieable flag is
     * set {@code true}. If a term of the document is contained in the key set of the given map it is replaced by the
     * corresponding value term of the map.
     *
     * @param document The document to preprocess.
     * @param termMapping The terms which are replaced in documents and their replacement term. Terms of the documents
     *            that are contained in the key set of the map are replaced by the corresponding value term.
     * @param preprocessUnmodifieable If set {@code true} unmodifieable terms are processed as well, if {@code false}
     *            they will be not replaced.
     * @return The preprocessed document.
     */
    public static final Document deepPPWithTermMapping(final Document document, final Map<Term, Term> termMapping,
        final boolean preprocessUnmodifieable) {
        final DocumentBuilder builder = new DocumentBuilder(document);
        for (final Section s : document.getSections()) {
            for (final Paragraph p : s.getParagraphs()) {
                for (final Sentence sen : p.getSentences()) {
                    for (final Term t : sen.getTerms()) {
                        // preprocess only if term is not unmodifieable or flag ist set true
                        if (!t.isUnmodifiable() || preprocessUnmodifieable) {
                            // if term mapping exists use mapping
                            if (termMapping.containsKey(t)) {
                                final Term mappedTerm = termMapping.get(t);
                                if (mappedTerm != null && mappedTerm.getText().length() > 0) {
                                    builder.addTerm(mappedTerm);
                                }
                            // if no mapping exists add old term
                            } else {
                                builder.addTerm(t);
                            }
                        // if term must not be preprocessed add old term
                        } else {
                            builder.addTerm(t);
                        }
                    }
                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        return builder.createDocument();
    }
}

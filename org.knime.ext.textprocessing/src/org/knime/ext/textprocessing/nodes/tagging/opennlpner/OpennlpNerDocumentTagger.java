/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinder;
import opennlp.tools.util.Span;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpennlpNerDocumentTagger extends AbstractDocumentTagger {

    private TokenNameFinder m_tagger;

    private OpenNlpModel m_model;


    /**
     * Creates a new instance of <code>OpennlpNerDocumentTagger</code> with
     * given unmodifiable flag and the model file to use.
     *
     * @param setNeUnmodifiable The flag specifying whether found named entities
     * will be set unmodifiable or not.
     * @param modelType The type of the specified model (person, time,
     * organization, etc.).
     * @param modelFileName Use of model file.
     * @throws IOException If something happens.
     * @since 2.7
     */
    public OpennlpNerDocumentTagger(final boolean setNeUnmodifiable,
            final String modelType, final String modelFileName)
    throws IOException {
        super(setNeUnmodifiable);
        if (modelType == null) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model type may not be null!");
        }
        if (modelFileName == null) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model file may not be null!");
        }
        File f = new File(modelFileName);
        if (!f.exists() || !f.canRead() || !f.isFile()) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model file is not valid!");
        }

        m_model = new OpenNlpModel(modelType, modelFileName,
                    OpenNlpModelFactory.getInstance().getTagByName(modelType));
        m_tagger = new NameFinderME(m_model.getModel());
    }

    /**
     * Creates a new instance of <code>OpennlpNerDocumentTagger</code> with
     * given unmodifiable flag and model to tag with.
     *
     * @param setNeUnmodifiable The flag specifying whether found named entities
     * will be set unmodifiable or not.
     * @param model The model to tag with.
     * @throws IOException If something happens.
     * @since 2.7
     */
    public OpennlpNerDocumentTagger(final boolean setNeUnmodifiable,
            final OpenNlpModel model)
    throws IOException {
        this(setNeUnmodifiable, model, null);
    }

    /**
     * Creates a new instance of <code>OpennlpNerDocumentTagger</code> with
     * given unmodifiable flag and model to tag with.
     *
     * @param setNeUnmodifiable The flag specifying whether found named entities
     * will be set unmodifiable or not.
     * @param model The model to tag with.
     * @param dictFileName Use of dictionary file is not supported anymore
     * @throws IOException If something happens.
     * @deprecated Use <code>public OpennlpNerDocumentTagger(
     * final boolean setNeUnmodifiable, final OpenNlpModel model)</code>.
     */
    @Deprecated
    public OpennlpNerDocumentTagger(final boolean setNeUnmodifiable,
            final OpenNlpModel model, final String dictFileName)
    throws IOException {
        super(setNeUnmodifiable);
        if (model == null) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model may not be null!");
        }
        m_model = model;
        m_tagger = new NameFinderME(m_model.getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(NamedEntityTag.stringToTag(tag));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<String> words = new ArrayList<String>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                words.add(w.getWord());
            }
        }
        String [] wordsArr = words.toArray(new String[0]);

        Span[] spans = m_tagger.find(wordsArr);
        List<TaggedEntity> nes = new ArrayList<TaggedEntity>();

        for (Span span : spans) {
            int start = span.getStart();
            int end = span.getEnd();

            String namedEntity = "";
            for (int i = start; i < end; i++) {
                namedEntity += wordsArr[i];
                if (i < end - 1) {
                    namedEntity += Term.WORD_SEPARATOR;
                }
            }
            TaggedEntity te = new TaggedEntity(namedEntity, m_model.getTag());
            nes.add(te);
        }

        return nes;
    }
}

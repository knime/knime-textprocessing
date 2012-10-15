/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

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

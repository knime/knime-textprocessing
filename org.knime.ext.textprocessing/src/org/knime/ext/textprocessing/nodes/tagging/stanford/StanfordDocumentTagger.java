/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 * ---------------------------------------------------------------------
 *
 * History
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanford;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.FrenchTreebankTag;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.STTSPartOfSpeechTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
import org.knime.ext.textprocessing.util.StanfordModelPaths;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * The POS tagger node adds part of speech (POS) tags to terms of documents.
 * Here the Penn Treebank part-of-speech tag set is used to define all kinds
 * of tags, see {@link org.knime.ext.textprocessing.data.PartOfSpeechTag} for
 * more details. The underlying tagger model which is used to choose the
 * (hopefully) proper tags for all the terms is an external model of the
 * Stanford framework, see (http://nlp.stanford.edu/software/tagger.shtml)
 * for more details.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StanfordDocumentTagger extends AbstractDocumentTagger {

    /**
     * Contains the paths to the models.
     */
    private static final StanfordModelPaths PATHS =
        StanfordModelPaths.getStanfordModelPaths();

    /**
     * The tagger model names and their corresponding model file.
     */
    public static final Hashtable<String, String> TAGGERMODELS =
        new Hashtable<String, String>();
    static {
        TAGGERMODELS.put("English bidirectional",
                PATHS.getEnglishBidirectionalPosModelFile());
        TAGGERMODELS.put("English left 3 words",
                PATHS.getEnglishLeft3WordsPosModelFile());
        TAGGERMODELS.put("English left 3 words caseless",
                PATHS.getEnglishLeft3WordsCaselessPosModelFile());
        TAGGERMODELS.put("German fast",
                PATHS.getGermanFastPosModelFile());
        TAGGERMODELS.put("German hgc",
                PATHS.getGermanHgcPosModelFile());
        TAGGERMODELS.put("German dewac",
                         PATHS.getGermanDewacPosModelFile());
        TAGGERMODELS.put("French",
                         PATHS.getFrenchPosModelFile());
    }

    private enum Language {
        ENGLISH,
        GERMAN,
        FRENCH,
        ARABIC;
    }

    private MaxentTagger m_tagger;

    private String m_modelName;

    private Language m_lang;

    /**
     * Creates a new instance of StanfordDocumentTagger and loads internally the
     * specified tagging model of the Stanford library to POS tag the
     * documents. If <code>setNeUnmodifiable</code> is set <code>true</code>
     * all recognized terms are set to unmodifiable.
     *
     * @param setNeUnmodifiable If true all recognized terms are set
     * unmodifiable.
     * @param model The model to load and use.
     * @throws ClassNotFoundException If model can not be found.
     * @throws IOException If model can not be red.
     */
    public StanfordDocumentTagger(final boolean setNeUnmodifiable,
            final String model)
    throws ClassNotFoundException, IOException {
        super(setNeUnmodifiable);
        if (!TAGGERMODELS.containsKey(model)) {
            throw new IllegalArgumentException("Model \"" + model
                    + "\" does not exists.");
        }
        m_tagger = new MaxentTagger(TAGGERMODELS.get(model));
        m_modelName = model;

        if (m_modelName.contains("German")) {
            m_lang = Language.GERMAN;
        } else if (m_modelName.contains("English")) {
            m_lang = Language.ENGLISH;
        } else if (m_modelName.contains("French")) {
            m_lang = Language.FRENCH;
        } else {
            throw new IllegalArgumentException(
                    "Language could not be specified from model!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        if (m_lang.equals(Language.GERMAN)) {
            tags.add(STTSPartOfSpeechTag.stringToTag(tag));
        } else if (m_lang.equals(Language.FRENCH)) {
            tags.add(FrenchTreebankTag.stringToTag(tag));
        } else {
            tags.add(PartOfSpeechTag.stringToTag(tag));
        }
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<HasWord> wordList = new ArrayList<HasWord>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                wordList.add(new edu.stanford.nlp.ling.Word(w.getText()));
            }
        }
        ArrayList<TaggedWord> taggedWords = m_tagger.tagSentence(wordList);

        List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>(
                wordList.size());
        for (TaggedWord tw : taggedWords) {
            taggedEntities.add(new TaggedEntity(tw.word(), tw.tag()));
        }
        return taggedEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }
}

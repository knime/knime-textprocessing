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
 * The POS tagger node adds part of speech (POS) tags to terms of documents. Here the Penn Treebank part-of-speech tag
 * set is used to define all kinds of tags, see {@link org.knime.ext.textprocessing.data.PartOfSpeechTag} for more
 * details. The underlying tagger model which is used to choose the (hopefully) proper tags for all the terms is an
 * external model of the Stanford framework, see (http://nlp.stanford.edu/software/tagger.shtml) for more details.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StanfordDocumentTagger extends AbstractDocumentTagger {

    /**
     * Contains the paths to the models.
     */
    private static final StanfordModelPaths PATHS = StanfordModelPaths.getStanfordModelPaths();

    /**
     * The tagger model names and their corresponding model file.
     */
    public static final Hashtable<String, String> TAGGERMODELS = new Hashtable<>();

    static {
        TAGGERMODELS.put("English bidirectional", PATHS.getEnglishBidirectionalPosModelFile());
        TAGGERMODELS.put("English left 3 words", PATHS.getEnglishLeft3WordsPosModelFile());
        TAGGERMODELS.put("English left 3 words caseless", PATHS.getEnglishLeft3WordsCaselessPosModelFile());
        TAGGERMODELS.put("German fast", PATHS.getGermanFastPosModelFile());
        TAGGERMODELS.put("German hgc", PATHS.getGermanHgcPosModelFile());
        TAGGERMODELS.put("German dewac", PATHS.getGermanDewacPosModelFile());
        TAGGERMODELS.put("French", PATHS.getFrenchPosModelFile());
    }

    private enum Language {
            ENGLISH, GERMAN, FRENCH, ARABIC;
    }

    private MaxentTagger m_tagger;

    private String m_modelName;

    private Language m_lang;

    /**
     * Creates a new instance of StanfordDocumentTagger and loads internally the specified tagging model of the Stanford
     * library to POS tag the documents. If <code>setNeUnmodifiable</code> is set <code>true</code> all recognized terms
     * are set to unmodifiable.
     *
     * @param setNeUnmodifiable If true all recognized terms are set unmodifiable.
     * @param model The model to load and use.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @throws ClassNotFoundException If model cannot be found.
     * @throws IOException If model cannot be red.
     * @since 3.3
     */
    public StanfordDocumentTagger(final boolean setNeUnmodifiable, final String model, final String tokenizerName)
        throws ClassNotFoundException, IOException {
        super(setNeUnmodifiable, tokenizerName);
        if (!TAGGERMODELS.containsKey(model)) {
            throw new IllegalArgumentException("Model \"" + model + "\" does not exists.");
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
            throw new IllegalArgumentException("Language could not be specified from model!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<>();
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
        List<HasWord> wordList = new ArrayList<>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                wordList.add(new edu.stanford.nlp.ling.Word(w.getText()));
            }
        }
        final List<TaggedWord> taggedWords = m_tagger.tagSentence(wordList);

        final List<TaggedEntity> taggedEntities = new ArrayList<>(wordList.size());
        for (final TaggedWord tw : taggedWords) {
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

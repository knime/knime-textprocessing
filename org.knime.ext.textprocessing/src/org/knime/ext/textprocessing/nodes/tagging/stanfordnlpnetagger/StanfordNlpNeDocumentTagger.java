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
 *   30.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModel;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModelRegistry;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;

/**
 * The StanfordNLP NE tagger node adds named entity recognition (NER) tags to terms of documents. Here the Named entity
 * tag set is used to define all kinds of tags, see {@link org.knime.ext.textprocessing.data.NamedEntityTag} for more
 * details. The underlying tagger model which is used to choose the (hopefully) proper tags for all the terms is an
 * external model of the Stanford framework, see (https://nlp.stanford.edu/software/CRF-NER.html) for more details.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeDocumentTagger extends AbstractDocumentTagger {

    private AbstractSequenceClassifier<CoreLabel> m_tagger;

    private Tag m_tag;

    private StanfordTaggerModel m_model;

    private boolean m_combineMultiWords;

    private static final String TOKEN_SEPARATOR = " ";

    /**
     * Creates a new instance of {@code StanfordNlpNeDocumentTagger}.
     *
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param combineMultiWords The multi-word combination flag.
     * @param modelName The model.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @throws ClassNotFoundException If the classifier could not be created from given path.
     * @throws IOException If the classifier could not be created from given path.
     * @since 3.4
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final boolean combineMultiWords,
        final String modelName, final String tokenizerName) throws ClassNotFoundException, IOException {
        super(setNeUnmodifiable, tokenizerName);
        if (!StanfordTaggerModelRegistry.getInstance().getNerTaggerModelMap().containsKey(modelName)) {
            throw new IllegalArgumentException("Model \"" + modelName + "\" does not exist.");
        }
        m_combineMultiWords = combineMultiWords;
        m_model = StanfordTaggerModelRegistry.getInstance().getNerTaggerModelMap().get(modelName);
        m_tagger = CRFClassifier.getClassifier(m_model.getModelPath());
    }

    /**
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param combineMultiWords The multi-word combination flag.
     * @param crf The CRFClassifer.
     * @param tag The used tag.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.4
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final boolean combineMultiWords,
        final CRFClassifier<CoreLabel> crf, final Tag tag, final String tokenizerName) {
        super(setNeUnmodifiable, tokenizerName);
        m_combineMultiWords = combineMultiWords;
        m_tagger = crf;
        m_tag = tag;
    }

    /**
     * Creates a new instance of {@code StanfordNlpNeDocumentTagger}. The combination of multi-words is set to
     * {@code false} by default.
     *
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param modelName The model.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @throws ClassNotFoundException If the classifier could not be created from given path.
     * @throws IOException If the classifier could not be created from given path.
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final String modelName,
        final String tokenizerName) throws ClassNotFoundException, IOException {
        this(setNeUnmodifiable, false, modelName, tokenizerName);
    }

    /**
     * Creates a new instance of {@code StanfordNlpNeDocumentTagger}. The combination of multi-words is set to
     * {@code false} by default.
     *
     * @param setNeUnmodifiable The unmodifiable flag.
     * @param crf The CRFClassifer.
     * @param tag The used tag.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     */
    public StanfordNlpNeDocumentTagger(final boolean setNeUnmodifiable, final CRFClassifier<CoreLabel> crf,
        final Tag tag, final String tokenizerName) {
        this(setNeUnmodifiable, false, crf, tag, tokenizerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags;
        if (m_tag == null) {
            tags = m_model.getTags(tag);
        } else {
            tags = new ArrayList<Tag>(1);
            tags.add(m_tag);
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
        List<CoreLabel> coreLabelList = edu.stanford.nlp.ling.SentenceUtils.toCoreLabelList(wordList);
        List<CoreLabel> taggedWords = m_tagger.classifySentence(coreLabelList);

        List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>();
        CoreLabel previousCl = null;
        int count = 0;
        for (CoreLabel tw : taggedWords) {
            // getting the tag from current CoreLabel
            String answer = tw.getString(CoreAnnotations.AnswerAnnotation.class);
            // checking if tag is available (O means no tag)
            if (!answer.equals("O")) {
                // checking if successive terms with same tags should be combined to one term
                if (m_combineMultiWords) {
                    // check if a previous CoreLabel with tag exists
                    if (previousCl == null) {
                        previousCl = tw;
                    } else {
                        // check if tag from previous corelabel matches tag from current corelabel and combine them
                        if (answer.equals(previousCl.getString(CoreAnnotations.AnswerAnnotation.class))) {
                            String prevValue = previousCl.getString(CoreAnnotations.ValueAnnotation.class);
                            String currentValue = tw.getString(CoreAnnotations.ValueAnnotation.class);
                            CoreLabel combinedCl = tw;
                            combinedCl.setValue(prevValue + TOKEN_SEPARATOR + currentValue);
                            previousCl = combinedCl;
                        } else {
                            addTaggedEntity(taggedEntities, previousCl);
                            previousCl = tw;
                        }
                        // add tagged entity if tagged term is the last one of the sentence
                        if (count == taggedWords.size() - 1) {
                            addTaggedEntity(taggedEntities, previousCl);
                        }
                    }
                } else {
                    addTaggedEntity(taggedEntities, tw);
                }
            } else {
                if (previousCl != null) {
                    addTaggedEntity(taggedEntities, previousCl);
                }
                previousCl = null;
            }
            count++;
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

    /**
     * Create a tagged entity from a CoreLabel and adds it to the tagged entity list.
     *
     * @param taggedEntities The list of tagged entities.
     * @param taggedWord The CoreLabel to build a new tagged entity from.
     * @since 3.4
     */
    private void addTaggedEntity(final List<TaggedEntity> taggedEntities, final CoreLabel taggedWord) {
        taggedEntities.add(new TaggedEntity(taggedWord.getString(CoreAnnotations.ValueAnnotation.class),
            taggedWord.getString(CoreAnnotations.AnswerAnnotation.class)));
    }
}

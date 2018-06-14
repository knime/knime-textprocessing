/*
 * ------------------------------------------------------------------------
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
 *   30.04.2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn.NamedEntityMatcher;
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * The {@code MultipleTagsetDocumentTagger} implements the {@link DocumentTagger} interface. This implementation is used
 * for nodes that tag documents with different document tagger (e.g. Dictionary Tagger (Multi Column) node). The
 * specific tagging method comes from a specific {@link SentenceTagger} implementation which is passed to the
 * constructor of the {@link MultipleTagsetDocumentTagger}.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
public class MultipleTagsetDocumentTagger implements DocumentTagger {
    /**
     * The unmodifiable flag.
     */
    protected final boolean m_setNeUnmodifiable;

    /**
     * Initialize old standard word tokenizer name for backwards compatibility.
     */
    protected String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * Initialize old standard word tokenizer for backwards compatibility.
     */
    protected Tokenizer m_wordTokenizer =
        DefaultTokenization.getWordTokenizer(TextprocessingPreferenceInitializer.tokenizerName());

    private final SentenceTagger m_sentenceTagger;

    /**
     * Constructor of {@code MultipleTagsetDocumentTagger} with the given flags specifying if recognized named entities
     * have to be set unmodifiable or not and which word tokenizer should be used. Additionally, a specific
     * implementation of the {@link SentenceTagger} is needed, which contains the tagging function as well as some other
     * tagging properties.
     *
     * @param setUnmodifiable Set {@code true}, if tagged terms should be set to unmodifiable, otherwise false.
     * @param sentenceTagger An instance of a specific {@code SentenceTagger} implementation.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     */
    public MultipleTagsetDocumentTagger(final boolean setUnmodifiable, final SentenceTagger sentenceTagger,
        final String tokenizerName) {
        m_tokenizerName = tokenizerName;
        m_setNeUnmodifiable = setUnmodifiable;
        m_sentenceTagger = sentenceTagger;
        m_wordTokenizer = DefaultTokenization.getWordTokenizer(tokenizerName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Document tag(final Document doc) {
        DocumentBuilder db = new DocumentBuilder(doc, m_tokenizerName);
        for (Section s : doc.getSections()) {
            for (Paragraph p : s.getParagraphs()) {
                List<Sentence> newSentenceList = new ArrayList<>();
                for (Sentence sn : p.getSentences()) {
                    final Sentence taggedSentence;
                    if (sn.getTerms().isEmpty()) {
                        //do not try to tag empty sentences but keep the original one
                        //to prevent exceptions in the taggers
                        taggedSentence = sn;
                    } else {
                        // tag sentence
                        taggedSentence = tagSentence(sn);
                    }
                    // add tagged sentence to document
                    newSentenceList.add(taggedSentence);
                }
                db.addParagraph(new Paragraph(newSentenceList));
            }
            db.createNewSection(s.getAnnotation());
        }
        return db.createDocument();
    }

    /**
     * Tags a {@code Sentence}.
     *
     * @param s The Sentence to tag.
     * @return Returns the tagged sentence.
     */
    private final Sentence tagSentence(final Sentence s) {
        // detect named entities and return a list of MultipleTaggedEntities
        final List<MultipleTaggedEntity> entities = m_sentenceTagger.tagEntities(s);
        if (entities.isEmpty()) {
            return s;
        }

        // Collect words and terms
        List<Term> termList = s.getTerms();

        // go through all recognized named entities and rearrange terms
        for (MultipleTaggedEntity entity : entities) {
            // build new term list with old term list, words of detected named
            // entities and entity tag.
            termList = buildTermList(termList, entity);
        }

        return new Sentence(termList);
    }

    /**
     * Builds the term list with newly tagged entities.
     *
     * @param oldTermList The term list containing terms from the original document.
     * @param entity The entity to be tagged.
     * @return Returns a list of terms containing the newly tagged terms.
     */
    private final List<Term> buildTermList(final List<Term> oldTermList, final MultipleTaggedEntity entity) {
        // Won't this blow up if we have "a,b,a" as an entity and "ababab" as a
        // sentence?
        Map<IndexRange, List<Tag>> rangesAndTags = findNe(oldTermList, entity);

        // if new list contains no entities to look up for return old list
        // so that no tag is assigned to empty entities.
        if (rangesAndTags.isEmpty()) {
            return oldTermList;
        }

        final List<Term> newTermList = new LinkedList<>();

        // list to save term representing named entity at.
        List<Word> namedEntity = new ArrayList<>();

        boolean endTerm = false;

        // go through all the old term list
        int termIndex = -1;
        for (Term term : oldTermList) {
            termIndex++;

            int numberOfWords = term.getWords().size();

            int entryIndex = 0;
            boolean newTermAdded = false;
            boolean newTermIsBuilt = false;
            List<Tag> tags = new ArrayList<>(term.getTags());
            int numberOfEntries = rangesAndTags.size();
            for (Entry<IndexRange, List<Tag>> entry : rangesAndTags.entrySet()) {
                // get tags for current index range

                List<Tag> newTags = entry.getValue();
                // only add tag if not already added
                for (Tag tag : newTags) {
                    if (!tags.contains(tag)) {
                        tags.add(tag);
                    }
                }

                Term newTerm = null;

                // if we reached the interesting terms containing the named
                // entity
                if (termIndex >= entry.getKey().getStartTermIndex() && termIndex <= entry.getKey().getStopTermIndex()) {

                    // detected a named entity consisting only of one term
                    // check if it has to be split up
                    if (entry.getKey().getStartTermIndex() == entry.getKey().getStopTermIndex()) {

                        //
                        // BUT does the term consist of one or more words ?
                        // If it consists of more words, it has to be split up,
                        // if it consists only of one word just add the tags.
                        //

                        // the old term consists only of one word, so just add a tag
                        // By the way, this is the _only_ situation an old tag is
                        // kept and also assigned to the new term.
                        if (numberOfWords == 1) {
                            // CREATE NEW TERM !!!
                            newTerm = new Term(term.getWords(), tags, m_setNeUnmodifiable);
                            newTermList.add(newTerm);
                            // the old term consists of more than one word so split
                            // it
                        } else if (numberOfWords > 1) {
                            List<Word> newWords = new ArrayList<>();

                            int w = -1;
                            for (Word word : term.getWords()) {
                                w++;

                                // add word if index matches
                                if (w >= entry.getKey().getStartWordIndex() && w <= entry.getKey().getStopWordIndex()) {
                                    newWords.add(word);

                                    // if last word to add, create term and add it
                                    // to new list
                                    if (w == entry.getKey().getStopWordIndex()) {
                                        // CREATE NEW TERM !!!
                                        newTerm = new Term(newWords, newTags, m_setNeUnmodifiable);
                                        newTermList.add(newTerm);
                                        endTerm = true;
                                    }

                                    // if word is not part of the named entity add
                                    // it as
                                    // a term.
                                } else {
                                    List<Word> newWord = new ArrayList<>();
                                    newWord.add(word);
                                    tags = new ArrayList<>();
                                    // CREATE NEW TERM !!!
                                    newTerm = new Term(newWord, tags, false);
                                    newTermList.add(newTerm);
                                }
                            }
                        }

                        // reset new named entity list and go to the next found
                        // named entity range
                        entryIndex++;
                        if (entryIndex < numberOfEntries) {
                            namedEntity.clear();
                        }

                        // entity consists of more than one term, so split it up.
                    } else {
                        List<Word> words = term.getWords();

                        int w = -1;
                        for (Word word : words) {
                            w++;

                            // if current term is start term
                            if (termIndex == entry.getKey().getStartTermIndex()) {
                                // if word is part of the named entity add it
                                if (w >= entry.getKey().getStartWordIndex()) {
                                    namedEntity.add(word);
                                    newTermIsBuilt = true;

                                    // otherwise create a new term containing the
                                    // word
                                } else {
                                    List<Word> newWord = new ArrayList<>();
                                    newWord.add(word);
                                    tags = new ArrayList<>();
                                    // CREATE NEW TERM !!!
                                    newTerm = new Term(newWord, newTags, false);
                                    newTermList.add(newTerm);
                                }

                                // if current term is stop term
                            } else if (termIndex == entry.getKey().getStopTermIndex()) {
                                // add words as long as stopWordIndex is not reached
                                if (w <= entry.getKey().getStartWordIndex()) {
                                    namedEntity.add(word);

                                    // if last word is reached, create term and
                                    // add it
                                    if (w == entry.getKey().getStopWordIndex()) {
                                        // CREATE NEW TERM !!!
                                        newTerm = new Term(namedEntity, newTags, m_setNeUnmodifiable);
                                        newTermList.add(newTerm);
                                    }

                                    // otherwise create a term for each word
                                } else {
                                    List<Word> newWord = new ArrayList<>();
                                    newWord.add(word);
                                    tags = new ArrayList<>();
                                    // CREATE NEW TERM !!!
                                    newTerm = new Term(newWord, tags, false);
                                    newTermList.add(newTerm);
                                }

                                // if we are in between the start term and the stop
                                // term just add all words to the new word list
                            } else if (termIndex > entry.getKey().getStartTermIndex()
                                && termIndex < entry.getKey().getStopTermIndex()) {
                                namedEntity.add(word);
                                newTermIsBuilt = true;
                            }
                        }
                        if (endTerm) {
                            // next found term range
                            endTerm = false;
                            entryIndex++;
                            if (entryIndex < numberOfEntries) {
                                namedEntity.clear();
                            }
                        }
                    }
                }
                if (newTerm != null) {
                    newTerm = null;
                    newTermAdded = true;
                }
            }
            if (!newTermAdded && !newTermIsBuilt) {
                // if we are before or after the interesting part just add the
                // terms without rearrangement
                newTermList.add(term);
            }
        }
        return newTermList;
    }

    /**
     * Finds named entities within a list of terms.
     *
     * @param sentence List of terms built from original sentence.
     * @param entity The {@code MultipleTaggedEntity} containing the name of the entity and properties how it has to be
     *            tagged.
     * @return Returns a map containing IndexRanges (location of found entities) and a list of
     *         {@code DocumentTaggerConfiguration}s containing properties for how the entity has to be tagged.
     */
    private final Map<IndexRange, List<Tag>> findNe(final List<Term> sentence, final MultipleTaggedEntity entity) {
        Map<IndexRange, List<Tag>> rangesAndTags = new LinkedHashMap<>();

        int startTermIndex = -1;
        int stopTermIndex = -1;
        int startWordIndex = -1;
        int stopWordIndex = -1;

        // named entity can contain one or more words, so they have to be
        // tokenized by the default tokenizer to create words out of them.
        List<String> neWords = m_wordTokenizer.tokenize(entity.getEntity());
        int numberOfNeWords = neWords.size();

        if (neWords.isEmpty()) {
            return rangesAndTags;
        }

        // search words of terms
        for (Entry<Tag, NamedEntityMatcher> entry : entity.getTagMatcherMap().entrySet()) {

            int foundIndex = 0;
            boolean foundFlag = false;
            int remainingWordsToGoBack = 0;
            boolean stepBack = false;

            // search all terms
            for (int termIndex = 0; termIndex < sentence.size(); termIndex++) {
                List<Word> wordsOfTerm = sentence.get(termIndex).getWords();
                int numberOfWords = wordsOfTerm.size();
                int wordsToGoForward = 0;

                if (stepBack) {
                    if (remainingWordsToGoBack - numberOfWords > 1) {
                        remainingWordsToGoBack = remainingWordsToGoBack - numberOfWords;
                        termIndex = termIndex - 2;
                        continue;
                    } else if (numberOfWords - remainingWordsToGoBack > 1) {
                        wordsToGoForward = numberOfWords - remainingWordsToGoBack;
                        remainingWordsToGoBack = 0;
                    }
                }

                for (int wordIndex = 0 + wordsToGoForward; wordIndex < numberOfWords; wordIndex++) {

                    // prepare word and ne for comparison (convert to lower case
                    // if case sensitivity is switched off)
                    String wordStr = wordsOfTerm.get(wordIndex).getWord();
                    String neStr = neWords.get(foundIndex);

                    // if ne element at "found" equals the current word
                    if (foundIndex < numberOfNeWords
                        && entry.getValue().matchWithWord(neStr, wordStr)) {
                        // if "found" 0 means we are at the beginning of the named
                        // entity
                        if (foundIndex == 0) {
                            startTermIndex = termIndex;
                            startWordIndex = wordIndex;
                        }

                        foundIndex++;
                        foundFlag = true;

                        // means we are at the end of the named entity
                        if (foundIndex == numberOfNeWords) {
                            stopTermIndex = termIndex;
                            stopWordIndex = wordIndex;

                            IndexRange indexRange =
                                new IndexRange(startTermIndex, stopTermIndex, startWordIndex, stopWordIndex);
                            List<Tag> tags = new ArrayList<>();
                            if (rangesAndTags.containsKey(indexRange)) {
                                tags = rangesAndTags.get(indexRange);
                            }
                            tags.add(entry.getKey());
                            rangesAndTags.put(indexRange, tags);
                            foundFlag = false;
                            foundIndex = 0;
                        }
                    } else if (foundFlag) {
                        int numberOfMatches = foundIndex;
                        foundIndex = 0;
                        foundFlag = false;
                        stepBack = true;
                        boolean stepBackTerms = (wordIndex - numberOfMatches < -1);
                        if (!stepBackTerms) {
                            wordIndex = wordIndex - numberOfMatches;
                        } else {
                            remainingWordsToGoBack = -(wordIndex - numberOfMatches + 1);
                            termIndex = termIndex-2;
                        }
                    }
                }
            }
        }
        return rangesAndTags;
    }
}

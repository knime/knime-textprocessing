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

    /**
     * Specific {@code SentenceTagger} implementation.
     */
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
     * @param s The sentence to tag.
     * @return Returns the tagged sentence.
     */
    private final Sentence tagSentence(final Sentence s) {
        // detect named entities and return a list of MultipleTaggedEntities
        final List<MultipleTaggedEntity> entities = m_sentenceTagger.tagEntities(s);
        if (entities.isEmpty()) {
            return s;
        }

        // get initial terms
        final List<Term> initialTerms = s.getTerms();
        List<Term> termList = initialTerms;

        // go through all recognized named entities and rearrange terms
        for (MultipleTaggedEntity entity : entities) {
            // find index ranges for every entity as well as the tags that should be used for the index range
            Map<IndexRange, List<Tag>> rangesAndTags = findNe(termList, entity);

            // build a new rearranged term list based on every index range
            for (Map.Entry<IndexRange, List<Tag>> rangeAndTags : rangesAndTags.entrySet()) {
                termList = buildTermList(termList, rangeAndTags);
            }
        }

        // merge tags from initial term list to the new term list and return it
        return mergeTermLists(initialTerms, termList);
    }

    /**
     * Merges the tags of same terms of two term lists.
     *
     * @param initialTerms The previous term list.
     * @param newTerms The new term list.
     */
    private Sentence mergeTermLists(final List<Term> initialTerms, final List<Term> newTerms) {
        final ArrayList<Term> mergedTerms = new ArrayList<Term>(newTerms.size());

        int prevInitWordCnt = -1;
        int prevNewWordCnt = -1;

        int initWordCnt = 0;
        int newWordCnt = 0;

        int initTermIdx = 0;
        int newTermIdx = 0;

        final int initSize = initialTerms.size();
        final int newSize = newTerms.size();
        while (initTermIdx < initSize || newTermIdx < newSize) {
            if (initWordCnt <= newWordCnt) {
                prevInitWordCnt = initWordCnt;
                initWordCnt += initialTerms.get(initTermIdx).getWords().size();
                initTermIdx++;
            } else if (initWordCnt >= newWordCnt) {
                prevNewWordCnt = newWordCnt;
                final Term t = newTerms.get(newTermIdx);
                newWordCnt += t.getWords().size();
                mergedTerms.add(t);
                newTermIdx++;
            }
            // if initWordCnt == newWordCnt and prev counts are also equal it's the same term and they have
            // to be merge
            if (initWordCnt == newWordCnt && prevInitWordCnt == prevNewWordCnt) {
                mergeTerms(initialTerms, mergedTerms, initTermIdx - 1);
            }
        }
        return new Sentence(mergedTerms);
    }

    /**
     * Merges the tags of terms of initial term list to the terms of new term list to keep tags from incoming data table
     * available even if the term has been split and reassembled during tagging process.
     *
     * @param initialTerms List of {@code Term}s from initial term list.
     * @param mergedTerms List of merged terms {code Term}s.
     * @param termIndex The index of the term to be merged.
     */
    private void mergeTerms(final List<Term> initialTerms, final ArrayList<Term> mergedTerms, final int termIndex) {
        final Term t = mergedTerms.remove(mergedTerms.size() - 1);
        List<Tag> tags = new ArrayList<Tag>(t.getTags());
        // only add tag if not already added
        List<Tag> newTags = initialTerms.get(termIndex).getTags();
        int addToPosition = 0;
        for (Tag ct : newTags) {
            if (!tags.contains(ct)) {
                tags.add(addToPosition++, ct);
            }
        }
        // CREATE NEW TERM !!!
        mergedTerms.add(new Term(t.getWords(), tags, m_setNeUnmodifiable));
    }

    /**
     * Builds the term list with newly tagged entities.
     *
     * @param oldTermList The term list containing terms from the original document.
     * @param entry A map entry containing the an index range and a list of tags that should be applied to the index
     *            range.
     * @return Returns a list of terms containing the newly tagged terms.
     */
    private final List<Term> buildTermList(final List<Term> oldTermList, final Map.Entry<IndexRange, List<Tag>> entry) {

        final List<Term> newTermList = new LinkedList<>();

        // get the start and stop indices from the index range
        final int startTermIndex = entry.getKey().getStartTermIndex();
        final int stopTermIndex = entry.getKey().getStopTermIndex();
        final int startWordIndex = entry.getKey().getStartWordIndex();
        final int stopWordIndex = entry.getKey().getStopWordIndex();

        // list to save term representing named entity at.
        final List<Word> namedEntity = new ArrayList<>();

        // go through all terms of old term list
        int t = -1;
        for (Term term : oldTermList) {
            t++;

            if (t < startTermIndex || t > stopTermIndex) {
                // if we are before or after the interesting part just add the
                // terms without rearrangement
                newTermList.add(term);
            } else {
                // we have found the interesting term

                // easy case start = end
                if (startTermIndex == stopTermIndex) {

                    if (term.getWords().size() - 1 == stopWordIndex - startWordIndex) {
                        List<Tag> tags = new ArrayList<>();
                        tags.addAll(term.getTags());
                        // only add tag if not already added
                        List<Tag> newTags = entry.getValue();
                        for (Tag ct : newTags) {
                            if (!tags.contains(ct)) {
                                tags.add(ct);
                            }
                        }

                        // CREATE NEW TERM !!!
                        Term newTerm = new Term(term.getWords(), tags, m_setNeUnmodifiable);
                        newTermList.add(newTerm);
                    } else {
                        // our term contains only a subset of the words
                        // so we have to split it into several terms

                        List<Word> newWords = new ArrayList<>();
                        int w = -1;
                        for (Word word : term.getWords()) {
                            w++;
                            // if we are outside our range
                            if (w < startWordIndex || w > stopWordIndex) {
                                createSingleWordTerm(newTermList, word);
                            } else {
                                newWords.add(word);
                                // if last word to add, create term and add it
                                // to new list
                                if (w == stopWordIndex) {
                                    createMultiWordTerm(entry, newTermList, newWords);
                                }
                            }
                        }
                    }
                } else {
                    // entity consists of more than one term, so split it up.
                    List<Word> words = term.getWords();
                    int w = -1;
                    for (Word word : words) {
                        w++;

                        // if current term is start term
                        if (t == startTermIndex) {
                            // if word is part of the named entity add it
                            if (w >= startWordIndex) {
                                namedEntity.add(word);
                                // otherwise create a new term containing the
                                // word
                            } else {
                                createSingleWordTerm(newTermList, word);
                            }
                            // if current term is stop term
                        } else if (t == stopTermIndex) {
                            // add words as long as stopWordIndex is not reached
                            if (w <= stopWordIndex) {
                                namedEntity.add(word);

                                // if last word is reached, create term and
                                // add it
                                if (w == stopWordIndex) {
                                    createMultiWordTerm(entry, newTermList, namedEntity);
                                }
                                // otherwise create a term for each word
                            } else {
                                createSingleWordTerm(newTermList, word);
                            }
                            // if we are in between the start term and the stop
                            // term just add all words to the new word list
                        } else if (t > startTermIndex && t < stopTermIndex) {
                            namedEntity.add(word);
                        }
                    }
                }
            }
        }
        return newTermList;
    }

    /**
     * Creates a new {@code Term} built from multiple {@code words}.
     *
     * @param entry The entry containing the {@code Tag}
     * @param newTermList The term list containing the new terms.
     * @param newWords The word list containing the new words to built a new term.
     */
    private void createMultiWordTerm(final Map.Entry<IndexRange, List<Tag>> entry, final List<Term> newTermList,
        final List<Word> newWords) {
        List<Tag> tags = new ArrayList<>();
        // only add tag if not already added
        List<Tag> newTags = entry.getValue();
        for (Tag ct : newTags) {
            if (!tags.contains(ct)) {
                tags.add(ct);
            }
        }

        // CREATE NEW TERM !!!
        Term newTerm = new Term(newWords, tags, m_setNeUnmodifiable);
        newTermList.add(newTerm);
    }

    /**
     * Creates a new {@code Term} built from one {@code Word}.
     *
     * @param newTermList The term list containing the new terms.
     * @param word The word used to built a new term.
     */
    private void createSingleWordTerm(final List<Term> newTermList, final Word word) {
        List<Word> newWord = new ArrayList<>();
        newWord.add(word);
        List<Tag> tags = new ArrayList<>();
        // CREATE NEW TERM !!!
        Term newTerm = new Term(newWord, tags, false);
        newTermList.add(newTerm);
    }

    /**
     * TODO: Implement Knuth-Morris-Pratt algorithm and overthink the whole idea of how we find named-entities right
     * now.
     *
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
        // named entity can contain one or more words, so they have to be
        // tokenized by the default tokenizer to create words out of them.
        List<String> neWords = m_wordTokenizer.tokenize(entity.getEntity());
        final int neWordsSize = neWords.size();
        if (neWords.isEmpty()) {
            return rangesAndTags;
        }

        int startTermIdx;
        int startWordIdx;
        final ArrayList<SentenceEntry> sentenceEntries = new ArrayList<>();
        // this is the index of the word in neWords to be checked - 1!
        // Similar to the solution with found etc.
        for (Entry<Tag, NamedEntityMatcher> entry : entity.getTagMatcherMap().entrySet()) {
            int curEntryTokenIdx = -1;
            startTermIdx = -1;
            startWordIdx = -1;
            sentenceEntries.clear();
            final NamedEntityMatcher matcher = entry.getValue();
            for (int termIdx = 0, termEndIdx = sentence.size(); termIdx < termEndIdx; termIdx++) {
                List<Word> words = sentence.get(termIdx).getWords();
                for (int wordIdx = 0, wordEndIdx = words.size(); wordIdx < wordEndIdx; wordIdx++) {
                    final String wordStr = words.get(wordIdx).getWord();
                    if (matcher.matchWithWord(neWords.get(curEntryTokenIdx + 1), wordStr)) {
                        // do not add the first word that matches !!!
                        if (++curEntryTokenIdx != 0) {
                            sentenceEntries.add(new SentenceEntry(termIdx, wordIdx, wordStr));
                        }
                    } else if (curEntryTokenIdx >= 0) {
                        // search for a substring match (note this list does not contain the first word that matched
                        // i.e. neWord.get(0) is not eWord[entityIdx].get(0)!!
                        sentenceEntries.add(new SentenceEntry(termIdx, wordIdx, wordStr));
                        curEntryTokenIdx = -1;
                        // what we do here is go from left to right if we found a match for neWord 0 then we check
                        // the rest. If everything else matches we update the curEntryTokenIdx and the eWords!
                        // check String.indexOf(int ch, int fromIndex)
                        for (int i = 0, end = sentenceEntries.size(); i < end; i++) {
                            final SentenceEntry e = sentenceEntries.get(i);
                            if (matcher.matchWithWord(neWords.get(0), e.getWord())) {
                                if (matchRest(neWords, matcher, sentenceEntries, i, end)) {
                                    // redundant in case that i + 1 = end
                                    startTermIdx = e.getTermIdx();
                                    startWordIdx = e.getWordIdx();
                                    curEntryTokenIdx = end - i - 1;
                                    // probably a linked list would be better!?!? to check
                                    for (int j = 0; j <= i; j++) {
                                        sentenceEntries.remove(0);
                                    }
                                    // stop the loop
                                    i = end;
                                }
                            }
                        }
                    }
                    if (curEntryTokenIdx == 0) {
                        startTermIdx = termIdx;
                        startWordIdx = wordIdx;
                    }
                    if (curEntryTokenIdx + 1 == neWordsSize) {
                        sentenceEntries.clear();
                        IndexRange indexRange = new IndexRange(startTermIdx, termIdx, startWordIdx, wordIdx);
                        final List<Tag> tags;
                        if (rangesAndTags.containsKey(indexRange)) {
                            tags = rangesAndTags.get(indexRange);
                        } else {
                            tags = new ArrayList<>();
                        }
                        tags.add(entry.getKey());
                        rangesAndTags.put(indexRange, tags);
                        curEntryTokenIdx = -1;
                        startTermIdx = -1;
                        startWordIdx = -1;
                    }
                }
            }
        }
        return rangesAndTags;

    }

    /**
     * Matches the remaining elements of named-entity word list with entries of an {@code SentenceEntry} list.
     *
     * @param neWords The words from the named entity to be tagged.
     * @param matcher The {@code NamedEntityMatcher} to match words.
     * @param w A list of {@code SentenceEntry}s.
     * @param i The index of the {@code SentenceEntry}.
     * @param end Size of the {@code SentenceEntry} list.
     * @return True, if the rest of the named-entity words match the words from the list {@code SentenceEntry}s.
     */
    private boolean matchRest(final List<String> neWords, final NamedEntityMatcher matcher,
        final ArrayList<SentenceEntry> w, final int i, final int end) {
        for (int j = i + 1; j < end; j++) {
            if (!matcher.matchWithWord(neWords.get(j - i), w.get(j).getWord())) {
                return false;
            }
        }
        return true;
    }

    /**
     * An instance of {@code SentenceEntry} containing a word and the specific term and word indices where the word has
     * been matched.
     *
     * @author Julian Bunzel, KNIME.com GmbH, Berlin, Germany
     */
    private static final class SentenceEntry {

        /**
         * The position in the term list where the word has been found.
         */
        private final int m_termIdx;

        /**
         * The position in the word list of a term where the word has been found.
         */
        private final int m_wordIdx;

        /**
         * The found word.
         */
        private final String m_word;

        /**
         * Creates a new instance of {@code SentenceEntry}.
         *
         * @param termIdx The position in the term list where the word has been found.
         * @param wordIdx The position in the word list of a term where the word has been found.
         * @param word The found word.
         */
        SentenceEntry(final int termIdx, final int wordIdx, final String word) {
            m_termIdx = termIdx;
            m_wordIdx = wordIdx;
            m_word = word;
        }

        /**
         * Returns position in the term list where the word has been found.
         *
         * @return Returns position in the term list where the word has been found.
         */
        int getTermIdx() {
            return m_termIdx;
        }

        /**
         * The position in the word list of a term where the word has been found.
         *
         * @return The position in the word list of a term where the word has been found.
         */
        int getWordIdx() {
            return m_wordIdx;
        }

        /**
         * The found word.
         *
         * @return The found word.
         */
        String getWord() {
            return m_word;
        }
    }

}

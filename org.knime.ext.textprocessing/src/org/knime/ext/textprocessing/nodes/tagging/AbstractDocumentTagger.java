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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * The abstract class <code>AbstractDocumentTagger</code> implements the interface
 * {@link org.knime.ext.textprocessing.nodes.tagging.DocumentTagger} and additionally provides methods to tag documents
 * and change their term granularity conveniently. External libraries as well as internal implementations can be used
 * easily. The whole process of applying the new term granularity is done by this class internally. Classes extending
 * <code>AbstractDocumentTagger</code> on the one hand have to provide the procedure of tagging terms (or recognizing
 * named entities etc.) by the implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger#tagEntities(Sentence)} and on the other hand
 * they need to provide the proper tag type accordant to their tagging, i.e. POS tagger need to provide POS tag and so
 * on. Proper tags are provided by the implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger#getTags(String)} which is called by
 * <code>AbstractDocumentTagger</code> to add the tags to a recognized term. Underlying classes have to build the right
 * tag out of the given string.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class AbstractDocumentTagger implements DocumentTagger {

    /**
     * The unmodifiable flag.
     */
    protected boolean m_setNeUnmodifiable;

    /**
     * The case sensitive flag.
     */
    protected boolean m_caseSensitive = true;

    /**
     * The exact match flag.
     *
     * @since 2.8
     */
    protected boolean m_exactMatch = true;

    /**
     * Initialize old standard word tokenizer name for backwards compatibility.
     *
     * @since 3.3
     */
    protected String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * Initialize old standard word tokenizer for backwards compatibility.
     */
    protected Tokenizer m_wordTokenizer =
        DefaultTokenization.getWordTokenizer(TextprocessingPreferenceInitializer.tokenizerName());

    /**
     * Constructor of {@code AbstractDocumentTagger} with the given flag which specifies if recognized named entities
     * have to be set unmodifiable or not and the tokenizer used for word tokenization.
     *
     * @param setUnmodifiable It true recognized tags are set unmodifiable.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public AbstractDocumentTagger(final boolean setUnmodifiable, final String tokenizerName) {
        m_setNeUnmodifiable = setUnmodifiable;
        m_tokenizerName = tokenizerName;
        m_wordTokenizer = DefaultTokenization.getWordTokenizer(tokenizerName);
    }

    /**
     * Constructor of <code>AbstractDocumentTagger</code> with the given flags specifying if recognized named entities
     * have to be set unmodifiable or not and if search for named entities is case sensitive or not.
     *
     * @param setUnmodifiable If <code>true</code> recognized tags are set unmodifiable.
     * @param caseSensitive If <code>true</code> search for named entities is done case sensitive, otherwise not.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    public AbstractDocumentTagger(final boolean setUnmodifiable, final boolean caseSensitive,
        final String tokenizerName) {
        m_setNeUnmodifiable = setUnmodifiable;
        m_caseSensitive = caseSensitive;
        m_tokenizerName = tokenizerName;
        m_wordTokenizer = DefaultTokenization.getWordTokenizer(tokenizerName);
    }

    /**
     * Creates proper tags out if the given string accordant to the underlying tagger implementation and returns them. A
     * part of speech tagger (POS tagger) for instance creates POS tags, a biomedical named entity recognizer provides
     * biomedical named entity tags, such as GENE, PROTEIN etc.
     *
     * @param tag The string to create a tag out of.
     * @return The tags build out of the given string.
     */
    protected abstract List<Tag> getTags(final String tag);

    /**
     * Analysis the given sentences and recognized certain terms, such as parts of speech or biomedical named entities.
     * These terms and their corresponding tags are returned as a list.
     *
     * @param sentence The sentence to analyze.
     * @return A list of recognized entities and the corresponding tags.
     */
    protected abstract List<TaggedEntity> tagEntities(final Sentence sentence);

    /**
     * Preprocesses a document before tagging. This is where a tagger would build a private model to use for tagging
     * entities in the method tagEntities(Sentence).
     *
     * @param doc The document to tag.
     */
    protected abstract void preprocess(final Document doc);

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

    private Sentence tagSentence(final Sentence s) {
        // detect named entities
        List<TaggedEntity> entities = tagEntities(s);
        if (entities.isEmpty()) {
            return s;
        }

        // Collect words and terms
        List<Term> termList = s.getTerms();

        // go through all recognized named entities and rearrange terms
        for (TaggedEntity entity : entities) {
            // build new term list with old term list, words of detected named
            // entities and entity tag.
            termList = buildTermList(termList, findNe(termList, entity), entity.getTagString());
        }
        return new Sentence(termList);
    }

    /**
     * Builds the term list with newly tagged entities.
     *
     * @param oldTermList The term list containing initial terms.
     * @param ranges A list containing the index ranges.
     * @param tagValue The tag value.
     * @return Returns a list of terms containing the newly tagged terms.
     */
    private final List<Term> buildTermList(final List<Term> oldTermList, final List<IndexRange> ranges,
        final String tagValue) {

        if (ranges.isEmpty()) {
            return oldTermList;
        }

        final List<Term> newTermList = new LinkedList<>();
        final List<Word> namedEntity = new ArrayList<>();
        // go through all terms of old term list
        int termIdx = 0;
        int wordIdx = -1;

        for (IndexRange range : ranges) {
            // get the start and stop indices from the index range
            int startTermIndex = range.getStartTermIndex();
            int stopTermIndex = range.getStopTermIndex();
            int startWordIndex = range.getStartWordIndex();
            int stopWordIndex = range.getStopWordIndex();
            if (wordIdx != -1) {
                termIdx--;
                if (termIdx != startTermIndex) {
                    addRemainingWordsAsTerms(oldTermList, newTermList, termIdx, wordIdx);
                    wordIdx = -1;
                    termIdx++;
                }
            }
            while (termIdx < startTermIndex) {
                newTermList.add(oldTermList.get(termIdx));
                termIdx++;
            }
            if (startTermIndex == stopTermIndex) {
                final Term term = oldTermList.get(termIdx);
                if (term.getWords().size() - 1 == stopWordIndex - startWordIndex) {
                    List<Tag> tags = new ArrayList<>();
                    tags.addAll(term.getTags());
                    // only add tag if not already added
                    List<Tag> newTags = getTags(tagValue);
                    for (Tag ct : newTags) {
                        if (!tags.contains(ct)) {
                            tags.add(ct);
                        }
                    }
                    // create the new term
                    Term newTerm = new Term(term.getWords(), tags, m_setNeUnmodifiable);
                    newTermList.add(newTerm);
                } else {
                    // our term contains only a subset of the words
                    // so we have to split it into several terms

                    List<Word> newWords = new ArrayList<>();
                    List<Word> words = term.getWords();
                    for (wordIdx++; wordIdx < words.size(); wordIdx++) {
                        // if we are outside our range
                        if (wordIdx < startWordIndex) {
                            createSingleWordTerm(newTermList, words.get(wordIdx));
                        } else {
                            newWords.add(words.get(wordIdx));
                            // if last word to add, create term and add it
                            // to new list
                            if (wordIdx == stopWordIndex) {
                                createMultiWordTerm(tagValue, newTermList, newWords);
                                break;
                            }
                        }
                    }
                }
                ++termIdx;
            }
            while (termIdx <= stopTermIndex) {
                final Term term = oldTermList.get(termIdx);
                // entity consists of more than one term, so split it up.
                // if current term is start term
                if (termIdx == startTermIndex) {
                    List<Word> words = term.getWords();
                    for (wordIdx++; wordIdx < words.size() ; wordIdx++) {
                        // if word is part of the named entity add it
                        if (wordIdx >= startWordIndex) {
                            namedEntity.add(words.get(wordIdx));
                            // otherwise create a new term containing the
                            // word
                        } else {
                            createSingleWordTerm(newTermList, words.get(wordIdx));
                        }
                    }
                    wordIdx = -1;
                    // if current term is stop term
                } else if (termIdx == stopTermIndex) {
                    // add words as long as stopWordIndex is not reached
                    List<Word> words = term.getWords();
                    for (wordIdx++ ; wordIdx < words.size(); wordIdx++) {
                        if (wordIdx <= stopWordIndex) {
                            namedEntity.add(words.get(wordIdx));
                            // if last word is reached, create term and
                            // add it
                            if (wordIdx == stopWordIndex) {
                                createMultiWordTerm(tagValue, newTermList, namedEntity);
                                break;
                            }
                            // otherwise create a term for each word
                        }
                    }
                    // if we are in between the start term and the stop
                    // term just add all words to the new word list
                } else {
                    namedEntity.addAll(term.getWords());
                }
                termIdx++;
            }
        }
        if (wordIdx != -1) {
            termIdx--;
            addRemainingWordsAsTerms(oldTermList, newTermList, termIdx, wordIdx);
            termIdx++;
        }

        for (final int end = oldTermList.size(); termIdx < end; termIdx++) {
            newTermList.add(oldTermList.get(termIdx));
        }
        return newTermList;
    }

    /**
     * Adds remaining words of a term as single terms to the new term list.
     *
     * @param oldTermList The old term list containing the terms of the incoming sentence.
     * @param newTermList The new term list containing the processed and/or tagged terms.
     * @param termIdx The index of the term to be processed.
     * @param wordIdx The index of the word where the remaining words start within the term.
     */
    private static void addRemainingWordsAsTerms(final List<Term> oldTermList, final List<Term> newTermList,
        final int termIdx, int wordIdx) {
        final List<Word> words = oldTermList.get(termIdx).getWords();
        for (wordIdx++; wordIdx < words.size(); wordIdx++) {
            createSingleWordTerm(newTermList, words.get(wordIdx));
        }
    }

    /**
     * Creates a new {@code Term} built from multiple {@code words}.
     *
     * @param tagValue The tag value.
     * @param newTermList The term list containing the new terms.
     * @param newWords The word list containing the new words to built a new term.
     */
    private void createMultiWordTerm(final String tagValue, final List<Term> newTermList, final List<Word> newWords) {
        List<Tag> tags = new ArrayList<>();
        // only add tag if not already added
        List<Tag> newTags = getTags(tagValue);
        for (Tag ct : newTags) {
            if (!tags.contains(ct)) {
                tags.add(ct);
            }
        }

        // create the new term
        Term newTerm = new Term(new ArrayList<Word>(newWords), tags, m_setNeUnmodifiable);
        newTermList.add(newTerm);
        newWords.clear();
    }

    /**
     * Creates a new {@code Term} built from one {@code Word}.
     *
     * @param newTermList The term list containing the new terms.
     * @param word The word used to built a new term.
     */
    private static void createSingleWordTerm(final List<Term> newTermList, final Word word) {
        List<Word> newWord = new ArrayList<>();
        newWord.add(word);
        List<Tag> tags = new ArrayList<>();
        // create the new term
        Term newTerm = new Term(newWord, tags, false);
        newTermList.add(newTerm);
    }

    /**
     * Finds named entities within a list of terms.
     *
     * @param sentence List of terms built from original sentence.
     * @param entity The {@code MultipleTaggedEntity} containing the name of the entity and properties how it has to be
     *            tagged.
     * @return A map storing for each {@link IndexRange} the assigned {@link Tag Tags}.
     */
    private final List<IndexRange> findNe(final List<Term> sentence, final TaggedEntity entity) {
        // Time could be improved using Knuth-Morris-Pratt algorithm
        LinkedList<IndexRange> ranges = new LinkedList<>();
        // named entity can contain one or more words, so they have to be
        // tokenized by the default tokenizer to create words out of them.
        List<String> neWords = m_wordTokenizer.tokenize(entity.getEntity());
        final int neWordsSize = neWords.size();
        if (neWords.isEmpty()) {
            return ranges;
        }

        int startTermIdx;
        int startWordIdx;
        final ArrayList<SentenceEntry> sentenceEntries = new ArrayList<>();
        // this is the index of the word in neWords to be checked - 1!
        // Similar to the solution with found etc.
        int curEntryTokenIdx = -1;
        startTermIdx = -1;
        startWordIdx = -1;
        sentenceEntries.clear();
        final NamedEntityMatcher matcher = new NamedEntityMatcher(m_caseSensitive, m_exactMatch);
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
                    ranges.add(new IndexRange(startTermIdx, termIdx, startWordIdx, wordIdx));
                    curEntryTokenIdx = -1;
                    startTermIdx = -1;
                    startWordIdx = -1;
                }
            }
        }

        return ranges;
    }

    /**
     * Matches the remaining elements of a named-entity word list with entries of an {@code SentenceEntry} list.
     *
     * @param neWords The words from the named entity to be tagged.
     * @param matcher The {@code NamedEntityMatcher} to match words.
     * @param sentenceEntries A list of {@code SentenceEntry}s.
     * @param firstMatchIdx The index of the {@code SentenceEntry}.
     * @param end Size of the {@code SentenceEntry} list.
     * @return {@code True}, if the rest of the named-entity words match the words from the list {@code SentenceEntry}s.
     */
    private static boolean matchRest(final List<String> neWords, final NamedEntityMatcher matcher,
        final ArrayList<SentenceEntry> sentenceEntries, final int firstMatchIdx, final int end) {
        for (int j = firstMatchIdx + 1; j < end; j++) {
            if (!matcher.matchWithWord(neWords.get(j - firstMatchIdx), sentenceEntries.get(j).getWord())) {
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
         * @param termIdx The position in the term list where the word was found.
         * @param wordIdx The position in the word list of a term where the word was found.
         * @param word The found word.
         */
        SentenceEntry(final int termIdx, final int wordIdx, final String word) {
            m_termIdx = termIdx;
            m_wordIdx = wordIdx;
            m_word = word;
        }

        /**
         * Returns position in the term list where the word was found.
         *
         * @return Returns position in the term list where the word was found.
         */
        int getTermIdx() {
            return m_termIdx;
        }

        /**
         * The position in the word list of a term where the word was found.
         *
         * @return The position in the word list of a term where the word was found.
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

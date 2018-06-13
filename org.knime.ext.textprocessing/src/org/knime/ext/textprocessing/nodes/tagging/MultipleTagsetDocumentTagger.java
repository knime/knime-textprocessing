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
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
        final List<TaggedEntity> entities = m_sentenceTagger.tagEntities(s);
        if (entities.isEmpty()) {
            return s;
        }

        // Collect words and terms
        List<Term> termList = s.getTerms();

        //Map<String, Set<Tag>> occurringNamedEntities = new HashMap<>();
        //occurringNamedEntities = getNeMap(occurringNamedEntities, termList);

        // go through all recognized named entities and rearrange terms
        for (TaggedEntity entity : entities) {
            // build new term list with old term list, words of detected named
            // entities and entity tag.
            termList = buildNewTermList(termList, entity);
            //occurringNamedEntities = getNeMap(occurringNamedEntities, termList);
        }

        //termList = updateTermList(termList, occurringNamedEntities);

        return new Sentence(termList);
    }

    private final List<Term> buildNewTermList(final List<Term> oldTermList, final TaggedEntity entity) {
        // new term list that will be filled with terms to create new sentence
        final List<Term> newTermList = new LinkedList<>();

        // tokenized entity & number of tokenized entities
        final List<String> neWords = m_wordTokenizer.tokenize(entity.getEntity());
        // number of tokenized entities
        final int numberOfNeWords = neWords.size();

        if (numberOfNeWords == 0) {
            return oldTermList;
        }

        //        // set of tag & matcher combinations for the specific entity
        //        final Set<Map.Entry<Tag, NamedEntityMatcher>> tagMatcherSetEntrySet = entity.getTagMap().entrySet();
        //        // number of combinations
        //        final int numberOfTagMatcherEntries = tagMatcherSetEntrySet.size();

        // list of words used to create the term
        final List<Word> newWords = new ArrayList<>();
        // list of new tags for specific term
        //        List<Tag> newTags = new ArrayList<>(numberOfTagMatcherEntries);

        // list of indices; each position refers to one specific tag/matcher combination and indicates how many
        // word matches we have in a row for this entity
        int foundIndex = 0;
        // list of booleans; each position refers to one specific tag/matcher combination and indicates
        // if the last words matched
        boolean foundFlag = false;

        // set true if a new term has been added
        boolean newTermAdded = false;
        // set true if two words matched
        boolean wordFound = false;

        List<Tag> newTags = new ArrayList<>();

        // iterate over terms
        for (int termIndex = 0; termIndex < oldTermList.size(); termIndex++) {
            // get words of term and numer of words
            final List<Word> wordsOfTerm = oldTermList.get(termIndex).getWords();
            final int numberOfWords = wordsOfTerm.size();
            // get old tags of current term
            final List<Tag> oldTags = oldTermList.get(termIndex).getTags();
            // set true if we found multiple word matches but the following words didnt match, so the total named entity
            // has not been found and we have to step back to previous terms!
            boolean broke = false;

            // iterate over the words of the term
            for (int wordIndex = 0; wordIndex < numberOfWords; wordIndex++) {
                // add the word to the new words list
                newWords.add(wordsOfTerm.get(wordIndex));
                // get the current word as string
                String wordStr = wordsOfTerm.get(wordIndex).getWord();

                // get word from the named entity
                String neStr = neWords.get(foundIndex);

                // if named entity element at "foundIndex" equals the current word of the term
                if (foundIndex < numberOfNeWords && entity.getMatcher().matchWithWord(neStr, wordStr)) {
                    // set to true
                    wordFound = true;
                    // update foundIndex and foundFlag
                    foundIndex++;
                    foundFlag = true;

                    // means we are at the end of the named entity
                    if (foundIndex == numberOfNeWords) {
                        // reset foundFlag and foundIndex
                        foundFlag = false;
                        foundIndex = 0;

                        // add old tags to new tags if old word list and new word list are the same
                        if (!oldTags.isEmpty() && wordListsMatch(wordsOfTerm, newWords)) {
                            Set<Tag> setOfTags = new LinkedHashSet<>(oldTags);
                            setOfTags.add(entity.getTag());
                            newTags = new ArrayList<>(setOfTags);
                        } else {
                            newTags.add(entity.getTag());
                        }
                        // add term to term list
                        newTermList
                            .add(new Term(new ArrayList<>(newWords), new ArrayList<>(newTags), m_setNeUnmodifiable));
                        newTermAdded = true;
                        // clear lists
                        newWords.clear();
                        newTags.clear();

                    }
                    // means that we had a match for the previous words but not for the whole entity
                } else if (foundFlag) {
                    // remove the words for the latest matches from the new word list
                    for (int j = 0; j < foundIndex; j++) {
                        newWords.remove(newWords.size() - 1);
                    }
                    // means we are at the end of tag/matcher entries
                    // create term for remaining words and add to term list
                    if (wordListsMatch(wordsOfTerm, newWords)) {
                        newTermList.add(new Term(new ArrayList<>(newWords), oldTags, m_setNeUnmodifiable));
                    } else {
                        newTermList.add(new Term(new ArrayList<>(newWords), null, false));
                    }
                    newTermAdded = true;
                    newWords.clear();

                    // step back termIndex if we have to
                    if (wordIndex - foundIndex < -1) {
                        termIndex = termIndex - (-(wordIndex - foundIndex));
                        foundFlag = false;
                        foundIndex = 0;
                        newTermAdded = false;
                        // break, since we have to step back in term index
                        broke = true;
                        break;
                        // or step back in wordIndex
                    } else {
                        wordIndex = wordIndex - foundIndex;
                        foundFlag = false;
                        foundIndex = 0;
                    }

                }
                // means we are at the end of words and at the end of tag/matcher entries
                // and there was no match at all
                if (wordIndex == numberOfWords - 1 && !newTermAdded && !wordFound) {
                    // create term from words
                    if (wordListsMatch(wordsOfTerm, newWords)) {
                        newTermList.add(new Term(new ArrayList<>(newWords), oldTags, m_setNeUnmodifiable));
                    } else {
                        newTermList.add(new Term(new ArrayList<>(newWords), null, false));
                    }
                    newTermAdded = true;
                    newWords.clear();
                }
                // means we are at the end of tag/matcher entries and added a new term
                // reset values
                if (newTermAdded) {
                    newTermAdded = false;
                    wordFound = false;
                }
                // break again to get to the term loop if we broke before
                if (broke) {
                    break;
                }
            }
        }

        return newTermList;
    }

    private boolean wordListsMatch(final List<Word> list1, final List<Word> list2) {
        if (list1.size() != list2.size()) {
            return false;
        }
        for (int i = 0; i < list1.size(); i++) {
            if (!list1.get(i).getWord().equals(list2.get(i).getWord())) {
                return false;
            }
        }
        return true;
    }

//    private Map<String, Set<Tag>> getNeMap(final Map<String, Set<Tag>> occurringNamedEntities,
//        final List<Term> termList) {
//        Map<String, Set<Tag>> neMap = occurringNamedEntities;
//        for (Term term : termList) {
//            if (!term.getTags().isEmpty()) {
//                String termAsString = term.getText();
//                List<Tag> tags = term.getTags();
//                Set<Tag> setOfTags = new HashSet<>();
//                if (neMap.containsKey(termAsString)) {
//                    setOfTags = neMap.get(termAsString);
//                }
//                setOfTags.addAll(tags);
//                neMap.put(termAsString, setOfTags);
//            }
//        }
//        return neMap;
//    }
//
//    /**
//     * @param newTermList
//     * @param occurringNamedEntities
//     */
//    private List<Term> updateTermList(final List<Term> termList, final Map<String, Set<Tag>> occurringNamedEntities) {
//        List<Term> newTermList = new ArrayList<>();
//        Term newTerm = null;
//        for (Term term : termList) {
//            String termAsString = term.getText();
//            if (occurringNamedEntities.containsKey(termAsString)) {
//                newTerm = new Term(term.getWords(), new ArrayList<>(occurringNamedEntities.get(termAsString)),
//                    m_setNeUnmodifiable);
//                newTermList.add(newTerm);
//            } else {
//                newTermList.add(term);
//            }
//        }
//        return newTermList;
//    }
}

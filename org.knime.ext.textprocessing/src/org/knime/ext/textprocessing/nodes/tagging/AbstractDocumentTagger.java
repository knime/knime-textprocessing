/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
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

/**
 * The abstract class <code>AbstractDocumentTagger</code> implements the
 * interface {@link org.knime.ext.textprocessing.nodes.tagging.DocumentTagger}
 * and additionally provides methods to tag documents and change their term
 * granularity conveniently. External libraries as well as internal
 * implementations can be used easily. The whole process of applying the new
 * term granularity is done by this class internally. Classes extending
 * <code>AbstractDocumentTagger</code> on the one hand have to provide the
 * procedure of tagging terms (or recognizing named entities etc.) by the
 * implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger#tagEntities(Sentence)}
 * and on the other hand they need to provide the proper tag type accordant to
 * their tagging, i.e. POS tagger need to provide POS tag and so on. Proper tags
 * are provided by the implementation of
 * {@link org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger#getTags(String)}
 * which is called by <code>AbstractDocumentTagger</code> to add the tags to a
 * recognized term. Underlying classes have to build the right tag out of the
 * given string.
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
     * Constructor of <code>AbstractDocumentTagger</code> with the given flag
     * which specifies if recognized named entities have to be set unmodifiable
     * or not.
     *
     * @param setUnmodifiable It true recognized tags are set unmodifiable.
     */
    public AbstractDocumentTagger(final boolean setUnmodifiable) {
        m_setNeUnmodifiable = setUnmodifiable;
    }

    /**
     * Constructor of <code>AbstractDocumentTagger</code> with the given flags
     * specifying if recognized named entities have to be set unmodifiable or
     * not and if search for named entities is case sensitive or not.
     *
     * @param setUnmodifiable If <code>true</code> recognized tags are set
     *            unmodifiable.
     * @param caseSensitive If <code>true</code> search for named entities is
     *            done case sensitive, otherwise not.
     */
    public AbstractDocumentTagger(final boolean setUnmodifiable,
            final boolean caseSensitive) {
        m_setNeUnmodifiable = setUnmodifiable;
        m_caseSensitive = caseSensitive;
    }

    /**
     * Creates proper tags out if the given string accordant to the underlying
     * tagger implementation and returns them. A part of speech tagger (POS
     * tagger) for instance creates POS tags, a biomedical named entity
     * recognizer provides biomedical named entity tags, such as GENE, PROTEIN
     * etc.
     *
     * @param tag The string to create a tag out of.
     * @return The tags build out of the given string.
     */
    protected abstract List<Tag> getTags(final String tag);

    /**
     * Analysis the given sentences and recognized certain terms, such as parts
     * of speech or biomedical named entities. These terms and their
     * corresponding tags are returned as a list.
     *
     * @param sentence The sentence to analyze.
     * @return A list of recognized entities and the corresponding tags.
     */
    protected abstract List<TaggedEntity> tagEntities(final Sentence sentence);

    /**
     * Preprocesses a document before tagging. This is where a tagger would
     * build a private model for use in tagEntities(Sentence).
     * @param doc
     */
    protected abstract void preprocess(final Document doc);

    /**
     * {@inheritDoc}
     */
    public synchronized Document tag(final Document doc) {
        DocumentBuilder db = new DocumentBuilder(doc);
        for (Section s : doc.getSections()) {
            for (Paragraph p : s.getParagraphs()) {
                List<Sentence> newSentenceList = new ArrayList<Sentence>();
                for (Sentence sn : p.getSentences()) {
                    // tag sentence
                    Sentence taggedSentence = tagSentence(sn);
                    // add tagged sentence to document
                    newSentenceList.add(taggedSentence);
                }
                db.addParagraph(new Paragraph(newSentenceList));
            }
            db.createNewSection(s.getAnnotation());
        }
        return db.createDocument();
    }

    private final Comparator<List<String>> m_comparator =
        Collections.reverseOrder(new ListComparator<String>());

    private Sentence tagSentence(final Sentence s) {
        // detect named entities
        List<TaggedEntity> entities = tagEntities(s);
        if (entities.size() <= 0) {
            return s;
        }

        // tokenize them into words
        // sort by descending order of length to prioritize entities with more
        // words (this is done in case we have nested entities)
        SortedMap<List<String>, TaggedEntity> te =
            new TreeMap<List<String>, TaggedEntity>(m_comparator);

        for (TaggedEntity entity : entities) {
            te.put(DefaultTokenization.tokenizeSentence(
                    entity.getEntity()), entity);
        }

        // Collect words and terms
        List<Term> termList = s.getTerms();

        for (Entry<List<String>, TaggedEntity> e : te.entrySet()) {
            List<String> neWords = e.getKey();
            String tagstr = e.getValue().getTagString();

            // build a new term list with the old term list, words of detected
            // tagged entities and the associated tags
            termList = buildTermList(termList, neWords, tagstr);
        }

        return new Sentence(termList);
    }

    /**
     * Compares lists based on their size.
     *
     * @author Pierre-Francois Laquerre, University of Konstanz
     */
    private class ListComparator<T> implements Comparator<List<T>> {

        /**
         * {@inheritDoc}
         */
        public int compare(final List<T> l1, final List<T> l2) {
            return l1.size() - l2.size();
        }
    }

    private List<Term> buildTermList(final List<Term> oldTermList,
            final List<String> neWords, final String entityTag) {
        List<Term> newTermList = null;
        List<Term> oldList = oldTermList;

        // Won't this blow up if we have "a,b,a" as an entity and "ababab" as a
        // sentence?
        List<IndexRange> startStopRanges = findNe(oldList, neWords);

        if (startStopRanges.size() <= 0) {
            return oldList;
        }

        // if start >= 0 means that there is a named entity contained
        // in the list of terms, so the terms have to be rearranged.
        int startStopIndex = 0;
        newTermList = new ArrayList<Term>();

        // get the first start and stop indices
        int startTermIndex =
                startStopRanges.get(startStopIndex).getStartTermIndex();
        int stopTermIndex =
                startStopRanges.get(startStopIndex).getStopTermIndex();
        int startWordIndex =
                startStopRanges.get(startStopIndex).getStartWordIndex();
        int stopWordIndex =
                startStopRanges.get(startStopIndex).getStopWordIndex();

        boolean endTerm = false;
        // list to save term representing named entity at.
        List<Word> namedEntity = new ArrayList<Word>(neWords.size());

        // go through all the old term list
        for (int t = 0; t < oldList.size(); t++) {

            // if we reached the interesting terms containing the named
            // entity
            if (t >= startTermIndex && t <= stopTermIndex) {

                // detected a named entity consist only of one term
                // check if it has to be split up
                if (startTermIndex == stopTermIndex) {

                    //
                    // BUT does the term consist of of one or more words ?
                    // If it consists of more words, it has to be split up,
                    // if it consists only of one word just add the tags.
                    //

                    // the old term consists only of one word, so just add a tag
                    // By the way, this is the _only_ situation an old tag is
                    // kept and also assigned to the new term.
                    if (oldList.get(t).getWords().size() == 1) {
                        List<Tag> tags = new ArrayList<Tag>();
                        tags.addAll(oldList.get(t).getTags());
                        tags.addAll(getTags(entityTag));
                        // CREATE NEW TERM !!!
                        Term newTerm =
                                new Term(oldList.get(t).getWords(), tags,
                                        m_setNeUnmodifiable);
                        newTermList.add(newTerm);

                        // the old term consists of more than one word so split
                        // it
                    } else if (oldList.get(t).getWords().size() > 1) {
                        List<Word> newWords = new ArrayList<Word>();
                        for (int w = 0; w < oldList.get(t).getWords().size(); w++) {
                            // add word if index matches
                            if (w >= startWordIndex && w <= stopWordIndex) {
                                newWords.add(oldList.get(t).getWords().get(w));

                                // if last word to add, create term and add it
                                // to new list
                                if (w == stopWordIndex) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    tags.addAll(getTags(entityTag));
                                    // CREATE NEW TERM !!!
                                    Term newTerm =
                                            new Term(newWords, tags,
                                                    m_setNeUnmodifiable);
                                    newTermList.add(newTerm);
                                    endTerm = true;
                                }

                                // if word is not part of the named entity add
                                // it as
                                // a term.
                            } else {
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(oldList.get(t).getWords().get(w));
                                List<Tag> tags = new ArrayList<Tag>();
                                // CREATE NEW TERM !!!
                                Term newTerm = new Term(newWord, tags, false);
                                newTermList.add(newTerm);
                            }
                        }
                    }

                    // reset new named entity list and go to the next found
                    // named entity range
                    startStopIndex++;
                    if (startStopIndex < startStopRanges.size()) {
                        startTermIndex =
                                startStopRanges.get(startStopIndex)
                                        .getStartTermIndex();
                        stopTermIndex =
                                startStopRanges.get(startStopIndex)
                                        .getStopTermIndex();
                        startWordIndex =
                                startStopRanges.get(startStopIndex)
                                        .getStartWordIndex();
                        stopWordIndex =
                                startStopRanges.get(startStopIndex)
                                        .getStopWordIndex();
                        namedEntity.clear();
                    }

                    // entity consists of more than one term, so split it up.
                } else {
                    List<Word> words = oldList.get(t).getWords();
                    for (int w = 0; w < words.size(); w++) {

                        // if current term is start term
                        if (t == startTermIndex) {
                            // if word part if the named entity add it
                            if (w >= startWordIndex) {
                                namedEntity.add(words.get(w));

                                // otherwise create a new term containing the
                                // word
                            } else {
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(words.get(w));
                                List<Tag> tags = new ArrayList<Tag>();
                                // CREATE NEW TERM !!!
                                Term newTerm = new Term(newWord, tags, false);
                                newTermList.add(newTerm);
                            }

                            // if current term is stop term
                        } else if (w == stopWordIndex) {
                            // add words as long as stopWordIndex is not reached
                            if (w <= stopWordIndex) {
                                namedEntity.add(words.get(w));

                                // if last word is reached, create terma and
                                // add it
                                if (w == stopWordIndex) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    tags.addAll(getTags(entityTag));
                                    // CREATE NEW TERM !!!
                                    Term newTerm =
                                            new Term(namedEntity, tags,
                                                    m_setNeUnmodifiable);
                                    newTermList.add(newTerm);
                                }

                                // otherwise create a term for each word
                            } else {
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(words.get(w));
                                List<Tag> tags = new ArrayList<Tag>();
                                // CREATE NEW TERM !!!
                                Term newTerm = new Term(newWord, tags, false);
                                newTermList.add(newTerm);
                            }

                            // if we are in between the start term and the stop
                            // term
                            // just add all words to the new word list
                        } else if (t > startTermIndex && t < stopTermIndex) {
                            namedEntity.add(words.get(w));
                        }
                    }
                    if (endTerm) {
                        // next found term range
                        endTerm = false;
                        startStopIndex++;
                        if (startStopIndex < startStopRanges.size()) {
                            startTermIndex =
                                    startStopRanges.get(startStopIndex)
                                            .getStartTermIndex();
                            stopTermIndex =
                                    startStopRanges.get(startStopIndex)
                                            .getStopTermIndex();
                            startWordIndex =
                                    startStopRanges.get(startStopIndex)
                                            .getStartWordIndex();
                            stopWordIndex =
                                    startStopRanges.get(startStopIndex)
                                            .getStopWordIndex();
                            namedEntity.clear();
                        }
                    }
                }
            }
            // if we are before or after the interesting part just add the
            // terms without rearrangement
            else {
                newTermList.add(oldList.get(t));
            }
        }
        return newTermList;
    }

    private List<IndexRange> findNe(final List<Term> sentence,
            final List<String> ne) {
        List<IndexRange> ranges = new ArrayList<IndexRange>();
        int found = 0;
        boolean foundFlag = false;
        int startTermIndex = -1;
        int stopTermIndex = -1;
        int startWordIndex = -1;
        int stopWordIndex = -1;

        // search all terms
        for (int t = 0; t < sentence.size(); t++) {
            List<Word> words = sentence.get(t).getWords();
            // search words of terms
            for (int w = 0; w < words.size(); w++) {

                // prepare word and ne for comparison (convert to lower case
                // if case sensitivity is switched off)
                String wordStr = words.get(w).getWord();
                String neStr = ne.get(found);
                if (!m_caseSensitive) {
                    wordStr = wordStr.toLowerCase();
                    neStr = neStr.toLowerCase();
                }

                // if ne element at "found" equals the current word
                if (found < ne.size() && wordStr.equals(neStr)) {
                    // if "found" 0 means we are at the beginning of the named
                    // entity
                    if (found == 0) {
                        startTermIndex = t;
                        startWordIndex = w;
                    }

                    found++;
                    foundFlag = true;

                    // means we are at the end of the named entity
                    if (found == ne.size()) {
                        stopTermIndex = t;
                        stopWordIndex = w;

                        ranges.add(new IndexRange(startTermIndex,
                                stopTermIndex, startWordIndex, stopWordIndex));

                        startTermIndex = -1;
                        stopTermIndex = -1;
                        startWordIndex = -1;
                        stopWordIndex = -1;
                        foundFlag = false;
                        found = 0;
                    }
                } else {
                    if (foundFlag) {
                        foundFlag = false;
                        found = 0;
                        startTermIndex = -1;
                        stopTermIndex = -1;
                        startWordIndex = -1;
                        stopWordIndex = -1;
                    }
                }
            }
        }
        return ranges;
    }
}

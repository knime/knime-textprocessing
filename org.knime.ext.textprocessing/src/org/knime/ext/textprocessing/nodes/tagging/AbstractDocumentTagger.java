/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.util.ArrayList;
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
                List<Sentence> newSentenceList = new ArrayList<Sentence>();
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
        if (entities.size() <= 0) {
            return s;
        }

        // Collect words and terms
        List<Term> termList = s.getTerms();

        // go through all recognized named entities and rearrange terms
        for (TaggedEntity entity : entities) {
            // named entity can contain one or more words, so they have to be
            // tokenized by the default tokenizer to create words out of them.
            List<String> neWords = m_wordTokenizer.tokenize(entity.getEntity());

            // build new term list with old term list, words of detected named
            // entities and entity tag.
            termList = buildTermList(termList, neWords, entity.getTagString());
        }
        return new Sentence(termList);
    }

    private List<Term> buildTermList(final List<Term> oldTermList, final List<String> neWords, final String entityTag) {
        List<Term> newTermList = null;
        List<Term> oldList = oldTermList;

        if (neWords.size() <= 0) {
            return oldList;
        }

        // Won't this blow up if we have "a,b,a" as an entity and "ababab" as a
        // sentence?
        List<IndexRange> startStopRanges = findNe(oldList, neWords);

        // if new list contains no entities to look up for return old list
        // so that no tag is assigned to empty entities.
        if (startStopRanges.size() <= 0) {
            return oldList;
        }

        // if start >= 0 means that there is a named entity contained
        // in the list of terms, so the terms have to be rearranged.
        int startStopIndex = 0;
        newTermList = new ArrayList<Term>();

        // get the first start and stop indices
        int startTermIndex = startStopRanges.get(startStopIndex).getStartTermIndex();
        int stopTermIndex = startStopRanges.get(startStopIndex).getStopTermIndex();
        int startWordIndex = startStopRanges.get(startStopIndex).getStartWordIndex();
        int stopWordIndex = startStopRanges.get(startStopIndex).getStopWordIndex();

        boolean endTerm = false;
        // list to save term representing named entity at.
        List<Word> namedEntity = new ArrayList<Word>(neWords.size());

        // go through all the old term list
        int t = -1;
        for (Term term : oldList) {
            t++;

            // if we reached the interesting terms containing the named
            // entity
            if (t >= startTermIndex && t <= stopTermIndex) {

                // detected a named entity consisting only of one term
                // check if it has to be split up
                if (startTermIndex == stopTermIndex) {

                    //
                    // BUT does the term consist of one or more words ?
                    // If it consists of more words, it has to be split up,
                    // if it consists only of one word just add the tags.
                    //

                    // the old term consists only of one word, so just add a tag
                    // By the way, this is the _only_ situation an old tag is
                    // kept and also assigned to the new term.
                    if (term.getWords().size() == 1) {
                        List<Tag> tags = new ArrayList<Tag>();
                        tags.addAll(term.getTags());
                        // only add tag if not already added
                        List<Tag> newTags = getTags(entityTag);
                        for (Tag ct : newTags) {
                            if (!tags.contains(ct)) {
                                tags.add(ct);
                            }
                        }

                        // CREATE NEW TERM !!!
                        Term newTerm = new Term(term.getWords(), tags, m_setNeUnmodifiable);
                        newTermList.add(newTerm);

                        // the old term consists of more than one word so split
                        // it
                    } else if (term.getWords().size() > 1) {
                        List<Word> newWords = new ArrayList<Word>();

                        int w = -1;
                        for (Word word : term.getWords()) {
                            w++;

                            // add word if index matches
                            if (w >= startWordIndex && w <= stopWordIndex) {
                                newWords.add(word);

                                // if last word to add, create term and add it
                                // to new list
                                if (w == stopWordIndex) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    // only add tag if not already added
                                    List<Tag> newTags = getTags(entityTag);
                                    for (Tag ct : newTags) {
                                        if (!tags.contains(ct)) {
                                            tags.add(ct);
                                        }
                                    }

                                    // CREATE NEW TERM !!!
                                    Term newTerm = new Term(newWords, tags, m_setNeUnmodifiable);
                                    newTermList.add(newTerm);
                                    endTerm = true;
                                }

                                // if word is not part of the named entity add
                                // it as
                                // a term.
                            } else {
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(word);
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
                        startTermIndex = startStopRanges.get(startStopIndex).getStartTermIndex();
                        stopTermIndex = startStopRanges.get(startStopIndex).getStopTermIndex();
                        startWordIndex = startStopRanges.get(startStopIndex).getStartWordIndex();
                        stopWordIndex = startStopRanges.get(startStopIndex).getStopWordIndex();
                        namedEntity.clear();
                    }

                    // entity consists of more than one term, so split it up.
                } else {
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
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(word);
                                List<Tag> tags = new ArrayList<Tag>();
                                // CREATE NEW TERM !!!
                                Term newTerm = new Term(newWord, tags, false);
                                newTermList.add(newTerm);
                            }

                            // if current term is stop term
                        } else if (t == stopTermIndex) {
                            // add words as long as stopWordIndex is not reached
                            if (w <= stopWordIndex) {
                                namedEntity.add(word);

                                // if last word is reached, create term and
                                // add it
                                if (w == stopWordIndex) {
                                    List<Tag> tags = new ArrayList<Tag>();
                                    // only add tag if not already added
                                    List<Tag> newTags = getTags(entityTag);
                                    for (Tag ct : newTags) {
                                        if (!tags.contains(ct)) {
                                            tags.add(ct);
                                        }
                                    }

                                    // CREATE NEW TERM !!!
                                    Term newTerm = new Term(namedEntity, tags, m_setNeUnmodifiable);
                                    newTermList.add(newTerm);
                                }

                                // otherwise create a term for each word
                            } else {
                                List<Word> newWord = new ArrayList<Word>();
                                newWord.add(word);
                                List<Tag> tags = new ArrayList<Tag>();
                                // CREATE NEW TERM !!!
                                Term newTerm = new Term(newWord, tags, false);
                                newTermList.add(newTerm);
                            }

                            // if we are in between the start term and the stop
                            // term just add all words to the new word list
                        } else if (t > startTermIndex && t < stopTermIndex) {
                            namedEntity.add(word);
                        }
                    }
                    if (endTerm) {
                        // next found term range
                        endTerm = false;
                        startStopIndex++;
                        if (startStopIndex < startStopRanges.size()) {
                            startTermIndex = startStopRanges.get(startStopIndex).getStartTermIndex();
                            stopTermIndex = startStopRanges.get(startStopIndex).getStopTermIndex();
                            startWordIndex = startStopRanges.get(startStopIndex).getStartWordIndex();
                            stopWordIndex = startStopRanges.get(startStopIndex).getStopWordIndex();
                            namedEntity.clear();
                        }
                    }
                }
            } else {
                // if we are before or after the interesting part just add the
                // terms without rearrangement
                newTermList.add(term);
            }
        }
        return newTermList;
    }

    private List<IndexRange> findNe(final List<Term> sentence, final List<String> ne) {
        List<IndexRange> ranges = new ArrayList<IndexRange>();
        int found = 0;
        boolean foundFlag = false;
        int startTermIndex = -1;
        int stopTermIndex = -1;
        int startWordIndex = -1;
        int stopWordIndex = -1;

        // search all terms
        int t = -1;
        for (Term term : sentence) {
            t++;
            List<Word> words = term.getWords();

            // search words of terms
            int w = -1;
            for (Word word : words) {
                w++;

                // prepare word and ne for comparison (convert to lower case
                // if case sensitivity is switched off)
                String wordStr = word.getWord();

                String neStr = ne.get(found);
                if (!m_caseSensitive) {
                    wordStr = wordStr.toLowerCase();
                    neStr = neStr.toLowerCase();
                }

                // if ne element at "found" equals the current word
                if (found < ne.size()
                    && ((m_exactMatch && wordStr.equals(neStr)) || (!m_exactMatch && wordStr.contains(neStr)))) {
                    //if (found < ne.size() && wordStr.equals(neStr)) {
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

                        ranges.add(new IndexRange(startTermIndex, stopTermIndex, startWordIndex, stopWordIndex));

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

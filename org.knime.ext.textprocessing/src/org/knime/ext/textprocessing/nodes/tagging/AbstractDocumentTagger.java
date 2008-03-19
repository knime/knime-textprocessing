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
 * which is called by <code>AbstractDocumentTagger</code> to add the tags to
 * a recognized term. Underlying classes have to build the right tag out of
 * the given string.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class AbstractDocumentTagger implements DocumentTagger {

    /**
     * The unmodifiable flag.
     */
    protected boolean m_setNeUnmodifiable;
    
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
     * Creates proper tags out if the given string accordant to the underlying 
     * tagger implementation and returns them. A part of speech tagger 
     * (POS tagger) for instance creates POS tags, a biomedical named entity
     * recognizer provides biomedical named entity tags, such as GENE, PROTEIN
     * etc. 
     * 
     * @param tag The string to create a tag out of.
     * @return The tags build out of the given string.
     */
    protected abstract List<Tag> getTags(final String tag);
    
    /**
     * Analysis the given sentences and recognized certain
     * terms, such as parts of speech or biomedical named entities. These terms
     * and their corresponding tags and returned as a list.
     * 
     * @param sentence The sentence to analyze.
     * @return A list of recognized entities and the corresponding tags.
     */
    protected abstract List<TaggedEntity> tagEntities(final Sentence sentence);    
    
    /**
     * {@inheritDoc}
     */
    public Document tag(Document doc) {
        DocumentBuilder db = new DocumentBuilder(doc);
        for (Section s : doc.getSections()) {
            for (Paragraph p : s.getParagraphs()) {
                List<Sentence> newSentenceList = new ArrayList<Sentence>();
                for (Sentence sn : p.getSentences()) {
                    newSentenceList.add(tagSentence(sn));
                }
                db.addParagraph(new Paragraph(newSentenceList));
            }
            db.createNewSection(s.getAnnotation());
        }
        return db.createDocument();
    }
    
    private Sentence tagSentence(final Sentence s) {
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
            List<String> neWords = DefaultTokenization.tokenizeSentence(
                    entity.getEntity());
            
            termList = buildTermList(termList, neWords, entity.getTagString());
        }
        return new Sentence(termList);
    }
    
    private List<Term> buildTermList(final List<Term> oldTermList, 
            final List<String> neWords, final String entityTag) {
        List<Term> newTermList = null;
        List<Term> oldList = oldTermList;
        List<IndexRange> startStopRanges = findNe(oldList, neWords);
        
        if (startStopRanges.size() <= 0) {
            return oldList;
        }
        
        // if start >= 0 means that there is a complete named entity contained
        // in the list of terms, so the terms have to be rearranged.
        for (int i = 0; i < startStopRanges.size(); i++) {
            if (i > 0) {
                oldList = newTermList;
            }
            newTermList = new ArrayList<Term>();
            int start = startStopRanges.get(i).getStart();
            int stop = startStopRanges.get(i).getStop();
            
            int neIndex = 0;
            // list to save term representing named entity at.
            List<Word> namedEntity = new ArrayList<Word>(neWords.size());
            
            for (int t = 0; t < oldList.size(); t++) {
                // if we reached the interesting terms containing the named 
                // entity
                if (t >= start && t <= stop) {
                    
                    // split up terms only if searched named entity is 
                    // represented by more than one term. If ne is represented
                    // by only one term just add a tag.
                    if (start == stop) {
                        List<Tag> tags = new ArrayList<Tag>();
                        tags.addAll(oldList.get(t).getTags());
                        tags.addAll(getTags(entityTag));
                        Term newTerm = 
                            new Term(oldList.get(t).getWords(), tags);
                        newTermList.add(newTerm);
                    } else {
                        List<Word> words = oldList.get(t).getWords();
                        for (Word w : words) {
                            // we have to split the term up if the
                            // words are not part of the named entity
                            if (neIndex >= neWords.size()
                                    || !w.getWord()
                                            .equals(neWords.get(neIndex))) {
                                List<Word> newWords = new ArrayList<Word>();
                                newWords.add(w);
                                Term newTerm = new Term(newWords);
                                newTermList.add(newTerm);
                            } else {
                                namedEntity.add(w);
                                neIndex++;

                                // if named entity is complete add it as a term
                                if (neIndex == neWords.size()) {
                                    List<Tag> tags = getTags(entityTag);

                                    // CREATE NEW TERM !!!
                                    Term newTerm = new Term(namedEntity, tags, 
                                            m_setNeUnmodifiable);

                                    newTermList.add(newTerm);
                                    neIndex = 0;
                                }
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
        }
        return newTermList;
    }
    
    private List<IndexRange> findNe(final List<Term>sentence, 
            final List<String> ne) {
        List<IndexRange> ranges = new ArrayList<IndexRange>();
        int found = 0;
        boolean foundFlag = false;
        int start = -1;
        int stop = -1;
        
        // search all terms
        for (int t = 0; t < sentence.size(); t++) {
            List<Word> words = sentence.get(t).getWords();
            // search words of terms
            for (int w = 0; w < words.size(); w++) {
                // if ne element at "found" equals the current word
                if (found < ne.size() 
                        && words.get(w).getWord().equals(ne.get(found))) {
                    // if "found" 0 means we are at the beginning of the named
                    // entity
                    if (found == 0) {
                        start = t;
                    }
                    
                    found++;
                    foundFlag = true;
                    
                    // means we are at the end of the named entity
                    if (found == ne.size()) {
                        stop = t;
                        
                        ranges.add(new IndexRange(start, stop));
                        start = -1;
                        stop = -1;
                        foundFlag = false;
                        found = 0;
                    }
                } else {
                    if (foundFlag) {
                        foundFlag = false;
                        found = 0;
                        start = -1;
                        stop = -1;
                    }
                }
            }
        }

        return ranges;
    }    
}

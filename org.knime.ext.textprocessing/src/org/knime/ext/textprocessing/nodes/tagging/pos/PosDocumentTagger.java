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
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.pos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSDictionary;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

/**
 * The POS tagger node adds part of speech (POS) tags to terms of documents.
 * Here the Penn Treebank part-of-speech tag set is used to define all kinds
 * of tags, see {@link org.knime.ext.textprocessing.data.PartOfSpeechTag} for
 * more details. The underlying tagger model which is used to choose the 
 * (hopefully) proper tags for all the terms is an external model of the 
 * OpenNLP framework, see (http://opennlp.sourceforge.net) for more details.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PosDocumentTagger implements DocumentTagger {
    
    private PosTagger m_tagger;
    
    /**
     * Creates a new instance of PosDocumentTagger and loads internally the
     * POS tagging model of the OpenNLP framework to POS tag the documents.
     * If the model file could not be loaded an <code>IOException</code> will
     * be thrown.
     * 
     * @throws IOException If the model file could not be loaded.
     */
    public PosDocumentTagger() throws IOException {
        //super(false);
        OpenNlpModelPaths paths = OpenNlpModelPaths.getOpenNlpModelPaths();
        m_tagger = new PosTagger(paths.getPosTaggerModelFile(),
                new POSDictionary(paths.getPosTaggerDictFile()));
    }
    
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected List<Tag> getTags(String tag) {
//        List<Tag> tags = new ArrayList<Tag>();
//        tags.add(PartOfSpeechTag.stringToTag(tag));
//        return tags;
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
//        List<String> words = new ArrayList<String>();
//        for (Term t : sentence.getTerms()) {
//            List<Word> wordList = t.getWords();
//            for (Word w : wordList) {
//                words.add(w.getWord());
//            }
//        }
//        String[] wordsArr = words.toArray(new String[0]);
//        String[] tagsArr = m_tagger.tag(wordsArr);
//        
//        List<TaggedEntity> entities = new ArrayList<TaggedEntity>();
//        for (int i = 0; i < wordsArr.length; i++) {
//            entities.add(new TaggedEntity(wordsArr[i], tagsArr[i]));
//        }
//        
//        return entities;
//    }
    
    
    /**
     * {@inheritDoc}
     */
    public Document tag(final Document doc) {
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
        // Collect words to tag
        Hashtable<String, Term> termCache = new Hashtable<String, Term>(); 
        List<String> words = new ArrayList<String>();
        for (Term t : s.getTerms()) {
            // one term consists of one word (that's so amazing)
            if (t.getWords().size() == 1) {
                termCache.put(t.getWords().get(0).getWord(), t);
                words.add(t.getWords().get(0).getWord());
            }
            // one term consists of more than one word.
            // This granularity is destroyed and the new, better,
            // democratic and freedom-loving term granularity is applied.
            else if (t.getWords().size() > 1) {
                List<Word> tempWords = t.getWords();
                for (Word w : tempWords) {
                    termCache.put(w.getWord(), null);
                    words.add(w.getWord());
                }
            }
        }
        String[] wordsArr = words.toArray(new String[0]);
        String[] tagsArr = m_tagger.tag(wordsArr);
        
        
        // Now build up new list of terms
        List<Term> newTermList = new ArrayList<Term>();
        for (int i = 0; i < wordsArr.length; i++) {
            Term t = termCache.get(wordsArr[i]);
            if (t == null) {
                List<Word> newWords = new ArrayList<Word>();
                newWords.add(new Word(wordsArr[i]));
                List<Tag> newTags = new ArrayList<Tag>();
                
                Tag tag = PartOfSpeechTag.stringToTag(tagsArr[i]);
                //LOGGER.info(tagsArr[i] + "->" + tag.getTagValue());
                newTags.add(tag);
                
                Term newTerm = new Term(newWords, newTags); 
                newTermList.add(newTerm);
            } else {
                List<Tag> tags = new ArrayList<Tag>();
                tags.addAll(t.getTags());
                
                Tag tag = PartOfSpeechTag.stringToTag(tagsArr[i]);
                //LOGGER.info(tagsArr[i] + "->" + tag.getTagValue());
                tags.add(tag);
                
                Term newTerm = new Term(t.getWords(), tags); 
                newTermList.add(newTerm);
            }
        }
        return new Sentence(newTermList);
    }
}

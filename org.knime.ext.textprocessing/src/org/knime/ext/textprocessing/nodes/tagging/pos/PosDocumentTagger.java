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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import opennlp.tools.lang.english.PosTagger;
import opennlp.tools.postag.POSDictionary;

import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;
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
public class PosDocumentTagger extends AbstractDocumentTagger {
    
    private PosTagger m_tagger;
    
    /**
     * Creates a new instance of PosDocumentTagger and loads internally the
     * POS tagging model of the OpenNLP framework to POS tag the documents.
     * If the model file could not be loaded an <code>IOException</code> will
     * be thrown. If <code>setNeUnmodifiable</code> is set <code>true</code>
     * all recognized terms are set to unmodifiable.
     * 
     * @param setNeUnmodifiable If true all recognized terms are set 
     * unmodifiable.
     * @throws IOException If the model file could not be loaded.
     */
    public PosDocumentTagger(final boolean setNeUnmodifiable) 
    throws IOException {
        super(setNeUnmodifiable);
        OpenNlpModelPaths paths = OpenNlpModelPaths.getOpenNlpModelPaths();
        m_tagger = new PosTagger(paths.getPosTaggerModelFile(),
                new POSDictionary(paths.getPosTaggerDictFile()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(PartOfSpeechTag.stringToTag(tag));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        SortedSet<String> words = new TreeSet<String>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                words.add(w.getWord());
            }
        }
        String[] wordsArr = words.toArray(new String[0]);
        String[] tagsArr = m_tagger.tag(wordsArr);
        
        List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>(
                tagsArr.length);
        for (int i = 0; i < wordsArr.length; i++) {
            taggedEntities.add(new TaggedEntity(wordsArr[i], tagsArr[i]));
        }
        
        return taggedEntities;
    }    
}

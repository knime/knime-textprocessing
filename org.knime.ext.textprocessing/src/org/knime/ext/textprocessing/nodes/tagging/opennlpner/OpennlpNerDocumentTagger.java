/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import opennlp.tools.namefind.NameFinderME;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpennlpNerDocumentTagger extends AbstractDocumentTagger {

    private static final String ENTITY_START = "start";
    
    private static final String ENTITY_CONTINUES = "cont";
    
    private NameFinderME m_tagger;
    
    private OpenNlpModel m_model;
    
    /**
     * Creates a new instance of <code>OpennlpNerDocumentTagger</code> with
     * given unmodifiable flag and model to tag with.
     * 
     * @param setNeUnmodifiable The flag specifying whether found named entities
     * will be set unmodifiable or not.
     * @param model The model to tag with.
     * @throws IOException If something happens.
     */
    public OpennlpNerDocumentTagger(final boolean setNeUnmodifiable,
            final OpenNlpModel model) throws IOException {
        super(setNeUnmodifiable);
        if (model == null) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model may not be null!");
        }
        m_model = model;
        m_tagger = new NameFinderME(m_model.getModel());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Tag> getTags(final String tag) {
        List<Tag> tags = new ArrayList<Tag>();
        tags.add(NamedEntityTag.stringToTag(tag));
        return tags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<String> words = new ArrayList<String>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                words.add(w.getWord());
            }
        }
        List<String> res = m_tagger.find(words, new HashMap<String, String>());
        List<TaggedEntity> nes = new ArrayList<TaggedEntity>();
        
        String namedEntity = null;
        for (int i = 0; i < res.size(); i++) {            
            if (res.get(i).equals(ENTITY_START)) {
                if (namedEntity != null) {
                    TaggedEntity te = new TaggedEntity(namedEntity,
                            m_model.getTag());
                    nes.add(te);
                    namedEntity = null;
                }
                namedEntity = words.get(i);
            } else if (res.get(i).equals(ENTITY_CONTINUES)) {
                namedEntity += Term.WORD_SEPARATOR + words.get(i);
            }
        }
        if (namedEntity != null) {
            TaggedEntity te = new TaggedEntity(namedEntity, m_model.getTag());
            nes.add(te);
        }
        return nes;
    }
}

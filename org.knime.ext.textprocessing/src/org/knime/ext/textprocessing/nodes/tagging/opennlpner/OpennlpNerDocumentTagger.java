/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    
    private HashMap<String, String> m_dictionary;
    
    /**
     * Creates a new instance of <code>OpennlpNerDocumentTagger</code> with
     * given unmodifiable flag and model to tag with.
     * 
     * @param setNeUnmodifiable The flag specifying whether found named entities
     * will be set unmodifiable or not.
     * @param model The model to tag with.
     * @param dictFileName The file name of teh dictionary to use.
     * @throws IOException If something happens.
     */
    public OpennlpNerDocumentTagger(final boolean setNeUnmodifiable,
            final OpenNlpModel model, final String dictFileName) 
    throws IOException {
        super(setNeUnmodifiable);
        if (model == null) {
            throw new IllegalArgumentException(
                    "The specified OpenNLP model may not be null!");
        }
        m_model = model;
        m_tagger = new NameFinderME(m_model.getModel());
        m_dictionary = new HashMap<String, String>();
        
        if (dictFileName != null) {
            File f = new File(dictFileName);
            if (f.exists() && f.canRead() && f.isFile()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.trim().split(" ");
                    for (int i = 0; i < words.length; i++) {
                        String tag = "start";
                        if (i > 0) {
                            tag = "cont";
                        }
                        m_dictionary.put(words[i], tag);
                    }
                }
                br.close();
            }
        }
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
        List<String> res = m_tagger.find(words, m_dictionary);
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
            namedEntity = null;
        }
        return nes;
    }
}

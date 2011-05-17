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
 * ---------------------------------------------------------------------
 *
 * History
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.stanfordtagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;


public class StanfordPosDocumentTagger extends AbstractDocumentTagger {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(StanfordPosDocumentTagger.class);
    
	private MaxentTagger m_tagger;
	
	private TagsetMapper m_mapping;

	
	public StanfordPosDocumentTagger(final boolean setNeUnmodifiable,
			final String modelFile, final TagsetMapper mapper)
			throws IOException, ClassNotFoundException {
		super(setNeUnmodifiable);
		try {
            m_tagger = new MaxentTagger(modelFile);
            m_mapping = mapper;
		} catch (ClassNotFoundException e) {
			LOGGER.warn("Could not load Stanford POS tagger model!");
			throw(e);
		}
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
        List<edu.stanford.nlp.ling.Word> words = 
        	new ArrayList<edu.stanford.nlp.ling.Word>();
        for (Term t : sentence.getTerms()) {
            for (Word w : t.getWords()) {
                words.add(new edu.stanford.nlp.ling.Word(w.getText()));
            }
        }
        ArrayList<TaggedWord> taggedWords = m_tagger.tagSentence(words);
		
		List<TaggedEntity> taggedEntities = new ArrayList<TaggedEntity>(
				taggedWords.size());
		for (int i = 0; i < taggedWords.size(); i++) {
			if (taggedWords.get(i).tag() != null) {
				String sttsTag = taggedWords.get(i).tag();
				String penntreebankTag = m_mapping.mapTag(
						sttsTag);
				taggedEntities.add(new TaggedEntity(taggedWords.get(i).word(),
						penntreebankTag));
			}
		}

		return taggedEntities;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void preprocess(final Document doc) {
		// no preprocessing required
	}	
}

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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.inport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

/**
 * A dictionary based tagger providing methods to detect and tag named entities
 * in a given sentence. The named entities to detect have to be specified
 * when calling the constructor.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryDocumentTagger extends AbstractDocumentTagger {

    private Set<String> m_namedEntities;

    private Tag m_tag;

    private boolean m_caseSensitve;

    /**
     * Creates a new instance of <code>DictionaryDocumentTagger</code> with
     * given flag to set found named entities unmodifiable, to ignore the case
     * of the named entities to detect, the tag to assign to the found named
     * entities and the set of named entities to watch out for.
     *
     * @param setUnmodifiable If <code>true</code> found named entities are set
     * unmodifiable, otherwise not.
     * @param namedEntities The set of named entities to watch out for.
     * @param tag The tag to assign to found named entities.
     * @param caseSensitive If <code>false</code> the case of named entities
     * and words of the sentences are ignored, otherwise not.
     */
    public DictionaryDocumentTagger(final boolean setUnmodifiable,
            final Set<String> namedEntities, final Tag tag,
            final boolean caseSensitive) {
        super(setUnmodifiable, caseSensitive);

        if (namedEntities == null) {
            throw new NullPointerException(
                    "Set of named entities may not be null!");
        } else if (tag == null) {
            throw new NullPointerException("Specified tag my not be null!");
        }

        m_namedEntities = namedEntities;
        m_tag = tag;
        m_caseSensitve = caseSensitive;
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
    protected List<TaggedEntity> tagEntities(final Sentence sentence) {
        List<TaggedEntity> foundEntities = new ArrayList<TaggedEntity>();

        String sentenceStr = sentence.getText();
        if (!m_caseSensitve) {
            sentenceStr = sentenceStr.toLowerCase();
        }

        for (String ne : m_namedEntities) {
            String entity = ne;
            if (!m_caseSensitve) {
                entity = entity.toLowerCase();
            }

            if (sentenceStr.contains(entity)) {
                TaggedEntity taggedEntity = new TaggedEntity(ne,
                        m_tag.getTagValue());
                foundEntities.add(taggedEntity);
            }
        }

        return foundEntities;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preprocess(final Document doc) {
        // no preprocessing required
    }
}

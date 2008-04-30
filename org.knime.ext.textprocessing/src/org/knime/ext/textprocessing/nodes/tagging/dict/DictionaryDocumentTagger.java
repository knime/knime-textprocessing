/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.AbstractDocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggedEntity;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryDocumentTagger extends AbstractDocumentTagger {

    public Set<String> m_namedEntities;
    
    public Tag m_tag;
    
    public boolean m_caseSensitve;
    
    public DictionaryDocumentTagger(final boolean setUnmodifiable,
            final Set<String> namedEntities, final Tag tag, 
            final boolean caseSensitive) {
        super(setUnmodifiable);
        
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
        List<TaggedEntity> m_foundEntities = new ArrayList<TaggedEntity>();
        
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
                m_foundEntities.add(taggedEntity);
            }
        }
        
        return m_foundEntities;
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   29.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

/**
 * This class holds two strings, the first represents an entity, such as a
 * term consisting of one or more words and the second represents a tag, such as
 * a part of speech tag (POS) or a named entity tag (ABNER) etc.
 * External tagger, like ABNER (A Biomedical Named Entity Recognizer) or the
 * OpenNLP POS tagger return their results in various kinds. This class helps
 * to unify the different results and enable an unique way of accessing them.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TaggedEntity {

    private String m_entity;

    private String m_tag;

    /**
     * Creates a new instance of <code>TaggedEntity</code> with given entity
     * (a term as string) and tag as string.
     *
     * @param entity The term entity to set.
     * @param tagString The tag string to set.
     */
    public TaggedEntity(final String entity, final String tagString) {
        m_entity = entity;
        m_tag = tagString;
    }

    /**
     * @return the entity as string.
     */
    public String getEntity() {
        return m_entity;
    }

    /**
     * @return the tag as string.
     */
    public String getTagString() {
        return m_tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_entity + "[" + m_tag + "]";
    }
}

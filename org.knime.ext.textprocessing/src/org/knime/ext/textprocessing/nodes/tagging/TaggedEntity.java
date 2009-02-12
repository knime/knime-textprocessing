/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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

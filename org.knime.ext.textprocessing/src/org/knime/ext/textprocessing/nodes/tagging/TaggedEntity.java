/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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

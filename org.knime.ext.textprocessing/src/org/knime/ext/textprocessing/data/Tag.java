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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 *   12.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Represents all tags which can be assigned to
 * {@link org.knime.ext.textprocessing.data.Term}s. Tags represent the meanings
 * of a {@link org.knime.ext.textprocessing.data.Term} according to their type
 * which can for instance be a certain Part-Of-Speech tag, a named entity tag,
 * etc.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Tag implements Externalizable {

    private String m_tagValue;

    private String m_tagType;

    /**
     * Creates empty instance of <code>Tag</code> with all <code>null</code>
     * values.
     */
    public Tag() {
        m_tagType = null;
        m_tagValue = null;
    }

    /**
     * Creates a new instance of <code>Tag</code> with given value and type,
     * which may not be null.
     * 
     * @param tagValue The value of the tag to set.
     * @param tagType The type of the tag to set.
     * @throws NullPointerException if given tag value or type is null.
     */
    public Tag(final String tagValue, final String tagType)
            throws NullPointerException {
        if (tagValue == null || tagType == null) {
            throw new NullPointerException(
                    "Tag value or type may not be null!");
        }
        m_tagValue = tagValue;
        m_tagType = tagType;
    }

    /**
     * @return The value of the tag.
     */
    public String getTagValue() {
        return m_tagValue;
    }

    /**
     * @return The type of the tag.
     */
    public String getTagType() {
        return m_tagType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Tag)) {
            return false;
        }
        Tag t = (Tag)o;
        if (!t.getTagValue().equals(getTagValue())) {
            return false;
        }
        if (!t.getTagType().equals(getTagType())) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getTagValue().hashCode() * getTagType().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(m_tagValue);
        out.writeUTF(m_tagType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_tagValue = in.readUTF();
        m_tagType = in.readUTF();
    }
}

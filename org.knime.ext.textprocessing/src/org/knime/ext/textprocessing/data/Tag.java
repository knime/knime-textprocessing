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
 *   12.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

/**
 * Represents all tags which can be assigned to 
 * {@link org.knime.ext.textprocessing.data.Term}s. Tags represent the meanings 
 * of a {@link org.knime.ext.textprocessing.data.Term} according to their type 
 * which can for instance be a certain Part-Of-Speech tag, a named entity tag, 
 * etc.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Tag {

    private String m_tagValue;
    
    private String m_tagType;
    
    /**
     * Creates a new instance of <code>Tag</code> with given value and type,
     * which may not be null.
     * 
     * @param tagValue The value of the tag to set.
     * @param tagType The type of the tag to set.
     * @throws NullPointerException if given tag value or type is null.
     */
    Tag(final String tagValue, final String tagType) 
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
}

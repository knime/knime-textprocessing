/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public enum NamedEntityTag implements TagBuilder {
    /** Unkown type. */
    UNKNOWN,
    /** A location */
    LOCATION,
    /** A person */
    PERSON,
    /** A company */
    COMPANY,
    /** A date */
    DATE;
    
    private final Tag m_tag;
    
    /**
     * The constant for ABNER tag types.
     */
    public static final String TAG_TYPE = "NE";
    
    /**
     * Creates new instance of <code>NamedEntityTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified
     * named antity tag.
     */
    private NamedEntityTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }
    
    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding 
     * to the specified <code>NamedEntityTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }
        
    /**
     * Returns the enum fields as a String list of their names.
     * 
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<NamedEntityTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }
    
    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to 
     * the given string. If no corresponding 
     * {@link org.knime.ext.textprocessing.data.Tag} is available the 
     * <code>UNKNOWN</code> tag is returned.
     * @param str The string representing a 
     * {@link org.knime.ext.textprocessing.data.Tag}. 
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to 
     * the given string.
     */
    public static Tag stringToTag(final String str) {        
        for (NamedEntityTag ne : values()) {
            if (ne.getTag().getTagValue().equals(str)) {
                return ne.getTag();
            }
        }
        return NamedEntityTag.UNKNOWN.getTag();
    }
    
    /**
     * {@inheritDoc}
     */
    public Tag buildTag(String type, String value) {
        if (type.equals(TAG_TYPE)) {
            return NamedEntityTag.stringToTag(value);
        }
        return null;
    }

    /**
     * @return The default "UNKNOWN" <code>NamedEntityTag</code> as
     * <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return NamedEntityTag.UNKNOWN;
    }
}

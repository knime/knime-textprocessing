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
 *   18.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.HashSet;

/**
 * All different kind of {@link org.knime.ext.textprocessing.data.Tag}s have
 * to be registered according their type in this factory to be able to create 
 * the right tag instance (i.e. PartOfSpeechTag) related to the type of the
 * tag. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFactory {

    private static final HashSet<TagBuilder> TAG_BUILDER = 
        new HashSet<TagBuilder>();
    
    // TODO Read tags and types from xml file !!!
    static {
        TAG_BUILDER.add(PartOfSpeechTag.UNKNOWN);
        // register other tags here ...
    }
    
    /**
     * Creates a valid instance of {@link org.knime.ext.textprocessing.data.Tag}
     * with given type and value.
     * 
     * @param type The type of the tag to create.
     * @param value The value of the tag to create.
     * @return A new instance of tag with given type and value.
     */
    public static Tag createTag(final String type, final String value) {
        for (TagBuilder tb : TAG_BUILDER) {
            Tag t = tb.buildTag(type, value);
            if (t != null) {
                return t;
            }
        }
        return null;
    } 
}

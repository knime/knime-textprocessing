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
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public enum SectionAnnotation {

    /** The UNKNOWN annotation. **/
    UNKNOWN,
    /** The title of a document. **/
    TITLE,
    /** The abstract of a document. **/
    ABSTRACT,
    /** A chapter of a document. **/
    CHAPTER,
    /** The title of the journal the document was published at. **/
    JOURNAL_TITLE,
    /** The title of the conference the document was published at. **/
    CONFERENCE_TITLE;
    
    
    /**
     * Returns the {@link org.knime.ext.textprocessing.data.SectionAnnotation} 
     * related to the given string. If no corresponding 
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} is available 
     * the <code>UNKNOWN</code> annotation is returned.
     * @param str The string representing a 
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation}. 
     * @return The related 
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} to the given 
     * string.
     */
    public static SectionAnnotation stringToAnnotation(final String str) {
        SectionAnnotation sa = valueOf(str);
        if (sa != null) {
            return sa;
        }
        return SectionAnnotation.UNKNOWN;
    }    
    
    /**
     * Returns the enum fields as a String list of their names.
     * 
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<SectionAnnotation>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }
}

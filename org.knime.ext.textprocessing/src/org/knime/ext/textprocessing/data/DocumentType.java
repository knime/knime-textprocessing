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
 *   21.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the type of a document, which can be for instance a transaction,
 * a proceeding, a book or unknown.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public enum DocumentType {
    
    /** Constant for a unkown type. */
    UNKNOWN,
    /** Constant for a transaction (journal). */
    TRANSACTION,
    /** Constant for a proceeding (conference). */
    PROCEEDING,
    /** Constant for a book. */
    BOOK;

    /**
     * Returns the enum fields as a String list of their names.
     * 
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<DocumentType>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }
    
    /**
     * Returns the proper <code>DocumentType</code> accordant to the given
     * string.
     * 
     * @param str The string to get the proper <code>DocumentType</code> for.
     * @return The proper <code>DocumentType</code> accordant to the given
     * string.
     */
    public static DocumentType stringToDocumentType(final String str) {
        for (DocumentType type : values()) {
            if (type.toString().equals(str)) {
                return DocumentType.valueOf(str);
            }
        }
        return DocumentType.UNKNOWN;
    }    
}

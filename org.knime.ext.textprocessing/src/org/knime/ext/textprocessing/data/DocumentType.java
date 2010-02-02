/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

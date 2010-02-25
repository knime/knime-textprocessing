/* 
========================================================================
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
 * -------------------------------------------------------------------
 * 
 * History
 *   19.12.2006 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;

/**
 * Comparator returned by the 
 * {@link org.knime.ext.textprocessing.data.DocumentValue} interface. 
 *
 * @see org.knime.ext.textprocessing.data.DocumentValue#UTILITY
 * @see org.knime.ext.textprocessing.data.DocumentValue.DocumentUtilityFactory
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentValueComparator extends DataValueComparator {

    /**
     * Compares two {@link org.knime.ext.textprocessing.data.DocumentValue}s 
     * based on their text.
     * 
     * {@inheritDoc}
     */
    @Override
    protected int compareDataValues(final DataValue v1, final DataValue v2) {
        if (v1 == v2) {
            return 0;
        }
        if (((DocumentValue)v1).getDocument() 
                == ((DocumentValue)v2).getDocument()) {
            return 0;
        }
        
        // compare title
        String str1 = ((DocumentValue)v1).getDocument().getTitle();
        String str2 = ((DocumentValue)v2).getDocument().getTitle();
        int res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // compare type
        str1 = ((DocumentValue)v1).getDocument().getType().toString();
        str2 = ((DocumentValue)v2).getDocument().getType().toString();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // compare date
        str1 = ((DocumentValue)v1).getDocument().getPubDate().toString();
        str2 = ((DocumentValue)v2).getDocument().getPubDate().toString();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // compare file
        str1 = ((DocumentValue)v1).getDocument().getDocFile().getAbsolutePath();
        str2 = ((DocumentValue)v2).getDocument().getDocFile().getAbsolutePath();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // compare authors
        str1 = ((DocumentValue)v1).getDocument().getAuthors().toString();
        str2 = ((DocumentValue)v2).getDocument().getAuthors().toString();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // compare categories
        str1 = ((DocumentValue)v1).getDocument().getCategories().toString();
        str2 = ((DocumentValue)v2).getDocument().getCategories().toString();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }

        // compare sources
        str1 = ((DocumentValue)v1).getDocument().getSources().toString();
        str2 = ((DocumentValue)v2).getDocument().getSources().toString();
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        // finally compare text (because its the most expensive comparison
        str1 = ((DocumentValue)v1).getDocument().getText();
        str2 = ((DocumentValue)v2).getDocument().getText();
        return str1.compareTo(str2);
    }
}

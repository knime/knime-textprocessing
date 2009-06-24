/* 
 * -------------------------------------------------------------------
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
        res = str1.compareTo(str2);
        if (res != 0) {
            return res;
        }
        
        return str1.compareTo(str2);
    }
}

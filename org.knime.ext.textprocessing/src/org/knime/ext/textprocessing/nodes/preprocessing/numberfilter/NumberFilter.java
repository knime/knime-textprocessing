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
 *   11.05.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.numberfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NumberFilter implements Preprocessing {

    private static String numbers = "^[-+]?[\\d.,]+";
    
    private static String replacement = "";
        
    /**
     * Filters all strings containing numbers . or , the strings may also 
     * start with + or - and replaces them with "". The filtered String is 
     * returned.
     * @param str String to filter numbers from.
     * @return Filtered String.
     */
    public static String numberFilter(final String str) {        
        return str.replaceAll(numbers, replacement);
    }


    /**
     * {@inheritDoc}
     */
    public Term preprocess(final Term term) {
        String filtered = NumberFilter.numberFilter(term.getText());
        if (filtered.length() <= 0) {
            return null;
        }
        return term;
    }
}

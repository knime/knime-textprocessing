/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   11.05.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.numberfilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.StringPreprocessing;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NumberFilter implements TermPreprocessing, StringPreprocessing {

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
    public Term preprocessTerm(final Term term) {
        String filtered = NumberFilter.numberFilter(term.getText());
        if (filtered.length() <= 0) {
            return null;
        }
        return term;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String preprocessString(final String str) {
        return NumberFilter.numberFilter(str);
    }
}

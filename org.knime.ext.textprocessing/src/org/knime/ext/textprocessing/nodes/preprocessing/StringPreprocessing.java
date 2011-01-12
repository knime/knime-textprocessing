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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;


/**
 * This interface can be implemented by all string preprocessing nodes
 * no matter if filter or modification nodes. The method 
 * {@link StringPreprocessing#preprocessString(String)} has to 
 * be implemented by all underlying classes and provide a certain 
 * string preprocessing functionality. A stemmer node for instance has to stem 
 * the given string and return the stemmed one. If a string preprocessing 
 * class filters out a given string, <code>null</code> has to be returned. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface StringPreprocessing {

    /**
     * Preprocesses the given string in a certain manner. Modification nodes, 
     * such as stemmer or case converter return the modified string. 
     * Filter nodes such as stop word filter return <code>null</code> if the 
     * given string was filtered out, otherwise the string is returned 
     * unmodified.
     * 
     * @param str The string to preprocess
     * @return The preprocessed string
     */
    public String preprocessString(final String str);
}

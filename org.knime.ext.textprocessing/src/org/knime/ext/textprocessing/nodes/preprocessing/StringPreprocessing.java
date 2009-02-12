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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;


/**
 * This interface can be implemented by all preprocessing nodes
 * no matter if filter or modification nodes. 
 * The method 
 * {@link StringPreprocessing#preprocessString(String)} has to 
 * be implemented by all underlying classes and provide a certain 
 * preprocessing functionality. A stemmer node for instance has to stem the 
 * given string and return the stemmed one. If a preprocessing class filters 
 * out a given string, <code>null</code> has to be returned. 
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

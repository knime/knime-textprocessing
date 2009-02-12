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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

/**
 * Provides the configuration keys of the dictionary tagger node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryTaggerConfigKeys {

    /**
     * The configuration key for the file containing the named entities.
     */
    public static final String CFGKEY_FILE = "File";
    
    /**
     * The configuration key of the tag to use.
     */
    public static final String CFGKEY_TAG = "Tag";

    /**
     * The configuration key of the case sensitive setting.
     */
    public static final String CFGKEY_CASE_SENSITIVE = "CS";
    
    /**
     * The configuration key of unmodifiable flag of terms.
     */
    public static final String CFGKEY_UNMODIFIABLE = "SetUnmodifiable";
}

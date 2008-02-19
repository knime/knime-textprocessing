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
 *   19.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

/**
 * Holds the configuration keys of the 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeModel}
 * node. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentParserConfigKeys {

    /**
     * The configuration key of the path of the directory containing the files
     * to parse.
     */
    public static final String CFGKEY_PATH = "Path";
    
    /**
     * The configuration key of the recursive flag (if set the specified 
     * directory is search recursively).
     */
    public static final String CFGKEY_RECURSIVE = "Rec";
}

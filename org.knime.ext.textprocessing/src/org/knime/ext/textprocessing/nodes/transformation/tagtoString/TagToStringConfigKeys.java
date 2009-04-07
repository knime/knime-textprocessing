/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2009
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringConfigKeys {

    /**
     * The configuration key of the tag types to convert.
     */
    public static String CFG_KEY_TAG_TYPES = "TagTypes";
    
    /**
     * The configuration key of the term column.
     */
    public static String CFG_KEY_TERM_COL = "TermColumn";
    
    /**
     * The configuration key of the missing tag string;
     */
    public static String CFG_KEY_MISSING_TAG_STRING = "MissingString";
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   03.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterConfigKeys {

    /**
     * The configuration key for the relative/absolute setting of TF 
     * computation.
     */
    public static final String CFG_KEY_FILTERCOL = "FilterCol";
    
    /**
     * The configuration key for the way of filtering (selection).
     */
    public static final String CFG_KEY_SELECTION = "Selection";
    
    /**
     * The configuration key for the min max threshold.
     */
    public static final String CFG_KEY_MINMAX = "MinMax";
    
    /**
     * The configuration key for the number of terms to filter.
     */
    public static final String CFG_KEY_NUMBER = "Number";
    
    /**
     * The configuration key for the deep filtering setting.
     */
    public static final String CFG_KEY_DEEPFILTERING = "DeepFiltering";
    
    /**
     * The configuration key for the setting, specifying if unmodifiable terms 
     * have to be filtered too.
     */
    public static final String CFG_KEY_MODIFY_UNMODIFIABLE = 
        "MofiyUnmodifiable";
    
    /**
     * The configuration key for the column containing he documents to process.
     */
    public static final String CFG_KEY_DOCUMENT_COL = "DocCol";    
}

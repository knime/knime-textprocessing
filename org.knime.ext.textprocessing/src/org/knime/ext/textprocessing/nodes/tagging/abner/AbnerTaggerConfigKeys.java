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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class AbnerTaggerConfigKeys {
    
    private AbnerTaggerConfigKeys() { }
    
    /**
     * The configuration key of unmodifiable flag of terms.
     */
    public static final String CFGKEY_UNMODIFIABLE = "SetUnmodifiable";
    
    /**
     * The configuration key for the ABNER tagging model.
     */
    public static final String CFGKEY_MODEL = "ABNER Model";
}

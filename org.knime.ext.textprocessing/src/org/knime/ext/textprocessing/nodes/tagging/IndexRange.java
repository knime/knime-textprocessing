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
 *   29.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class IndexRange {
    
    private int m_start = -1;
    
    private int m_stop = -1;
    
    public IndexRange(final int start, final int stop) {
        m_start = start;
        m_stop = stop;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return m_start;
    }

    /**
     * @return the stop
     */
    public int getStop() {
        return m_stop;
    }
}

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
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

/**
 * Classes implementing this interface have to contain textual data in any
 * kind of representations. Therefore the method
 * {@link org.knime.ext.textprocessing.data.TextContainer#getText()} enables the
 * access of this textual data in an unified way.   
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TextContainer {

    /**
     * @return The textual data of the <code>TextContainer</code> as a 
     * single String. The difference to {@link java.lang.Object#toString()} is
     * that not a string representation of the instance is returned but only
     * the useful textual data as string.
     */
    public String getText();
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
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
 *   11.09.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud.tcfontsize;

/**
 *
 * @author Iris Adae, University of Konstanz
 */
public interface TCFontsize {

    /** return the new value.
     * @param minf the minimal fontsize
     * @param maxf the maximal fontsize
     * @param minv the minimal value
     * @param maxv the maximal value
     * @param value the selected value
     * @return the fontsize in the new interval
     */
    double getSize(final double minf, final double maxf, final double minv,
            final double maxv, final double value);
}


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
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;


/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterFactory {

    private FilterFactory() { }
    
    public static final FrequencyFilter createFilter(final String filterOption, 
            final int termColIndex, final int filterColIndex, final int number, 
            final double minVal, final double maxVal) {
        if (filterOption.equals(FilterNodeModel.SELECTION_NUMBER)) {
            return new KTermsFilter(termColIndex, filterColIndex, number);
        } else if (filterOption.equals(FilterNodeModel.SELECTION_THRESHOLD)) {
            return new ThresholdFilter(termColIndex, filterColIndex, minVal, 
                    maxVal);
        }
        return null;
    }
    
}

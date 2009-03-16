/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   Aug 1, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

/**
 * Generic condition-testing interface used by collection filters.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the objects to test the condition against
 */
public interface Condition<T> {
    /**
     * @param t the object to test
     * @return true if the test is successful, false otherwise
     */
    public boolean test(T t);
}

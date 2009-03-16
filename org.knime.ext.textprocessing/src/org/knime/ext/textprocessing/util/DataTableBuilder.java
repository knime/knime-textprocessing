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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataTableSpec;

/**
 * All classes building up {@link org.knime.core.data.DataTable}s have to
 * implement this interface in order to provide a common type.
 * The method {@link DataTableBuilder#createDataTableSpec()} returns the
 * {@link org.knime.core.data.DataTableSpec} of the data table created by each
 * concrete implementation.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DataTableBuilder {
    
    /**
     * @return The <code>DataTableSpec</code> of the data table build by the
     * underlying implementation.
     */
    public DataTableSpec createDataTableSpec();
}

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
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * A {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory}
 * creating {@link org.knime.ext.textprocessing.data.TermCell}s out
 * of given {@link org.knime.ext.textprocessing.data.Term}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermDataCellFactory implements TextContainerDataCellFactory {

    /**
     * {@inheritDoc}
     * 
     * Creates <code>TermCell</code> out of given <code>TextContainer</code>
     * which have to be <code>Term</code> instances, otherwise 
     * <code>null</code> is returned.
     */
    public DataCell createDataCell(final TextContainer tc) {
        DataCell dc = null;
        if (tc instanceof Term) {
            dc = new TermCell((Term)tc);
        }
        return dc;
    }

}

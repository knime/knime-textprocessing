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
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * A {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory}
 * creating {@link org.knime.ext.textprocessing.data.DocumentCell}s out
 * of given {@link org.knime.ext.textprocessing.data.Document}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentDataCellFactory implements TextContainerDataCellFactory {

    /**
     * {@inheritDoc}
     * 
     * Creates <code>DocumentCell</code> out of given <code>TextContainer</code>
     * which have to be <code>Document</code> instances, otherwise 
     * <code>null</code> is returned.
     */
    public DataCell createDataCell(final TextContainer tc) {
        DataCell dc = null;
        if (tc instanceof Document) {
            dc = new DocumentCell((Document)tc);
        }
        return dc;
    }

    @Override
    public DataType getDataType() {
        return DocumentCell.TYPE;
    }

    @Override
    public boolean validateCellType(final DataCell cell) {
        if (cell instanceof DocumentCell) {
            return true;
        }
        return false;
    }
}

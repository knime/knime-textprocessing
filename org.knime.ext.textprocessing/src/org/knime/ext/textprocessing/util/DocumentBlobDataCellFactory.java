/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   03.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * A {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory}
 * creating {@link org.knime.ext.textprocessing.data.DocumentBlobCell}s out
 * of given {@link org.knime.ext.textprocessing.data.Document}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentBlobDataCellFactory implements
        TextContainerDataCellFactory {

    /**
     * {@inheritDoc}
     *
     * Creates <code>DocumentBlobCell</code> out of given
     * <code>TextContainer</code> which have to be <code>Document</code>
     * instances, otherwise <code>null</code> is returned.
     */
    @Override
    public DataCell createDataCell(final TextContainer tc) {
        DataCell dc = null;
        if (tc instanceof Document) {
            dc = new DocumentBlobCell((Document)tc);
        }
        return dc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataType getDataType() {
        return DocumentBlobCell.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateCellType(final DataCell cell) {
        if (cell instanceof DocumentBlobCell) {
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * @since 2.9
     */
    @Override
    public void prepare(final ExecutionContext exec) { }
}

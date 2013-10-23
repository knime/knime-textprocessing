/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * Classes implementing this interface are factories which create certain types
 * of {@link org.knime.core.data.DataCell}s containing certain types of
 * {@link org.knime.ext.textprocessing.data.TextContainer}. The method
 * {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory#createDataCell(TextContainer)}
 * which has to be implemented creates a new <code>DataCell</code> of a certain
 * type, i.e. a <code>DocumentCell</code> containing the given
 * <code>TextContainer</code> which has to be of the right type accordant to
 * the <code>DataCell</code> (i.e. <code>Document</code>).
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface TextContainerDataCellFactory {

    /**
     * creates a new <code>DataCell</code> of a certain
     * type, i.e. a <code>DocumentCell</code> containing the given
     * <code>TextContainer</code>. If the type of the container does not match
     * the type of the <code>DataCell</code> to create (i.e. a
     * <code>DocumentCell</code> requires a <code>Document</code>
     * <code>null</code> is returned, since no proper <code>DataCell</code>
     * can be created.
     *
     * @param tc The <code>TextContainer</code> to create the
     * <code>DataCell</code> for.
     * @return The created <code>DataCell</code> containing the given
     * <code>TextContainer</code>
     */
    public DataCell createDataCell(final TextContainer tc);

    /**
     * Returns the type of the DataCell(s) which will be created by this
     * factory.
     * @return The data type of the cells this factory creates.
     */
    public DataType getDataType();

    /**
     * Returns false if given data cell is not compatible with expected
     * type.
     * @param cell The cell to validate its type.
     * @return <code>true</code> is the type of the given cell is valid,
     * <code>false</code> otherwise.
     */
    public boolean validateCellType(final DataCell cell);

    /**
     * Preparing factory in order to be ready creating data cells.
     * @param exec The execution context to set.
     * @since 2.9
     */
    public void prepare(final ExecutionContext exec);
}

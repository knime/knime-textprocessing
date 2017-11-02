/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.filestore.FileStoreFactory;
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
     * @param fileStoreFactory The factory to create file stores.
     * @since 2.11
     */
    public void prepare(final FileStoreFactory fileStoreFactory);
}

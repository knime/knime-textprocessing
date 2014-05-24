/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.ExecutionMonitor;

/**
 * A {@link org.knime.core.data.container.CellFactory} adapted for frequency
 * computation nodes.
 * Underlying implementations only have to add a data cell containing
 * a certain frequency value.
 * The rest is done automatically by this abstract class, like
 * adding the related {@link org.knime.core.data.DataColumnSpec} specifying a
 * double or integer value, monitoring the progress and providing the index
 * values of the term and the document cell.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyCellFactory implements CellFactory {

    private int m_documentColIndex = -1;

    private int m_termColIndex = -1;

    /**
     * The name of the column to add.
     */
    protected String m_colName;

    /**
     * The flag specifying if a column containing integers or doubles is added.
     */
    protected boolean m_addIntCol = false;


    /**
     * Creates a new instance of <code>FrequencyCellFactory</code>.
     *
     * @param documentCellIndex The index of the document column.
     * @param termCellindex The index of the term column.
     * @param colName The name of the column to add.
     * @param addIntCol If <code>true</code> a column containing integer values
     * will be added otherwise a column containing doubles.
     */
    protected FrequencyCellFactory(final int documentCellIndex,
            final int termCellindex, final String colName,
            final boolean addIntCol) {
        m_documentColIndex = documentCellIndex;
        m_termColIndex = termCellindex;
        m_colName = colName;
        m_addIntCol = addIntCol;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final DataColumnSpec[] getColumnSpecs() {
        DataColumnSpec freq = null;
        if (m_addIntCol) {
            freq = new DataColumnSpecCreator(m_colName, IntCell.TYPE)
                    .createSpec();
        } else {
            freq = new DataColumnSpecCreator(m_colName, DoubleCell.TYPE)
                    .createSpec();
        }
        return new DataColumnSpec[]{freq};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Computing frequency of row: " + curRowNr
                + " of " + rowCount + " rows");
    }


    /**
     * @return the documentColIndex
     */
    protected final int getDocumentColIndex() {
        return m_documentColIndex;
    }


    /**
     * @return the termColIndex
     */
    protected final int getTermColIndex() {
        return m_termColIndex;
    }
}

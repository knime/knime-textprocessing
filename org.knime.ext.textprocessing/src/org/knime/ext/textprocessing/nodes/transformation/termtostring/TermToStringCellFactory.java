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
 *   26.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termtostring;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStringCellFactory implements CellFactory {

    private int m_termColIndex = -1;

    private String m_newColName;

    /**
     * Creates a new instance of <code>TermToStringCellFactory</code> with the
     * given index of the term cell column and the name of the column to append.
     *
     * @param termColindex The index of the term cell column.
     * @param newColName The name of the column to append.
     * @throws InvalidSettingsException if the given index of the term column
     * is less than zero.
     */
    public TermToStringCellFactory(final int termColindex,
            final String newColName) throws InvalidSettingsException {
        if (termColindex < 0) {
            throw new InvalidSettingsException(
                    "The specified term column index is not valid!");
        }
        m_termColIndex = termColindex;
        m_newColName = newColName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        Term term = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
        StringCell strCell = new StringCell(term.getText());
        return new DataCell[]{strCell};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        DataColumnSpec strCol = new DataColumnSpecCreator(m_newColName,
                StringCell.TYPE).createSpec();
        return new DataColumnSpec[]{strCol};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Processing row: " + curRowNr
                + " of " + rowCount + " rows");
    }
}

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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.data.Word;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermCellFactory implements CellFactory {

    private int m_colIndex;
    
    private DataTableSpec m_inSpec;
    
    /**
     * Creates new instance of <code>TermCellFactory</code> with given index
     * of column containing the strings to convert to terms.
     * 
     * @param colIndex The index of the column containing the string to
     * convert to terms.
     * @param inSpec The incoming <code>DataTableSpec</code>.
     */
    public TermCellFactory(final int colIndex, final DataTableSpec inSpec) {
        if (colIndex < 0) {
            throw new IllegalArgumentException("Given column index " 
                    + colIndex + " is not valid!");
        }
        if (inSpec == null) {
            throw new IllegalArgumentException(
                    "Given input spec is not valid!");            
        }
        m_colIndex = colIndex;
        m_inSpec = inSpec;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        List<Word> words = new ArrayList<Word>();
        words.add(new Word(
                ((StringValue)row.getCell(m_colIndex)).getStringValue()));
        TermCell tc = new TermCell(new Term(words, null, false));
        return new DataCell[]{tc};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        return new DataColumnSpec[]{
                StringToTermNodeModel.getTermColumnSpec(m_inSpec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount, 
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Converting string to term of row: " + curRowNr 
                + " of " + rowCount + " rows");
    }
}

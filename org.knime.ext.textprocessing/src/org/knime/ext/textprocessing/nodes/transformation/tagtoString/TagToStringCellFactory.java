/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;

import java.util.Hashtable;
import java.util.List;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringCellFactory implements CellFactory {

    private int m_termColIndex = -1;
    
    private List<String> m_tagTypes;
    
    private DataTableSpec m_oldSpec;
    
    private DataCell m_missingCell;
    
    public TagToStringCellFactory(final int termColIndex, 
            final List<String> tagTypes, final DataTableSpec oldSpec,
            final String missingValue) {
        if (tagTypes == null) {
            throw new IllegalArgumentException(
                    "Set of valid tag types must not be null!");
        }
        if (termColIndex < 0) {
            throw new IllegalArgumentException(
                    "Index of term column must be >= 0!");            
        }
        if (oldSpec == null) {
            throw new IllegalArgumentException(
                    "DataTableSpec must not be null!");            
        }
        m_termColIndex = termColIndex;
        m_tagTypes = tagTypes;
        m_oldSpec = oldSpec;
        
        if (missingValue == null || missingValue.equals(
                TagToStringNodeModel.MISSING_CELL_VALUE)) {
            m_missingCell = DataType.getMissingCell();
        } else {
            m_missingCell = new StringCell(missingValue);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {        
        Term t = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
        // get tag types and values
        Hashtable<String, String> appliedTagTypes = 
            new Hashtable<String, String>();
        for (Tag tag : t.getTags()) {
            appliedTagTypes.put(tag.getTagType(), tag.getTagValue());
        }
        // create new data cells
        DataCell[] newCells = new DataCell[m_tagTypes.size()];
        int i = 0;
        for (String tagType : m_tagTypes) {
            String tagVal = appliedTagTypes.get(tagType); 
            if (tagVal != null) {
                newCells[i] = new StringCell(tagVal);
            } else {
                newCells[i] = m_missingCell;
            }
            i++;
        }
        return newCells;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        return TagToStringNodeModel.getDataTableSpec(m_tagTypes, m_oldSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(int curRowNr, int rowCount, RowKey lastKey,
            ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Converting tag to string of row: " + curRowNr 
                + " of " + rowCount + " rows");
    }
}

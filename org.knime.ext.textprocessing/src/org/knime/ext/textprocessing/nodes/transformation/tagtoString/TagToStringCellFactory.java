/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
    
    /**
     * Creates new instance of <code>TagToStringCellFactory</code>.
     * 
     * @param termColIndex The index of the term column.
     * @param tagTypes The tag types to consider.
     * @param oldSpec The incoming spec.
     * @param missingValue The missing-value value.
     */
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
    public void setProgress(final int curRowNr, final int rowCount, 
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Converting tag to string of row: " + curRowNr 
                + " of " + rowCount + " rows");
    }
}

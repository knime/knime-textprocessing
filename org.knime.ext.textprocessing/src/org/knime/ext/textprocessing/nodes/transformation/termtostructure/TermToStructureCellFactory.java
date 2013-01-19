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
 *   26.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termtostructure;

import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;

import uk.ac.cam.ch.wwmm.oscar.Oscar;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ChemicalStructure;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.FormatType;
import uk.ac.cam.ch.wwmm.oscar.chemnamedict.entities.ResolvedNamedEntity;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStructureCellFactory implements CellFactory {

    private int m_termColIndex = -1;

    private String m_newColName;

    private Oscar m_oscar;

    private FormatType m_type;

    /**
     * Creates a new instance of <code>TermToStructureCellFactory</code> with
     * the given index of the term cell column and the name of the column to
     * append.
     *
     * @param termColindex The index of the term cell column.
     * @param newColName The name of the column to append.
     * @param type The type of the format to convert the structure to.
     * @throws InvalidSettingsException if the given index of the term column
     * is less than zero.
     */
    public TermToStructureCellFactory(final int termColindex,
            final String newColName, final FormatType type)
    throws InvalidSettingsException {
        if (termColindex < 0) {
            throw new InvalidSettingsException(
                    "The specified term column index is not valid!");
        }
        m_termColIndex = termColindex;
        m_newColName = newColName;
        m_oscar = new Oscar();
        m_type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DataCell cell = row.getCell(m_termColIndex);
        if (cell.isMissing()) {
            return new DataCell[]{DataType.getMissingCell()};
        }
        Term term = ((TermValue)cell).getTermValue();
        List<ResolvedNamedEntity> entities =
            m_oscar.findAndResolveNamedEntities(term.getText());
        for (ResolvedNamedEntity ne : entities) {
            ChemicalStructure inchi = ne.getFirstChemicalStructure(m_type);
            if (inchi != null) {
                return new DataCell[]{new StringCell(inchi.getValue())};
            }
        }
        return new DataCell[]{DataType.getMissingCell()};
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

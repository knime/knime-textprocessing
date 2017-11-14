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
 * -------------------------------------------------------------------
 *
 * History
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import java.util.Hashtable;
import java.util.List;

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
        if (!row.getCell(m_termColIndex).isMissing()) {
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
        } else {
            return new DataCell[]{DataType.getMissingCell()};
        }
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
     *
     * @since 3.5
     */
    @Override
    public void setProgress(final long curRowNr, final long rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Converting tag to string of row: " + curRowNr
                + " of " + rowCount + " rows");
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated Use {@link TagToStringCellFactory#setProgress(long, long, RowKey, ExecutionMonitor)} instead.
     */
    @Deprecated
    @Override
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Converting tag to string of row: " + curRowNr
                + " of " + rowCount + " rows");
    }
}

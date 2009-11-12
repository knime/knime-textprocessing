/*
========================================================================
 *
 *  Copyright (C) 2003 - 2009
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
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.node.ExecutionMonitor;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;


/**
 * The {@link CellFactory} implementation of the DocumentDataExtractor node
 * that creates a cell for each selected document property.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorCellFactory implements CellFactory {

    private final DocumentDataExtractor[] m_extractors;
    private final int m_docColIdx;
    private final DataColumnSpec[] m_columnSpecs;

    /**Constructor for class DocumentExtractorCellFactory.
     * @param docColIdx the index of the document column
     * @param columnSpecs the {@link DataColumnSpec}s to return in the same
     * order as the extractors
     * @param extractors the {@link DocumentDataExtractor}s to use
     */
    public DocumentDataExtractorCellFactory(final int docColIdx,
            final DataColumnSpec[] columnSpecs,
            final DocumentDataExtractor[] extractors) {
        if (columnSpecs == null || columnSpecs.length < 1) {
            throw new NullPointerException(
                    "column specs must not be empty");
        }
        if (docColIdx < 0) {
            throw new IllegalArgumentException("Invalid document column");
        }
        if (extractors == null || extractors.length < 1) {
            throw new IllegalArgumentException("extractors must not be empty");
        }
        if (columnSpecs.length != extractors.length) {
            throw new IllegalArgumentException(
                    "Column specs and extractors must have the same sice");
        }
        m_columnSpecs = columnSpecs;
        m_docColIdx = docColIdx;
        m_extractors = extractors;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DataCell cell = row.getCell(m_docColIdx);
        Document doc = null;
        if (cell instanceof DocumentValue) {
            final DocumentValue docCell = (DocumentValue)cell;
            doc = docCell.getDocument();
        } else {
            throw new IllegalStateException("Invalid column type");
        }
        final DataCell[] cells = new DataCell[m_extractors.length];
        for (int i = 0, length = m_extractors.length; i < length; i++) {
            cells[i] = m_extractors[i].getValue(doc);
        }
        return cells;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        return m_columnSpecs;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount,
            final RowKey lastKey, final ExecutionMonitor exec) {
        exec.setProgress(1.0 / rowCount * curRowNr,
                "Processing row " + curRowNr + " of " + rowCount);
    }
}

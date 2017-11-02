/*
========================================================================
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
 * @since 3.4
 */
public class DocumentDataExtractorCellFactory2 implements CellFactory {

    private final DocumentDataExtractor2[] m_extractors;
    private final int m_docColIdx;
    private final DataColumnSpec[] m_columnSpecs;

    /**Constructor for class DocumentExtractorCellFactory.
     * @param docColIdx the index of the document column
     * @param columnSpecs the {@link DataColumnSpec}s to return in the same
     * order as the extractors
     * @param extractors the {@link DocumentDataExtractor}s to use
     */
    public DocumentDataExtractorCellFactory2(final int docColIdx,
            final DataColumnSpec[] columnSpecs,
            final DocumentDataExtractor2[] extractors) {
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

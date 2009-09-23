/*
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorCellFactory implements CellFactory {

    private final DocumentDataExtractor[] m_extractors;
    private final int m_docColIdx;

    /**Constructor for class DocumentExtractorCellFactory.
     * @param docColIdx the index of the document column
     * @param extractors the {@link DocumentDataExtractor}s to use
     */
    public DocumentDataExtractorCellFactory(final int docColIdx,
            final DocumentDataExtractor[] extractors) {
        m_docColIdx = docColIdx;
        if (docColIdx < 0) {
            throw new IllegalArgumentException("Invalid document column");
        }
        if (extractors == null || extractors.length < 1) {
            throw new IllegalArgumentException("extractors must not be empty");
        }
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
        final DataColumnSpec[] specs = new DataColumnSpec[m_extractors.length];
        for (int i = 0, length = m_extractors.length; i < length; i++) {
            specs[i] = m_extractors[i].getColumnSpec();
        }
        return specs;
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

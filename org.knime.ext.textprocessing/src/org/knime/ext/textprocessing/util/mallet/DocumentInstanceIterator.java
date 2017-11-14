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
 * ---------------------------------------------------------------------
 *
 * Created on 20.05.2013 by koetter
 */
package org.knime.ext.textprocessing.util.mallet;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;

import cc.mallet.types.Instance;

/**
 * Helper class that converts a {@link BufferedDataTable} with documents into an {@link Instance} {@link Iterator}
 * whereas each {@link Instance} represents a {@link Document} which can be retrieved using the
 * {@link Instance#getData()} method.
 *
 * @author Tobias Koetter, KNIME AG, Zurich, Switzerland
 */
public class DocumentInstanceIterator implements Iterator<Instance> {
    private final int m_docColIdx;
    private CloseableRowIterator m_iterator;
    private Document m_next = null;
    private final ExecutionMonitor m_exec;
    private final long m_noOfRows;
    private int m_rowCounter = 0;
    private final Set<UUID> m_processedDocUUIDs = new HashSet<>();
    private final boolean m_checkDuplicates;


    /**
     * @param exec {@link ExecutionMonitor} to provide progress
     * @param table the {@link BufferedDataTable} that contains the documents to use
     * @param docColIdx the index of the document column
     * @param checkDuplicates <code>true</code> if duplicate documents should be ignored
     */
    public DocumentInstanceIterator(final ExecutionMonitor exec, final BufferedDataTable table, final int docColIdx,
        final boolean checkDuplicates) {
        if (table == null) {
            throw new IllegalArgumentException("table must not be null");
        }
        if (docColIdx < 0) {
            throw new IllegalArgumentException("Document column index must not be negative");
        }
        if (!table.getDataTableSpec().getColumnSpec(docColIdx).getType().isCompatible(DocumentValue.class)) {
            throw new IllegalArgumentException("Selected column with index "
        + docColIdx + " does not contain documents");
        }
        m_exec = exec;
        m_docColIdx = docColIdx;
        m_noOfRows = table.size();
        m_iterator = table.iterator();
        m_checkDuplicates = checkDuplicates;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        try {
            if (m_next == null) {
                while (m_iterator.hasNext()) {
                    m_exec.setProgress(++m_rowCounter / (double) m_noOfRows,
                        "Reading row " + m_rowCounter + " of " + m_noOfRows
                        + "-" + m_processedDocUUIDs.size() + " documents processed so far");
                    m_exec.checkCanceled();
                    final DataRow row = m_iterator.next();
                    final DataCell cell = row.getCell(m_docColIdx);
                    if (cell instanceof DocumentValue) {
                        final Document doc = ((DocumentValue)cell).getDocument();
                        final UUID uuid = doc.getUUID();
                        if (!m_checkDuplicates || m_processedDocUUIDs.add(uuid)) {
                            m_next = doc;
                            break;
                        }
                    }
                }
            }
            return m_next != null;
        } catch (CanceledExecutionException e) {
            //return no more docs since the user has canceled the execution
            return false;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Instance next() {
        final Instance instance = createDocInstance(m_next);
        m_next = null;
        return instance;
    }


    /**
     * @param doc the {@link Document} to convert
     * @return the {@link Instance} representing the {@link Document}
     */
    public static Instance createDocInstance(final Document doc) {
        return new Instance(doc, null, doc.getTitle(), doc.getDocFile());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}

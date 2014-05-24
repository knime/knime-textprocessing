/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * Provides a row by row preprocessing strategy.
 *
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class RowPreprocessor extends AbstractPreprocessor {

    private AtomicInteger m_currRow;

    private int m_noRows = 0;

    private HashMap<UUID, DataCell> m_preprocessedDocuments;

    private HashMap<DataCell, Set<Term>> m_addedRows;

    private TermPreprocessing m_termPreprocessing = null;

    /**
     * Creates new instance of <code>RowPreprocessor</code>.
     */
    public RowPreprocessor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPreprocessing() throws InvalidSettingsException {
        if (!(m_preprocessing instanceof TermPreprocessing)) {
            throw new InvalidSettingsException("Specified preprocessing "
                    + "instance is not an instance of TermPreprocessing!");
        }
        m_termPreprocessing = (TermPreprocessing)m_preprocessing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BufferedDataTable applyPreprocessing(final BufferedDataTable inData, final ExecutionContext exec)
        throws Exception {
        m_currRow = new AtomicInteger(0);
        m_noRows = inData.getRowCount();
        m_exec = exec;
        m_docCellFac.prepare(m_exec);

        m_dc = exec.createDataContainer(m_fac.createDataTableSpec(m_appendIncomingDocument));
        // create hash map with appropriate size
        m_preprocessedDocuments = new HashMap<UUID, DataCell>(m_noRows);
        // size is not known before hand but minimum number of documents
        m_addedRows = new HashMap<DataCell, Set<Term>>(m_noRows);

        final RowIterator i = inData.iterator();
        while (i.hasNext()) {
            exec.checkCanceled();
            final DataRow row = i.next();

            setProgress();
            processRow(row);
        }
        m_dc.close();
        m_preprocessedDocuments.clear();
        m_addedRows.clear();
        return m_dc.getTable();
    }

    private void setProgress() {
        final int curr = m_currRow.incrementAndGet();
        final double prog = (double)curr / (double)m_noRows;
        m_exec.setProgress(prog, "Preprocessing row " + curr + " of " + m_noRows);
    }

    /**
     * Preprocesses the given row.
     * @param row The row to apply preprocessing step on.
     */
    private void processRow(final DataRow row) {
        DataCell newDocCell = null;
        final RowKey rowKey = row.getKey();
        final DataCell termcell = row.getCell(m_termColIndex);
        final DataCell doccell = row.getCell(m_documentColIndex);
        final DataCell origDocCell = row.getCell(m_origDocumentColIndex);

        // handle missing value (ignore rows with missing values)
        if (termcell.isMissing() || doccell.isMissing()) {
            return;
        }
        Term term = ((TermValue)termcell).getTermValue();

        // is the term unmodifiable ???
        if (!term.isUnmodifiable() || m_preprocessUnmodifiable) {
            term = m_termPreprocessing.preprocessTerm(term);

            // if term is null or empty continue with next term !
            if (term == null || term.getText().length() <= 0) {
                return;
            }
        }
        // do we have to preprocess the documents itself too ?
        if (m_deepPreprocessing) {
            final Document doc = ((DocumentValue)doccell).getDocument();
            newDocCell = m_preprocessedDocuments.get(doc.getUUID());

            // deep-preprocess only if document has not been preprocessed yet
            // (is not in cache m_preprocessedDocuments)
            if (newDocCell == null) {
                newDocCell =
                    m_docCellFac.createDataCell(PreprocessingUtils.deepPPWithPreprocessing(doc, m_termPreprocessing,
                        m_preprocessUnmodifiable));
                m_preprocessedDocuments.put(doc.getUUID(), newDocCell);
            }
        } else {
            // new doc is the same as the old doc
            newDocCell = doccell;
        }
        addRowToContainer(rowKey, term, newDocCell, origDocCell);
    }

    private synchronized void addRowToContainer(final RowKey rk, final Term t, final DataCell preprocessedDoc,
        final DataCell origDoc) {
        Set<Term> terms = m_addedRows.get(preprocessedDoc);
        if (terms == null) {
            terms = new HashSet<Term>();
        } else if (terms.contains(t)) {
            // do not add row, since this preprocessed term has already been added.
            return;
        }
        // if term has not been added, memorize it
        terms.add(t);
        m_addedRows.put(preprocessedDoc, terms);

        // add row with or without unchanged document.
        final DataRow row;
        if (m_appendIncomingDocument) {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), preprocessedDoc, origDoc);
        } else {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), preprocessedDoc);
        }
        m_dc.addRowToTable(row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyMinimumTermCells(1, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateSettings(final int documentColIndex, final int origDocumentColIndex, final int termColIndex,
        final boolean deepPrepro, final boolean appendOrigDoc, final boolean preproUnmodifiable)
                throws InvalidSettingsException {
        if (documentColIndex < 0) {
            throw new InvalidSettingsException("Index of document column [" + documentColIndex + "] is not valid!");
        }
        if (origDocumentColIndex < 0 && appendOrigDoc) {
            throw new InvalidSettingsException("Index of original document column [" + origDocumentColIndex
                + "] is not valid!");
        }
        if (termColIndex < 0) {
            throw new InvalidSettingsException("Index of term column [" + termColIndex + "] is not valid!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec createDataTableSpec(final boolean appendIncomingDocument) {
        return m_fac.createDataTableSpec(appendIncomingDocument);
    }
}

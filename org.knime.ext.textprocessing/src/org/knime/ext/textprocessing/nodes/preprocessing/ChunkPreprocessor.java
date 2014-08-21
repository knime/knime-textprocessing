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
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentChunk;

/**
 * Provides a chunk wise preprocessing strategy.
 *
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class ChunkPreprocessor extends AbstractPreprocessor {

    private AtomicInteger m_currRow;

    private int m_noRows = 0;

    private ChunkPreprocessing m_chunkPreprocessing = null;

    /**
     * Creates new instance of <code>ChunkPreprocessor</code>.
     */
    public ChunkPreprocessor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPreprocessing() throws InvalidSettingsException {
        if (!(m_preprocessing instanceof ChunkPreprocessing) && !(m_preprocessing instanceof TermPreprocessing)) {
            throw new InvalidSettingsException("Specified preprocessing "
                + "instance is not an instance of ChunkPreprocessing!");
        } else if ((m_preprocessing instanceof TermPreprocessing)) {
            m_chunkPreprocessing = new ChunkToTermPreprocessingAdapter((TermPreprocessing)m_preprocessing);
        } else {
            m_chunkPreprocessing = (ChunkPreprocessing)m_preprocessing;
        }
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
        m_docCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(m_exec));

        // sort data table by documents
        final String docColName = inData.getDataTableSpec().getColumnSpec(m_documentColIndex).getName();
        final List<String> colList = new ArrayList<String>();
        colList.add(docColName);

        m_exec.setMessage("Sorting input table");
        final ExecutionContext subEC = m_exec.createSubExecutionContext(0.3);
        final SortedTable sortedTable = new SortedTable(inData, colList, new boolean[]{true}, subEC);
        final BufferedDataTable sortedBDT = exec.createBufferedDataTable(sortedTable, subEC);

        // prepare for chunking
        List<DataRow> chunk = new ArrayList<DataRow>();
        Document lastDoc = null;

        // go through data table, chunk and preprocess chunk when ready.
        m_exec.setMessage("Grouping");
        final ExecutionMonitor subExec = m_exec.createSubExecutionContext(1.0);
        m_dc = exec.createDataContainer(m_fac.createDataTableSpec(m_appendIncomingDocument));

        final RowIterator i = sortedBDT.iterator();
        while (i.hasNext()) {
            m_exec.checkCanceled();
            final DataRow row = i.next();
            setProgress(subExec);

            final Document currDoc = ((DocumentValue)row.getCell(m_documentColIndex)).getDocument();

            if (lastDoc == null || currDoc.equals(lastDoc)) {
                // add document to chunk
                chunk.add(row);
            } else {
                // preprocess chunk and add nurrent row to new chunk
                processChunk(chunk);
                chunk.clear();
                chunk.add(row);
            }
            lastDoc = currDoc;
        }
        // preprocess the last chunk
        processChunk(chunk);
        chunk.clear();

        // create output data table
        m_dc.close();
        return m_dc.getTable();
    }

    private void setProgress(final ExecutionMonitor exec) {
        final int curr = m_currRow.incrementAndGet();
        final double prog = (double)curr / (double)m_noRows;
        exec.setProgress(prog, "Preprocessing row " + curr + " of " + m_noRows);
    }

    /**
     * Preprocesses the given chunk of rows.
     * @param chunk The chunk of rows to apply preprocessing step on.
     */
    private void processChunk(final List<DataRow> chunk) {
        if (chunk != null && chunk.size() > 0) {
            // To save unmodifieable term that will not be preprocessed
            final Set<Term> unmodifieableTerms = new HashSet<Term>();

            // CREATE DOCUMENT CHUNK
            DataCell newDocCell = null;
            final DataCell docCell = chunk.get(0).getCell(m_documentColIndex);
            final DataCell origDocCell = chunk.get(0).getCell(m_origDocumentColIndex);
            final Document document = ((DocumentValue)docCell).getDocument();

            // check for missing document cell
            if (docCell.isMissing()) {
                return;
            }
            // get all terms
            final Set<Term> termSet = new HashSet<Term>();
            for (final DataRow row : chunk) {
                final DataCell termcell = row.getCell(m_termColIndex);
                // handle missing value (ignore rows with missing values)
                if (termcell.isMissing()) {
                    continue;
                }
                final Term term = ((TermValue)termcell).getTermValue();

                if (!term.isUnmodifiable() || m_preprocessUnmodifiable) {
                    // save term in order to preprocess it.
                    termSet.add(term);
                } else {
                    // save term in order to add it unmodified to the data
                    // container.
                    unmodifieableTerms.add(term);
                }
            }

            // APPLY CHUNK PREPROCESSING
            final Hashtable<Term, Term> termMapping =
                m_chunkPreprocessing.preprocessChunk(new DocumentChunk(document, termSet));

            // DEEP PREPROCESSING
            if (m_deepPreprocessing && termMapping != null) {
                newDocCell =
                    m_docCellFac.createDataCell(PreprocessingUtils.deepPPWithTermMapping(document, termMapping,
                        m_preprocessUnmodifiable));
            } else {
                newDocCell = docCell;
            }

            // CREATE DATA TABLE
            // first add unchanged terms
            for (final Term t : unmodifieableTerms) {
                addRowToContainer(t, newDocCell, origDocCell);
            }
            // than add preprocessed terms
            if (termMapping != null) {
                final Set<Term> uniqeTerms = new HashSet<Term>(termMapping.values());
                for (final Term t : uniqeTerms) {
                    // if term is null or empty don't add it to data table
                    if (t != null && t.getText().length() > 0) {
                        addRowToContainer(t, newDocCell, origDocCell);
                    }
                }
            }
        }
    }

    private int m_rowIndex = 0;

    private void addRowToContainer(final Term t, final DataCell preprocessedDoc, final DataCell origDoc) {
        // add row with or without unchanged document.
        final DataRow row;
        final RowKey rk = RowKey.createRowKey(m_rowIndex);
        m_rowIndex++;
        if (m_appendIncomingDocument) {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t),
                    preprocessedDoc, origDoc);
        } else {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t),
                    preprocessedDoc);
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

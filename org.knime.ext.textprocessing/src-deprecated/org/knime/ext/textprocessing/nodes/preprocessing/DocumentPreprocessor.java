/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * Created on 23.10.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * Provides a row by row preprocessing strategy on a data table of documents (not a bag of words as the
 * {@link RowPreprocessor}). This preprocessor applies only deep preprocessing of documents and thus does not require
 * and create a bag of words.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 * @deprecated use {@link StreamablePreprocessingNodeModel} instead.
 */
@Deprecated
public final class DocumentPreprocessor extends AbstractPreprocessor {

    private AtomicInteger m_currRow;

    private int m_noRows = 0;

    private TermPreprocessing m_termPreprocessing = null;

    /**
     * Empty constructor of {@link DocumentPreprocessor}.
     */
    public DocumentPreprocessor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkPreprocessing() throws InvalidSettingsException {
        if (!TermPreprocessing.class.isInstance(m_preprocessing)) {
            throw new InvalidSettingsException("This preprocessing cannot be applied on list of documents.");
        }
        m_termPreprocessing = (TermPreprocessing)m_preprocessing;
    }

    /**
     * Creates the data table spec of the data table created by the {@link DocumentPreprocessor}.
     * @return The created data table spec.
     */
    private DataTableSpec createDataTableSpec() {
        final DataColumnSpecCreator docs =
            new DataColumnSpecCreator(BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME, m_docCellFac.getDataType());
        final DataColumnSpecCreator docsOrig =
            new DataColumnSpecCreator(BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME, m_docCellFac.getDataType());

        if (!m_appendIncomingDocument) {
            return new DataTableSpec(docs.createSpec());
        }
        return new DataTableSpec(docs.createSpec(), docsOrig.createSpec());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable applyPreprocessing(final BufferedDataTable inData, final ExecutionContext exec)
        throws Exception {
        // check deep preprocessing
        if (!m_deepPreprocessing) {
            throw new IllegalStateException("Deep preprocessing must be enabled to apply document preprocessing.");
        }

        m_currRow = new AtomicInteger(0);
        m_noRows = inData.getRowCount();
        m_exec = exec;
        m_docCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(m_exec));

        m_dc = exec.createDataContainer(createDataTableSpec());

        final RowIterator i = inData.iterator();
        while (i.hasNext()) {
            m_exec.checkCanceled();
            setProgress();

            final DataRow row = i.next();
            final RowKey rk = row.getKey();
            final DataCell documentCell = row.getCell(m_documentColIndex);
            final DataCell origDocumentCell = row.getCell(m_origDocumentColIndex);

            // apply preprocessing
            final DataCell preprocessedDocumentCell = deepPreprocessing(documentCell);

            // add row to data container
            m_dc.addRowToTable(createDataRow(rk, preprocessedDocumentCell, origDocumentCell));
        }

        m_dc.close();
        return m_dc.getTable();
    }

    /**
     * Sets progress to execution context.
     */
    private void setProgress() {
        int curr = m_currRow.incrementAndGet();
        double prog = curr / (double)m_noRows;
        m_exec.setProgress(prog, "Preprocessing row " + curr + " of " + m_noRows);
    }

    /**
     * Applies deep preprocesing to document contained in given data cell.
     *
     * @param docCell The cell containing the document to preprocess.
     * @return A new data cell containing the preprocessed document.
     */
    private DataCell deepPreprocessing(final DataCell docCell) {
        if (!docCell.getType().isCompatible(DocumentValue.class)) {
            throw new IllegalArgumentException("DataCell is not compaitle with DocumentValue!");
        }

        if (m_deepPreprocessing && !docCell.isMissing()) {
            return m_docCellFac.createDataCell(PreprocessingUtils.deepPPWithPreprocessing(
                ((DocumentValue)docCell).getDocument(), m_termPreprocessing, m_preprocessUnmodifiable));
        }

        return docCell;
    }

    /**
     * Creates new data row with given documents and row key. If appendIncomingDocument flag is {@code true} the
     * original document will be added to row as well, otherwise not.
     *
     * @param rk The row key of the row to create.
     * @param preprocessedDoc The preprocessed document to add.
     * @param origDoc The original document to add.
     * @return The create data row with row key and documents.
     */
    private DataRow createDataRow(final RowKey rk, final DataCell preprocessedDoc, final DataCell origDoc) {
        final DataRow row;
        if (m_appendIncomingDocument) {
            row = new DefaultRow(rk, preprocessedDoc, origDoc);
        } else {
            row = new DefaultRow(rk, preprocessedDoc);
        }
        return row;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
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
        if (!deepPrepro) {
            throw new InvalidSettingsException("Deep preprocessing must be enabled!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataTableSpec createDataTableSpec(final boolean appendIncomingDocument) {
        final DataColumnSpecCreator docs = new DataColumnSpecCreator(BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME,
            m_docCellFac.getDataType());
        final DataColumnSpecCreator origDoc =
            new DataColumnSpecCreator(BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME, m_docCellFac.getDataType());

        if (!appendIncomingDocument) {
            return new DataTableSpec(docs.createSpec());
        }
        return new DataTableSpec(docs.createSpec(), origDoc.createSpec());
    }
}

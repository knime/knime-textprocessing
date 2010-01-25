/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.bow;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The model class of the Bag of word creator node. The method
 * {@link BagOfWordsNodeModel#configure(DataTableSpec[])} validates the incoming
 * {@link org.knime.core.data.DataTableSpec}. Only one column containing
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s is allowed.
 * The output table contains two columns. The first consists of
 * {@link org.knime.ext.textprocessing.data.TermCell}s the second of
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s. A single row
 * represents a tupel of a term and a document meaning that the term is
 * contained in the document.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class BagOfWordsNodeModel extends NodeModel {

    private int m_documentColIndex = -1;

    private BagOfWordsDataTableBuilder m_dtBuilder = 
        new BagOfWordsDataTableBuilder();
    
    private TextContainerDataCellFactory m_termFac = 
        TextContainerDataCellFactoryBuilder.createTermCellFactory();

    private int m_rowId = 1;
    
    /**
     * Creates a new instance of <code>BagOfWordsNodeModel</code> with one in
     * and one data table out port.
     */
    public BagOfWordsNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    private void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyDocumentCell(true);
        m_documentColIndex = verifier.getDocumentCellIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        // sort list of documents
        ExecutionContext subExec = exec.createSubExecutionContext(0.3);
        List<String> sortBy = new ArrayList<String>();
        sortBy.add(inData[0].getDataTableSpec().getColumnSpec(
                m_documentColIndex).getName());
        BufferedDataTable sortedTable = new SortedTable(inData[0], sortBy,
                new boolean[]{false}, false, subExec).getBufferedDataTable();

        // prepare data container
        BufferedDataContainer bdc = exec.createDataContainer(
                m_dtBuilder.createDataTableSpec());

        ExecutionContext subExec2 = exec.createSubExecutionContext(0.7);
        Document currDoc = null;
        Document lastDoc = null;
        DataCell docCell = null;
        int rowCount = sortedTable.getRowCount();
        int currRow = 1;
        RowIterator it = sortedTable.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();

            // get terms for document
            docCell = row.getCell(m_documentColIndex);
            DocumentValue docVal = (DocumentValue)row
                    .getCell(m_documentColIndex);

            currDoc = docVal.getDocument();
            if (lastDoc == null || !currDoc.equals(lastDoc)) {
                lastDoc = currDoc;

                Set<Term> terms = setOfTerms(currDoc);
                addToBOW(terms, docCell, bdc);
            }

            // report status
            double progress = (double)currRow / (double)rowCount;
            subExec2.setProgress(progress, "Processing document " + currRow
                    + " of " + rowCount);
            exec.checkCanceled();
            currRow++;
        }
        
        bdc.close();
        return new BufferedDataTable[]{bdc.getTable()};
    }

    private void addToBOW(final Set<Term> terms, final DataCell docCell, 
            final BufferedDataContainer bdc) {
        for (Term t : terms) {
            RowKey key = RowKey.createRowKey(m_rowId);
            DataCell tc = m_termFac.createDataCell(t);
            DataRow newRow = new DefaultRow(key, tc, docCell);
            bdc.addRowToTable(newRow);
            m_rowId++;
        } 
    }

    private Set<Term> setOfTerms(final Document doc) {
        Set<Term> termSet = null;
        if (doc != null) {
            termSet = new HashSet<Term>();

            Iterator<Sentence> it = doc.sentenceIterator();
            while (it.hasNext()) {
                Sentence sen = it.next();
                termSet.addAll(sen.getTerms());
            }
        }
        return termSet;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }
}

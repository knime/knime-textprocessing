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
 *   22.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * Provides methods to purge terms out of documents based on the specified
 * bag of words data table. The data table contains terms and documents. All
 * terms of the documents which are not contained in the bag of words
 * (the term column of the data table) are purged from the documents, except
 * those which are unmodifiable.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermPurger {

    private Set<Term> m_terms;

    private BufferedDataTable m_inData;

    private int m_termColIndex;

    private int m_docColIndex;

    private ExecutionContext m_exec;

    /**
     * Creates a new instance of <code>TermPurger</code> with given bag of word
     * input data contained in the data table and an
     * <code>ExecutionContext</code> to create a <code>BufferedDataTable</code>
     * and monitor the progress.
     *
     * @param inData The input data table containing a bag of words.
     * @param exec A execution context to monitor the progress.
     * @param documentColumnName The name of the column containing the documents
     * to apply the filtering to.
     * @throws InvalidSettingsException If the given data table contains no
     * column with documents or terms.
     */
    public TermPurger(final BufferedDataTable inData,
            final ExecutionContext exec, final String documentColumnName)
    throws InvalidSettingsException {
        m_inData = inData;

        DataTableSpecVerifier verifier = new DataTableSpecVerifier(
                inData.getDataTableSpec());
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
        m_docColIndex = inData.getDataTableSpec().findColumnIndex(
                documentColumnName);
        if (!inData.getDataTableSpec().getColumnSpec(m_docColIndex).getType()
                .isCompatible(DocumentValue.class)) {
            throw new InvalidSettingsException("Specified column name \""
                    + documentColumnName + "\" contains no document values!");
        }
        m_exec = exec;

        cacheTerms();
    }

    /**
     * Clears the cached terms.
     */
    public void cleaTerms() {
        m_terms.clear();
    }

    private void cacheTerms() {
        m_terms = new HashSet<Term>();
        RowIterator it = m_inData.iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Term t = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
            if (!m_terms.contains(t)) {
                m_terms.add(t);
            }
        }
    }

    /**
     * Deletes all terms out of the documents which are not contained in the
     * specified bag of words, creates a new data table with the modified
     * documents and returns it.
     *
     * @return A data table containing the modified documents.
     * @throws CanceledExecutionException If progress was canceled
     */
    public BufferedDataTable getPurgedDataTable()
    throws CanceledExecutionException {
        Hashtable<Document, Document> preprocessedDoc =
            new Hashtable<Document, Document>();

        ExecutionContext subExec = m_exec.createSubExecutionContext(0.7);
        int currRow = 1;
        int maxRows = m_inData.getRowCount();
        RowIterator it = m_inData.iterator();
        while (it.hasNext()) {
            subExec.checkCanceled();
            double prog = (double)currRow / (double)maxRows;
            subExec.setProgress(prog,
                    "Preprocessing row " + currRow + " of " + maxRows);
            currRow++;

            DataRow row = it.next();
            Document origDoc = ((DocumentValue)row.getCell(m_docColIndex))
                            .getDocument();

            // purge only if not purged yet
            if (!preprocessedDoc.containsKey(origDoc)) {
                Document purgedDocument = purgeDocument(origDoc);
                preprocessedDoc.put(origDoc, purgedDocument);
            }
        }

        return createNewDataTable(preprocessedDoc);
    }

    private BufferedDataTable createNewDataTable(
            final Hashtable<Document, Document> preprocessedDoc)
    throws CanceledExecutionException {

        TextContainerDataCellFactory docCellFac =
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataCellCache dataCellCache = new LRUDataCellCache(docCellFac);
        BufferedDataContainer dc = m_exec.createDataContainer(
            m_inData.getDataTableSpec());

        try {
            ExecutionContext subExec = m_exec.createSubExecutionContext(0.3);
            int currRow = 1;
            int maxRows = m_inData.getRowCount();
            RowIterator it = m_inData.iterator();
            while (it.hasNext()) {
                m_exec.checkCanceled();
                double prog = (double)currRow / (double)maxRows;
                subExec.setProgress(prog, "Adding row " + currRow + " of " + maxRows);
                currRow++;

                DataRow row = it.next();
                Document origDoc = ((DocumentValue)row.getCell(m_docColIndex)).getDocument();

                // add all cells of old data table except the document cell,
                // which has to be re-created with the purged document.
                DataCell[] cells = new DataCell[row.getNumCells()];
                for (int i = 0; i < row.getNumCells(); i++) {
                    if (i == m_docColIndex) {
                        DataCell docCell = dataCellCache.getInstance(preprocessedDoc.get(origDoc));
                        cells[i] = docCell;
                    } else {
                        cells[i] = row.getCell(i);
                    }
                }

                DataRow newRow = new DefaultRow(row.getKey(), cells);
                dc.addRowToTable(newRow);
            }
        } finally {
            dc.close();
            dataCellCache.close();
        }

        return dc.getTable();
    }

    private Document purgeDocument(final Document doc) {
        Document newDoc;

        DocumentBuilder builder = new DocumentBuilder(doc);
        List<Section> sections = doc.getSections();
        for (Section s : sections) {
            List<Paragraph> paragraphs = s.getParagraphs();
            for (Paragraph p : paragraphs) {
                List<Sentence> sentences = p.getSentences();
                for (Sentence sen : sentences) {
                    List<Term> senTerms = sen.getTerms();
                    for (Term t : senTerms) {
                        // if not unmodifiable and not contains in terms set,
                        // set t null.
                        if (!t.isUnmodifiable()) {
                            if (!m_terms.contains(t)) {
                                t = null;
                            }
                        }
                        if (t != null && t.getText().length() > 0) {
                            builder.addTerm(t);
                        }
                    }
                    builder.createNewSentence();
                }
                builder.createNewParagraph();
            }
            builder.createNewSection(s.getAnnotation());
        }
        newDoc = builder.createDocument();

        return newDoc;
    }
}

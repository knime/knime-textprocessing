/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a row by row preprocessing strategy.
 * 
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class RowPreprocessor extends AbstractPreprocessor {

    private AtomicInteger m_currRow;
    
    private int m_noRows = 0;
    
    private HashMap<Document, DataCell> m_preprocessedDocuments;
    
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
    public BufferedDataTable applyPreprocessing(
            final BufferedDataTable inData, final ExecutionContext exec) 
    throws Exception {        
        m_currRow = new AtomicInteger(0);
        m_noRows = inData.getRowCount();
        m_exec = exec;        
        
        m_dc = exec.createDataContainer(m_fac.createDataTableSpec(
                m_appendIncomingDocument));
        m_preprocessedDocuments = new HashMap<Document, DataCell>();
        m_addedRows = new HashMap<DataCell, Set<Term>>();
        
        RowIterator i = inData.iterator();
        while (i.hasNext()) {
            exec.checkCanceled();
            DataRow row = i.next();
            
            setProgress();
            processRow(row);
        }
        m_dc.close();
        m_preprocessedDocuments.clear();
        m_addedRows.clear();
        return m_dc.getTable();
    }

    private void setProgress() {
        int curr = m_currRow.incrementAndGet();
        double prog = (double)curr / (double)m_noRows;
        m_exec.setProgress(prog, "Preprocessing row " + curr + " of "
                        + m_noRows);
    }
    
    /**
     * Preprocesses the given row.
     * @param row The row to apply preprocessing step on.
     */
    private void processRow(final DataRow row) {
        DataCell newDocCell = null;
        RowKey rowKey = row.getKey();
        DataCell termcell = row.getCell(m_termColIndex);
        DataCell doccell = row.getCell(m_documentColIndex);
        DataCell origDocCell = row.getCell(m_origDocumentColIndex);

        // handle missing value (ignore rows with missing values)
        if (termcell.isMissing() || doccell.isMissing()) {
            return;
        }
        Term term = ((TermValue)termcell).getTermValue();

        //
        // do the preprocessing twist
        //
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
            Document doc = ((DocumentValue)doccell).getDocument();
            newDocCell = m_preprocessedDocuments.get(doc);

            if (newDocCell == null) {
                // preprocess doc here !!!
                DocumentBuilder builder = new DocumentBuilder(doc);
                for (Section s : doc.getSections()) {
                    for (Paragraph p : s.getParagraphs()) {
                        for (Sentence sen : p.getSentences()) {
                            for (Term t : sen.getTerms()) {
                                if (!t.isUnmodifiable() 
                                        || m_preprocessUnmodifiable) {
                                    t = m_termPreprocessing.preprocessTerm(t);
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
                Document newDoc = builder.createDocument();
                newDocCell = m_docCellFac.createDataCell(newDoc);
                m_preprocessedDocuments.put(doc, newDocCell);
            }
        } else {
            // new doc is the same as the old doc
            newDocCell = doccell;
        }
        addRowToContainer(rowKey, term, newDocCell, origDocCell);
    }
    
    private synchronized void addRowToContainer(final RowKey rk, final Term t, 
            final DataCell preprocessedDoc, final DataCell origDoc) {
        Set<Term> terms = m_addedRows.get(preprocessedDoc);
        if (terms == null) {
            terms = new HashSet<Term>();
        } else if (terms.contains(t)) {
            // do not add row, since this preprocessed term has already been 
            // added.
            return;
        }
        // if term has not been added, memorize it
        terms.add(t);
        m_addedRows.put(preprocessedDoc, terms);
        
        // add row with or without unchanged document.
        DataRow row;
        if (m_appendIncomingDocument) {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), 
                    preprocessedDoc, origDoc);
        } else {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), 
                    preprocessedDoc);
        }
        m_dc.addRowToTable(row);
    } 
}

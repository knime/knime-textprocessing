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
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.DocumentChunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
        if (!(m_preprocessing instanceof ChunkPreprocessing)) {
            throw new InvalidSettingsException("Specified preprocessing "
                    + "instance is not an instance of ChunkPreprocessing!");
        }
        m_chunkPreprocessing = (ChunkPreprocessing)m_preprocessing;
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
        
        // sort data table by documents
        String docColName = inData.getDataTableSpec().getColumnSpec(
                m_documentColIndex).getName();
        List<String> colList = new ArrayList<String>();
        colList.add(docColName);
        
        m_exec.setMessage("Sorting input table");
        ExecutionContext subEC = m_exec.createSubExecutionContext(0.3);
        SortedTable sortedTable = new SortedTable(inData, colList, 
                new boolean[]{true}, subEC);
        BufferedDataTable sortedBDT = exec.createBufferedDataTable(sortedTable,
                subEC);
        
        // prepare for chunking
        List<DataRow> chunk = new ArrayList<DataRow>();
        Document lastDoc = null;
        
        // go through data table, chunk and preprocess chunk when ready.
        m_exec.setMessage("Grouping");
        ExecutionMonitor subExec = m_exec.createSubExecutionContext(1.0);
        m_dc = exec.createDataContainer(m_fac.createDataTableSpec(
                m_appendIncomingDocument));
        RowIterator i = sortedBDT.iterator();
        while (i.hasNext()) {
            m_exec.checkCanceled();
            DataRow row = i.next();
            setProgress(subExec);
            
            Document currDoc = ((DocumentValue)row.getCell(m_documentColIndex))
                               .getDocument();
            
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
        int curr = m_currRow.incrementAndGet();
        double prog = (double)curr / (double)m_noRows;
        exec.setProgress(prog, "Preprocessing row " + curr + " of "
                        + m_noRows);
    }
    
    /**
     * Preprocesses the given chunk of rows.
     * @param chunk The chunk of rows to apply preprocessing step on.
     */
    private void processChunk(final List<DataRow> chunk) {
        if (chunk != null && chunk.size() > 0) {
            // To save unmodifieable term that will not be preprocessed
            Set<Term> unmodifieableTerms = new HashSet<Term>();

            //
            // CREATE DOCUMENT CHUNK
            //
            DataCell newDocCell = null;
            DataCell docCell = chunk.get(0).getCell(m_documentColIndex);
            DataCell origDocCell = chunk.get(0).getCell(m_origDocumentColIndex);
            Document document = ((DocumentValue)docCell).getDocument();
            // check for missing document cell
            if (docCell.isMissing()) {
                return;
            }
            // get all terms
            Set<Term> termSet = new HashSet<Term>();
            for (DataRow row : chunk) {
                DataCell termcell = row.getCell(m_termColIndex);
                // handle missing value (ignore rows with missing values)
                if (termcell.isMissing()) {
                    continue;
                }
                Term term = ((TermValue)termcell).getTermValue();
                
                if (!term.isUnmodifiable() || m_preprocessUnmodifiable) {
                    // save term in order to preprocess it.
                    termSet.add(term);
                } else {
                    // save term in order to add it unmodified to the data 
                    // container.
                    unmodifieableTerms.add(term);
                }
            }
            DocumentChunk docChunk = new DocumentChunk(document, termSet);
            
            //
            // APPLY CHUNK PREPROCESSING
            //
            Hashtable<Term, Term> termMapping = 
                m_chunkPreprocessing.preprocessChunk(docChunk);
            
            //
            // DEEP PREPROCESSING
            //
            if (m_deepPreprocessing && termMapping != null) {
                DocumentBuilder builder = new DocumentBuilder(document);
                for (Section s : document.getSections()) {
                    for (Paragraph p : s.getParagraphs()) {
                        for (Sentence sen : p.getSentences()) {
                            for (Term t : sen.getTerms()) {
                                // if term mapping exists use mapping
                                if (termMapping.containsKey(t)) {
                                    Term mappedTerm = termMapping.get(t);
                                    if (t != null && t.getText().length() > 0) {
                                        builder.addTerm(mappedTerm);
                                    }
                                } else {
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
            } else {
                newDocCell = docCell;
            }
            
            //
            // CREATE DATA TABLE
            //
            // first add unchanged terms
            for (Term t : unmodifieableTerms) {
                addRowToContainer(t, newDocCell, origDocCell);
            }
            // than add preprocessed terms
            if (termMapping != null) {
                Set<Term> uniqeTerms = new HashSet<Term>(termMapping.values());
                for (Term t : uniqeTerms) {
                    // if term is null or empty don't add it to data table
                    if (t != null && t.getText().length() > 0) {
                        addRowToContainer(t, newDocCell, origDocCell);
                    }
                }
            }
        }
    }
    
    private int rowIndex = 0;
    
    private void addRowToContainer(final Term t, 
            final DataCell preprocessedDoc, final DataCell origDoc) {
        // add row with or without unchanged document.
        DataRow row;
        RowKey rk = RowKey.createRowKey(rowIndex);
        rowIndex++;
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

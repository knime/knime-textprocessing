/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.bow;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
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
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableBuilderFactory;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

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
    
    private BagOfWordsDataTableBuilder m_dtBuilder;
    
    /**
     * Creates a new instance of <code>BagOfWordsNodeModel</code> with one in
     * and one data table out port.
     */
    public BagOfWordsNodeModel() {
        super(1, 1);
        m_dtBuilder = DataTableBuilderFactory.createBowDataTableBuilder();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(DataTableSpec[] inSpecs)
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
            ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
       Hashtable<Document, Set<Term>> docTerms = 
           new Hashtable<Document, Set<Term>>();
       Hashtable<DataCell, Set<Term>> docCellTerms = 
           new Hashtable<DataCell, Set<Term>>();
       
       ExecutionMonitor subExec = exec.createSubProgress(0.5);
       int rowCount = inData[0].getRowCount();
       int currRow = 1;
       RowIterator it = inData[0].iterator();       
       while (it.hasNext()) {
           DataRow row = it.next();
           
           // get terms for document
           DocumentValue docCell = 
               (DocumentValue)row.getCell(m_documentColIndex); 
           Document doc = docCell.getDocument();
           Set<Term> terms = setOfTerms(doc);
           
           // add data cell and corresponding terms
           docTerms.put(doc, terms);
           docCellTerms.put(row.getCell(m_documentColIndex), terms);
           
           // report status
           double progress = (double)currRow / (double)rowCount;
           subExec.setProgress(progress, "Processing document " + currRow + " of " 
                   + rowCount);
           exec.checkCanceled();
           currRow++;           
       }
       
       // build data table
       ExecutionContext subContext = exec.createSubExecutionContext(0.5);
//       return new BufferedDataTable[]{m_dtBuilder.createReusedDataTable(
//               subContext, docCellTerms, false)};
       return new BufferedDataTable[]{m_dtBuilder.createDataTable(
               subContext, docTerms, false)};
    }
    
    
    private Set<Term> setOfTerms(final Document doc) {
        Set<Term> termSet = null;
        if (doc != null) {
            termSet = new HashSet<Term>();
            
            // All Sections
            List<Section> secs = doc.getSections();
            for (Section s : secs) {
                
                // All Paragraphs
                List<Paragraph> paras = s.getParagraphs();
                for (Paragraph p : paras) {
                    
                    // All Sentences
                    List<Sentence> sentences = p.getSentences();
                    for (Sentence sen : sentences) {
                        
                        // All Terms
                        List<Term> terms = sen.getTerms();
                        for (Term t : terms) {
                            if (!termSet.contains(t)) {
                                termSet.add(t);
                            }
                        }
                    }
                }
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

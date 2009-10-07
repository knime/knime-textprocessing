/* ------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   20.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.sentenceextraction;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentBlobDataCellFactory;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.FullDataCellCache;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class SentenceExtractionNodeModel extends NodeModel {

    /**
     * The name of the column containing the number of terms.
     */
    static final String TERMCOUNT_COLNAME = "Number of terms";
    
    /**
     * The name of the column containing the sentence.
     */
    static final String SENTENCE_COLNAME = "Sentence";
    
    private int m_docColIndex = -1;
    
    private SettingsModelString m_documentColModel =
        SentenceExtractionNodeDialog.getDocumentColumnModel();
    
    
    /**
     * Creates a new instance of <code>SentenceStatisticsNodeModel</code>.
     */
    public SentenceExtractionNodeModel() {
        super(1, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier v = new DataTableSpecVerifier(inSpecs[0]);
        v.verifyMinimumDocumentCells(1, true);
        
        m_docColIndex = inSpecs[0].findColumnIndex(
                m_documentColModel.getStringValue());
        if (m_docColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! " 
                    + "Check your settings!");
        }
        
        return new DataTableSpec[]{createOutDataTableSpec()};
        
    }
    
    private DataTableSpec createOutDataTableSpec() {
        DataColumnSpecCreator docCreator = new DataColumnSpecCreator(
                DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME, 
                DocumentCell.TYPE);        
        DataColumnSpecCreator sentenceCreator = new DataColumnSpecCreator(
                SENTENCE_COLNAME, StringCell.TYPE);
        DataColumnSpecCreator lengthCreator = new DataColumnSpecCreator(
                TERMCOUNT_COLNAME, IntCell.TYPE);
        
        return new DataTableSpec(docCreator.createSpec(), 
                sentenceCreator.createSpec(), lengthCreator.createSpec());
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        m_docColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_documentColModel.getStringValue());
        
        // create cache
        FullDataCellCache docCache = new FullDataCellCache(
                new DocumentBlobDataCellFactory());
        BufferedDataContainer dc = exec.createDataContainer(
                createOutDataTableSpec());
        
        int count = 1;
        RowIterator it = inData[0].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            Document doc = 
                ((DocumentValue)row.getCell(m_docColIndex)).getDocument();
            DataCell docCell = docCache.getInstance(doc);
            
            Iterator<Sentence> si = doc.sentenceIterator();
            while (si.hasNext()) {
                exec.checkCanceled();
                
                Sentence s = si.next();
                String sentenceStr = s.getText();
                int termCount = s.getTerms().size();
                
                RowKey rowKey = RowKey.createRowKey(count);
                DefaultRow newRow = new DefaultRow(rowKey, docCell, 
                        new StringCell(sentenceStr), new IntCell(termCount));
                dc.addRowToTable(newRow);
                
                count++;
            }
        }
        docCache.reset();
        dc.close();
        
        return new BufferedDataTable[]{dc.getTable()};
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec) 
    throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }  
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
    throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

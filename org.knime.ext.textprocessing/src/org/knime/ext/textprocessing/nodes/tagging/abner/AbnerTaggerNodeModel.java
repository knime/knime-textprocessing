/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The node model of the ABNER (A Biomedical Named Entity Recognizer) tagger. 
 * Extends {@link org.knime.core.node.NodeModel} and provides methods to 
 * configure and execute the node.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTaggerNodeModel extends NodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;
    
    /**
     * The default value of the ABNER tagging model.
     */
    public static final String DEF_ABNERMODEL = 
        AbnerDocumentTagger.MODEL_BIOCREATIVE; 
    
    private int m_docColIndex = -1;
    
    private SettingsModelBoolean m_setUnmodifiableModel = 
        AbnerTaggerNodeDialog.createSetUnmodifiableModel();
    
    private SettingsModelString m_abnerTaggingModel = 
        AbnerTaggerNodeDialog.createAbnerModelModel();
    
    private DocumentDataTableBuilder m_dtBuilder;
    
    /**
     * Creates a new instance of <code>AbnerTaggerNodeModel</code> with one
     * table in and one out port.
     */
    public AbnerTaggerNodeModel() {
        super(1, 1);
        m_dtBuilder = new DocumentDataTableBuilder();
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
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(spec);
        verfier.verifyDocumentCell(true);
        m_docColIndex = verfier.getDocumentCellIndex();
    }  
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
        List<Document> newDocuments = new ArrayList<Document>();
        DocumentTagger tagger = new AbnerDocumentTagger(
                m_setUnmodifiableModel.getBooleanValue(), 
                m_abnerTaggingModel.getStringValue());
        
        RowIterator it = inData[0].iterator();
        int rowCount = inData[0].getRowCount();
        int currDoc = 1;
        while (it.hasNext()) {
            
            double progress = (double)currDoc / (double)rowCount;
            exec.setProgress(progress, "Tagging document " + currDoc + " of " 
                    + rowCount);
            exec.checkCanceled();
            currDoc++;
            
            DataRow row = it.next();
            DocumentValue docVal = (DocumentValue)row.getCell(m_docColIndex);
            newDocuments.add(tagger.tag(docVal.getDocument()));
        }
        
        return new BufferedDataTable[]{m_dtBuilder.createDataTable(
                exec, newDocuments)};
    }   
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_setUnmodifiableModel.saveSettingsTo(settings);
        m_abnerTaggingModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
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
    protected void reset() {
    }     
}

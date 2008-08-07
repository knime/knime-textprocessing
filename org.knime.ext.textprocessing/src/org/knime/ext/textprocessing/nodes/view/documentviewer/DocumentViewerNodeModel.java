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
 *   27.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.view.documentviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.container.DataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentViewerNodeModel extends NodeModel {

    private static final int INPUT_INDEX = 0;
    
    private int m_documentCellindex = -1;
    
    private Set<Document> m_documents;
    
    private DataTable m_data;
    
    private static final String INDATA_FILE = 
        "DocumentViewerNodeModelInData.dat";

    private static final String SETTINGS_FILE = 
        "DocumentViewerNodeModelSettings.dat";    
    
    private static final String INTERNAL_MODEL = "DocViewerModel";
    
    private static final String DOCUMENT_INDEX = "DocIndex";
    
    /**
     * Creates new instance of <code>DocumentViewerNodeModel</code>.
     */
    public DocumentViewerNodeModel() {
        super(1, 0);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(
                inSpecs[INPUT_INDEX]);
        verifier.verifyDocumentCell(true);
        m_documentCellindex = verifier.getDocumentCellIndex();
        return new DataTableSpec[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        m_documents = new HashSet<Document>();
        m_data = inData[INPUT_INDEX];
        buildDocumentSet();
        return new BufferedDataTable[]{};
    }

    private void buildDocumentSet() {
        if (m_documents == null) {
            m_documents = new HashSet<Document>();
        }
        
        RowIterator it = m_data.iterator();
        while(it.hasNext()) {
            DataRow row = it.next();
            Document doc = ((DocumentValue)row.getCell(m_documentCellindex))
                            .getDocument();
            m_documents.add(doc);
        }
    }
    
    /**
     * @return the set of documents to display.
     */
    Set<Document> getDocuments() {
        
        return m_documents;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        m_data = 
            DataContainer.readFromZip(new File(nodeInternDir, INDATA_FILE));
        
        File file = new File(nodeInternDir, SETTINGS_FILE);
        FileInputStream fis = new FileInputStream(file);
        ModelContentRO modelContent = ModelContent.loadFromXML(fis);        

        // Load settings
        try {
            m_documentCellindex = modelContent.getInt(DOCUMENT_INDEX);
        } catch (InvalidSettingsException e1) {
            IOException ioe = new IOException("Could not load internals!");
            ioe.initCause(e1);
            fis.close();
            throw ioe;
        }
        
        buildDocumentSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_documents.clear();
        m_documents = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        DataContainer.writeToZip(m_data, new File(nodeInternDir, 
                INDATA_FILE), exec);
        
        // Save tree
        ModelContent modelContent = new ModelContent(INTERNAL_MODEL);

        // Save settings
        modelContent.addInt(DOCUMENT_INDEX, m_documentCellindex);
        
        File file = new File(nodeInternDir, SETTINGS_FILE);
        FileOutputStream fos = new FileOutputStream(file);
        modelContent.saveToXML(fos);
        fos.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // Nothing to do ...
    }
}

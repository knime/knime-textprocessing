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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.util.DataTableBuilderFactory;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * The model class of the dictionary based named entity recognizer.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DictionaryTaggerNodeModel extends NodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static boolean DEFAULT_UNMODIFIABLE = true;
    
    /**
     * The default value of the case sensitive setting.
     */
    public static boolean DEFAULT_CASE_SENSITIVE = true;
    
    /**
     * The default value of the default tag.
     */
    public static String DEFAULT_TAG = 
        NamedEntityTag.UNKNOWN.getTag().getTagValue();
    
    private int m_docColIndex = -1;
    
    private SettingsModelBoolean m_setUnmodifiableModel = 
        DictionaryTaggerNodeDialog.createSetUnmodifiableModel();
    
    private SettingsModelString m_tagModel = 
        DictionaryTaggerNodeDialog.createTagModel();
    
    private SettingsModelString m_fileModel = 
        DictionaryTaggerNodeDialog.createFileModel();
    
    private SettingsModelBoolean m_caseSensitiveModel = 
        DictionaryTaggerNodeDialog.createCaseSensitiveModel();
    
    private DocumentDataTableBuilder m_dtBuilder;
    
    
    
    /**
     * Creates a new instance of <code>DictionaryTaggerNodeModel</code> with one
     * table in and one out port.
     */
    public DictionaryTaggerNodeModel() {
        super(1, 1);
        m_dtBuilder = DataTableBuilderFactory.createDocumentDataTableBuilder();
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
        
        // Read file with named entities
        Set<String> namedEntities = new HashSet<String>();
        File file = new File(m_fileModel.getStringValue());
        if (file.exists()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                namedEntities.add(line.trim());
            }
            br.close();
        }
        
        List<Document> newDocuments = new ArrayList<Document>();
        DocumentTagger tagger = new DictionaryDocumentTagger(
                m_setUnmodifiableModel.getBooleanValue(), namedEntities,
                NamedEntityTag.stringToTag(m_tagModel.getStringValue()),
                m_caseSensitiveModel.getBooleanValue());
        
        RowIterator it = inData[0].iterator();
        int rowCount = inData[0].getRowCount();
        int currDoc = 1;
        while(it.hasNext()) {
            
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
    protected void reset() {
        // Nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_caseSensitiveModel.loadSettingsFrom(settings);
        m_tagModel.loadSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
        m_setUnmodifiableModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_caseSensitiveModel.saveSettingsTo(settings);
        m_tagModel.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_caseSensitiveModel.validateSettings(settings);
        m_tagModel.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

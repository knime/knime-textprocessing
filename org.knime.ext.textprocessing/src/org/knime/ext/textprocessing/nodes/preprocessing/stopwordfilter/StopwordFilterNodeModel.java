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
 *   16.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.ThreadedPreprocessingNodeModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StopwordFilterNodeModel extends ThreadedPreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(StopwordFilterNodeModel.class);
    
    /**
     * The default setting for the use of case sensitivity. 
     */
    public static final boolean DEF_CASE_SENSITIVE = false;
    
    private SettingsModelString m_fileModel = 
        StopwordFilterNodeDialog.getFileModel();
    
    private SettingsModelBoolean m_caseModel = 
        StopwordFilterNodeDialog.getCaseSensitiveModel();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        File f = new File(m_fileModel.getStringValue());
        if (f.exists()) {
            HashSet<String> stopWords = new HashSet<String>();
            
            try {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    stopWords.add(line.trim());
                }
                reader.close();
            } catch (FileNotFoundException e) {
                LOGGER.warn("Not such file !");
            } catch (IOException e) {
                LOGGER.warn("Cant read from file");
            }
            
            m_preprocessing = new StopWordFilter(stopWords, 
                    m_caseModel.getBooleanValue());
        }
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
        super.loadValidatedSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
        m_caseModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_caseModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        m_caseModel.validateSettings(settings);
        
        // check selected file
        String file = ((SettingsModelString)m_fileModel.
                createCloneWithValidatedValue(settings)).getStringValue();
        File f = new File(file);
        if (!f.isFile() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected Stopword file: " 
                    + file + " is not valid!");
        }        
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

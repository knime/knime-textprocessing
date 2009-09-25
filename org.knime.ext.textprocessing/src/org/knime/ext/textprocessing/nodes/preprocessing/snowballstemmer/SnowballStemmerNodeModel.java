/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;

import java.io.File;
import java.io.IOException;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class SnowballStemmerNodeModel extends PreprocessingNodeModel {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(SnowballStemmerNodeModel.class);
    
    /**
     * Default snowball stemmer name.
     */
    public static final String DEF_STEMMER_NAME = "Porter";
    
    private SettingsModelString m_stemmerNameModel = 
        SnowballStemmerNodeDialog.getStemmerNameModel();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        try {
            m_preprocessing = new WrappedSnowballStemmer(
                    SnowballStemmerFactory.getInstance().getStemmerByName(
                            m_stemmerNameModel.getStringValue()));
        } catch (Exception e) {
            LOGGER.warn("Could not load Snowball stemmer!");
            LOGGER.debug(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_stemmerNameModel.loadSettingsFrom(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_stemmerNameModel.saveSettingsTo(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_stemmerNameModel.validateSettings(settings);
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
    protected void reset() {
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

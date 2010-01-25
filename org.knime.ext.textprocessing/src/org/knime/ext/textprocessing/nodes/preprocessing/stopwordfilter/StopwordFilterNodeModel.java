/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StopwordFilterNodeModel extends PreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(StopwordFilterNodeModel.class);
    
    /**
     * The default setting for the use of case sensitivity. 
     */
    public static final boolean DEF_CASE_SENSITIVE = false;

    /**
     * The default setting for the usage of build in lists. 
     */
    public static final boolean DEF_USE_BUILIN_LIST = true;    
    
    private SettingsModelString m_fileModel = 
        StopwordFilterNodeDialog.getFileModel();
    
    private SettingsModelBoolean m_caseModel = 
        StopwordFilterNodeDialog.getCaseSensitiveModel();
    
    private SettingsModelBoolean m_useBuildinListModel = 
        StopwordFilterNodeDialog.getUseBuildInListModel();
    
    private SettingsModelString m_buildinListModel = 
        StopwordFilterNodeDialog.getBuildInListModel();
    
    /**
     * Creates a new instance of <code>StopwordFilterNodeModel</code>.
     */
    public StopwordFilterNodeModel() {
        super();
        m_useBuildinListModel.addChangeListener(new StopwordChangeListener());
        updateModels();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        HashSet<String> stopWords;
        if (m_useBuildinListModel.getBooleanValue()) {
            Set<String> stopWordList = BuildInStopwordListFactory.getInstance()
                    .getStopwordListByName(m_buildinListModel.getStringValue());
            stopWords = new HashSet<String>(stopWordList);
        } else {
            stopWords = new HashSet<String>();
            File f = new File(m_fileModel.getStringValue());
            if (f.exists() && f.canRead() && f.isFile()) {
                try {
                    BufferedReader reader = new BufferedReader(
                            new FileReader(f));
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
            }
        }
        m_preprocessing = new StopWordFilter(stopWords, 
                m_caseModel.getBooleanValue());
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
        m_useBuildinListModel.loadSettingsFrom(settings);
        m_buildinListModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_caseModel.saveSettingsTo(settings);
        m_useBuildinListModel.saveSettingsTo(settings);
        m_buildinListModel.saveSettingsTo(settings);
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
        m_useBuildinListModel.validateSettings(settings);
        m_buildinListModel.validateSettings(settings);

        boolean useBuildIn = ((SettingsModelBoolean)m_useBuildinListModel.
                createCloneWithValidatedValue(settings)).getBooleanValue();
        
        // check selected file only if no build in list is used
        if (!useBuildIn) {
            String file = ((SettingsModelString)m_fileModel
                    .createCloneWithValidatedValue(settings)).getStringValue();
            File f = new File(file);
            if (!f.isFile() || !f.exists() || !f.canRead()) {
                throw new InvalidSettingsException("Selected Stopword file: "
                        + file + " is not valid!");
            }
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
    
    private class StopwordChangeListener implements ChangeListener {

        @Override
        public void stateChanged(final ChangeEvent arg0) {
            updateModels();
        }        
    }
    
    private void updateModels() {
        if (m_useBuildinListModel.getBooleanValue()) {
            m_buildinListModel.setEnabled(true);
            m_fileModel.setEnabled(false);
        } else {
            m_buildinListModel.setEnabled(false);
            m_fileModel.setEnabled(true);
        }
    }    
}

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
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpennlpNerTaggerNodeModel extends NodeModel {
    
    /**
     * The default value for the unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;
    
    /**
     * The default model.
     */
    public static final String DEF_OPENNLPMODEL = 
        OpenNlpModelFactory.getInstance().getDefaultName();
    
    /**
     * The default value for the dictionary flag.
     */
    public static final boolean DEFAULT_USE_DICT = false;
    
    /**
     * The default dictionary file location.
     */
    public static final String DEFAULT_DICT_FILENAME = 
        System.getProperty("user.home");
    
    private int m_docColIndex = -1;
    
    private DocumentDataTableBuilder m_dtBuilder;
    
    private SettingsModelBoolean m_unmodifiableModel = 
        OpenNlpNerNodeDialog.createSetUnmodifiableModel();
    
    private SettingsModelString m_modelNameModel = 
        OpenNlpNerNodeDialog.createOpenNlpModelModel();
    
    private SettingsModelBoolean m_useDictFileModel = 
        OpenNlpNerNodeDialog.createUseDictModel();
    
    private SettingsModelString m_dictFileModel = 
        OpenNlpNerNodeDialog.createDictFileModel();
    
    /**
     * Creates new instance of <code>OpennlpNerTaggerNodeModel</code>.
     */
    public OpennlpNerTaggerNodeModel() {
        super(1, 1);
        m_dtBuilder = new DocumentDataTableBuilder();
        m_useDictFileModel.addChangeListener(new SettingsChangeListener());
        checkSettings();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
        List<Document> newDocuments = new ArrayList<Document>();
        String dictFileName = null;
        if (m_useDictFileModel.getBooleanValue()) {
            dictFileName = m_dictFileModel.getStringValue();
        }
        DocumentTagger tagger = new OpennlpNerDocumentTagger(
                m_unmodifiableModel.getBooleanValue(),
                OpenNlpModelFactory.getInstance().getModelByName(
                        m_modelNameModel.getStringValue()), dictFileName);
        
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
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_unmodifiableModel.loadSettingsFrom(settings);
        m_modelNameModel.loadSettingsFrom(settings);
        m_useDictFileModel.loadSettingsFrom(settings);
        m_dictFileModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_modelNameModel.saveSettingsTo(settings);
        m_unmodifiableModel.saveSettingsTo(settings);
        m_useDictFileModel.saveSettingsTo(settings);
        m_dictFileModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_modelNameModel.validateSettings(settings);
        m_unmodifiableModel.validateSettings(settings);
        m_useDictFileModel.validateSettings(settings);
        m_dictFileModel.validateSettings(settings);

        boolean useDictFile = ((SettingsModelBoolean)m_useDictFileModel
                .createCloneWithValidatedValue(settings)).getBooleanValue();
        if (useDictFile) {
            String file = ((SettingsModelString)m_dictFileModel
                    .createCloneWithValidatedValue(settings)).getStringValue();
            File f = new File(file);
            if (!f.isFile() || !f.exists() || !f.canRead()) {
                throw new InvalidSettingsException("Selected file file: "
                        + file + " is not valid!");
            }
        }
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
    
    private class SettingsChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent arg0) {
            checkSettings();
        }
        
    }
    
    private void checkSettings() {
        if (m_useDictFileModel.getBooleanValue()) {
            m_dictFileModel.setEnabled(true);
        } else {
            m_dictFileModel.setEnabled(false);
        }
    }    
}

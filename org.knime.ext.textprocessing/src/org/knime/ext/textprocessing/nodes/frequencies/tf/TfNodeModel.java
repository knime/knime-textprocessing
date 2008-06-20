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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyNodeModel;

/**
 * The model of the TF-Node, specifying the proper cell factory
 * {@link org.knime.ext.textprocessing.nodes.frequencies.tf.TfCellFactory}.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TfNodeModel extends FrequencyNodeModel {

    /**
     * By default the relative tf value is computed.
     */
    public static final boolean DEF_RELATIVE = true;
    
    private SettingsModelBoolean m_relativeModel = 
        TfNodeDialog.getRelativeModel();
    
    /**
     * Creates a new instance of <code>TfNodeModel</code>.
     */
    public TfNodeModel() {
        super(TfCellFactory.COLNAME_REL, false);
        m_noOutputSpec = true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initCellFactory(final BufferedDataTable inData,
            final ExecutionContext exec) throws CanceledExecutionException {
        m_cellFac = new TfCellFactory(getDocumentColIndex(), getTermColIndex(),
                m_relativeModel.getBooleanValue());
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
        m_relativeModel.loadSettingsFrom(settings);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_relativeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_relativeModel.validateSettings(settings);
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

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
 *   18.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.idf;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyNodeModel;

import java.io.File;
import java.io.IOException;

/**
 * The model of the IDF-Node, specifying the proper cell factory
 * {@link org.knime.ext.textprocessing.nodes.frequencies.idf.IdfCellFactory}.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class IdfNodeModel extends FrequencyNodeModel {

    /**
     * Creates a new instance of <code>IdfNodeModel</code>.
     */
    public IdfNodeModel() {
        super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void initCellFactory(BufferedDataTable inData,
            ExecutionContext exec) throws CanceledExecutionException {
        m_cellFac = new IdfCellFactory(getDocumentColIndex(), getTermColIndex(),
                inData, exec);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
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
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
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

    @Override
    protected DataTableSpec createDataTableSpec(DataTableSpec inDataSpec) {
        DataColumnSpec freq = 
            new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(
                            inDataSpec, IdfCellFactory.COLNAME), 
                            DoubleCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(freq));
    }
}

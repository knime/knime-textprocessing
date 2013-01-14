/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
    protected void initCellFactory(final BufferedDataTable inData,
            final ExecutionContext exec) throws CanceledExecutionException {
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        DataColumnSpec freq = 
            new DataColumnSpecCreator(
                    DataTableSpec.getUniqueColumnName(
                            inDataSpec, IdfCellFactory.COLNAME), 
                            DoubleCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(freq));
    }
}

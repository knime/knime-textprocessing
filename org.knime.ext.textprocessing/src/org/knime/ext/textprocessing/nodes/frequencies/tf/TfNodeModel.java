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
 * ---------------------------------------------------------------------
 * 
 * History
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyNodeModel;

import java.io.File;
import java.io.IOException;

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
        super();
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
        super.loadValidatedSettingsFrom(settings);
        m_relativeModel.loadSettingsFrom(settings);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_relativeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        DataColumnSpec freq;
        if (m_relativeModel.getBooleanValue()) {
            String colName = DataTableSpec.getUniqueColumnName(
                    inDataSpec, TfCellFactory.COLNAME_REL);
            freq = new DataColumnSpecCreator(colName, 
                    DoubleCell.TYPE).createSpec();
            
        } else {
            String colName = DataTableSpec.getUniqueColumnName(
                    inDataSpec, TfCellFactory.COLNAME_ABS);
            freq = new DataColumnSpecCreator(colName, 
                    IntCell.TYPE).createSpec();
        }
        return new DataTableSpec(inDataSpec, new DataTableSpec(freq));
    }    
}

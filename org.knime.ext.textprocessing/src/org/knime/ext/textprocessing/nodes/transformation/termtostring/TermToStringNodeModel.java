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
 *   26.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termtostring;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermToStringNodeModel extends NodeModel {

    private static final int INDATA_INDEX = 0;
    
    private int m_termColIndex = -1;
    
    private String m_newColName;
    
    private SettingsModelString m_termColModel = 
        TermToStringNodeDialog.getTermColModel();
    
    /**
     * Creates a new instance of <code>TermToStringNodeModel</code>.
     */
    public TermToStringNodeModel() {
        super(1, 1);
    }
    
    private final void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumTermCells(1, true);
        
        m_termColIndex = spec.findColumnIndex(
                m_termColModel.getStringValue());
        if (m_termColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified term column is not valid! " 
                    + "Check your settings!");
        } 
    }
    
    private DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        
        String termCol = m_termColModel.getStringValue();
        if (termCol.isEmpty()) {
            termCol = "Term as String";
        } else {
            termCol += " as String";
        }
        
        int count = 1;
        m_newColName = termCol;
        while (inDataSpec.containsName(m_newColName)) {
            m_newColName = termCol + " " + count;
            count++;
        }
        
        DataColumnSpec strCol = new DataColumnSpecCreator(m_newColName, 
                StringCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(strCol));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[INDATA_INDEX]);
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INDATA_INDEX])};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
                
        BufferedDataTable inDataTable = inData[INDATA_INDEX];
        
        // find index of term column
        m_termColIndex = inDataTable.getDataTableSpec().findColumnIndex(
                m_termColModel.getStringValue());
        
        // initializes the corresponding cell factory
        TermToStringCellFactory cellFac = new TermToStringCellFactory(
                m_termColIndex, m_newColName);
        
        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(
                inDataTable.getDataTableSpec());
        rearranger.append(cellFac);
        
        return new BufferedDataTable[] {
                exec.createColumnRearrangeTable(inDataTable, rearranger, 
                exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_termColModel.loadSettingsFrom(settings);
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
        m_termColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_termColModel.validateSettings(settings);
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

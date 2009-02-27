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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringtoterm;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringToTermNodeModel extends NodeModel {

    /**
     * The name of the term column to append.
     */
    static final String TERM_COLNAME = 
        BagOfWordsDataTableBuilder.DEF_TERM_COLNAME;
    
    private SettingsModelString m_stringColModel = 
        StringToTermNodeDialog.getStringColModel();
    
    /**
     * Creates new instance of <code>StringToTermNodeModel</code>.
     */
    public StringToTermNodeModel() {
        super(1, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[0]);
        verifier.verifyMinimumStringCells(1, true);
        return new DataTableSpec[]{createDataTableSpec(inSpecs[0])};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
        BufferedDataTable bfd = inData[0];
        int colIndex = bfd.getDataTableSpec().findColumnIndex(
                m_stringColModel.getStringValue());
        
        CellFactory fac = new TermCellFactory(colIndex, bfd.getDataTableSpec());
        ColumnRearranger rearranger = new ColumnRearranger(
                bfd.getDataTableSpec());
        rearranger.append(fac);
        
        return new BufferedDataTable[]{exec.createColumnRearrangeTable(
                bfd, rearranger, exec)};
    }
    
    private static final DataTableSpec createDataTableSpec(
            final DataTableSpec inSpec) {
        return new DataTableSpec(inSpec,  
                new DataTableSpec(getTermColumnSpec(inSpec)));
    }
    
    /**
     * @return The column spec of the term column to append.
     */
    static final DataColumnSpec getTermColumnSpec(final DataTableSpec spec) {
        return new DataColumnSpecCreator(
                DataTableSpec.getUniqueColumnName(spec, TERM_COLNAME), 
                TermCell.TYPE).createSpec();
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
        m_stringColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_stringColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_stringColModel.validateSettings(settings);
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

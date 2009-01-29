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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
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
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentNodeModel extends NodeModel {

    
    private final static int INPORT = 0; 
    
    private SettingsModelString m_titleColModel = 
        StringsToDocumentNodeDialog.getTitleStringModel();
    
//    private SettingsModelString m_abstractColModel;
//    
//    private SettingsModelString m_fulltextColModel;
    
    private SettingsModelString m_authorsColModel = 
        StringsToDocumentNodeDialog.getAuthorsStringModel();
    
    /**
     * Creates new instance of <code>StringsToDocumentNodeModel</code>.
     */
    public StringsToDocumentNodeModel() {
        super(1, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        
        DataTableSpecVerifier verifier = 
            new DataTableSpecVerifier(inSpecs[INPORT]);
        verifier.verifyMinimumStringCells(1, true);
        
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INPORT])};
    }

    private DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec) {
        DataColumnSpec strCol = new DataColumnSpecCreator("Document", 
                DocumentCell.TYPE).createSpec();
        return new DataTableSpec(inDataSpec, new DataTableSpec(strCol));
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        
        BufferedDataTable inDataTable = inData[INPORT];
        
        StringsToDocumentConfig conf = new StringsToDocumentConfig();
        
        int titleIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_titleColModel.getStringValue());
        conf.setTitleStringIndex(titleIndex);
        int authorIndex = inData[INPORT].getDataTableSpec().findColumnIndex(
                m_authorsColModel.getStringValue());
        conf.setAuthorsStringIndex(authorIndex);
        
        // initializes the corresponding cell factory
        StringsToDocumentCellFactory cellFac = 
            new StringsToDocumentCellFactory(conf);
        
        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(
                inDataTable.getDataTableSpec());
        rearranger.append(cellFac);
        
        return new BufferedDataTable[] {
                exec.createColumnRearrangeTable(inDataTable, rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
//        m_abstractColModel.loadSettingsFrom(settings);
//        m_fulltextColModel.loadSettingsFrom(settings);
        m_authorsColModel.loadSettingsFrom(settings);
        m_titleColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
//        m_abstractColModel.saveSettingsTo(settings);
//        m_fulltextColModel.saveSettingsTo(settings);
        m_authorsColModel.saveSettingsTo(settings);
        m_titleColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {
//        m_abstractColModel.validateSettings(settings);
//        m_fulltextColModel.validateSettings(settings);
        m_authorsColModel.validateSettings(settings);
        m_titleColModel.validateSettings(settings);
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

/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2009
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
 *   17.03.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.tagtoString;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
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
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Kilian Thiel
 */
public class TagToStringNodeModel extends NodeModel {

    public static Set<String> ALL_TAG_TYPES = 
        TagFactory.getInstance().getTagTypes();
    
    public static String DEFAULT_TAG_TYPE = 
        PartOfSpeechTag.getDefault().getType();
    
    static String MISSING_CELL_VALUE = "<MissingCell>";
    
    public static String DEFAULT_MISSING_VALUE = MISSING_CELL_VALUE;
    
    
    private SettingsModelStringArray m_tagTypesModel = 
        TagToStringNodeDialog.getTagTypesModel();
    
    private SettingsModelString m_termColModel = 
        TagToStringNodeDialog.getTermColModel();
    
    private SettingsModelString m_missingTagValueModel = 
        TagToStringNodeDialog.getMissingTagModel();
    
    public TagToStringNodeModel() {
        super(1, 1);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpecs[0]);
        verifier.verifyMinimumTermCells(1, true);
        return null;
    }

    /**
     * @param tagTypes
     * @param oldSpec
     * @return
     */
    static DataColumnSpec[] getDataTableSpec(final List<String> tagTypes,
            final DataTableSpec oldSpec) {
        DataColumnSpec[] dataColumnSpecs = new DataColumnSpec[tagTypes.size()];
        int i = 0;
        for (String tagType : tagTypes) {
            String name = DataTableSpec.getUniqueColumnName(oldSpec, tagType);
            dataColumnSpecs[i] = new DataColumnSpecCreator(
                    name, StringCell.TYPE).createSpec();
            i++;
        }
        return dataColumnSpecs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        int termColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_termColModel.getStringValue());
        
        List<String> tagTypes = new ArrayList<String>();
        for (String tagType : m_tagTypesModel.getStringArrayValue()) {
            tagTypes.add(tagType);
        }
        CellFactory cellFac = new TagToStringCellFactory(termColIndex, tagTypes, 
                    inData[0].getDataTableSpec(), 
                    m_missingTagValueModel.getStringValue());
        ColumnRearranger rearranger = new ColumnRearranger(
                inData[0].getDataTableSpec());
        rearranger.append(cellFac);
        
        return new BufferedDataTable[]{exec.createColumnRearrangeTable(
                inData[0], rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(NodeSettingsWO settings) {
        m_tagTypesModel.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
        m_missingTagValueModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tagTypesModel.validateSettings(settings);
        m_termColModel.validateSettings(settings);
        m_missingTagValueModel.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_tagTypesModel.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
        m_missingTagValueModel.loadSettingsFrom(settings);
    }    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do ...
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, 
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // nothing to do ...
    }    
}

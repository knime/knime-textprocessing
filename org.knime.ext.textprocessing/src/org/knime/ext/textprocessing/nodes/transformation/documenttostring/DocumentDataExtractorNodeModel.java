/*
 * -------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.ext.textprocessing.data.DocumentValue;

import java.io.File;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorNodeModel extends NodeModel {

    private final SettingsModelString m_documentCol = getDocumentColConfigObj();

    private final SettingsModelStringArray m_extractorNames =
        getExtractorNamesConfigObj();

    /**Constructor for class DocumentExtractorNodeMode.
     * @param nrInDataPorts
     * @param nrOutDataPorts
     */
    protected DocumentDataExtractorNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
    throws InvalidSettingsException {
        if (inSpecs == null || inSpecs.length < 1) {
            throw new InvalidSettingsException("Invalid input spec");
        }
        final DocumentDataExtractor[] extractors = 
            DocumentDataExtractor.getExctractor(
                    m_extractorNames.getStringArrayValue());
        if (extractors == null || extractors.length < 1) {
            setWarningMessage("No data extractors selected");
        }
        final String colName = m_documentCol.getStringValue();
        final DataTableSpec inSpec = inSpecs[0];
        if (colName == null) {
            //pre select the first document column
            for (final DataColumnSpec colSpec : inSpec) {
                if (colSpec.getType().isCompatible(DocumentValue.class)) {
                    m_documentCol.setStringValue(colSpec.getName());
                    break;
                }
            }
        } else {
            //check if the selected column name is available
            final int columnIndex = inSpec.findColumnIndex(colName);
            if (columnIndex < 0) {
                throw new InvalidSettingsException("Invalid column name: "
                        + colName);
            }
        }
        return new DataTableSpec[] {createSpec(inSpecs[0], extractors)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        if (inData == null || inData.length < 1) {
            throw new IllegalArgumentException("Invalid input data");
        }
        final BufferedDataTable table = inData[0];
        final DataTableSpec inSpec = table.getSpec();
        final int docColIdx =
            inSpec.findColumnIndex(m_documentCol.getStringValue());
        if (docColIdx < 0) {
            throw new InvalidSettingsException("Invalid document column");
        }
        final DocumentDataExtractor[] extractors = 
            DocumentDataExtractor.getExctractor(
                    m_extractorNames.getStringArrayValue());
        final BufferedDataTable resultTable;
        if (extractors == null || extractors.length == 0) {
            setWarningMessage(
                    "No extractor selected. Node returns unaltered table");
            resultTable = table;
        } else {
            final ColumnRearranger rearranger =
                new ColumnRearranger(inSpec);
            final CellFactory factory =
                new DocumentDataExtractorCellFactory(docColIdx, extractors);
            rearranger.append(factory);
            resultTable =
                exec.createColumnRearrangeTable(table, rearranger, exec);
        }
        return new BufferedDataTable[] {resultTable};
    }

    private static final DataTableSpec createSpec(final DataTableSpec origSpec,
            final DocumentDataExtractor[] extractors) {
        if (extractors == null || extractors.length == 0) {
            return origSpec;
        }
        final DataColumnSpec[] cols = new DataColumnSpec[extractors.length];
        for (int i = 0, length = extractors.length; i < length; i++) {
            cols[i] = extractors[i].getColumnSpec();
        }
        return new DataTableSpec(origSpec, new DataTableSpec(cols));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentCol.saveSettingsTo(settings);
        m_extractorNames.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentCol.validateSettings(settings);
        m_extractorNames.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentCol.loadSettingsFrom(settings);
        m_extractorNames.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec) {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec) {
        // nothing to do
    }

    /**
     * @return Creates and returns new <code>SettingsModelString</code>
     * containing the name of the document column.
     */
    protected static SettingsModelString getDocumentColConfigObj() {
        return new SettingsModelString("documentColumn", null);
    }
    /**
     * @return Creates and returns new <code>SettingsModelStringArray</code>
     * containing the document fields to extract.
     */
    protected static SettingsModelStringArray getExtractorNamesConfigObj() {
        return new SettingsModelStringArray("extractorNames", null);
    }

}

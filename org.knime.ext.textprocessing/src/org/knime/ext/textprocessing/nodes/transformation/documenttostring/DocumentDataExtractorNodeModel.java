/*
========================================================================
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
 * -------------------------------------------------------------------
 *
 * History
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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

import java.io.File;

import org.knime.ext.textprocessing.data.DocumentValue;


/**
 * {@link NodeModel} implementation of the DocumentDataExtractor node.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class DocumentDataExtractorNodeModel extends NodeModel {

    private final SettingsModelString m_documentCol = getDocumentColConfigObj();

    private final SettingsModelStringArray m_extractorNames =
        getExtractorNamesConfigObj();

    private DataColumnSpec[] m_extractorColumnSpecs = null;

    /**Constructor for class DocumentExtractorNodeMode.
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
        m_extractorColumnSpecs = createColumnSpecs(inSpecs[0], extractors);
        final DataTableSpec resultSpec =
            createSpec(inSpecs[0], m_extractorColumnSpecs);
        return new DataTableSpec[] {resultSpec};
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
            final CellFactory factory = new DocumentDataExtractorCellFactory(
                    docColIdx, m_extractorColumnSpecs, extractors);
            rearranger.append(factory);
            resultTable =
                exec.createColumnRearrangeTable(table, rearranger, exec);
        }
        return new BufferedDataTable[] {resultTable};
    }

    /**
     * @param origSpec the original {@link DataTableSpec}
     * @param columnSpecs the extractor {@link DataColumnSpec}s
     * @return the original {@link DataTableSpec} with a new column
     * specification per {@link DocumentDataExtractor} attached to it
     */
    private static final DataTableSpec createSpec(final DataTableSpec origSpec,
            final DataColumnSpec[] columnSpecs) {
        if (columnSpecs == null || columnSpecs.length == 0) {
            return origSpec;
        }
        return new DataTableSpec(origSpec, new DataTableSpec(columnSpecs));
    }

    /**
     *@param origSpec the original {@link DataTableSpec}
     * @param extractors the extractors to use
     * @return the {@link DataColumnSpec} for the given
     * {@link DocumentDataExtractor} in the same order as given
     */
    private static DataColumnSpec[] createColumnSpecs(
            final DataTableSpec origSpec,
            final DocumentDataExtractor[] extractors) {
        if (extractors == null || extractors.length < 1) {
            return new DataColumnSpec[0];
        }
        final DataColumnSpec[] cols = new DataColumnSpec[extractors.length];
        for (int i = 0, length = extractors.length; i < length; i++) {
             final String name = DataTableSpec.getUniqueColumnName(origSpec,
                     extractors[i].getName());
             final DataType type = extractors[i].getDataType();
             cols[i] = new DataColumnSpecCreator(name, type).createSpec();
        }
        return cols;
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
        m_extractorColumnSpecs = null;
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

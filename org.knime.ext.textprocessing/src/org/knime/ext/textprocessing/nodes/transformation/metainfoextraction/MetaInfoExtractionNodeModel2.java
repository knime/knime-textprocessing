/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Apr 26, 2019 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoextraction;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The {@code NodeModel} for the Meta Info Extractor node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MetaInfoExtractionNodeModel2 extends SimpleStreamableFunctionNodeModel {

    /**
     * Config key for the document column.
     */
    private static final String CFG_KEY_DOC_COLUMN = "document_column";

    /**
     * Default setting for document column name.
     */
    private static final String DEF_DOC_COLUMN = "";

    /**
     * Creates and returns a {@link SettingsModelString} containing the column name of the document column.
     *
     * @return The {@code SettingsModelString} containing the column name of the document column.
     */
    static final SettingsModelString createDocColModel() {
        return new SettingsModelString(CFG_KEY_DOC_COLUMN, DEF_DOC_COLUMN);
    }

    /**
     * The {@link SettingsModelString} containing the column name of the document column.
     */
    private final SettingsModelString m_docColModel = createDocColModel();

    /**
     * Creates a new instance of {@code MetaInfoExtractionNodeModel2} with one in- and one output port.
     */
    MetaInfoExtractionNodeModel2() {
        super();
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec inSpec = inSpecs[0];
        checkDataTableSpec(inSpec);
        return new DataTableSpec[]{null};
    }

    /**
     * Checks, if the input table contains a document column and automatically guesses it, if it isn't set yet.
     *
     * @param inSpec The {@link DataTableSpec}.
     * @throws InvalidSettingsException Thrown, if the {@code DataTableSpec} is invalid.
     */
    private final void checkDataTableSpec(final DataTableSpec inSpec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(inSpec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, inSpec, DocumentValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final BufferedDataTable dataTable = inData[0];
        final DataTableSpec spec = dataTable.getDataTableSpec();
        final int docColIdx = spec.findColumnIndex(m_docColModel.getStringValue());

        if (dataTable.size() == 0) {
            return inData;
        }

        // get keys
        final Map<String, Integer> keys = extractKeys(dataTable, docColIdx);

        // return input table if documents do not contain any meta information
        if (keys.isEmpty()) {
            setWarningMessage("No meta information extracted, as the documents don't contain any.");
            return inData;
        }

        final ColumnRearranger rearranger = createColumnRearranger(spec);
        rearranger.append(new AbstractCellFactory() {

            @Override
            public DataCell[] getCells(final DataRow row) {
                final DataCell docCell = row.getCell(docColIdx);
                final DataCell[] newCells =
                    Stream.generate(DataType::getMissingCell).limit(keys.size()).toArray(DataCell[]::new);
                if (!docCell.isMissing()) {
                    final Document doc = ((DocumentValue)docCell).getDocument();
                    final DocumentMetaInfo metaInfo = doc.getMetaInformation();
                    final Set<String> docKeys = metaInfo.getMetaInfoKeys();
                    // look up position for each meta info key from the document in the total key set and
                    // add a new StringCell with the corresponding value
                    for (final String key : docKeys) {
                        newCells[keys.get(key)] = new StringCell(metaInfo.getMetaInfoValue(key));
                    }
                }
                return newCells;
            }

            @Override
            public DataColumnSpec[] getColumnSpecs() {
                return keys.entrySet().stream()//
                    .map(e -> new DataColumnSpecCreator(e.getKey(), StringCell.TYPE).createSpec())//
                    .toArray(DataColumnSpec[]::new);
            }
        });
        return new BufferedDataTable[]{
            exec.createColumnRearrangeTable(dataTable, rearranger, exec.createSubProgress(1.0))};
    }

    /**
     * Extracts all keys for meta information in documents of a specified column.
     *
     * @param dataTable The data table
     * @param docColIdx The document column index.
     * @return A map of keys and an index.
     */
    private static final Map<String, Integer> extractKeys(final BufferedDataTable dataTable, final int docColIdx) {
        final Map<String, Integer> keys = new LinkedHashMap<>();
        int index = 0;
        for (final DataRow row : dataTable) {
            final DataCell cell = row.getCell(docColIdx);
            if (!cell.isMissing()) {
                final Set<String> metaInfoKeys =
                    ((DocumentValue)cell).getDocument().getMetaInformation().getMetaInfoKeys();
                for (final String key : metaInfoKeys) {
                    if (!keys.containsKey(key)) {
                        keys.put(key, index);
                        index++;
                    }
                }
            }
        }
        return keys;
    }

    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec spec) throws InvalidSettingsException {
        return new ColumnRearranger(spec);
    }

    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
    }

    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
    }

    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    @Override
    protected void reset() {
        // Nothing to do here...
    }
}

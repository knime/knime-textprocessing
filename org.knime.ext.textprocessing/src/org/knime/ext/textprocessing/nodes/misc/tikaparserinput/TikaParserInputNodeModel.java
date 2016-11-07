/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 *   26.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikaparserinput;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.uri.URIDataCell;
import org.knime.core.data.uri.URIDataValue;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaParser;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaParserConfig;

/**
 * The node model of the Tika Parser URL Input node. This model extends {@link org.knime.core.node.NodeModel} and is
 * streamable.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
final class TikaParserInputNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParserInputNodeModel.class);

    private final SettingsModelString m_colModel = TikaParserConfig.getColModel();

    private SettingsModelString m_typesModel = TikaParserConfig.getTypeModel();

    private SettingsModelStringArray m_columnModel = TikaParserConfig.getColumnModel();

    private SettingsModelBoolean m_extractAttachmentModel = TikaParserConfig.getExtractAttachmentModel();

    private SettingsModelString m_extractPathModel = TikaParserConfig.getExtractPathModel(m_extractAttachmentModel);

    private SettingsModelBoolean m_authBooleanModel = TikaParserConfig.getAuthBooleanModel();

    private SettingsModelString m_authModel = TikaParserConfig.getCredentials(m_authBooleanModel);

    private final SettingsModelBoolean m_errorColumnModel = TikaParserConfig.getErrorColumnModel();

    private final SettingsModelString m_errorColNameModel =
        TikaParserConfig.getErrorColumnNameModel(m_errorColumnModel);

    private final SettingsModelFilterString m_filterModel = TikaParserConfig.getFilterModel();

    private long m_noRows = 0;

    /**
     * Creates a new instance of {@code TikaParserInputNodeModel}
     */
    TikaParserInputNodeModel() {
        super(1, 2);
        stateChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec in = inSpecs[0];
        int colIndex = -1;
        final String colName = m_colModel.getStringValue();
        if (colName == null) {
            // auto-guessing parameters
            for (int i = 0; i < in.getNumColumns(); i++) {
                DataType dtype = in.getColumnSpec(i).getType();
                if (dtype.isCompatible(StringValue.class) || dtype.isCompatible(URIDataValue.class)) {
                    colIndex = i;
                    LOGGER.info("Guessing column \"" + in.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
            CheckUtils.checkSetting(colIndex >= 0, "No string/URI compatible column in input");
            m_colModel.setStringValue(in.getColumnSpec(colIndex).getName());
        } else {
            // we have user setting -- expect the column to be present and of appropriate type
            colIndex = in.findColumnIndex(colName);
            // column must be present, otherwise fail
            CheckUtils.checkSetting(colIndex >= 0, "No such URI/String column in input: \"%s\"", colName);
            DataType type = in.getColumnSpec(colIndex).getType();
            // column must be URI or string compatible, otherwise fails
            CheckUtils.checkSetting(type.isCompatible(StringValue.class) || type.isCompatible(URIDataCell.class),
                "Column \"%s\" is present in the input table but not String/URI compatible, its type is \"%s\"",
                colName, type.toPrettyString());
        }

        assert colIndex >= 0 : "colindex expected to be non-negative at this point";

        List<String> listOutputCols = Arrays.asList(m_columnModel.getStringArrayValue());
        if (m_errorColumnModel.getBooleanValue()) {
            listOutputCols = new ArrayList<String>(listOutputCols);
            listOutputCols.add(m_errorColNameModel.getStringValue());
        }
        DataTableSpec col1 = createOutputTableSpec(listOutputCols);
        DataTableSpec col2 = createOutputTableSpec(Arrays.asList(TikaParserConfig.OUTPUT_TWO_COL_NAMES));
        return new DataTableSpec[]{col1, col2};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] data, final ExecutionContext exec)
        throws Exception {
        List<String> listOutputCols = Arrays.asList(m_columnModel.getStringArrayValue());
        if (m_errorColumnModel.getBooleanValue()) {
            listOutputCols = new ArrayList<String>(listOutputCols);
            listOutputCols.add(m_errorColNameModel.getStringValue());
        }

        // create output spec and its container
        BufferedDataContainer container1 = exec.createDataContainer(createOutputTableSpec(listOutputCols));
        BufferedDataContainer container2 =
            exec.createDataContainer(createOutputTableSpec(Arrays.asList(TikaParserConfig.OUTPUT_TWO_COL_NAMES)));

        BufferedDataTableRowOutput output1 = new BufferedDataTableRowOutput(container1);
        BufferedDataTableRowOutput output2 = new BufferedDataTableRowOutput(container2);

        m_noRows = data[0].size();

        createStreamableOperator(null, null).runFinal(new PortInput[]{new DataTableRowInput(data[0])},
            new PortOutput[]{output1, output2}, exec);

        return new BufferedDataTable[]{output1.getDataTable(), output2.getDataTable()};
    }

    private DataTableSpec createOutputTableSpec(final List<String> selectedColumns) {
        DataColumnSpec[] cspecs = new DataColumnSpec[selectedColumns.size()];
        int i = 0;
        for (String col : selectedColumns) {
            cspecs[i] = new DataColumnSpecCreator(col, StringCell.TYPE).createSpec();
            i++;
        }

        return new DataTableSpec(cspecs);
    }

    /** {@inheritDoc} */
    @Override
    public InputPortRole[] getInputPortRoles() {
        InputPortRole[] in = new InputPortRole[getNrInPorts()];
        Arrays.fill(in, InputPortRole.NONDISTRIBUTED_STREAMABLE);
        return in;
    }

    /** {@inheritDoc} */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        OutputPortRole[] out = new OutputPortRole[getNrOutPorts()];
        Arrays.fill(out, OutputPortRole.DISTRIBUTED);
        return out;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
                throws Exception {
                RowInput rowInput = (RowInput)inputs[0];
                RowOutput rowOutput1 = (RowOutput)outputs[0]; // data output port 1
                RowOutput rowOutput2 = (RowOutput)outputs[1]; // data output port 2

                boolean ext, error = false;
                if (m_typesModel.getStringValue().equals(TikaParserConfig.EXT_TYPE)) {
                    ext = true;
                } else {
                    ext = false;
                }
                List<String> validTypes = m_filterModel.getIncludeList();
                int colIndex = rowInput.getDataTableSpec().findColumnIndex(m_colModel.getStringValue());
                List<String> outputColumnsOne = Arrays.asList(m_columnModel.getStringArrayValue());
                if (m_errorColumnModel.getBooleanValue()) {
                    outputColumnsOne = new ArrayList<String>(outputColumnsOne);
                    outputColumnsOne.add(m_errorColNameModel.getStringValue());
                }
                HashMap<String, Integer> duplicateFiles = new HashMap<String, Integer>();
                int rowKeyTwo = 0;
                DataRow row;
                int count = 0;
                final File attachmentDir = getAttachmentDir();

                while ((row = rowInput.poll()) != null) {
                    String errorMsg = "";
                    String url;
                    DataCell cell = row.getCell(colIndex);
                    RowKey rowKeyOne = row.getKey();
                    if (cell.isMissing()) {
                        errorMsg = "Missing cell. Cannot locate file path";
                        rowOutput1.push(TikaParser.setMissingRow(outputColumnsOne, "", rowKeyOne, errorMsg,
                            m_errorColNameModel.getStringValue()));
                        continue;
                    }
                    // we can safely assume the type of the cell is either String or URI compatible as this was
                    // asserted during #configure
                    if (cell instanceof URIDataValue) {
                        url = ((URIDataValue)cell).getURIContent().getURI().toString();
                    } else {
                        url = ((StringCell)cell).getStringValue();
                    }

                    File file = TikaParser.getFile(url, false);

                    TikaParser tikaParser = new TikaParser(false);
                    tikaParser.setOutputColumnsOne(outputColumnsOne);
                    tikaParser.setValidTypes(validTypes);
                    tikaParser.setErrorColName(m_errorColNameModel.getStringValue());
                    tikaParser.setAuthBoolean(m_authBooleanModel.getBooleanValue());
                    tikaParser.setExtBoolean(ext);
                    tikaParser.setPassword(m_authModel.getStringValue());
                    tikaParser.setDuplicates(duplicateFiles);

                    List<DataCell[]> datacells = tikaParser.parse(file, attachmentDir);
                    duplicateFiles = tikaParser.getDuplicates();
                    errorMsg = tikaParser.getErrorMsg();
                    if (datacells.isEmpty()) {
                        if (!errorMsg.isEmpty()) {
                            setWarningMessage(errorMsg + ": " + file.getAbsolutePath());
                            error = true;
                        }
                        continue; // skipped files
                    }

                    DataCell[] rowOne = datacells.get(0);
                    rowOutput1.push(new DefaultRow(rowKeyOne, rowOne));

                    if (!errorMsg.isEmpty()) {
                        setWarningMessage(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    }

                    if (datacells.size() > 1) {
                        for (int j = 1; j < datacells.size(); j++) {
                            rowOutput2.push(new DefaultRow(RowKey.createRowKey((long)rowKeyTwo), datacells.get(j)));
                            rowKeyTwo++;
                        }
                    }

                    exec.checkCanceled();

                    String msg = "Reading file #" + count++;
                    if (m_noRows > 0) {
                        msg += " of " + m_noRows;
                    }
                    exec.setProgress(msg);

                } // end for loop

                if (error) {
                    setWarningMessage("Not all files are parsed!");
                }

                rowInput.close();
                for (int i = 0; i < outputs.length; i++) {
                    ((RowOutput)outputs[i]).close();
                }

            }

        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_typesModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_filterModel.saveSettingsTo(settings);
        m_extractAttachmentModel.saveSettingsTo(settings);
        m_extractPathModel.saveSettingsTo(settings);
        m_authModel.saveSettingsTo(settings);
        m_authBooleanModel.saveSettingsTo(settings);
        m_errorColNameModel.saveSettingsTo(settings);
        m_errorColumnModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_typesModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_filterModel.validateSettings(settings);
        m_extractAttachmentModel.validateSettings(settings);
        m_extractPathModel.validateSettings(settings);
        m_authModel.validateSettings(settings);
        m_authBooleanModel.validateSettings(settings);
        m_errorColNameModel.validateSettings(settings);
        m_errorColumnModel.validateSettings(settings);

        Boolean extract =
            ((SettingsModelBoolean)m_extractAttachmentModel.createCloneWithValidatedValue(settings)).getBooleanValue();

        if (extract) {
            String outputDir =
                ((SettingsModelString)m_extractPathModel.createCloneWithValidatedValue(settings)).getStringValue();
            CheckUtils.checkSetting(StringUtils.isNotBlank(outputDir),
                "Path to attachment directory must not be blank");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.loadSettingsFrom(settings);
        m_typesModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_filterModel.loadSettingsFrom(settings);
        m_extractAttachmentModel.loadSettingsFrom(settings);
        m_extractPathModel.loadSettingsFrom(settings);
        m_authModel.loadSettingsFrom(settings);
        m_authBooleanModel.loadSettingsFrom(settings);
        m_errorColNameModel.loadSettingsFrom(settings);
        m_errorColumnModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_noRows = 0;
    }

    private void stateChange() {
        if (m_extractAttachmentModel.getBooleanValue()) {
            m_extractPathModel.setEnabled(true);
        } else {
            m_extractPathModel.setEnabled(false);
        }

        if (m_authBooleanModel.getBooleanValue()) {
            m_authModel.setEnabled(true);
        } else {
            m_authModel.setEnabled(false);
        }
        if (m_errorColumnModel.getBooleanValue()) {
            m_errorColNameModel.setEnabled(true);
        } else {
            m_errorColNameModel.setEnabled(false);
        }
    }

    private File getAttachmentDir() throws InvalidSettingsException {
        final File attachmentDir;
        if (m_extractAttachmentModel.getBooleanValue()) {
            String outputDir = m_extractPathModel.getStringValue();

            File file = TikaParser.getFile(outputDir, false);

            if (!file.exists()) {
                CheckUtils.checkSetting(file.mkdirs(), "Directory \"%s\" cannot be created. Please give a valid path.",
                    outputDir);
                setWarningMessage("Attachment directory didn't exist and was created: " + outputDir);
            }
            attachmentDir = file;
        } else {
            attachmentDir = null;
        }
        return attachmentDir;
    }

}

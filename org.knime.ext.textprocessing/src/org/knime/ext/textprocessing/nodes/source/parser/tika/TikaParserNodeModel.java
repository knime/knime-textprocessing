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
 *   08.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.exception.EncryptedDocumentException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.PasswordProvider;
import org.apache.tika.sax.BodyContentHandler;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.nodes.source.parser.FileCollector;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaParserConfig.TikaColumnKeys;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The node model of the Tika Parser node. This model extends {@link org.knime.core.node.NodeModel} and is streamable.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
final class TikaParserNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParserNodeModel.class);

    private SettingsModelString m_pathModel = TikaParserConfig.getPathModel();

    private SettingsModelBoolean m_recursiveModel = TikaParserConfig.getRecursiveModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel = TikaParserConfig.getIgnoreHiddenFilesModel();

    private SettingsModelString m_typesModel = TikaParserConfig.getTypeModel();

    private SettingsModelStringArray m_columnModel = TikaParserConfig.getColumnModel();

    private SettingsModelBoolean m_extractAttachmentModel = TikaParserConfig.getExtractAttachmentModel();

    private SettingsModelString m_extractPathModel = TikaParserConfig.getExtractPathModel(m_extractAttachmentModel);

    private SettingsModelBoolean m_authBooleanModel = TikaParserConfig.getAuthBooleanModel();

    private SettingsModelString m_authModel = TikaParserConfig.getCredentials(m_authBooleanModel);

    private SettingsModelBoolean m_errorColumnModel = TikaParserConfig.getErrorColumnModel();

    private SettingsModelString m_errorColNameModel = TikaParserConfig.getErrorColumnNameModel(m_errorColumnModel);

    private SettingsModelFilterString m_filterModel = TikaParserConfig.getFilterModel();

    /**
     * Creates a new instance of {@code TikaParserNodeModel}
     */
    TikaParserNodeModel() {
        super(0, 2);
        stateChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
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
        // create output spec and its container
        List<String> listOutputCols = Arrays.asList(m_columnModel.getStringArrayValue());
        if (m_errorColumnModel.getBooleanValue()) {
            listOutputCols = new ArrayList<String>(listOutputCols);
            listOutputCols.add(m_errorColNameModel.getStringValue());
        }
        BufferedDataContainer container1 = exec.createDataContainer(createOutputTableSpec(listOutputCols));
        BufferedDataContainer container2 =
            exec.createDataContainer(createOutputTableSpec(Arrays.asList(TikaParserConfig.OUTPUT_TWO_COL_NAMES)));

        BufferedDataTableRowOutput output1 = new BufferedDataTableRowOutput(container1);
        BufferedDataTableRowOutput output2 = new BufferedDataTableRowOutput(container2);

        createStreamableOperator(null, null).runFinal(new PortInput[0], new PortOutput[]{output1, output2}, exec);

        return new BufferedDataTable[]{output1.getDataTable(), output2.getDataTable()};
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
                assert inputs.length == 0;

                RowOutput rowOutput1 = (RowOutput)outputs[0]; // data output port 1
                RowOutput rowOutput2 = (RowOutput)outputs[1]; // data output port 2

                boolean ext, error = false;
                if (m_typesModel.getStringValue().equals(TikaParserConfig.EXT_TYPE)) {
                    ext = true;
                } else {
                    ext = false;
                }

                final File dir = TikaParserUtils.getFile(m_pathModel.getStringValue(), true);
                final boolean recursive = m_recursiveModel.getBooleanValue();
                final boolean ignoreHiddenFiles = m_ignoreHiddenFilesModel.getBooleanValue();

                final FileCollector fc;
                if (ext) {
                    fc = new FileCollector(dir, m_filterModel.getIncludeList(), recursive, ignoreHiddenFiles);
                } else {
                    fc = new FileCollector(dir, new ArrayList<String>(), recursive, ignoreHiddenFiles);
                }

                final List<File> files = fc.getFiles();
                final int numberOfFiles = files.size();

                if (numberOfFiles == 0) {
                    setWarningMessage("Directory is empty: " + dir.getPath());
                }

                HashMap<String, Integer> duplicateFiles = new HashMap<String, Integer>();
                List<String> outputColumnsOne = Arrays.asList(m_columnModel.getStringArrayValue());
                if (m_errorColumnModel.getBooleanValue()) {
                    outputColumnsOne = new ArrayList<String>(outputColumnsOne);
                    outputColumnsOne.add(m_errorColNameModel.getStringValue());
                }

                int rowKeyOne = 0;
                int rowKeyTwo = 0;

                final File attachmentDir;
                if (m_extractAttachmentModel.getBooleanValue()) {
                    String outputDir = m_extractPathModel.getStringValue();

                    File file = TikaParserUtils.getFile(outputDir,false);

                    if (!file.exists()) {
                        CheckUtils.checkSetting(file.mkdirs(),
                            "Directory \"%s\" cannot be created. Please give a valid path.", outputDir);
                        setWarningMessage("Attachment directory didn't exist and was created: " + outputDir);
                    }
                    attachmentDir = file;
                } else {
                    attachmentDir = null;
                }

                for (int i = 0; i < numberOfFiles; i++) {
                    String errorMsg = "";
                    File file = files.get(i);

                    if (!ext && !file.isFile()) {
                        continue; // skip all directories
                    }
                    if (!file.canRead()) {
                        errorMsg = "Unreadable file";
                        if (ext) {
                            rowOutput1
                                .push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                        }
                        LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    }

                    String mime_type = "-";
                    ContentHandler handler = new BodyContentHandler(-1);
                    AutoDetectParser parser = new AutoDetectParser();
                    Metadata metadata = new Metadata();
                    ParseContext context = new ParseContext();
                    if (m_authBooleanModel.getBooleanValue()) {
                        final String password = m_authModel.getStringValue();
                        context.set(PasswordProvider.class, new PasswordProvider() {
                            @Override
                            public String getPassword(final Metadata md) {
                                return password;
                            }
                        });
                    }

                    metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName());

                    try (BufferedInputStream str = new BufferedInputStream(new FileInputStream(file.getPath()))) {
                        mime_type = parser.getDetector().detect(str, metadata).toString();
                    } catch (FileNotFoundException e) {
                        errorMsg = "Could not find file";
                        if (ext) {
                            rowOutput1
                                .push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                        }
                        LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    } catch (IOException e) {
                        errorMsg = "Error while detecting MIME-type of file";
                        if (ext) {
                            rowOutput1
                                .push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                        }
                        LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    }

                    if (mime_type.equals(MediaType.OCTET_STREAM.toString())) {
                        if (ext) {
                            errorMsg = "Could not detect/parse file";
                            rowOutput1
                                .push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                            LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                            error = true;
                            continue;
                        }
                    }
                    if (!ext) {
                        List<String> validTypes = m_filterModel.getIncludeList();
                        if (!validTypes.contains(mime_type)) {
                            continue; // skip if mime type is not in the list of input mime types
                        }
                    }

                    try {
                        if (attachmentDir != null) {
                            try (TikaInputStream stream = TikaInputStream.get(file.toPath());) {
                                EmbeddedFilesExtractor ex = new EmbeddedFilesExtractor();
                                ex.setContext(context);
                                ex.setDuplicateFilesList(duplicateFiles);
                                ex.extract(stream, attachmentDir.toPath(), file.getName());
                                metadata = ex.getMetadata();
                                handler = ex.getHandler();

                                if (ex.hasError()) {
                                    errorMsg = "Could not write embedded files to the output directory";
                                    LOGGER.error(errorMsg + ": " + file.getAbsolutePath());
                                    error = true;
                                }

                                DataCell[] cellsTwo;
                                DataRow rowTwo;
                                for (String entry : ex.getOutputFiles()) {
                                    cellsTwo = new DataCell[TikaParserConfig.OUTPUT_TWO_COL_NAMES.length];
                                    cellsTwo[0] = new StringCell(file.getAbsolutePath());
                                    cellsTwo[1] = new StringCell(entry);
                                    rowTwo = new DefaultRow(RowKey.createRowKey((long)rowKeyTwo), cellsTwo);
                                    rowOutput2.push(rowTwo);
                                    rowKeyTwo++;
                                }
                            }
                        } else {
                            try (TikaInputStream stream = TikaInputStream.get(file.toPath());) {
                                parser.parse(stream, handler, metadata, context);
                            }
                        }
                    } catch (EncryptedDocumentException e) {
                        errorMsg = "Could not parse encrypted file, invalid password";
                        rowOutput1.push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                        LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    } catch (IOException | SAXException | TikaException e) {
                        errorMsg = "Could not parse file, it might be broken";
                        rowOutput1.push(setMissingRow(outputColumnsOne, file.getAbsolutePath(), rowKeyOne++, errorMsg));
                        LOGGER.warn(errorMsg + ": " + file.getAbsolutePath());
                        error = true;
                        continue;
                    }

                    DataCell[] cellsOne = new DataCell[outputColumnsOne.size()];
                    for (int j = 0; j < outputColumnsOne.size(); j++) {
                        String colName = outputColumnsOne.get(j);
                        Property prop = TikaColumnKeys.COLUMN_PROPERTY_MAP.get(colName);
                        if (prop == null && colName.equals(TikaColumnKeys.COL_FILEPATH)) {
                            cellsOne[j] = new StringCell(file.getAbsolutePath());
                        } else if (prop == null && colName.equals(TikaColumnKeys.COL_MIME_TYPE)) {
                            if (mime_type.equals("-")) {
                                cellsOne[j] = DataType.getMissingCell();
                            } else {
                                cellsOne[j] = new StringCell(mime_type);
                            }
                        } else if (prop == null && colName.equals(TikaColumnKeys.COL_CONTENT)) {
                            cellsOne[j] = new StringCell(handler.toString());
                        } else if (prop == null && colName.equals(m_errorColNameModel.getStringValue())) {
                            cellsOne[j] = errorMsg.isEmpty() ? DataType.getMissingCell() : new StringCell(errorMsg);
                        } else {
                            String val = metadata.get(prop);
                            if (val == null) {
                                cellsOne[j] = DataType.getMissingCell();
                            } else {
                                cellsOne[j] = new StringCell(val);
                            }
                        }
                    }

                    DataRow rowOne = new DefaultRow(RowKey.createRowKey((long)rowKeyOne), cellsOne);
                    rowOutput1.push(rowOne);
                    rowKeyOne++;

                    exec.checkCanceled();
                    exec.setProgress(i / (double)numberOfFiles, "Parsing file " + i + " of " + numberOfFiles);

                } // end for loop

                if (error) {
                    setWarningMessage("Could not parse all files properly!");
                }

                for (int i = 0; i < outputs.length; i++) {
                    ((RowOutput)outputs[i]).close();
                }

            }
        };
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

    private DataRow setMissingRow(final List<String> outputCols, final String file, final int rowKey,
        final String errorMsg) {
        int outputSize = outputCols.size();
        DataCell[] cellsOne = new DataCell[outputSize];
        for (int j = 0; j < outputSize; j++) {
            String colName = outputCols.get(j);
            if (colName.equals(TikaColumnKeys.COL_FILEPATH)) {
                cellsOne[j] = new StringCell(file);
            } else if (colName.equals(m_errorColNameModel.getStringValue())) {
                cellsOne[j] = new StringCell(errorMsg);
            } else {
                cellsOne[j] = DataType.getMissingCell();
            }
        }
        return new DefaultRow(RowKey.createRowKey((long)rowKey), cellsOne);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
        m_ignoreHiddenFilesModel.saveSettingsTo(settings);
        m_typesModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_extractAttachmentModel.saveSettingsTo(settings);
        m_extractPathModel.saveSettingsTo(settings);
        m_authModel.saveSettingsTo(settings);
        m_authBooleanModel.saveSettingsTo(settings);
        m_errorColumnModel.saveSettingsTo(settings);
        m_errorColNameModel.saveSettingsTo(settings);
        m_filterModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);
        m_typesModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_extractAttachmentModel.validateSettings(settings);
        m_extractPathModel.validateSettings(settings);
        m_authModel.validateSettings(settings);
        m_authBooleanModel.validateSettings(settings);
        m_errorColumnModel.validateSettings(settings);
        m_errorColNameModel.validateSettings(settings);
        m_filterModel.validateSettings(settings);

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
        m_pathModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
        m_ignoreHiddenFilesModel.loadSettingsFrom(settings);
        m_typesModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_extractAttachmentModel.loadSettingsFrom(settings);
        m_extractPathModel.loadSettingsFrom(settings);
        m_authModel.loadSettingsFrom(settings);
        m_authBooleanModel.loadSettingsFrom(settings);
        m_errorColumnModel.loadSettingsFrom(settings);
        m_errorColNameModel.loadSettingsFrom(settings);
        m_filterModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

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

}

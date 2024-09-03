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
 *   17.11.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelPassword;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.DataTableRowInput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowInput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;

import com.google.common.collect.Iterables;

/**
 * The super class for the Tika node model.
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public abstract class AbstractTikaNodeModel extends NodeModel {

    private SettingsModelString m_typesModel = TikaParserConfig.getTypeModel();

    private SettingsModelStringArray m_columnModel = TikaParserConfig.getColumnModel();

    private SettingsModelBoolean m_extractAttachmentModel = TikaParserConfig.getExtractAttachmentModel();

    private SettingsModelString m_extractPathModel = TikaParserConfig.getExtractPathModel(m_extractAttachmentModel);

    private SettingsModelBoolean m_authBooleanModel = TikaParserConfig.getAuthBooleanModel();

    private SettingsModelPassword m_authModel = TikaParserConfig.getCredentialsPWD(m_authBooleanModel);

    private SettingsModelBoolean m_errorColumnModel = TikaParserConfig.getErrorColumnModel();

    private SettingsModelString m_errorColNameModel = TikaParserConfig.getErrorColumnNameModel(m_errorColumnModel);

    private SettingsModelFilterString m_filterModel = TikaParserConfig.getFilterModel();

    private SettingsModelBoolean m_extractInlineImagesModel = TikaParserConfig.getExtractInlineImagesModel();

    /**
     * Creates a new instance.
     *
     * @param isSourceNode Whether or not this is the source node (no input port) or not.
     */
    protected AbstractTikaNodeModel(final boolean isSourceNode) {
        super(isSourceNode ? 0 : 1, 2);
        stateChange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return createDataTableSpec();
    }

    /**
     * @return the output data table spec
     */
    protected DataTableSpec[] createDataTableSpec() {
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

        PortInput[] portInput;

        if (isSourceNode()) {
            portInput = new PortInput[0];
        } else {
            portInput = new PortInput[]{new DataTableRowInput(data[0])};
        }
        createStreamableOperator(null, null).runFinal(portInput, new PortOutput[]{output1, output2}, exec);

        return new BufferedDataTable[]{output1.getDataTable(), output2.getDataTable()};
    }

    /**
     * @param input null for Tika Parser node, otherwise the rowInput from the input port
     * @return iterable list of parsable files
     * @throws Exception InvalidSettingsException, InterruptedException
     */
    protected abstract Iterable<URL> readInput(final RowInput input) throws Exception;

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
                assert inputs.length > 0 || isSourceNode();

                RowOutput rowOutput1 = (RowOutput)outputs[0]; // data output port 1
                RowOutput rowOutput2 = (RowOutput)outputs[1]; // data output port 2

                boolean ext, error = false;
                if (m_typesModel.getStringValue().equals(TikaParserConfig.EXT_TYPE)) {
                    ext = true;
                } else {
                    ext = false;
                }

                final List<String> validTypes = m_filterModel.getIncludeList();
                Map<String, Integer> duplicateFiles = new HashMap<String, Integer>();
                List<String> outputColumnsOne = Arrays.asList(m_columnModel.getStringArrayValue());
                if (m_errorColumnModel.getBooleanValue()) {
                    outputColumnsOne = new ArrayList<String>(outputColumnsOne);
                    outputColumnsOne.add(m_errorColNameModel.getStringValue());
                }
                final File attachmentDir = getAttachmentDir();
                int rowKeyOne = 0;
                int rowKeyTwo = 0;

                Iterable<URL> urls = readInput(isSourceNode() ? null : (RowInput)inputs[0]);
                int count = 1;
                for (URL url : urls) {
                    String errorMsg = "";

                    if (url == null) {
                        errorMsg = "Missing cell. Cannot locate file path";
                        rowOutput1.push(TikaParser.setMissingRow(outputColumnsOne, "", rowKeyOne, errorMsg,
                            m_errorColNameModel.getStringValue()));
                        continue;
                    }
                    TikaParser tikaParser = new TikaParser(isSourceNode());
                    tikaParser.setOutputColumnsOne(outputColumnsOne);
                    tikaParser.setValidTypes(validTypes);
                    tikaParser.setErrorColName(m_errorColNameModel.getStringValue());
                    tikaParser.setAuthBoolean(m_authBooleanModel.getBooleanValue());
                    tikaParser.setExtBoolean(ext);
                    tikaParser.setPassword(m_authModel.getStringValue());
                    tikaParser.setDuplicates(duplicateFiles);
                    boolean inlineImage = m_extractInlineImagesModel.getBooleanValue();
                    if (!m_extractInlineImagesModel.isEnabled()) {
                        inlineImage = false;
                    }
                    tikaParser.setExtractInlineImages(inlineImage);

                    List<DataCell[]> datacells = tikaParser.parse(url, attachmentDir);
                    duplicateFiles = tikaParser.getDuplicates();
                    errorMsg = tikaParser.getErrorMsg();
                    if (datacells == null || datacells.isEmpty()) {
                        if (!errorMsg.isEmpty()) {
                            setWarningMessage(errorMsg + ": " + TikaParser.getStringRepresentation(url));
                            error = true;
                        }
                        continue; // skipped files
                    }

                    DataCell[] rowOne = datacells.get(0);
                    rowOutput1.push(new DefaultRow(RowKey.createRowKey((long)rowKeyOne), rowOne));
                    rowKeyOne++;

                    if (!errorMsg.isEmpty()) {
                        setWarningMessage(errorMsg + ": " + TikaParser.getStringRepresentation(url));
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
                    int filesSize = Iterables.size(urls);
                    exec.setProgress(count / (double)filesSize, "Parsing file " + count + " of " + filesSize);
                }

                if (error) {
                    setWarningMessage("Not all files are parsed!");
                }

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
        saveInputSettingsTo(settings);
        m_typesModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_extractAttachmentModel.saveSettingsTo(settings);
        m_extractPathModel.saveSettingsTo(settings);
        m_authModel.saveSettingsTo(settings);
        m_authBooleanModel.saveSettingsTo(settings);
        m_errorColumnModel.saveSettingsTo(settings);
        m_errorColNameModel.saveSettingsTo(settings);
        m_filterModel.saveSettingsTo(settings);
        m_extractInlineImagesModel.saveSettingsTo(settings);
    }

    /**
     * Saves input settingsmodels
     *
     * @param settings the WO node settings
     */
    protected abstract void saveInputSettingsTo(final NodeSettingsWO settings);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        validateInputSettings(settings);
        m_typesModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_extractAttachmentModel.validateSettings(settings);
        m_extractPathModel.validateSettings(settings);
        // only validate if password is stored as password not as string (for backwards compatibility)
        if (settings.getString(m_authModel.getKey(), null) == null) {
            m_authModel.validateSettings(settings);
        }
        m_authBooleanModel.validateSettings(settings);
        m_errorColumnModel.validateSettings(settings);
        m_errorColNameModel.validateSettings(settings);
        m_filterModel.validateSettings(settings);
        try {
            m_extractInlineImagesModel.validateSettings(settings);
        } catch (Exception e) {
            //do nothing, just to make sure it's backwards compatible
        }

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
     * Validates input settingsmodels
     *
     * @param settings the node settings
     * @throws InvalidSettingsException throws exception if the settings are incorrect
     */
    protected abstract void validateInputSettings(final NodeSettingsRO settings) throws InvalidSettingsException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        loadValidatedInputSettingsFrom(settings);
        m_typesModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_extractAttachmentModel.loadSettingsFrom(settings);
        m_extractPathModel.loadSettingsFrom(settings);
        // check if password is stored as string for backwards compatibility
        String oldPwdString = settings.getString(TikaParserConfigKeys.CFGKEY_CREDENTIALS, null);
        if (oldPwdString != null) {
            // set the password to SettingsModelPassword
            m_authModel.setStringValue(oldPwdString);
        } else {
            // just do normal loading if password is not stored as string but as password
            m_authModel.loadSettingsFrom(settings);
        }
        m_authBooleanModel.loadSettingsFrom(settings);
        m_errorColumnModel.loadSettingsFrom(settings);
        m_errorColNameModel.loadSettingsFrom(settings);
        m_filterModel.loadSettingsFrom(settings);
        try {
            m_extractInlineImagesModel.loadSettingsFrom(settings);
        } catch (Exception e) {
            //do nothing, just to make sure it's backwards compatible
        }
    }

    /**
     * Loads validated input settingsmodels
     *
     * @param settings the node settings
     * @throws InvalidSettingsException throws exception if the settings are incorrect
     */
    protected abstract void loadValidatedInputSettingsFrom(final NodeSettingsRO settings)
        throws InvalidSettingsException;

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

    /**
     * Enables/disables dialog components according to the corresponding boolean values
     */
    private void stateChange() {
        m_extractPathModel.setEnabled(m_extractAttachmentModel.getBooleanValue());
        m_extractInlineImagesModel.setEnabled(m_extractAttachmentModel.getBooleanValue());
        m_authModel.setEnabled(m_authBooleanModel.getBooleanValue());
        m_errorColNameModel.setEnabled(m_errorColumnModel.getBooleanValue());
    }

    /**
     * Retrieves the attachment dir, or creates one if the path doesn't exist
     *
     * @return the path to the attachment directory
     * @throws InvalidSettingsException
     */
    private File getAttachmentDir() throws InvalidSettingsException {
        final File attachmentDir;
        if (m_extractAttachmentModel.getBooleanValue()) {
            String outputDir = m_extractPathModel.getStringValue();

            File file = getFile(outputDir, false);

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

    /**
     * @return whether this is the source node (no input).
     */
    protected boolean isSourceNode() {
        return getNrInPorts() == 0;
    }

    /**
     * @param file the file path in String.
     * @param dir true if the file is a directory.
     * @return the file path in type of File.
     * @throws InvalidSettingsException if the file is not a directory and unreadable.
     */
    public static File getFile(final String file, final boolean dir) throws InvalidSettingsException {
        File f = null;
        try {
            // first try if file string is an URL (files in drop dir come as URLs)
            final URL url = FileUtil.toURL(file);
            f = FileUtil.getFileFromURL(url);
        } catch (MalformedURLException e) {
            // if no URL try string as path to file
            f = new File(file);
        }

        // for remote dir
        if (f == null) {
            return f;
        }

        if (dir && (!f.isDirectory() || !f.exists() || !f.canRead())) {
            throw new InvalidSettingsException("Selected dir: " + file + " cannot be accessed!");
        }

        return f;
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

    /**
     * @return the m_typesModel
     */
    protected SettingsModelString getTypesModel() {
        return m_typesModel;
    }

    /**
     * @param typesModel the m_typesModel to set
     */
    protected void setTypesModel(final SettingsModelString typesModel) {
        this.m_typesModel = typesModel;
    }

    /**
     * @return the m_filterModel
     */
    protected SettingsModelFilterString getFilterModel() {
        return m_filterModel;
    }

    /**
     * @param filterModel the m_filterModel to set
     */
    protected void setFilterModel(final SettingsModelFilterString filterModel) {
        this.m_filterModel = filterModel;
    }

}

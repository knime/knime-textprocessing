/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 *
 * History
 *   Aug 20, 2014 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.knime.core.data.DataColumnProperties;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * Abstract definition of a node that applies a tagger using a {@link ColumnRearranger}.
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
public abstract class StreamableTaggerNodeModel2 extends NodeModel implements DocumentTaggerFactory {

    /**
     * Default number of threads to use for parallel tagging.
     */
    static final int DEFAULT_NUMBER_OF_THREADS = 1;

    /**
     * The default setting for replacing the documents with the tagged documents.
     */
    static final boolean DEF_REPLACE = true;

    /**
     * The default name of the new, tagged document column.
     */
    static final String DEF_NEW_DOCUMENT_COL = "Tagged Document";

    /** The settings model storing the number of threads to use for tagging. */
    private final SettingsModelIntegerBounded m_numberOfThreadsModel =
        TaggerNodeSettingsPane2.getNumberOfThreadsModel();

    /** The settings model storing the name of the tokenizer for word tokenization. */
    private final SettingsModelString m_tokenizer = TaggerNodeSettingsPane2.getTokenizerModel();

    /** The settings model storing the name of the selected document column. */
    private final SettingsModelString m_documentColModel = TaggerNodeSettingsPane2.getDocumentColumnModel();

    /** The settings model storing the name of the new, appended column. */
    private final SettingsModelString m_newDocumentColModel = TaggerNodeSettingsPane2.getNewDocumentColumnModel();

    /** The settings model storing the boolean value for the document column replacement option. */
    private final SettingsModelBoolean m_replaceOldDocModel = TaggerNodeSettingsPane2.getReplaceDocumentModel();

    private InputPortRole[] m_roles;

    /**
     * Default constructor, defining one data input and one data output port.
     */
    public StreamableTaggerNodeModel2() {
        this(1, new InputPortRole[]{});
    }

    /**
     * Constructor defining a specified number of data input and one data output port.
     *
     * @param dataInPorts The number of data input ports.
     * @param roles The roles of the input ports after the first port.
     */
    public StreamableTaggerNodeModel2(final int dataInPorts, final InputPortRole[] roles) {
        super(dataInPorts, 1);

        if (roles.length != dataInPorts - 1) {
            throw new IllegalArgumentException(
                "Number of input port roles must be equal to number of data in ports -1!");
        }
        m_roles = new InputPortRole[dataInPorts];
        m_roles[0] = InputPortRole.DISTRIBUTED_STREAMABLE;
        for (int i = 1; i < m_roles.length; i++) {
            m_roles[i] = roles[i - 1];
        }

        m_replaceOldDocModel.addChangeListener(e -> checkSettings());
    }

    /**
     * Constructor defining specific input ports, their roles and one data output port.
     *
     * @param portTypes The input port types.
     * @param roles The input port roles.
     */
    public StreamableTaggerNodeModel2(final PortType[] portTypes, final InputPortRole[] roles) {
        this(portTypes, roles, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * Constructor defining specific input ports, their roles and specific output ports.
     *
     * @param inPortTypes The input port types.
     * @param roles The input port roles.
     * @param outPortTypes The output port types.
     */
    public StreamableTaggerNodeModel2(final PortType[] inPortTypes, final InputPortRole[] roles,
        final PortType[] outPortTypes) {
        super(inPortTypes, outPortTypes);

        if (roles.length != inPortTypes.length - 1) {
            throw new IllegalArgumentException(
                "Number of input port roles must be equal to number of data in ports -1!");
        }
        m_roles = new InputPortRole[inPortTypes.length];
        m_roles[0] = InputPortRole.DISTRIBUTED_STREAMABLE;
        for (int i = 1; i < m_roles.length; i++) {
            m_roles[i] = roles[i - 1];
        }

        m_replaceOldDocModel.addChangeListener(e -> checkSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        prepareTagger(inObjects, exec);

        BufferedDataTable inDataDocumentTable = (BufferedDataTable)inObjects[0];
        final ColumnRearranger rearranger = createColumnRearranger(inDataDocumentTable.getDataTableSpec());
        return new BufferedDataTable[]{exec.createColumnRearrangeTable(inDataDocumentTable, rearranger, exec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        DataTableSpec[] inDataTableSpecs = Arrays.stream(inSpecs).filter(spec -> spec instanceof DataTableSpec)
            .map(spec -> (DataTableSpec)spec).toArray(DataTableSpec[]::new);

        DataTableSpec in = inDataTableSpecs[0];

        // auto guess settings if document column has not been set
        ColumnSelectionVerifier.verifyColumn(m_documentColModel, in, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        checkInputDataTableSpecs(inDataTableSpecs);
        checkInputPortSpecs(inSpecs);
        checkDocColTokenizerSettings(inDataTableSpecs);

        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizer.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizer.getStringValue());
        }

        ColumnRearranger r = createColumnRearranger(in);
        DataTableSpec out = r.createSpec();

        checkSettings();
        return new DataTableSpec[]{out};
    }

    /**
     * Method to check specs of input data tables. This method can be overwritten to apply specific checks.
     *
     * @param inSpecs Specs of the input data tables.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    protected void checkInputDataTableSpecs(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
    }

    /**
     * Method to check specs of input ports. This method can be overwritten to apply specific checks.
     *
     * @param inSpecs Specs of the input ports.
     * @throws InvalidSettingsException If settings or specs of input ports are invalid.
     */
    protected void checkInputPortSpecs(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
    }

    /**
     * Method to check tokenizer settings from incoming document column.
     *
     * @param inSpecs Specs of the input data tables.
     */
    private void checkDocColTokenizerSettings(final DataTableSpec[] inSpecs) {
        DataTableSpec inSpec = inSpecs[0];
        DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(inSpec);
        String tokenizerFromInput = dataTableSpecVerifier
            .getTokenizerFromInputDocCol(inSpec.findColumnIndex(m_documentColModel.getStringValue()));
        if (m_tokenizer.getStringValue().isEmpty() && tokenizerFromInput != null) {
            m_tokenizer.setStringValue(tokenizerFromInput);
            setWarningMessage("Auto select: Using  '" + m_tokenizer.getStringValue()
            + "' as word tokenizer based on incoming documents.");
        } else if (m_tokenizer.getStringValue().isEmpty()) {
            m_tokenizer.setStringValue(TextprocessingPreferenceInitializer.tokenizerName());
        }
        if (!dataTableSpecVerifier.verifyTokenizer(m_tokenizer.getStringValue())) {
            setWarningMessage(dataTableSpecVerifier.getTokenizerWarningMsg());
        }
    }

    /**
     * Method to prepare the tagger model. This method can be overwritten to apply loading of data from input data
     * tables for tagging.
     *
     * @param inData Input data tables.
     * @param exec The execution context of the node.
     * @throws Exception If tagger cannot be prepared.
     */
    protected void prepareTagger(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
    }

    /**
     * Method to prepare the tagger model. This method can be overwritten to apply loading of data from input data
     * tables for tagging.
     *
     * @param inObjects Input port objects.
     * @param exec The execution context of the node.
     * @throws Exception If tagger cannot be prepared.
     */
    protected void prepareTagger(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataTable[] inData =
            Arrays.stream(inObjects).map(po -> po instanceof BufferedDataTable ? (BufferedDataTable)po : null)
                .toArray(BufferedDataTable[]::new);
        prepareTagger(inData, exec);
    }

    /**
     * Creates column rearranger for creation of new data table.
     *
     * @param in the input data table spec.
     * @return A new instance of column rearranger to create output data table
     * @throws InvalidSettingsException If tagger instance cannot be created.
     */
    protected final ColumnRearranger createColumnRearranger(final DataTableSpec in) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(in);
        verifier.verifyMinimumDocumentCells(1, true);
        String docColName = m_documentColModel.getStringValue();

        docColName = m_documentColModel.getStringValue();
        int docColIndex = in.findColumnIndex(docColName);

        // check new column name
        String newColName = m_newDocumentColModel.getStringValue();
        if (m_replaceOldDocModel.getBooleanValue()) {
            newColName = docColName;
        } else {
            if (in.containsName(newColName)) {
                throw new InvalidSettingsException(
                    "Can't create new column \"" + newColName + "\" as input spec already contains such column!");
            }
        }

        final TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataColumnSpecCreator docColSpecCreator = new DataColumnSpecCreator(newColName, docFactory.getDataType());
        docColSpecCreator.setProperties(new DataColumnProperties(
            Collections.singletonMap(DocumentDataTableBuilder.WORD_TOKENIZER_KEY, m_tokenizer.getStringValue())));
        DataColumnSpec docCol = docColSpecCreator.createSpec();

        final int maxNumberOfParallelThreads;
        if (getMaxNumberOfParallelThreads() <= 0) {
            maxNumberOfParallelThreads = 1;
        } else {
            maxNumberOfParallelThreads = getMaxNumberOfParallelThreads();
        }

        final TaggerCellFactory cellFac = new TaggerCellFactory(this, docColIndex, docCol, maxNumberOfParallelThreads);
        final ColumnRearranger rearranger = new ColumnRearranger(in);
        // replace or append
        if (m_replaceOldDocModel.getBooleanValue()) {
            rearranger.replace(cellFac, docColIndex);
        } else {
            rearranger.append(cellFac);
        }

        return rearranger;
    }

    /** {@inheritDoc} */
    @Override
    public InputPortRole[] getInputPortRoles() {
        return m_roles;
    }

    /** {@inheritDoc} */
    @Override
    public OutputPortRole[] getOutputPortRoles() {
        OutputPortRole out = OutputPortRole.DISTRIBUTED;
        return new OutputPortRole[]{out};
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

                // covert non streamable in ports to PortObject
                PortObject[] inData = new PortObject[inputs.length];
                for (int i = 0; i < inputs.length; i++) {
                    if (m_roles[i].equals(InputPortRole.DISTRIBUTED_STREAMABLE)
                        || m_roles[i].equals(InputPortRole.NONDISTRIBUTED_STREAMABLE)) {
                        inData[i] = null;
                    } else {
                        // added null check, if nondistributed/nonstreamable inport is optional and has no data
                        if (inputs[i] != null) {
                            inData[i] = ((PortObjectInput)inputs[i]).getPortObject();
                        }
                    }
                }

                prepareTagger(inData, exec);
                ColumnRearranger colre = createColumnRearranger((DataTableSpec)inSpecs[0]);
                colre.createStreamableFunction().runFinal(inputs, outputs, exec);
            }
        };
    }

    /**
     * @return the maximum number of parallel threads to use for tagging.
     */
    protected int getMaxNumberOfParallelThreads() {
        return m_numberOfThreadsModel.getIntValue();
    }

    /**
     * @return The name of the tokenizer used for word tokenization.
     */
    protected String getTokenizerName() {
        return m_tokenizer.getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // possibly overwritten
    }

    /** {@inheritDoc} */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // possibly overwritten
    }

    /** {@inheritDoc} */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // possibly overwritten
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        // don't call if method if key is not available (needed since abner tagger does not use this setting)
        if (settings.containsKey(m_numberOfThreadsModel.getKey())) {
            m_numberOfThreadsModel.loadSettingsFrom(settings);
        }
        m_tokenizer.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
        m_replaceOldDocModel.loadSettingsFrom(settings);
        m_newDocumentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_numberOfThreadsModel.saveSettingsTo(settings);
        m_tokenizer.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
        m_replaceOldDocModel.saveSettingsTo(settings);
        m_newDocumentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        // don't call if method if key is not available (needed since abner tagger does not use this setting)
        if (settings.containsKey(m_numberOfThreadsModel.getKey())) {
            m_numberOfThreadsModel.validateSettings(settings);
        }
        m_tokenizer.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
        m_replaceOldDocModel.validateSettings(settings);
        m_newDocumentColModel.validateSettings(settings);
    }

    private void checkSettings() {
        if (m_replaceOldDocModel.getBooleanValue()) {
            m_newDocumentColModel.setEnabled(false);
        } else {
            m_newDocumentColModel.setEnabled(true);
        }
    }
}

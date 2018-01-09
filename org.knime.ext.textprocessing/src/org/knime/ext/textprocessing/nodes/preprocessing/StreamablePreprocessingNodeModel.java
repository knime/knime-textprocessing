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
 *   28.10.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public abstract class StreamablePreprocessingNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StreamablePreprocessingNodeModel.class);

    /** The default settings for preprocessing unmodifiable terms. */
    public static final boolean DEF_PREPRO_UNMODIFIABLE = false;

    /** The default setting for replacing the preprocessed document. */
    public static final boolean DEF_REPLACE = false;

    /** The default name of the new, preprocessed document column. */
    public static final String DEF_NEW_DOCUMENT_COL = "Preprocessed Document";

    private SettingsModelString m_documentColModel = PreprocessingNodeSettingsPane2.getDocumentColumnModel();

    private SettingsModelString m_newDocumentColModel = PreprocessingNodeSettingsPane2.getNewDocumentColumnModel();

    private SettingsModelBoolean m_replaceOldDocModel = PreprocessingNodeSettingsPane2.getReplaceDocumentModel();

    private SettingsModelBoolean m_preproUnModifiableModel =
        PreprocessingNodeSettingsPane2.getPreprocessUnmodifiableModel();

    private InputPortRole[] m_roles;

    /**
     * Default constructor, defining one data input and one data output port.
     */
    public StreamablePreprocessingNodeModel() {
        this(1, new InputPortRole[]{});
    }

    /**
     * Constructor defining a specified number of data input and one data output port.
     *
     * @param dataInPorts The number of data input ports.
     * @param roles The roles of the input ports after the first port.
     */
    public StreamablePreprocessingNodeModel(final int dataInPorts, final InputPortRole[] roles) {
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

        m_replaceOldDocModel.addChangeListener(new ColumnHandlingListener());
    }

    /** {@inheritDoc} */
    @Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        preparePreprocessing(inData, exec);
        final ColumnRearranger rearranger = createColumnRearranger(inData[0].getDataTableSpec());

        BufferedDataTable[] output =
            new BufferedDataTable[]{exec.createColumnRearrangeTable(inData[0], rearranger, exec)};
        afterProcessing();
        return output;
    }

    /** {@inheritDoc} */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        internalConfigure(inSpecs);
        ColumnRearranger r = createColumnRearranger(inSpecs[0]);
        DataTableSpec out = r.createSpec();
        return new DataTableSpec[]{out};
    }

    /**
     * Method to check specs of input data tables. This method can be overwritten to apply specific checks.
     *
     * @param inSpecs Specs of the input data tables.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
    }

    /**
     * Method to prepare preprocessing instance before it can be applied. This method can be overwritten to apply
     * preprocessing routines.
     *
     * @param inData input data tables.
     * @param exec the {@link ExecutionContext} during node execution.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    protected void preparePreprocessing(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws InvalidSettingsException {
    }

    /**
     * Creates a new instance of {@link TermPreprocessing} that will be used to preprocess the terms of the input
     * documents. Extending classes need to create the corresponding preprocessing instance here.
     *
     * @return A new instance of {@link TermPreprocessing} that will be used to preprocess the terms of the input
     *         documents.
     * @throws Exception If preprocessing instance cannot be created.
     */
    protected abstract TermPreprocessing createPreprocessing() throws Exception;

    /**
     * Creates a new instance of {@link TermPreprocessing} that will be used to preprocess the terms of the input
     * documents. Extending classes need to create the corresponding preprocessing instance here. Extends the behavior
     * of {@link StreamablePreprocessingNodeModel#createPreprocessing()} by an empty internals object that is filled
     * while processing the data.
     *
     * @param internals the empty internals.
     * @return A new instance of {@link TermPreprocessing} that will be used to preprocess the terms of the input
     *         documents.
     * @throws Exception
     * @since 3.3
     */
    protected TermPreprocessing createPreprocessingWithInternals(final StreamableOperatorInternals internals)
        throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates column rearranger for creation of new data table.
     *
     * @param in the input data table spec.
     * @return A new instance of column rearranger to create output data table
     * @throws InvalidSettingsException
     */
    protected final ColumnRearranger createColumnRearranger(final DataTableSpec in) throws InvalidSettingsException {
        return createColumnRearranger(in, createStreamingOperatorInternals());
    }

    /**
     * Creates column rearranger for creation of new data table. Extends the behavior of
     * {@link StreamablePreprocessingNodeModel#createColumnRearranger(DataTableSpec)} by an empty internals object that
     * is filled while processing the data.
     *
     * @param in the input data table spec.
     * @param internals the empty internals. Should be passed on to the cell factory
     * @return A new instance of column rearranger to create output data table
     * @throws InvalidSettingsException
     * @since 3.3
     */
    protected final ColumnRearranger createColumnRearranger(final DataTableSpec in,
        final StreamableOperatorInternals internals) throws InvalidSettingsException {
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(in);
        verfier.verifyMinimumDocumentCells(1, true);
        String docColName = m_documentColModel.getStringValue();
        int numberOfDocumentCols = verfier.getNumDocumentCells();

        // auto guess settings if document column has not been set
        if (docColName.isEmpty()) {
            // only one document col available, probably the first preprocessing node in the chain
            if (numberOfDocumentCols == 1) {
                String documentCol = in.getColumnSpec(verfier.getDocumentCellIndex()).getName();
                m_documentColModel.setStringValue(documentCol);
                m_replaceOldDocModel.setBooleanValue(false);
                m_newDocumentColModel.setStringValue(DEF_NEW_DOCUMENT_COL);
            } else if (numberOfDocumentCols > 1) {
                m_replaceOldDocModel.setBooleanValue(true);
                m_documentColModel.setStringValue(DEF_NEW_DOCUMENT_COL);
            }
        }

        // check selected document column
        docColName = m_documentColModel.getStringValue();
        int docColIndex = in.findColumnIndex(docColName);
        if (docColIndex < 0) {
            throw new InvalidSettingsException("Selected document column \"" + m_documentColModel.getStringValue()
                + "\" could not be found in the input data table.");
        }

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

        // create new column spec
        final TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataColumnSpec docCol = new DataColumnSpecCreator(newColName, docFactory.getDataType()).createSpec();

        // create cell factory and column rearranger
        try {

            TermPreprocessing preprocessing = createPreprocessing();
            if (preprocessing == null) {
                preprocessing = createPreprocessingWithInternals(internals);
            }
            final PreprocessingCellFactory cellFac = new PreprocessingCellFactory(preprocessing, docColIndex, docCol,
                m_preproUnModifiableModel.getBooleanValue());
            final ColumnRearranger rearranger = new ColumnRearranger(in);

            // replace or append
            if (m_replaceOldDocModel.getBooleanValue()) {
                rearranger.replace(cellFac, docColIndex);
            } else {
                rearranger.append(cellFac);
            }

            return rearranger;
        } catch (Exception e) {
            LOGGER.error("Preprocessing instance could not be created!");
            throw new InvalidSettingsException(e);
        }
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

                // covert non streamable in ports to BufferedDatatables
                BufferedDataTable[] inData = new BufferedDataTable[inputs.length];
                for (int i = 0; i < inputs.length; i++) {
                    if (m_roles[i].equals(InputPortRole.DISTRIBUTED_STREAMABLE)
                        || m_roles[i].equals(InputPortRole.NONDISTRIBUTED_STREAMABLE)) {
                        inData[i] = null;
                    } else {
                        inData[i] = (BufferedDataTable)((PortObjectInput)inputs[i]).getPortObject();
                    }
                }

                preparePreprocessing(inData, exec);
                ColumnRearranger colre = createColumnRearranger((DataTableSpec)inSpecs[0]);
                colre.createStreamableFunction().runFinal(inputs, outputs, exec);
            }
        };
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
        m_preproUnModifiableModel.saveSettingsTo(settings);
        m_replaceOldDocModel.saveSettingsTo(settings);
        m_newDocumentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
        m_preproUnModifiableModel.validateSettings(settings);
        m_replaceOldDocModel.validateSettings(settings);
        m_newDocumentColModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
        m_preproUnModifiableModel.loadSettingsFrom(settings);
        m_replaceOldDocModel.loadSettingsFrom(settings);
        m_newDocumentColModel.loadSettingsFrom(settings);
    }

    private final class ColumnHandlingListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            if (m_replaceOldDocModel.getBooleanValue()) {
                m_newDocumentColModel.setEnabled(false);
            } else {
                m_newDocumentColModel.setEnabled(true);
            }
        }
    }

    /**
     * Called after all rows have been processed in the
     * {@link StreamablePreprocessingNodeModel#execute(BufferedDataTable[],ExecutionContext)} method. Can be overridden
     * to set, e.g Warning messages, etc
     *
     * @since 3.3
     */
    protected void afterProcessing() {
        // nothing to do by default
    }

    /**
     * Creates new empty instance of the internals. Should be overridden in extending classes.
     *
     * @return A new instance of the internals (should not be null)
     * @since 3.3
     */
    protected StreamableOperatorInternals createStreamingOperatorInternals() {
        return null;
    }
}

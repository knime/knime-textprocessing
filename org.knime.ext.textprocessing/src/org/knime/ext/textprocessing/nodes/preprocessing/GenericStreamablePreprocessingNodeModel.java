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
 *   Oct 4, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.NotImplementedException;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.core.node.streamable.OutputPortRole;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * Generic abstract node model for preprocessing nodes. It keeps functionality for streaming, column rearranging,
 * settings models etc. Extensions of this class are using specific implementations of the {@link Preprocessing}
 * interface to provide the desired preprocessing functionality.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
abstract class GenericStreamablePreprocessingNodeModel<T extends Preprocessing> extends NodeModel {

    /** The default settings for preprocessing unmodifiable terms. */
    public static final boolean DEF_PREPRO_UNMODIFIABLE = false;

    /** The default setting for replacing the preprocessed document. */
    public static final boolean DEF_REPLACE = false;

    /** The default name of the new, preprocessed document column. */
    public static final String DEF_NEW_DOCUMENT_COL = "Preprocessed Document";

    /** The {@code SettingsModelString} keeping the the name of the document to preprocess. */
    private final SettingsModelString m_documentColModel = PreprocessingNodeSettingsPane2.getDocumentColumnModel();

    /** The {@code SettingsModelString} keeping the the name of the preprocessed document column to create. */
    private final SettingsModelString m_newDocumentColModel =
        PreprocessingNodeSettingsPane2.getNewDocumentColumnModel();

    /** The {@code SettingsModelBoolean} keeping the boolean value of the option to replace the old document column. */
    private final SettingsModelBoolean m_replaceOldDocModel = PreprocessingNodeSettingsPane2.getReplaceDocumentModel();

    /** The {@code SettingsModelBoolean} keeping the boolean value of the option to preprocess unmodifiable terms. */
    private final SettingsModelBoolean m_preproUnModifiableModel =
            PreprocessingNodeSettingsPane2.getPreprocessUnmodifiableModel();

    /** The role of the input ports. */
    private InputPortRole[] m_roles;

    /**
     * Default constructor, defining one data input and one data output port.
     */
    protected GenericStreamablePreprocessingNodeModel() {
        this(1, new InputPortRole[]{});
    }

    /**
     * Constructor defining a specified number of data input and one data output port.
     *
     * @param dataInPorts The number of data input ports.
     * @param roles The roles of the input ports after the first port.
     */
    protected GenericStreamablePreprocessingNodeModel(final int dataInPorts, final InputPortRole[] roles) {
        super(dataInPorts, 1);

        if (roles.length != (dataInPorts - 1)) {
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

    /**
     * Constructor defining a node model with different input and output port types.
     *
     * @param inPortTypes The input port types. First port type has to be BufferedDataTable, since its role is set to
     *            {@link InputPortRole#DISTRIBUTED_STREAMABLE}.
     * @param outPortTypes The output port types.
     * @param roles The roles of the input ports after the first port.
     */
    protected GenericStreamablePreprocessingNodeModel(final PortType[] inPortTypes, final PortType[] outPortTypes,
        final InputPortRole[] roles) {
        super(inPortTypes, outPortTypes);

        if (roles.length != (inPortTypes.length - 1)) {
            throw new IllegalArgumentException(
                    "Number of input port roles must be equal to number of data in ports -1!");
        }
        m_roles = new InputPortRole[inPortTypes.length];
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

        final BufferedDataTable[] output =
                new BufferedDataTable[]{exec.createColumnRearrangeTable(inData[0], rearranger, exec)};
        afterProcessing();
        return output;
    }

    /** {@inheritDoc} */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        internalConfigure(inSpecs);
        final ColumnRearranger r = createColumnRearranger(inSpecs[0]);
        final DataTableSpec out = r.createSpec();
        return new DataTableSpec[]{out};
    }

    /**
     * Method to check specs of input data tables. This method can be overwritten to apply specific checks.
     *
     * @param inSpecs Specs of the input data tables.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        // nothing to do here
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
        // nothing to do here
    }

    /**
     * Creates a new instance of an implementation of {@link Preprocessing} that will be used to preprocess the terms of
     * the input documents. Extending classes need to create the corresponding preprocessing instance here.
     *
     * @return A new instance of an implementation of {@link Preprocessing} that will be used to preprocess the terms of
     *         the input documents.
     * @throws Exception If preprocessing instance cannot be created.
     */
    protected T createPreprocessing() throws Exception {
        throw new NotImplementedException(
            "Either createPreprocessing() or createPreprocessing(DataTableSpec) must be implemented in subclasses of "
                + GenericStreamablePreprocessingNodeModel.class.getSimpleName());
    }

    /**
     * Creates a new instance of an implementation of {@link Preprocessing} that will be used to preprocess the terms of
     * the input documents. Extending classes need to create the corresponding preprocessing instance here.
     *
     * @param columnSpec the {@link DataColumnSpec} of the document column for which the preprocessing is created
     * @return A new instance of an implementation of {@link Preprocessing} that will be used to preprocess the terms of
     *         the input documents.
     * @throws Exception If preprocessing instance cannot be created.
     */
    protected T createPreprocessing(final DataColumnSpec columnSpec) throws Exception {
        return createPreprocessing();
    }

    /**
     * Creates a new instance of an implementation of {@link Preprocessing} that will be used to preprocess the terms of
     * the input documents. Extending classes need to create the corresponding preprocessing instance here. Extends the
     * behavior of {@code createPreprocessing()} by an empty internals object that is filled while processing the data.
     *
     * @param internals the empty internals.
     * @return A new instance of {@link TermPreprocessing} that will be used to preprocess the terms of the input
     *         documents.
     * @throws Exception
     */
    protected T createPreprocessingWithInternals(final StreamableOperatorInternals internals) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a new instance of an extension of {@code SingleCellFactory}.
     * This can be {@link PreprocessingCellFactory} or {@link SentencePreprocessingCellFactory}, depending on the
     * specific implementation of {@link Preprocessing} that is passed as an argument.
     *
     * @param preprocessing The specific implementation of {@code Preprocessing}.
     * @param docColIndex The document column index.
     * @param docCol The {@code DataColumnSpec}
     * @param unmodifiable True, if unmodifiable terms should be preprocessed.
     * @return Returns a new instance of {@code SingleCellFactory}.
     */
    private SingleCellFactory createDocumentCellFactory(final T preprocessing, final int docColIndex,
        final DataColumnSpec docCol, final boolean unmodifiable) {
        if (preprocessing instanceof TermPreprocessing) {
            return new PreprocessingCellFactory((TermPreprocessing)preprocessing, docColIndex, docCol, unmodifiable);
        }
        if (preprocessing instanceof SentencePreprocessing) {
            return new SentencePreprocessingCellFactory((SentencePreprocessing)preprocessing, docColIndex, docCol,
                unmodifiable);
        }
        throw new IllegalArgumentException(
            "There is no cell factory supporting \"" + preprocessing.getClass().getName() + "\".");
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
     */
    protected final ColumnRearranger createColumnRearranger(final DataTableSpec in,
        final StreamableOperatorInternals internals) throws InvalidSettingsException {
        final DataTableSpecVerifier verfier = new DataTableSpecVerifier(in);
        verfier.verifyMinimumDocumentCells(1, true);
        String docColName = m_documentColModel.getStringValue();
        final int numberOfDocumentCols = verfier.getNumDocumentCells();

        // auto guess settings if document column has not been set
        if (docColName.isEmpty()) {
            // only one document col available, probably the first preprocessing node in the chain
            if (numberOfDocumentCols == 1) {
                final String documentCol = in.getColumnSpec(verfier.getDocumentCellIndex()).getName();
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
        final int docColIndex = in.findColumnIndex(docColName);
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
        final var docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        final var selectedDocCol = in.getColumnSpec(docColIndex);
        var docColCreator = new DataColumnSpecCreator(selectedDocCol);
        docColCreator.setType(docFactory.getDataType());
        docColCreator.setName(newColName);
        final DataColumnSpec docCol = docColCreator.createSpec();

        // create cell factory and column rearranger
        try {

            T preprocessing = createPreprocessing(selectedDocCol);
            if (preprocessing == null) {
                preprocessing = createPreprocessingWithInternals(internals);
            }


            final ColumnRearranger rearranger = new ColumnRearranger(in);
            final SingleCellFactory cellFac = createDocumentCellFactory(preprocessing, docColIndex, docCol,
                m_preproUnModifiableModel.getBooleanValue());

            // replace or append
            if (m_replaceOldDocModel.getBooleanValue()) {
                rearranger.replace(cellFac, docColIndex);
            } else {
                rearranger.append(cellFac);
            }

            return rearranger;
        } catch (final Exception e) {
            throw new InvalidSettingsException("Preprocessing instance could not be created", e);
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
        final OutputPortRole out = OutputPortRole.DISTRIBUTED;
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
                final BufferedDataTable[] inData = new BufferedDataTable[inputs.length];
                for (int i = 0; i < inputs.length; i++) {
                    if (m_roles[i].equals(InputPortRole.DISTRIBUTED_STREAMABLE)
                            || m_roles[i].equals(InputPortRole.NONDISTRIBUTED_STREAMABLE)) {
                        inData[i] = null;
                    } else if (inputs[i] != null) {
                        inData[i] = (BufferedDataTable)((PortObjectInput)inputs[i]).getPortObject();
                    }
                }

                preparePreprocessing(inData, exec);
                final ColumnRearranger colre = createColumnRearranger((DataTableSpec)inSpecs[0]);
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

    /**
     * Returns the document column name.
     *
     * @return Returns the document column name.
     */
    protected final String getDocumentColumn() {
        return m_documentColModel.getStringValue();
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
     * {@code execute(BufferedDataTable[],ExecutionContext)} method. Can be overridden
     * to set, e.g Warning messages, etc
     */
    protected void afterProcessing() {
        // nothing to do by default
    }

    /**
     * Creates new empty instance of the internals. Should be overridden in extending classes.
     *
     * @return A new instance of the internals (should not be null)
     */
    protected StreamableOperatorInternals createStreamingOperatorInternals() {
        return null;
    }

}

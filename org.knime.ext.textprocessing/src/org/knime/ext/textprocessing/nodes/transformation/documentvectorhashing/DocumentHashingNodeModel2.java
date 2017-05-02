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
 *   10.08.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectorhashing;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortObjectOutput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.node.streamable.StreamableOperatorInternals;
import org.knime.ext.textprocessing.data.VectorHashingPortObject;
import org.knime.ext.textprocessing.data.VectorHashingPortObjectSpec;

/**
 * The node model of the Document vector hashing node. This model extends
 * {@link org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel} and is streamable.
 *
 * @author Tobias Koetter and Andisa Dewi, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public class DocumentHashingNodeModel2 extends AbstractDocumentHashingNodeModel {

    /**
     * Default seed value
     */
    protected static final int DEFAULT_SEED = new Random().nextInt();

    /**
     * Default value whether to use settings from input port model or from dialog.
     */
    public static final boolean DEFAULT_USEINPORTSPECS = false;

    private final SettingsModelIntegerBounded m_dimModel = DocumentHashingNodeDialog2.getDimModel();

    private SettingsModelInteger m_seedModel = DocumentHashingNodeDialog2.getSeedModel();

    private final SettingsModelString m_vectValModel = DocumentHashingNodeDialog2.getVectorValueModel();

    private final SettingsModelString m_hashFuncModel = DocumentHashingNodeDialog2.getHashingMethod();

    private PortObjectSpec m_modelSpec;

    /**
     * Creates a new instance of <code>DocumentHashingNodeModel2</code>. For each node, a new integer value is assigned
     * as initial value of the seed
     */
    public DocumentHashingNodeModel2() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE,
            PortTypeRegistry.getInstance().getPortType(VectorHashingPortObject.class, false)}, 0, 0);
        m_seedModel.setIntValue(new Random().nextInt());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataTable in = (BufferedDataTable)inObjects[0];
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable table = exec.createColumnRearrangeTable(in, r, exec);
        return new PortObject[]{table, new VectorHashingPortObject(m_modelSpec)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec in = (DataTableSpec)inSpecs[0];
        setValues(m_dimModel.getIntValue(), m_seedModel.getIntValue(), m_hashFuncModel.getStringValue(),
            m_vectValModel.getStringValue());
        ColumnRearranger r = createColumnRearranger(in);
        DataTableSpec out = r.createSpec();
        m_modelSpec = new VectorHashingPortObjectSpec(m_dimModel.getIntValue(), m_seedModel.getIntValue(),
            m_hashFuncModel.getStringValue(), m_vectValModel.getStringValue());
        return new PortObjectSpec[]{out, m_modelSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec) throws Exception {
                ColumnRearranger colre = createColumnRearranger((DataTableSpec)inSpecs[0]);
                colre.createStreamableFunction(0, 0).runFinal(inputs, outputs, exec);

                // set model output to null since the runFinal method affects distributed outputs and the model output
                // is not distributed
                outputs[1] = null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishStreamableExecution(final StreamableOperatorInternals internals, final ExecutionContext exec,
        final PortOutput[] output) throws Exception {
        ((PortObjectOutput)output[1]).setPortObject(new VectorHashingPortObject(m_modelSpec));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergeOperator createMergeOperator() {
        // create MergeOperator to run finishStreamableExecution
        return new MergeOperator() {

            @Override
            public StreamableOperatorInternals mergeFinal(final StreamableOperatorInternals[] operators) {
                return operators[0];
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_dimModel.saveSettingsTo(settings);
        m_seedModel.saveSettingsTo(settings);
        m_vectValModel.saveSettingsTo(settings);
        m_hashFuncModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_dimModel.validateSettings(settings);
        m_seedModel.validateSettings(settings);
        m_vectValModel.validateSettings(settings);
        m_hashFuncModel.validateSettings(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_dimModel.loadSettingsFrom(settings);
        m_seedModel.loadSettingsFrom(settings);
        m_vectValModel.loadSettingsFrom(settings);
        m_hashFuncModel.loadSettingsFrom(settings);
    }

    @Override
    protected void reset() {
        m_modelSpec = null;
    }

    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub
    }

}

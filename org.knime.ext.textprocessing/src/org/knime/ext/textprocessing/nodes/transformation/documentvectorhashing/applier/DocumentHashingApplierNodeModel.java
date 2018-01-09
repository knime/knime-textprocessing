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
 *   28.04.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectorhashing.applier;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.ext.textprocessing.data.VectorHashingPortObject;
import org.knime.ext.textprocessing.data.VectorHashingPortObjectSpec;
import org.knime.ext.textprocessing.nodes.transformation.documentvectorhashing.AbstractDocumentHashingNodeModel;

/**
 * The {@code NodeModel} for the Document vector hashing applier node. This node model extends the
 * {@link AbstractDocumentHashingNodeModel} which contains the business logic of this node. This class is necessary
 * since this node has different input-/output-ports, configuration, execution and streaming handling compared to the
 * Document vector hashing node ({@code DocumentHashingNodeModel2}) which shares the same superclass.
 * This node model is streamable.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
final class DocumentHashingApplierNodeModel extends AbstractDocumentHashingNodeModel {

    /**
     * Creates a new instance of the {@code DocumentHashingApplierNodeModel} with one {@code VectorHashingPortObject}
     * input port, one {@code BufferedDataTable} input port and one {@code BufferedDataTable} output port.
     */
    DocumentHashingApplierNodeModel() {
        super(new PortType[]{PortTypeRegistry.getInstance().getPortType(VectorHashingPortObject.class, false),
            BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE}, 1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataTable in = (BufferedDataTable)inObjects[1];
        ColumnRearranger r = createColumnRearranger(in.getDataTableSpec());
        BufferedDataTable table = exec.createColumnRearrangeTable(in, r, exec);
        return new PortObject[]{table};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec in = (DataTableSpec)inSpecs[1];
        VectorHashingPortObjectSpec modelSpec = (VectorHashingPortObjectSpec)inSpecs[0];
        // set parameter for vector creation
        setValues(modelSpec.getDimension(), modelSpec.getSeed(), modelSpec.getHashFunc(), modelSpec.getVectVal());
        ColumnRearranger r = createColumnRearranger(in);
        DataTableSpec out = r.createSpec();
        return new PortObjectSpec[]{out};
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
                ColumnRearranger colre = createColumnRearranger((DataTableSpec)inSpecs[1]);
                colre.createStreamableFunction(1, 0).runFinal(inputs, outputs, exec);
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

}

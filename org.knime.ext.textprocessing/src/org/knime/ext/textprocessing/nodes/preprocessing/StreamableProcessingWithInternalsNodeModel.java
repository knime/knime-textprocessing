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
 *   31.10.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.lang.reflect.Array;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.MergeOperator;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.StreamableFunction;
import org.knime.core.node.streamable.StreamableOperatorInternals;

/**
 * Abstract class for preprocessing streaming nodes that need to have some final processing afterwards e.g, set an error
 * message.
 *
 * @param <I> The sub type of the internals used by the implementation
 * @author Andisa Dewi , KNIME.com, Berlin, Germany
 * @since 3.3
 */
public abstract class StreamableProcessingWithInternalsNodeModel<I extends StreamableOperatorInternals>
    extends StreamablePreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StreamableProcessingWithInternalsNodeModel.class);

    private Class<I> m_class;

    /**
     * @param cl The class of the {@link StreamableOperatorInternals}.
     */
    public StreamableProcessingWithInternalsNodeModel(final Class<I> cl) {
        m_class = cl;
    }

    /**
     * Method to prepare preprocessing instance before it can be applied. This method can be overwritten to apply
     * preprocessing routines.
     *
     * @param inSpecs the specs of the input port objects.
     * @throws InvalidSettingsException If settings or specs are invalid.
     */
    protected void preparePreprocessing(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void preparePreprocessing(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws InvalidSettingsException {
        PortObjectSpec[] inSpecs = new PortObjectSpec[inData.length];
        for (int i = 0; i < inData.length; i++) {
            inSpecs[i] = inData[i].getDataTableSpec();
        }
        preparePreprocessing(inSpecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    protected TermPreprocessing createPreprocessingWithInternals(final StreamableOperatorInternals internals)
        throws Exception {
        return createPreprocessing((I)internals);
    }

    /**
     * Extends the behavior of {@link StreamableProcessingWithInternalsNodeModel#createPreprocessing()} by an empty
     * internals object that should be passed to the {@link TermPreprocessing} object.
     *
     * @param internals the empty internals
     * @return a {@link TermPreprocessing} object
     * @throws Exception
     */
    protected abstract TermPreprocessing createPreprocessing(I internals) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public StreamableFunction createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        preparePreprocessing(inSpecs);
        DataTableSpec in = (DataTableSpec)inSpecs[0];
        final I emptyInternals = createStreamingOperatorInternals();
        if (emptyInternals == null) {
            throw new NullPointerException("createStreamingOperatorInternals" + " in class "
                + getClass().getSimpleName() + " must not return null");
        }
        return createColumnRearranger(in, emptyInternals).createStreamableFunction(emptyInternals);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected I createStreamingOperatorInternals() {
        try {
            return m_class.newInstance();
        } catch (Exception e) {
            final String msg = "Internals class \"" + m_class.getSimpleName()
                + "\" does not appear to have public default constructor";
            LOGGER.coding(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergeOperator createMergeOperator() {
        return new MergeOperator() {

            @Override
            public StreamableOperatorInternals mergeFinal(final StreamableOperatorInternals[] internals) {
                @SuppressWarnings("unchecked")
                I[] castedInternals = (I[])Array.newInstance(m_class, internals.length);
                for (int i = 0; i < internals.length; i++) {
                    StreamableOperatorInternals o = internals[i];
                    if (o == null) {
                        throw new NullPointerException("internals at position " + i + " is null");
                    } else if (!m_class.isInstance(o)) {
                        throw new IllegalStateException(String.format(
                            "Internals at position %d is not of expected class \"%s\", it's a \"%s\"", i,
                            m_class.getSimpleName(), o.getClass().getSimpleName()));
                    }
                    castedInternals[i] = m_class.cast(o);
                }
                return mergeStreamingOperatorInternals(castedInternals);
            }

        };
    }

    /**
     * Called the merge operator to merge internals created by different streamable operators (possibly on remote
     * machines).
     *
     * @param operatorInternals The internals to merge.
     * @return A new merged internals object.
     */
    protected abstract I mergeStreamingOperatorInternals(final I[] operatorInternals);

    /**
     * {@inheritDoc}
     */
    @Override
    public void finishStreamableExecution(final StreamableOperatorInternals internals, final ExecutionContext exec,
        final PortOutput[] output) throws Exception {
        finishStreamableExecution(m_class.cast(internals));
    }

    /**
     * Finalizes execution with a merged internals object. Clients can access its fields and update view content or set
     * warning messages.
     *
     * @param operatorInternals The merged internals object.
     */
    protected abstract void finishStreamableExecution(final I operatorInternals);

}

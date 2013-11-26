/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 25.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.tagging;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;


/**
 * An abstract node model for tagger nodes.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public abstract class TaggerNodeModel extends NodeModel implements DocumentTaggerFactory {

    /**
     * Default number of threads to use for parallel tagging.
     */
    public static final int DEFAULT_NUMBER_OF_THREADS = 1;

    /**
     * Default number of threads to use for parallel tagging.
     */
    public static final int MAX_NUMBER_OF_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    /** Index of the document column. */
    private int m_docColIndex = -1;

    /** The settings model storing the number of threads to use for tagging. */
    private SettingsModelIntegerBounded m_numberOfThreadsModel = TaggerNodeSettingsPane.getNumberOfThreadsModel();

    /**
     * Constructor of {@link TaggerNodeModel} creating one data in and one data out port.
     */
    public TaggerNodeModel() {
        super(1, 1);
    }

    /**
     * Constructor for class {@link TaggerNodeModel} with the specified number of in ports and one out port.
     * @param inports The number of input data ports.
     */
    public TaggerNodeModel(final int inports) {
        super(inports, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(inSpecs[0]);
        verfier.verifyDocumentCell(true);
        m_docColIndex = verfier.getDocumentCellIndex();

        checkInputDataTableSpecs(inSpecs);

        return new DataTableSpec[]{createDataTableSpec()};
    }

    /**
     * Method to check specs of input data tables. This method can be overwritten to apply specific checks of
     * underlying implementations of {@link TaggerNodeModel}.
     *
     * @param inSpecs Specs of the input data tables.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    protected void checkInputDataTableSpecs(final DataTableSpec[] inSpecs) throws InvalidSettingsException { }

    private static final DataTableSpec createDataTableSpec() {
        return new DataTableSpec(createNewColSpecs());
    }

    private static final DataColumnSpec[] createNewColSpecs() {
        final TextContainerDataCellFactory docFactory = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        DataColumnSpec docCol = new DataColumnSpecCreator("Document", docFactory.getDataType()).createSpec();
        return new DataColumnSpec[]{docCol};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        prepareTagger(inData, exec);

        final TaggerCellFactory cellFac =
            new TaggerCellFactory(this, m_docColIndex, createNewColSpecs(), m_numberOfThreadsModel.getIntValue(), exec);
        final ColumnRearranger rearranger = new ColumnRearranger(inData[0].getDataTableSpec());
        rearranger.replace(cellFac, m_docColIndex);

        // remove all columns except the document column (due to downwards compatibility reasons)
        final int numberOfCols = inData[0].getDataTableSpec().getNumColumns();
        if (numberOfCols > 1) {
            int[] removeIndices = new int[numberOfCols - 1];
            int idx = 0;
            for (int i = 0; i < numberOfCols; i++) {
                if (i != m_docColIndex) {
                   removeIndices[idx] = i;
                   idx++;
                }
            }
            rearranger.remove(removeIndices);
        }

        return new BufferedDataTable[]{exec.createColumnRearrangeTable(inData[0], rearranger, exec)};
    }

    /**
     * Method to prepare the tagger model. This method can be overwritten to apply loading of data from input data
     * tables for tagging.
     * @param inData Input data tables.
     * @param exec The execution context of the node.
     * @throws Exception If tagger can not be prepared.
     */
    protected void prepareTagger(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            m_numberOfThreadsModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            // don't warn just catch (for downwards compatibility)
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_numberOfThreadsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            m_numberOfThreadsModel.validateSettings(settings);
        } catch (InvalidSettingsException e) {
            // don't warn just catch (for downwards compatibility)
        }
    }
}

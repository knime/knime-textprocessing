/*
 * ------------------------------------------------------------------------
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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * This class represents the super class of all text frequency computation node 
 * models. Classes which extend <code>FrequencyNodeModel</code> need to 
 * implement the method
 * {@link FrequencyNodeModel#initCellFactory(BufferedDataTable, ExecutionContext)} 
 * and take care of a proper initialization of the used 
 * {@link org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory} 
 * instance. The configure and execute procedure is done by the 
 * <code>FrequencyNodeModel</code>, classes extending this model do not
 * need to care about that. Once the used <code>FrequencyCellFactory</code> 
 * instance is initialized properly the rest is done automatically.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class FrequencyNodeModel extends NodeModel {

    private static final int INDATA_INDEX = 0;
    
    
    private int m_documentColIndex = -1;
    
    private int m_termColIndex = -1;
    
    private SettingsModelString m_documentColModel =
        FrequenciesNodeSettingsPane.getDocumentColumnModel();
    
    
    
    /**
     * The cell factory creating the cells containing a certain frequency value.
     */
    protected FrequencyCellFactory m_cellFac;
    
    
    /**
     * Creates a new instance of <code>FrequencyNodeModel</code> with given
     * column name and flag if an integer column has to be appended or a
     * double column.
     */
    public FrequencyNodeModel() {
        super(1, 1);
    }
    
    /**
     * Initializes the underlying <code>FrequencyCellFactory</code> and passes 
     * through the in data and an execution context.
     * 
     * @param inData The input data.
     * @param exec An execution context to monitor the progress.
     * @throws CanceledExecutionException If the user canceled the execution.
     */
    protected abstract void initCellFactory(BufferedDataTable inData,
            ExecutionContext exec) throws CanceledExecutionException;
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[INDATA_INDEX]);
        return new DataTableSpec[]{createDataTableSpec(inSpecs[INDATA_INDEX])};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
        
        m_documentColIndex = spec.findColumnIndex(
                m_documentColModel.getStringValue());
        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! " 
                    + "Check your settings!");
        } 
    }
    
    /**
     * Creates a new <code>DataTableSpec</code>.
     * 
     * @param inDataSpec The input <code>DataTableSpec</code>.
     * @return The new created output <code>DataTableSpec</code>.
     */
    protected abstract DataTableSpec createDataTableSpec(
            final DataTableSpec inDataSpec);
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(
            final BufferedDataTable[] inData, final ExecutionContext exec) 
    throws Exception {
        BufferedDataTable inDataTable = inData[INDATA_INDEX];
        
        m_documentColIndex = inDataTable.getDataTableSpec().findColumnIndex(
                m_documentColModel.getStringValue());
        
        // initializes the corresponding cell factory
        initCellFactory(inDataTable, exec);
        if (m_cellFac == null) {
            throw new NullPointerException(
                    "CellFactory instance may not be null!");
        }
        
        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(
                inDataTable.getDataTableSpec());
        rearranger.append(m_cellFac);
        
        return new BufferedDataTable[] {
                exec.createColumnRearrangeTable(inDataTable, rearranger, 
                exec)};
    }

    /**
     * @return the documentColIndex
     */
    public int getDocumentColIndex() {
        return m_documentColIndex;
    }

    /**
     * @return the termColIndex
     */
    public int getTermColIndex() {
        return m_termColIndex;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
    }
}

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
 *   21.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.filter;

import java.io.File;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.base.node.preproc.filter.row.RowFilterTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleRange;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The model class of the filter node. Providing all default settings of the
 * filters, as well as the management of the filtering process.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class FilterNodeModel extends NodeModel {

    /**
     * Specifies the threshold filtering.
     */
    public static final String SELECTION_THRESHOLD = "Threshold";

    /**
     * Specifies the number filtering.
     */
    public static final String SELECTION_NUMBER = "Number of terms";

    /**
     * The default filtering.
     */
    public static final String DEF_SELECTION = SELECTION_NUMBER;


    /**
     * The default number of numbe filtering.
     */
    public static final int DEF_NUMBER = 1000;

    /**
     * The min number of number filtering.
     */
    public static final int MIN_NUMBER = 0;

    /**
     * The max number of number filtering.
     */
    public static final int MAX_NUMBER = Integer.MAX_VALUE;


    /**
     * The default minimum number of threshold filtering.
     */
    public static final double DEF_MIN_THRESHOLD = 0.01;

    /**
     * The min minimum number of threshold filtering.
     */
    public static final double MIN_MIN_THRESHOLD = 0;

    /**
     * The default maximum number of threshold filtering.
     */
    public static final double DEF_MAX_THRESHOLD = 1.0;

    /**
     * The max maximum number of threshold filtering.
     */
    public static final double MAX_MAX_THRESHOLD = 1000;

    /**
     * The default settings for deep filtering.
     */
    public static final boolean DEF_DEEP_FILTERING = true;

    /**
     * The default setting for modification of unmodifiable terms.
     */
    public static final boolean DEF_MODIFY_UNMODIFIABLE = false;

    private SettingsModelString m_filterSelectionModel =
        FilterNodeDialog.getSelectionModel();

    private SettingsModelString m_colModel = FilterNodeDialog.getColModel();

    private SettingsModelIntegerBounded m_numberModel =
        FilterNodeDialog.getNumberModel();

    private SettingsModelDoubleRange m_minMaxModel =
        FilterNodeDialog.getMinMaxModel();

    private SettingsModelBoolean m_deepFilteringModel =
        FilterNodeDialog.getDeepFilteringModel();

    private SettingsModelBoolean m_modifyUnmodifiableModel =
        FilterNodeDialog.getModifyUnmodifiableModel();

    private SettingsModelString m_documentColModel =
        PreprocessingNodeSettingsPane.getDocumentColumnModel();

    private int m_termColIndex = -1;

    /**
     * Creates an new instance of <code>FilterNodeModel</code>.
     */
    public FilterNodeModel() {
        super(1, 1);

        m_filterSelectionModel.addChangeListener(
                new FilterOptionChangeListener());
        enableModels();
    }

    private final void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        verifier.verifyMinimumNumberCells(1, true);
        m_termColIndex = verifier.getTermCellIndex();

        int documentColIndex = spec.findColumnIndex(
                m_documentColModel.getStringValue());
        if (documentColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! "
                    + "Check your settings!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return inSpecs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        int filterColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_colModel.getStringValue());

        // Filtering
        FrequencyFilter filter = FilterFactory.createFilter(
                m_filterSelectionModel.getStringValue(), m_termColIndex,
                filterColIndex, m_numberModel.getIntValue(),
                m_minMaxModel.getMinRange(), m_minMaxModel.getMaxRange(),
                m_modifyUnmodifiableModel.getBooleanValue());

        ExecutionContext subExec1 = exec.createSubExecutionContext(0.3);
        BufferedDataTable preprocessedTable =
            filter.preprocessData(inData[0], subExec1);

        BufferedDataTable filteredTable;
        synchronized (this) {
            filteredTable = exec.createBufferedDataTable(
                    new RowFilterTable(preprocessedTable, filter), exec);
        }

        // Deep filtering
        ExecutionContext subExec3 = exec.createSubExecutionContext(0.7);
        if (m_deepFilteringModel.getBooleanValue()) {
            TermPurger purger = new TermPurger(filteredTable, subExec3,
                    m_documentColModel.getStringValue());
            return new BufferedDataTable[]{purger.getPurgedDataTable()};
        }

        return new BufferedDataTable[]{
                exec.createBufferedDataTable(filteredTable, subExec3)};
    }

    private void enableModels() {
        if (m_filterSelectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_NUMBER)) {
            m_numberModel.setEnabled(true);
            m_minMaxModel.setEnabled(false);
        } else if (m_filterSelectionModel.getStringValue().equals(
                FilterNodeModel.SELECTION_THRESHOLD)) {
            m_numberModel.setEnabled(false);
            m_minMaxModel.setEnabled(true);
        }
    }

    private class FilterOptionChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            enableModels();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filterSelectionModel.saveSettingsTo(settings);
        m_colModel.saveSettingsTo(settings);
        m_numberModel.saveSettingsTo(settings);
        m_minMaxModel.saveSettingsTo(settings);
        m_deepFilteringModel.saveSettingsTo(settings);
        m_modifyUnmodifiableModel.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterSelectionModel.validateSettings(settings);
        m_colModel.validateSettings(settings);
        m_numberModel.validateSettings(settings);
        m_minMaxModel.validateSettings(settings);
        m_deepFilteringModel.validateSettings(settings);
        m_modifyUnmodifiableModel.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_filterSelectionModel.loadSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_numberModel.loadSettingsFrom(settings);
        m_minMaxModel.loadSettingsFrom(settings);
        m_deepFilteringModel.loadSettingsFrom(settings);
        m_modifyUnmodifiableModel.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

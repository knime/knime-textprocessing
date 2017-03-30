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
 *   24.05.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.misc.markuptagfilter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.ext.textprocessing.data.DocumentCell;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class MarkupTagFilterNodeModel extends SimpleStreamableFunctionNodeModel {

    /**
     * Default append column setting.
     */
    public static final boolean DEF_APPEND_COLUMNS = true;

    /**
     * Default column suffix.
     */
    public static final String DEF_COLUMN_SUFFIX = " (filtered)";

    private SettingsModelColumnFilter2 m_filterColModel = MarkupTagFilterNodeDialog.getFilterColModel();

    private SettingsModelBoolean m_appendColumnsModel = MarkupTagFilterNodeDialog.getAppendColumnModel();

    private SettingsModelString m_columnSuffixModel = MarkupTagFilterNodeDialog.getColumnSuffixModel();

    private SettingsModelString m_tokenizerNameModel = MarkupTagFilterNodeDialog.getTokenizerNameModel();

    private boolean m_includesContainDocuments = false;

    /**
     * Creates new instance of {@code MarkupTagFilterNodeModel}
     */
    MarkupTagFilterNodeModel() {
        m_appendColumnsModel.addChangeListener(new AppendColumnChangeListener());
        m_filterColModel.addChangeListener(new FilteredColumnsChangeListener());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ColumnRearranger createColumnRearranger(final DataTableSpec dataSpec) throws InvalidSettingsException {
        //
        /// SPEC CHECKS
        //
        FilterResult filteredCols = m_filterColModel.applyTo(dataSpec);
        for (String includedCol : filteredCols.getIncludes()) {
            if(dataSpec.getColumnSpec(includedCol).getType().equals(DocumentCell.TYPE)) {
                m_includesContainDocuments = true;
            }
        }

        // check for at least one string column in input data table spec
        if (filteredCols.getIncludes().length == 0) {
            throw new InvalidSettingsException(
                "There are no columns containing string or document values in the input table!");
        }

        // check if all included columns are available in spec
        String[] unknownCols = filteredCols.getRemovedFromIncludes();
        if (unknownCols.length == 1) {
            setWarningMessage("Column \"" + unknownCols[0] + "\" is not available.");
        } else if (unknownCols.length > 1) {
            setWarningMessage("" + unknownCols.length + " selected columns are not available anymore.");
        }

        //
        /// CREATE COLUMN REARRANGER
        //
        // parameters
        boolean append = m_appendColumnsModel.getBooleanValue();
        String colSuffix = m_columnSuffixModel.getStringValue();

        // get array of indices of included columns
        int[] includedColIndices = getIncludedColIndices(dataSpec, filteredCols.getIncludes());

        ColumnRearranger cR = new ColumnRearranger(dataSpec);

        // create specs of new output columns
        DataColumnSpec[] newColsSpecs = getNewColSpecs(append, colSuffix, filteredCols.getIncludes(), dataSpec);

        // Pass all necessary parameters to the cell factory, which filters
        // the strings and creates new cells to replace.
        MarkupTagFilterCellFactory cellFac =
            new MarkupTagFilterCellFactory(includedColIndices, newColsSpecs, m_tokenizerNameModel.getStringValue());

        // replace or append columns
        if (append) {
            cR.append(cellFac);
        } else {
            cR.replace(cellFac, includedColIndices);
        }

        return cR;

    }

    private static final DataColumnSpec[] getNewColSpecs(final boolean append, final String colSuffix,
        final String[] colNamesToFilter, final DataTableSpec origInSpec) {
        DataColumnSpec[] appColumnSpecs = new DataColumnSpec[colNamesToFilter.length];
        int i = 0;

        // walk through column names to filter to create the new column specs
        for (String colName : colNamesToFilter) {
            String newColName = colName;
            DataType newColDataType = origInSpec.getColumnSpec(colName).getType();

            //if columns are appended, append suffix to column name
            if (append) {
                newColName += colSuffix;
                newColName = DataTableSpec.getUniqueColumnName(origInSpec, newColName);
            }

            // create a StringCell or DocumentCell spec
            DataColumnSpec newCol = new DataColumnSpecCreator(newColName, newColDataType).createSpec();

            // collect column specs
            appColumnSpecs[i] = newCol;
            i++;
        }
        return appColumnSpecs;
    }

    /**
     * Creates and returns an array containing the indices of the included columns in the input data table spec.
     *
     * @param dataSpec The input data table spec.
     * @param includedColNames sorted list of column names to include (sorted by position in table)
     * @return An array containing the indices of the included columns.
     */
    private int[] getIncludedColIndices(final DataTableSpec dataSpec, final String[] includedColNames) {
        int[] includedColIndices = new int[includedColNames.length];
        int noCols = dataSpec.getNumColumns();
        List<String> asList = Arrays.asList(includedColNames);
        int j = 0;
        for (int i = 0; i < noCols; i++) {
            String currColName = dataSpec.getColumnSpec(i).getName();
            if (asList.contains(currColName)) {
                includedColIndices[j] = i;
                j++;
            }
        }
        return includedColIndices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_filterColModel.saveSettingsTo(settings);
        m_appendColumnsModel.saveSettingsTo(settings);
        m_columnSuffixModel.saveSettingsTo(settings);
        m_tokenizerNameModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_filterColModel.validateSettings(settings);
        m_appendColumnsModel.validateSettings(settings);
        m_columnSuffixModel.validateSettings(settings);
        m_tokenizerNameModel.validateSettings(settings);

        // additional sanity checks
        StringBuffer errMsgBuffer = new StringBuffer();
        boolean err = false;

        // if filtered string values have to be appended, check for valid column suffix
        boolean append =
            ((SettingsModelBoolean)m_appendColumnsModel.createCloneWithValidatedValue(settings)).getBooleanValue();
        if (append) {
            String suffix =
                ((SettingsModelString)m_columnSuffixModel.createCloneWithValidatedValue(settings)).getStringValue();
            if (suffix.length() <= 0) {
                errMsgBuffer.append("Column suffix may not be empty if append " + "columns is set!\n");
                err = true;
            }
        }
        if (err) {
            throw new InvalidSettingsException(errMsgBuffer.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_filterColModel.loadSettingsFrom(settings);
        m_appendColumnsModel.loadSettingsFrom(settings);
        m_columnSuffixModel.loadSettingsFrom(settings);
        m_tokenizerNameModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to reset
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    private class AppendColumnChangeListener implements ChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_columnSuffixModel.setEnabled(m_appendColumnsModel.getBooleanValue());
        }
    }

    private class FilteredColumnsChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            m_tokenizerNameModel.setEnabled(m_includesContainDocuments);
        }
    }
}

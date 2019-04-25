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
 *   Apr 25, 2019 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The {@link NodeModel} for the Meta Info Inserter node.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MetaInfoInsertionNodeModel2 extends NodeModel {

    /** Configuration key for the document column. */
    private static final String CFG_KEY_DOCUMENT_COLUMN = "document_column";

    /** Configuration key for the column selection/filter. */
    private static final String CFG_KEY_COLUMN_FILTER = "metainf_column_filter";

    /** Configuration key for removing meta info columns that have been selected for insertion. */
    private static final String CFG_KEY_REMOVE_META_INF_COLS = "remove_meta_inf_cols";

    /** Default value for removing meta info columns that have been selected for insertion. . */
    private static final boolean DEF_REMOVE_META_INF_COLS = true;

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the column with the documents to add the
     * meta information to.
     *
     * @return {@code SettingsModelString} containing the name of the document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(CFG_KEY_DOCUMENT_COLUMN, "");
    }

    /**
     * Creates and returns a {@link SettingsModelColumnFilter2} with the names of the columns containing the meta
     * information.
     *
     * @return {@code SettingsModelColumnFilter2} with the names of the columns containing the meta information.
     */
    static final SettingsModelColumnFilter2 getColumnSelectionModel() {
        return new SettingsModelColumnFilter2(CFG_KEY_COLUMN_FILTER, new InputFilter<DataColumnSpec>() {
            @Override
            public boolean include(final DataColumnSpec cSpec) {
                final DataType type = cSpec.getType();
                return type.getCellClass().equals(StringCell.class);
            }
        }, NameFilterConfiguration.FILTER_BY_NAMEPATTERN);
    }

    /**
     * Creates and returns a {@link SettingsModelBoolean} which stores information whether to remove meta information
     * columns after insertion or not.
     *
     * @return {@link SettingsModelBoolean} which stores information whether to remove meta information columns after
     *         insertion or not.
     */
    static final SettingsModelBoolean getRemoveMetaInfoColsModel() {
        return new SettingsModelBoolean(CFG_KEY_REMOVE_META_INF_COLS, DEF_REMOVE_META_INF_COLS);
    }

    /**
     * A {@link SettingsModelString} storing the name of the document column.
     */
    private final SettingsModelString m_docColModel = getDocumentColumnModel();

    /**
     * A {@link SettingsModelColumnFilter2} storing the names of the columns containing meta information.
     */
    private final SettingsModelColumnFilter2 m_columnFilterModel = getColumnSelectionModel();

    /**
     * A {@link SettingsModelBoolean} storing the information whether to remove meta info columns after insertion or
     * not.
     */
    private final SettingsModelBoolean m_removeMetaInfoColsModel = getRemoveMetaInfoColsModel();

    /**
     * Creates a new instance of {@code MetaInfoInserterNodeModel}.
     */
    MetaInfoInsertionNodeModel2() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec inSpec = inSpecs[0];
        checkDataTableSpec(inSpec);
        final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        if (m_removeMetaInfoColsModel.getBooleanValue()) {
            rearranger.remove(m_columnFilterModel.applyTo(inSpec).getIncludes());
        }
        return new DataTableSpec[]{rearranger.createSpec()};
    }

    /**
     * Validates the incoming {@link DataTableSpec}.
     *
     * @param spec The {@code DataTableSpec}
     * @throws InvalidSettingsException Thrown if the {@code DataTableSpec} is invalid.
     */
    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyMinimumStringCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        final DataTableSpec inSpec = inData[0].getDataTableSpec();
        final int docColIdx = inSpec.findColumnIndex(m_docColModel.getStringValue());
        checkDataTableSpec(inSpec);
        final String[] metaInfCols = m_columnFilterModel.applyTo(inSpec).getIncludes();
        final ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        rearranger.replace(new MetaInfoCellFactory2(inSpec, docColIdx, metaInfCols, exec), docColIdx);
        if (m_removeMetaInfoColsModel.getBooleanValue()) {
            rearranger.remove(metaInfCols);
        }

        return new BufferedDataTable[]{
            exec.createColumnRearrangeTable(inData[0], rearranger, exec.createSubExecutionContext(1.0))};
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
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_columnFilterModel.saveSettingsTo(settings);
        m_removeMetaInfoColsModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_columnFilterModel.validateSettings(settings);
        m_removeMetaInfoColsModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_columnFilterModel.loadSettingsFrom(settings);
        m_removeMetaInfoColsModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

}

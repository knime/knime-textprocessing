/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
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
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @deprecated Use custom node model instead.
 */
@Deprecated
public class MetaInfoInsertionNodeModel extends NodeModel {

    /**
     * The default name of the key column.
     */
    public static final String DEF_KEYCOL = "Key";

    /**
     * The default name of the value column.
     */
    public static final String DEF_VALUECOL = "Value";

    /**
     * The default setting of keeping key and value columns.
     */
    public static final boolean DEF_KEEPKEYVALCOLS = true;

    private SettingsModelString m_docColModel = MetaInfoInsertionNodeDialog.createDocumentColumnModel();

    private SettingsModelString m_keyColModel = MetaInfoInsertionNodeDialog.createKeyColumnModel();

    private SettingsModelString m_valueColModel = MetaInfoInsertionNodeDialog.createValueColumnModel();

    private SettingsModelBoolean m_keepKeyValColModel = MetaInfoInsertionNodeDialog.createKeepKeyValColsModel();

    /**
     * Constructor of {@link MetaInfoInsertionNodeModel}.
     */
    public MetaInfoInsertionNodeModel() {
        super(1, 1);
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#configure(org.knime.core.data.DataTableSpec[])
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec spec = inSpecs[0];
        checkDataTableSpec(spec);

        return new DataTableSpec[]{createDataTableSpec(spec)};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyMinimumStringCells(2, true);

        // set and verify column selection and set warning message if present
        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
        ColumnSelectionVerifier.verifyColumn(m_keyColModel, spec, StringValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
        ColumnSelectionVerifier.verifyColumn(m_valueColModel, spec, StringValue.class, m_keyColModel.getStringValue())
            .ifPresent(a -> setWarningMessage(a));

    }

    private DataTableSpec createDataTableSpec(final DataTableSpec inSpec) {
        final int docColIndx = inSpec.findColumnIndex(m_docColModel.getStringValue());
        final int keyColIndx = inSpec.findColumnIndex(m_keyColModel.getStringValue());
        final int valueColIndx = inSpec.findColumnIndex(m_valueColModel.getStringValue());
        final boolean keepKeyValCols = m_keepKeyValColModel.getBooleanValue();

        ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        rearranger.replace(
            new MetaInfoCellFactory(inSpec.getColumnSpec(docColIndx), docColIndx, keyColIndx, valueColIndx, null),
            docColIndx);
        if (!keepKeyValCols) {
            rearranger.remove(keyColIndx, valueColIndx);
        }

        return rearranger.createSpec();
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#execute(org.knime.core.node.BufferedDataTable[],
     * org.knime.core.node.ExecutionContext)
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        final DataTableSpec inSpec = inData[0].getDataTableSpec();
        checkDataTableSpec(inSpec);
        final int docColIndx = inSpec.findColumnIndex(m_docColModel.getStringValue());
        final int keyColIndx = inSpec.findColumnIndex(m_keyColModel.getStringValue());
        final int valueColIndx = inSpec.findColumnIndex(m_valueColModel.getStringValue());
        final boolean keepKeyValCols = m_keepKeyValColModel.getBooleanValue();

        // compute frequency and add column
        final ColumnRearranger rearranger = new ColumnRearranger(inData[0].getDataTableSpec());
        rearranger.replace(
            new MetaInfoCellFactory(inSpec.getColumnSpec(docColIndx), docColIndx, keyColIndx, valueColIndx, exec),
            docColIndx);
        if (!keepKeyValCols) {
            rearranger.remove(keyColIndx, valueColIndx);
        }

        return new BufferedDataTable[]{
            exec.createColumnRearrangeTable(inData[0], rearranger, exec.createSubExecutionContext(1.0))};
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.NodeSettingsWO)
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_keyColModel.saveSettingsTo(settings);
        m_valueColModel.saveSettingsTo(settings);
        m_keepKeyValColModel.saveSettingsTo(settings);
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_keepKeyValColModel.validateSettings(settings);
        m_keyColModel.validateSettings(settings);
        m_valueColModel.validateSettings(settings);
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_keepKeyValColModel.loadSettingsFrom(settings);
        m_keyColModel.loadSettingsFrom(settings);
        m_valueColModel.loadSettingsFrom(settings);
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#reset()
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#loadInternals(java.io.File, org.knime.core.node.ExecutionMonitor)
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#saveInternals(java.io.File, org.knime.core.node.ExecutionMonitor)
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

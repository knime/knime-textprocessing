package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
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
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 */
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

        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyMinimumStringCells(2, true);

        return new DataTableSpec[]{createDataTableSpec(spec)};
    }

    private DataTableSpec createDataTableSpec(final DataTableSpec inSpec) {
        final int docColIndx = inSpec.findColumnIndex(m_docColModel.getStringValue());
        final int keyColIndx = inSpec.findColumnIndex(m_keyColModel.getStringValue());
        final int valueColIndx = inSpec.findColumnIndex(m_valueColModel.getStringValue());
        final boolean keepKeyValCols = m_keepKeyValColModel.getBooleanValue();

        ColumnRearranger rearranger = new ColumnRearranger(inSpec);
        rearranger.replace(new MetaInfoCellFactory(inSpec.getColumnSpec(docColIndx), docColIndx, keyColIndx,
                                                   valueColIndx), docColIndx);
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
        final int docColIndx = inSpec.findColumnIndex(m_docColModel.getStringValue());
        final int keyColIndx = inSpec.findColumnIndex(m_keyColModel.getStringValue());
        final int valueColIndx = inSpec.findColumnIndex(m_valueColModel.getStringValue());
        final boolean keepKeyValCols = m_keepKeyValColModel.getBooleanValue();

        // compute frequency and add column
        ColumnRearranger rearranger = new ColumnRearranger(inData[0].getDataTableSpec());
        rearranger.replace(new MetaInfoCellFactory(inSpec.getColumnSpec(docColIndx), docColIndx, keyColIndx,
                                                   valueColIndx), docColIndx);
        if (!keepKeyValCols) {
            rearranger.remove(keyColIndx, valueColIndx);
        }

        return new BufferedDataTable[]{exec.createColumnRearrangeTable(inData[0], rearranger,
                                                                       exec.createSubExecutionContext(1.0))};
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
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#saveInternals(java.io.File, org.knime.core.node.ExecutionMonitor)
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }
}

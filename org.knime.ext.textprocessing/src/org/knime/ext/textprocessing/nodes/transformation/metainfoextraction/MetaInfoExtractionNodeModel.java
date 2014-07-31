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
 * Created on 30.03.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoextraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public final class MetaInfoExtractionNodeModel extends NodeModel {

    /**
     * Default setting for appending documents.
     */
    public static final boolean DEF_APPENDDOCS = true;

    /**
     * Default setting for distinct document handling.
     */
    public static final boolean DEF_DISTINCTDOCS = true;

    /**
     * Default setting for meta info keys to extract.
     */
    public static final boolean DEF_ONLYMETAKEYS = false;


    private SettingsModelString m_docColModel = MetaInfoExtractionNodeDialog.createDocColModel();

    private SettingsModelBoolean m_appendDocsModel = MetaInfoExtractionNodeDialog.createAppendDocsModel();

    private SettingsModelBoolean m_distinctDocsModel = MetaInfoExtractionNodeDialog.createDistinctDocsModel();

    private SettingsModelBoolean m_metaKeysOnlyModel = MetaInfoExtractionNodeDialog.createKeysOnlyModel();

    private SettingsModelString m_metaKeysModel = MetaInfoExtractionNodeDialog.createKeysModel();


    /**
     * Constructor of {@link MetaInfoExtractionNodeModel}.
     */
    public MetaInfoExtractionNodeModel() {
        super(1, 1);
        m_metaKeysOnlyModel.addChangeListener(new MetaInfoDialogChangeListener());
        enableDialogs();
    }

    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec spec = inSpecs[0];

        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        return new DataTableSpec[]{createDataTableSpec()};
    }

    private DataTableSpec createDataTableSpec() {
       List<DataColumnSpec> colSpecs = new ArrayList<DataColumnSpec>();

       if (m_appendDocsModel.getBooleanValue()) {
           DataColumnSpecCreator docColCreator = new DataColumnSpecCreator(
               BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME, DocumentCell.TYPE);
           colSpecs.add(docColCreator.createSpec());
       }
       DataColumnSpecCreator keyColCreator = new DataColumnSpecCreator("Key", StringCell.TYPE);
       colSpecs.add(keyColCreator.createSpec());

       DataColumnSpecCreator valueColCreator = new DataColumnSpecCreator("Value", StringCell.TYPE);
       colSpecs.add(valueColCreator.createSpec());

       return new DataTableSpec(colSpecs.toArray(new DataColumnSpec[]{}));
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {

        boolean distinctDocs = m_distinctDocsModel.getBooleanValue();
        boolean keysOnly = m_metaKeysOnlyModel.getBooleanValue();
        String keys = m_metaKeysModel.getStringValue();
        Set<String> keySet = new HashSet<String>();
        for (String k : keys.split(",")) {
            if (k != null && !k.isEmpty()) {
                keySet.add(k);
            }
        }

        int docColIndx = inData[0].getDataTableSpec().findColumnIndex(m_docColModel.getStringValue());

        final BufferedDataContainer bdc = exec.createDataContainer(createDataTableSpec());
        final Set<UUID> processedDocs = new HashSet<UUID>();

        int rowCount = 0;
        for (final DataRow row : inData[0]) {
            if (!row.getCell(docColIndx).isMissing()) {
                final Document d = ((DocumentValue)row.getCell(docColIndx)).getDocument();

                if (!distinctDocs || !processedDocs.contains(d.getUUID())) {
                    if (distinctDocs) {
                        processedDocs.add(d.getUUID());
                    }

                    final DocumentMetaInfo metaInfo = d.getMetaInformation();
                    if (metaInfo != null) {
                        for (final String key : metaInfo.getMetaInfoKeys()) {
                            final String value = metaInfo.getMetaInfoValue(key);

                            if (key != null && value != null && (!keysOnly || keySet.contains(key))) {
                                final DataRow newRow;

                                if (m_appendDocsModel.getBooleanValue()) {
                                    newRow = new DefaultRow(RowKey.createRowKey(rowCount), row.getCell(docColIndx),
                                        new StringCell(key), new StringCell(value));
                                } else {
                                    newRow = new DefaultRow(RowKey.createRowKey(rowCount), new StringCell(key),
                                        new StringCell(value));
                                }

                                bdc.addRowToTable(newRow);
                                rowCount++;
                            }
                        }
                    }
                }
            }
        }

        bdc.close();
        return new BufferedDataTable[]{bdc.getTable()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_appendDocsModel.saveSettingsTo(settings);
        m_distinctDocsModel.saveSettingsTo(settings);
        m_metaKeysModel.saveSettingsTo(settings);
        m_metaKeysOnlyModel.saveSettingsTo(settings);
        m_docColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_appendDocsModel.validateSettings(settings);
        m_distinctDocsModel.validateSettings(settings);
        m_metaKeysOnlyModel.validateSettings(settings);
        m_metaKeysModel.validateSettings(settings);
        m_docColModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_appendDocsModel.loadSettingsFrom(settings);
        m_distinctDocsModel.loadSettingsFrom(settings);
        m_metaKeysModel.loadSettingsFrom(settings);
        m_metaKeysOnlyModel.loadSettingsFrom(settings);
        m_docColModel.validateSettings(settings);
    }

    private void enableDialogs() {
        m_metaKeysModel.setEnabled(m_metaKeysOnlyModel.getBooleanValue());
    }

    /**
     * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
     */
    class MetaInfoDialogChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            enableDialogs();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // Nothing to do ...
    }
}

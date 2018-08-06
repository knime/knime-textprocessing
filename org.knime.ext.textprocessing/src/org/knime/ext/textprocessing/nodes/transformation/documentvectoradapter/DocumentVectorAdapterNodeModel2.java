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
 *   15.09.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.filestore.FileStoreFactory;
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
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.DocumentVectorPortObject;
import org.knime.ext.textprocessing.data.DocumentVectorPortObjectSpec;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model of the document vector adapter node. This node creates a document feature vector for each document exactly
 * as the normal document vector node. It has two inputs, the first one is the input table, whose features are to be
 * filtered based on the reference column names stored in the model of the second input. This node returns a document
 * feature vector with the features stored in the input model or the features selected in the node dialog.
 *
 * @author Andisa Dewi & Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.5
 */
class DocumentVectorAdapterNodeModel2 extends NodeModel {

    /**
     * The default setting of the creation of bit vectors. By default bit vectors are created ({@code true}).
     */
    static final boolean DEFAULT_BOOLEAN = true;

    /**
     * The default column to use.
     */
    static final String DEFAULT_COL = "";

    /**
     * The default value to the as collection flag.
     */
    static final boolean DEFAULT_ASCOLLECTION = false;

    private final TextContainerDataCellFactory m_documentCellFac;

    private int m_documentColIndex = -1;

    private int m_termColIndex = -1;

    private final SettingsModelString m_colModel = DocumentVectorAdapterNodeDialog2.getColumnModel();

    private final SettingsModelBoolean m_booleanModel = DocumentVectorAdapterNodeDialog2.getBooleanModel();

    private final SettingsModelString m_documentColModel = DocumentVectorAdapterNodeDialog2.getDocumentColModel();

    private final SettingsModelBoolean m_asCollectionModel = DocumentVectorAdapterNodeDialog2.getAsCollectionModel();

    private final SettingsModelFilterString m_vectorColsModel =
        DocumentVectorAdapterNodeDialog2.getVectorColumnsModel();

    private final SettingsModelBoolean m_useSettingsFromModelPortModel =
        DocumentVectorAdapterNodeDialog2.getUseModelSettings();

    private String[] m_previousFeatureColumns = null;

    private boolean m_hasNumberCol = true;

    private static final DoubleCell DEFAULT_CELL = new DoubleCell(0.0);

    /**
     * Creates a new instance of {@code DocumentVectorAdapterNodeModel2}.
     */
    DocumentVectorAdapterNodeModel2() {
        super(new PortType[]{PortTypeRegistry.getInstance().getPortType(DocumentVectorPortObject.class, false),
            BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        m_booleanModel.addChangeListener(e -> checkUncheck());
        m_useSettingsFromModelPortModel.addChangeListener(e -> checkUncheck());
        checkUncheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec dataTableSpec = (DataTableSpec)inSpecs[1];

        checkDataTableSpec(dataTableSpec);
        DocumentVectorPortObjectSpec modelSpec = checkModelInput(dataTableSpec, inSpecs[0]);

        // create spec if collection flag is checked
        DataTableSpec spec = null;
        if ((m_asCollectionModel.isEnabled() && m_asCollectionModel.getBooleanValue())
            || (m_useSettingsFromModelPortModel.getBooleanValue() && modelSpec.getCollectionCellSetting())) {
            spec = createDataTableSpecAsCollection(null);
        }

        return new DataTableSpec[]{spec};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_hasNumberCol = spec.containsCompatibleType(DoubleValue.class);
        checkUncheck();

        // set and verify column selections and set warning if present
        ColumnSelectionVerifier.verifyColumn(m_documentColModel, spec, DocumentValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
        if (!m_useSettingsFromModelPortModel.getBooleanValue() && !m_booleanModel.getBooleanValue()) {
            ColumnSelectionVerifier.verifyColumn(m_colModel, spec, DoubleValue.class, null)
                .ifPresent(a -> setWarningMessage(a));
        }

        m_termColIndex = verifier.getTermCellIndex();
        m_documentColIndex = spec.findColumnIndex(m_documentColModel.getStringValue());
    }

    private final DocumentVectorPortObjectSpec checkModelInput(final DataTableSpec spec,
        final PortObjectSpec portObjectSpec) throws InvalidSettingsException {

        // check if valid model is connected
        DocumentVectorPortObjectSpec modelSpec = null;
        if (portObjectSpec instanceof DocumentVectorPortObjectSpec) {
            modelSpec = (DocumentVectorPortObjectSpec)portObjectSpec;
        } else {
            throw new InvalidSettingsException("No model or model of wrong type is connected to model port!");
        }

        // check if vector value column name is in the incoming datatable
        if (m_useSettingsFromModelPortModel.getBooleanValue() && !modelSpec.getBitVectorSetting()) {
            int vectorValueColumnIndex = spec.findColumnIndex(modelSpec.getVectorValueColumnName());
            if (vectorValueColumnIndex < 0) {
                throw new InvalidSettingsException("DoubleValue column '" + modelSpec.getVectorValueColumnName()
                    + "' from input model could not be found in the input data table.");
            } else if (!spec.getColumnSpec(vectorValueColumnIndex).getType().isCompatible(DoubleValue.class)) {
                throw new InvalidSettingsException(
                    "Column '" + modelSpec.getVectorValueColumnName() + "' is not a DoubleValue column.");
            }
        }

        // check if column space from input model has changed
        // throw exception in case
        if (m_previousFeatureColumns == null || m_useSettingsFromModelPortModel.getBooleanValue()) {
            m_previousFeatureColumns = modelSpec.getFeatureSpaceColumns();
        } else if (!Arrays.equals(modelSpec.getFeatureSpaceColumns(), m_previousFeatureColumns)
            && !m_useSettingsFromModelPortModel.getBooleanValue()) {
            m_previousFeatureColumns = modelSpec.getFeatureSpaceColumns();
            throw new InvalidSettingsException("Input model has changed! Please configure node!");
        }

        return modelSpec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        DataTableSpec dataTableSpec = ((BufferedDataTable)inData[1]).getDataTableSpec();
        checkDataTableSpec(dataTableSpec);
        DocumentVectorPortObjectSpec modelSpec = checkModelInput(dataTableSpec, inData[0].getSpec());

        boolean useBitvector = m_booleanModel.getBooleanValue();
        boolean ignoreTags = modelSpec.getIgnoreTagsSetting();
        String vectorValueColumn = m_colModel.getStringValue();
        boolean asCollectionCell = m_asCollectionModel.getBooleanValue();
        List<String> includedTerms = m_vectorColsModel.getIncludeList();

        // set model values if 'use settings from model port' is checked, otherwise keep values from dialog.
        if (m_useSettingsFromModelPortModel.getBooleanValue()) {
            useBitvector = modelSpec.getBitVectorSetting();
            vectorValueColumn = modelSpec.getVectorValueColumnName();
            asCollectionCell = modelSpec.getCollectionCellSetting();
            includedTerms = Arrays.asList(modelSpec.getFeatureSpaceColumns());
        }

        // document column index.
        m_documentColIndex = dataTableSpec.findColumnIndex(m_documentColModel.getStringValue());
        m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
        int colIndex = -1;

        // Check if no valid column selected, the use of boolean values is
        // specified !
        if (!useBitvector) {
            final String colName = vectorValueColumn;
            colIndex = dataTableSpec.findColumnIndex(colName);
        }

        // Get all terms from reference table
        exec.setProgress("Collecting all terms from the reference table");
        if (includedTerms.isEmpty()) {
            setWarningMessage("No feature columns selected: No document vector will be created.");
        }

        // Sort the data table first by documents
        exec.setProgress("Sorting input table");
        final List<String> colList = new ArrayList<>();
        colList.add(m_documentColModel.getStringValue());
        boolean[] sortAsc = {true};
        BufferedDataTable sortedTable =
            new SortedTable((BufferedDataTable)inData[1], colList, sortAsc, exec).getBufferedDataTable();

        // Create data table spec
        exec.setProgress("Filling feature vectors");
        BufferedDataContainer dc;
        if (asCollectionCell) {
            dc = exec.createDataContainer(createDataTableSpecAsCollection(includedTerms));
        } else {
            dc = exec.createDataContainer(createDataTableSpecAsColumns(includedTerms));
        }

        // first go through data table to collect the features, create double cells and rows
        RowIterator it = sortedTable.iterator();
        long rowid = 0;
        Document currDoc = null;
        Document lastDoc = null;
        Map<String, DoubleCell> featureVector = initializeFeatureVector(includedTerms);

        while (it.hasNext()) {
            exec.checkCanceled();
            final DataRow row = it.next();
            final DataCell termCell = row.getCell(m_termColIndex);
            final DataCell docCell = row.getCell(m_documentColIndex);
            // if the term or document is missing, then skip the row
            if (termCell.isMissing() || docCell.isMissing()) {
                setWarningMessage(row.getKey() + " has missing term/document. This row will be ignored...");
                // add last feature vector to table if last row has missing term/document
            } else {
                // get current document, current term and its frequency (or bitvector)
                currDoc = ((DocumentValue)docCell).getDocument();
                final Term currTerm = ((TermValue)termCell).getTermValue();
                double currValue = getDoubleValue(colIndex, row);

                // if current doc is not equals last doc, create new feature vector
                // for last doc
                if (lastDoc != null && !currDoc.equals(lastDoc)) {
                    // add old feature vector to table
                    createRowAndAddToDc(asCollectionCell, dc, rowid++, lastDoc, featureVector);
                    // reset feature vector
                    featureVector = initializeFeatureVector(includedTerms);
                }

                // if tags have to be ignored
                String key = "";
                if (ignoreTags) {
                    key = currTerm.getText();
                } else {
                    key = currTerm.toString();
                }
                if (includedTerms.contains(key)) {
                    featureVector.replace(key, new DoubleCell(currValue));
                }
                lastDoc = currDoc;
            }
        }

        // add last row
        if (lastDoc != null) {
            createRowAndAddToDc(asCollectionCell, dc, rowid, lastDoc, featureVector);
        }

        dc.close();
        featureVector.clear();

        return new BufferedDataTable[]{dc.getTable()};
    }

    private double getDoubleValue(final int colIndex, final DataRow row) {
        double currValue = 1;
        if (colIndex > -1) {
            DataCell cell = row.getCell(colIndex);
            // if the value is missing, set it to 0
            if (cell.isMissing()) {
                currValue = 0;
                setWarningMessage(row.getKey() + " has a missing TF value. The value will be set to 0...");
            } else {
                currValue = ((DoubleValue)cell).getDoubleValue();
            }
        }
        return currValue;
    }

    private void createRowAndAddToDc(final boolean asCollectionCell, final BufferedDataContainer dc, final long rowid,
        final Document lastDoc, final Map<String, DoubleCell> featureVector) {
        DataRow newRow;
        if (asCollectionCell) {
            newRow = createDataRowAsCollection(lastDoc, new ArrayList<DoubleCell>(featureVector.values()), rowid);
        } else {
            newRow = createDataRowAsColumns(lastDoc, new ArrayList<DoubleCell>(featureVector.values()), rowid);
        }
        dc.addRowToTable(newRow);
    }

    private static Map<String, DoubleCell> initializeFeatureVector(final List<String> refTerms) {
        Map<String, DoubleCell> featureVector = new LinkedHashMap<>();
        for (String term : refTerms) {
            featureVector.put(term, DEFAULT_CELL);
        }
        return featureVector;
    }

    private DataRow createDataRowAsCollection(final Document doc, final List<DoubleCell> featureVector,
        final long rowid) {
        final RowKey rowKey = RowKey.createRowKey(rowid);
        final DataCell docCell = m_documentCellFac.createDataCell(doc);
        final DataCell vectorCell = CollectionCellFactory.createSparseListCell(featureVector, DEFAULT_CELL);
        return new DefaultRow(rowKey, docCell, vectorCell);
    }

    private DataRow createDataRowAsColumns(final Document doc, final List<DoubleCell> featureVector, final long rowid) {
        final RowKey rowKey = RowKey.createRowKey(rowid);
        final List<DataCell> cells = new ArrayList<>(featureVector.size()+1);
        cells.add(m_documentCellFac.createDataCell(doc));
        cells.addAll(1, featureVector);
        return new DefaultRow(rowKey, cells);
    }

    private DataTableSpec createDataTableSpecAsCollection(final List<String> features) {
        DataColumnSpec[] columnSpecs = new DataColumnSpec[2];

        // add document column
        final String documentColumnName = DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME;
        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(documentColumnName, m_documentCellFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();

        // add feature vector columns
        columnSpecCreator = new DataColumnSpecCreator(DocumentDataTableBuilder.DEF_DOCUMENT_VECTOR_COLNAME,
            ListCell.getCollectionType(DoubleCell.TYPE));
        if (features != null) {
            columnSpecCreator.setElementNames(features.toArray(new String[features.size()]));
        }
        columnSpecs[1] = columnSpecCreator.createSpec();

        return new DataTableSpec(columnSpecs);
    }

    private DataTableSpec createDataTableSpecAsColumns(final List<String> features) {
        int featureCount = features.size();
        final DataColumnSpec[] columnSpecs = new DataColumnSpec[featureCount + 1];

        // add document column
        UniqueNameGenerator uniqueNameGen = new UniqueNameGenerator(new LinkedHashSet<String>(features));
        String documentColumnName = uniqueNameGen.newName(DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME);

        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(documentColumnName, m_documentCellFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();
        // add feature vector columns
        int colIndex = 1;
        for (final String t : features) {
            columnSpecCreator = new DataColumnSpecCreator(t, DoubleCell.TYPE);
            columnSpecs[colIndex++] = columnSpecCreator.createSpec();
        }

        return new DataTableSpec(columnSpecs);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_booleanModel.loadSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
        m_asCollectionModel.loadSettingsFrom(settings);
        m_vectorColsModel.loadSettingsFrom(settings);
        m_useSettingsFromModelPortModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_booleanModel.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
        m_asCollectionModel.saveSettingsTo(settings);
        m_vectorColsModel.saveSettingsTo(settings);
        m_useSettingsFromModelPortModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_booleanModel.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
        m_asCollectionModel.validateSettings(settings);
        m_vectorColsModel.validateSettings(settings);
        m_useSettingsFromModelPortModel.validateSettings(settings);
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

    private void checkUncheck() {
        m_booleanModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue() && m_hasNumberCol);
        m_asCollectionModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue());
        m_vectorColsModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue());

        // set bitVector setting to true if no number column present
        if (!m_hasNumberCol) {
            m_booleanModel.setBooleanValue(true);
        }
        m_colModel.setEnabled(
            !m_useSettingsFromModelPortModel.getBooleanValue() && m_hasNumberCol && !m_booleanModel.getBooleanValue());
    }

}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   15.09.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvectoradapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model of the document vector adapter node. This node creates a document feature vector for each document exactly
 * as the normal document vector node. It has two inputs, the first one is the input table, whose features are to be
 * filtered based on the reference column names stored in the model of the second input.
 * This node returns a document feature vector with the features stored in the input model or the features selected
 * in the node dialog.
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

    /**
     * Creates a new instance of {@code DocumentVectorAdapterNodeModel2}.
     */
    DocumentVectorAdapterNodeModel2() {
        super(
            new PortType[]{BufferedDataTable.TYPE,
                PortTypeRegistry.getInstance().getPortType(DocumentVectorPortObject.class, false)},
            new PortType[]{BufferedDataTable.TYPE});
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
        DataTableSpec dataTableSpec = (DataTableSpec)inSpecs[0];
        checkDataTableSpec(dataTableSpec);

        // check if valid model is connected
        DocumentVectorPortObjectSpec modelSpec = null;
        if (inSpecs[1] instanceof DocumentVectorPortObjectSpec) {
            modelSpec = (DocumentVectorPortObjectSpec)inSpecs[1];
        } else {
            throw new InvalidSettingsException("No model or model of wrong type is connected to model port!");
        }

        // create spec if collection flag is checked
        DataTableSpec spec = null;
        if ((m_asCollectionModel.isEnabled() && m_asCollectionModel.getBooleanValue())
            || (m_useSettingsFromModelPortModel.getBooleanValue() && modelSpec.getCollectionCellSetting())) {
            spec = createDataTableSpecAsCollection(null);
        }

        // check if vector value model is in the incoming datatable
        if (m_useSettingsFromModelPortModel.getBooleanValue()) {
            int vectorValueColumnIndex = dataTableSpec.findColumnIndex(modelSpec.getVectorValueColumnName());
            if (vectorValueColumnIndex < 0) {
                throw new InvalidSettingsException("Vector value column \"" + modelSpec.getVectorValueColumnName()
                    + "\" from input model could not be found in the input data table.");
            }
        } else if ((!m_useSettingsFromModelPortModel.getBooleanValue() || !m_booleanModel.getBooleanValue())) {
            int vectorValueColumnIndex = dataTableSpec.findColumnIndex(m_colModel.getStringValue());
            if (vectorValueColumnIndex < 0) {
                throw new InvalidSettingsException("Vector value column \"" + m_colModel.getStringValue()
                    + "\" could not be found in the input data table.");
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

        return new DataTableSpec[]{spec};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyMinimumNumberCells(1, true);
        verifier.verifyTermCell(true);
        int numberOfDocumentCols = verifier.getNumDocumentCells();
        m_termColIndex = verifier.getTermCellIndex();

        String docColumn = m_documentColModel.getStringValue();

        if (docColumn.isEmpty()) {
            docColumn = null;
            // only one document col available
            if (numberOfDocumentCols == 1) {
                docColumn = spec.getColumnSpec(verifier.getDocumentCellIndex()).getName();
                // multiple document columns available
            } else if (numberOfDocumentCols > 1) {
                // take first document column
                for (String colName : spec.getColumnNames()) {
                    if (spec.getColumnSpec(colName).getType().isCompatible(DocumentValue.class)) {
                        docColumn = colName;
                        break;
                    }
                }
                setWarningMessage("Auto guessing: Using column '" + docColumn + "' as document column");
            }
            m_documentColModel.setStringValue(docColumn);
        }

        m_documentColIndex = spec.findColumnIndex(docColumn);
        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Selected document column \"" + docColumn + "\" could not be found in the input data table.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {
        DataTableSpec dataTableSpec = ((BufferedDataTable)inData[0]).getDataTableSpec();
        DocumentVectorPortObjectSpec modelSpec = ((DocumentVectorPortObject)inData[1]).getSpec();

        checkDataTableSpec(dataTableSpec);

        boolean useBitvector = m_booleanModel.getBooleanValue();
        boolean ignoreTags = modelSpec.getIgnoreTagsSetting();
        String vectorValueColumn = m_colModel.getStringValue();
        boolean asCollectionCell = m_asCollectionModel.getBooleanValue();
        List<String> includedCols = m_vectorColsModel.getIncludeList();

        // set model values if 'use settings from model port' is checked, otherwise keep values from dialog.
        if (m_useSettingsFromModelPortModel.getBooleanValue()) {
            useBitvector = modelSpec.getBitVectorSetting();
            vectorValueColumn = modelSpec.getVectorValueColumnName();
            asCollectionCell = modelSpec.getCollectionCellSetting();
            includedCols = Arrays.asList(modelSpec.getFeatureSpaceColumns());
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
        if (includedCols.isEmpty()) {
            setWarningMessage("No feature columns selected: No document vector will be created.");
        }
        List<String> refTerms = includedCols;

        // Sort the data table first by documents
        exec.setProgress("Sorting input table");
        final List<String> colList = new ArrayList<String>();
        colList.add(m_documentColModel.getStringValue());
        boolean[] sortAsc = {true};
        BufferedDataTable sortedTable =
            new SortedTable((BufferedDataTable)inData[0], colList, sortAsc, exec).getBufferedDataTable();

        // hash table holding an index for each feature
        final Map<String, Integer> featureIndexTable = new HashMap<String, Integer>();

        // first go through data table to collect the features
        exec.setProgress("Collecting features");
        int currIndex = 0;
        RowIterator it = sortedTable.iterator();
        while (it.hasNext()) {
            exec.checkCanceled();
            final DataRow row = it.next();
            final DataCell termCell = row.getCell(m_termColIndex);
            final DataCell docCell = row.getCell(m_documentColIndex);
            // if the term or document is missing, then skip the row
            if (termCell.isMissing() || docCell.isMissing()) {
                continue;
            }
            final Term t = ((TermValue)termCell).getTermValue();

            String key = null;
            // if tags have o be ignored
            if (ignoreTags) {
                key = t.getText();
            } else {
                key = t.toString();
            }
            if (refTerms.contains(key) && !featureIndexTable.containsKey(key)) {
                    featureIndexTable.put(key, currIndex);
                    currIndex++;
            }
        }

        // Add terms that are in the reference table, but not in the main table
        for (String term : refTerms) {
            if (!featureIndexTable.containsKey(term)) {
                featureIndexTable.put(term, currIndex);
                currIndex++;
            }
        }

        // second go through data table to create feature vectors
        exec.setProgress("Creating feature vectors");
        BufferedDataContainer dc;
        if (asCollectionCell) {
            dc = exec.createDataContainer(createDataTableSpecAsCollection(featureIndexTable));
        } else {
            dc = exec.createDataContainer(createDataTableSpecAsColumns(featureIndexTable));
        }

        Document lastDoc = null;
        List<DoubleCell> featureVector = initFeatureVector(featureIndexTable.size());

        long numberOfRows = sortedTable.size();
        long rowid = 0;
        int currRow = 1;
        it = sortedTable.iterator();
        for (DataRow row : sortedTable) {
            exec.checkCanceled();
            final DataCell termCell = row.getCell(m_termColIndex);
            final DataCell docCell = row.getCell(m_documentColIndex);
            // if the term or document is missing, then skip the row
            if (termCell.isMissing() || docCell.isMissing()) {
                setWarningMessage(row.getKey() + " has missing term/document. This row will be ignored...");
                // add last feature vector to table if last row has missing term/document
                if (currRow == numberOfRows) {
                    DataRow newRow;
                    if (asCollectionCell) {
                        newRow = createDataRowAsCollection(lastDoc, featureVector, rowid++);
                    } else {
                        newRow = createDataRowAsColumns(lastDoc, featureVector, rowid++);
                    }
                    dc.addRowToTable(newRow);
                }
                numberOfRows -= 1;
                continue;
            }
            final Document currDoc = ((DocumentValue)docCell).getDocument();
            final Term currTerm = ((TermValue)termCell).getTermValue();
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

            // if current doc is not equals last doc, create new feature vector
            // for last doc
            if (lastDoc != null && !currDoc.equals(lastDoc)) {
                // add old feature vector to table

                DataRow newRow;
                if (asCollectionCell) {
                    newRow = createDataRowAsCollection(lastDoc, featureVector, rowid++);
                } else {
                    newRow = createDataRowAsColumns(lastDoc, featureVector, rowid++);
                }
                dc.addRowToTable(newRow);
                // create new feature vector
                featureVector = initFeatureVector(featureIndexTable.size());
            }

            // add new term at certain index to feature vector
            String key = "";
            // if tags have to be ignored
            if (ignoreTags) {
                key = currTerm.getText();
            } else {
                key = currTerm.toString();
            }
            Integer index = featureIndexTable.get(key);
            if (index != null) {
                featureVector.set(index, new DoubleCell(currValue));
            }

            // if last row, add feature vector to table
            if (currRow == numberOfRows) {
                DataRow newRow;
                if (asCollectionCell) {
                    newRow = createDataRowAsCollection(currDoc, featureVector, rowid++);
                } else {
                    newRow = createDataRowAsColumns(currDoc, featureVector, rowid++);
                }
                dc.addRowToTable(newRow);
            }
            lastDoc = currDoc;
            currRow++;
        }

        dc.close();
        featureIndexTable.clear();

        return new BufferedDataTable[]{dc.getTable()};
    }

    private static final DoubleCell DEFAULT_CELL = new DoubleCell(0.0);

    private DataRow createDataRowAsCollection(final Document doc, final List<DoubleCell> featureVector,
        final long rowid) {
        final RowKey rowKey = RowKey.createRowKey(rowid);
        final DataCell docCell = m_documentCellFac.createDataCell(doc);
        final DataCell vectorCell = CollectionCellFactory.createSparseListCell(featureVector, DEFAULT_CELL);

        return new DefaultRow(rowKey, new DataCell[]{docCell, vectorCell});
    }

    private DataRow createDataRowAsColumns(final Document doc, final List<DoubleCell> featureVector, final long rowid) {
        final RowKey rowKey = RowKey.createRowKey(rowid);
        final DataCell[] cells = new DataCell[featureVector.size() + 1];
        cells[0] = m_documentCellFac.createDataCell(doc);
        for (int i = 0; i < cells.length - 1; i++) {
            cells[i + 1] = featureVector.get(i);
        }

        return new DefaultRow(rowKey, cells);
    }

    private DataTableSpec createDataTableSpecAsCollection(final Map<String, Integer> featureIndexTable) {
        DataColumnSpec[] columnSpecs = new DataColumnSpec[2];

        // add document column
        final String documentColumnName = DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME;
        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(documentColumnName, m_documentCellFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();

        // add feature vector columns
        columnSpecCreator = new DataColumnSpecCreator(DocumentDataTableBuilder.DEF_DOCUMENT_VECTOR_COLNAME,
            ListCell.getCollectionType(DoubleCell.TYPE));
        if (featureIndexTable != null) {
            String[] featureNames = new String[featureIndexTable.size()];

            for (Entry<String, Integer> entry : featureIndexTable.entrySet()) {
                featureNames[entry.getValue()] = entry.getKey();
            }
            columnSpecCreator.setElementNames(featureNames);
        }
        columnSpecs[1] = columnSpecCreator.createSpec();

        return new DataTableSpec(columnSpecs);
    }

    private DataTableSpec createDataTableSpecAsColumns(final Map<String, Integer> featureIndexTable) {
        int featureCount = featureIndexTable.size();
        final DataColumnSpec[] columnSpecs = new DataColumnSpec[featureCount + 1];

        // add document column
        UniqueNameGenerator uniqueNameGen = new UniqueNameGenerator(featureIndexTable.keySet());
        String documentColumnName = uniqueNameGen.newName(DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME);

        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(documentColumnName, m_documentCellFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();

        // add feature vector columns
        final Set<String> terms = featureIndexTable.keySet();
        for (final String t : terms) {
            int index = featureIndexTable.get(t) + 1;
            columnSpecCreator = new DataColumnSpecCreator(t, DoubleCell.TYPE);
            columnSpecs[index] = columnSpecCreator.createSpec();
        }

        return new DataTableSpec(columnSpecs);
    }

    private List<DoubleCell> initFeatureVector(final int size) {
        final List<DoubleCell> featureVector = new ArrayList<DoubleCell>(size);
        for (int i = 0; i < size; i++) {
            featureVector.add(i, DEFAULT_CELL);
        }
        return featureVector;
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
        m_booleanModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue());
        m_asCollectionModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue());
        m_vectorColsModel.setEnabled(!m_useSettingsFromModelPortModel.getBooleanValue());

        if (m_useSettingsFromModelPortModel.getBooleanValue()
            || (m_booleanModel.isEnabled() && m_booleanModel.getBooleanValue())) {
            m_colModel.setEnabled(false);
        } else {
            m_colModel.setEnabled(true);
        }
    }

}

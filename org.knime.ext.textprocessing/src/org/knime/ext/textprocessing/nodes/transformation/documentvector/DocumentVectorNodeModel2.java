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
 *   06.05.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentvector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.UniqueNameGenerator;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.CommonColumnNames;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model of the document vector node, creates a document feature vector for each document. As features all term of
 * the given bag of words are used. As vector values, a column can be specified or bit vectors can be created.
 *
 * @author Kilian Thiel, University of Konstanz
 */
class DocumentVectorNodeModel2 extends NodeModel {

    /**
     * The default setting of the creation of bit vectors. By default bit vectors are created (<code>true</code>).
     */
    static final boolean DEFAULT_BOOLEAN = true;

    /**
     * The default column to use.
     */
    static final String DEFAULT_COL = "";

    /**
     * The default document column to use.
     */
    static final String DEFAULT_DOCUMENT_COLNAME = CommonColumnNames.DEF_ORIG_DOCUMENT_COLNAME;

    /**
     * The default value to ignore tags.
     */
    static final boolean DEFAULT_IGNORE_TAGS = true;

    /**
     * The default value to the as collection flag.
     */
    static final boolean DEFAULT_ASCOLLECTION = true;

    private final TextContainerDataCellFactory m_documentCellFac;

    private int m_documentColIndex = -1;

    private int m_termColIndex = -1;

    private SettingsModelString m_colModel = DocumentVectorNodeDialog2.getColumnModel();

    private SettingsModelBoolean m_booleanModel = DocumentVectorNodeDialog2.getBooleanModel();

    private SettingsModelString m_documentColModel = DocumentVectorNodeDialog2.getDocumentColModel();

    private SettingsModelBoolean m_ignoreTags = DocumentVectorNodeDialog2.getIgnoreTagsModel();

    private SettingsModelBoolean m_asCollectionModel = DocumentVectorNodeDialog2.getAsCollectionModel();

    /**
     * Creates a new instance of <code>DocumentVectorNodeModel</code>.
     */
    DocumentVectorNodeModel2() {
        super(1, 1);
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        m_booleanModel.addChangeListener(new InternalChangeListener());
        checkUncheck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);

        DataTableSpec spec = null;
        if (m_asCollectionModel.getBooleanValue()) {
            spec = createDataTableSpecAsCollection(null);
        }
        return new DataTableSpec[]{spec};
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();

        m_documentColIndex = spec.findColumnIndex(m_documentColModel.getStringValue());
        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified document column is not valid! " + "Check your settings!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        final boolean ignoreTags = m_ignoreTags.getBooleanValue();

        // document column index.
        m_documentColIndex = inData[0].getSpec().findColumnIndex(m_documentColModel.getStringValue());

        m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));

        int colIndex = -1;
        // Check if no valid column selected, the use of boolean values is
        // specified !
        if (!m_booleanModel.getBooleanValue()) {
            final String colName = m_colModel.getStringValue();
            colIndex = inData[0].getDataTableSpec().findColumnIndex(colName);
            if (colIndex < 0) {
                throw new InvalidSettingsException("No valid column selected!");
            }
        }

        // Sort the data table first by documents
        exec.setProgress("Sorting input table");
        final List<String> colList = new ArrayList<String>();
        colList.add(m_documentColModel.getStringValue());
        boolean[] sortAsc = new boolean[]{true};
        BufferedDataTable sortedTable = new SortedTable(inData[0], colList, sortAsc, exec).getBufferedDataTable();

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
            if (key != null && !featureIndexTable.containsKey(key)) {
                featureIndexTable.put(key, currIndex);
                currIndex++;
            }
        }

        // second go through data table to create feature vectors
        exec.setProgress("Create feature vectors");
        BufferedDataContainer dc;
        if (m_asCollectionModel.getBooleanValue()) {
            dc = exec.createDataContainer(createDataTableSpecAsCollection(featureIndexTable));
        } else {
            dc = exec.createDataContainer(createDataTableSpecAsColumns(featureIndexTable));
        }

        Document lastDoc = null;
        List<DoubleCell> featureVector = initFeatureVector(featureIndexTable.size());

        long numberOfRows = sortedTable.size();
        int currRow = 1;
        it = sortedTable.iterator();
        while (it.hasNext()) {
            exec.checkCanceled();
            final DataRow row = it.next();
            final DataCell termCell = row.getCell(m_termColIndex);
            final DataCell docCell = row.getCell(m_documentColIndex);
            // if the term or document is missing, then skip the row
            if (termCell.isMissing() || docCell.isMissing()) {
                setWarningMessage(row.getKey() + " has missing term/document. This row will be ignored...");
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
                    setWarningMessage(
                        row.getKey().getString() + " has a missing TF value. The value will be set to 0...");
                } else {
                    currValue = ((DoubleValue)cell).getDoubleValue();
                }
            }

            // if current doc is not equals last doc, create new feature vector
            // for last doc
            if (lastDoc != null && !currDoc.equals(lastDoc)) {
                // add old feature vector to table

                DataRow newRow;
                if (m_asCollectionModel.getBooleanValue()) {
                    newRow = createDataRowAsCollection(lastDoc, featureVector);
                } else {
                    newRow = createDataRowAsColumns(lastDoc, featureVector);
                }
                dc.addRowToTable(newRow);
                // create new feature vector
                featureVector = initFeatureVector(featureIndexTable.size());
            }

            // add new term at certain index to feature vector
            String key = "";
            // if tags have o be ignored
            if (ignoreTags) {
                key = currTerm.getText();
            } else {
                key = currTerm.toString();
            }
            final int index = featureIndexTable.get(key);
            featureVector.set(index, new DoubleCell(currValue));

            // if last row, add feature vector to table
            if (currRow == numberOfRows) {
                DataRow newRow;
                if (m_asCollectionModel.getBooleanValue()) {
                    newRow = createDataRowAsCollection(currDoc, featureVector);
                } else {
                    newRow = createDataRowAsColumns(currDoc, featureVector);
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

    private long m_rowKeyNr = 0;

    private static final DoubleCell DEFAULT_CELL = new DoubleCell(0.0);

    private DataRow createDataRowAsCollection(final Document doc, final List<DoubleCell> featureVector) {
        final RowKey rowKey = RowKey.createRowKey(m_rowKeyNr);
        m_rowKeyNr++;
        final DataCell docCell = m_documentCellFac.createDataCell(doc);
        final DataCell vectorCell = CollectionCellFactory.createSparseListCell(featureVector, DEFAULT_CELL);

        return new DefaultRow(rowKey, new DataCell[]{docCell, vectorCell});
    }

    private DataRow createDataRowAsColumns(final Document doc, final List<DoubleCell> featureVector) {
        final RowKey rowKey = RowKey.createRowKey(m_rowKeyNr);
        m_rowKeyNr++;
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
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_booleanModel.loadSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
        m_ignoreTags.loadSettingsFrom(settings);
        m_asCollectionModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_booleanModel.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
        m_ignoreTags.saveSettingsTo(settings);
        m_asCollectionModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_booleanModel.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
        m_ignoreTags.validateSettings(settings);
        m_asCollectionModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    private void checkUncheck() {
        if (m_booleanModel.getBooleanValue()) {
            m_colModel.setEnabled(false);
        } else {
            m_colModel.setEnabled(true);
        }
    }

    /**
     *
     * @author Kilian Thiel, University of Konstanz
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            checkUncheck();
        }
    }
}

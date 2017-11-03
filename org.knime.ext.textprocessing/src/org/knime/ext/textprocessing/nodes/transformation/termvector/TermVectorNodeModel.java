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
package org.knime.ext.textprocessing.nodes.transformation.termvector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.transformation.documentvector.DocumentVectorNodeDialog;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.CommonColumnNames;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model of the document vector node, creates a document feature vector for each document. As features all term of
 * the given bag of words are used. As vector values, a column can be specified or bit vectors can be created.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TermVectorNodeModel extends NodeModel {

    /**
     * The default setting of the creation of bit vectors. By default bit vectors are created (<code>true</code>).
     */
    public static final boolean DEFAULT_BOOLEAN = true;

    /**
     * The default column to use.
     */
    public static final String DEFAULT_COL = "";

    /**
     * The default setting to ignore tags or not.
     */
    public static final boolean DEFAULT_IGNORE_TAGS = true;

    /**
     * The default value to the as collection flag.
     */
    public static final boolean DEFAULT_ASCOLLECTION = true;

    /**
     * Default name of column containing the terms.
     */
    public static final String DEFAULT_DOCUMENT_COLNAME = CommonColumnNames.DEF_ORIG_DOCUMENT_COLNAME;

    private int m_documentColIndex = -1;

    private int m_termColIndex = -1;

    private final SettingsModelString m_colModel = TermVectorNodeDialog.getColumnModel();

    private final SettingsModelBoolean m_booleanModel = TermVectorNodeDialog.getBooleanModel();

    private final SettingsModelBoolean m_ignoreTagsModel = TermVectorNodeDialog.getIgnoreTagsModel();

    private final SettingsModelString m_documentColModel = TermVectorNodeDialog.getDocColModel();

    private SettingsModelBoolean m_asCollectionModel = DocumentVectorNodeDialog.getAsCollectionModel();

    private static final DoubleCell DEFAULT_CELL = new DoubleCell(0.0);

    private final TextContainerDataCellFactory m_termFac = TextContainerDataCellFactoryBuilder.createTermCellFactory();

    /**
     * Creates a new instance of <code>TermVectorNodeModel</code>.
     */
    public TermVectorNodeModel() {
        super(1, 1);
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

        ColumnSelectionVerifier docVerifier =
                new ColumnSelectionVerifier(m_documentColModel, spec, DocumentValue.class);
        if (docVerifier.hasWarningMessage()) {
            setWarningMessage(docVerifier.getWarningMessage());
        }

        m_documentColIndex = spec.findColumnIndex(m_documentColModel.getStringValue());

        if (!m_booleanModel.getBooleanValue()) {
            ColumnSelectionVerifier valueVerifier =
                    new ColumnSelectionVerifier(m_colModel, spec, DoubleValue.class);
            if (valueVerifier.hasWarningMessage()) {
                setWarningMessage(valueVerifier.getWarningMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        int colIndex = -1;
        // Check if no valid column selected, the use of boolean values is
        // specified !
        if (!m_booleanModel.getBooleanValue()) {
            colIndex = inData[0].getDataTableSpec().findColumnIndex(m_colModel.getStringValue());
        }

        // Sort the data table first by term
        final List<String> colList = new ArrayList<String>();
        colList.add(inData[0].getDataTableSpec().getColumnSpec(m_termColIndex).getName());
        final boolean[] sortAsc = new boolean[colList.size()];
        sortAsc[0] = true;
        final SortedTable sortedTable = new SortedTable(inData[0], colList, sortAsc, exec);

        // hash table holding an index for each feature
        Map<Document, Integer> featureIndexTable = new HashMap<Document, Integer>();

        // first go through data table to collect the features
        int currIndex = 0;
        RowIterator it = sortedTable.iterator();
        while (it.hasNext()) {
            exec.checkCanceled();
            final DataRow row = it.next();
            if (!row.getCell(m_documentColIndex).isMissing()) {
                final Document d = ((DocumentValue)row.getCell(m_documentColIndex)).getDocument();
                if (!featureIndexTable.containsKey(d)) {
                    featureIndexTable.put(d, currIndex);
                    currIndex++;
                }
            }
        }

        // second go through data table to create feature vectors
        BufferedDataContainer dc;
        if (m_asCollectionModel.getBooleanValue()) {
            dc = exec.createDataContainer(createDataTableSpecAsCollection(featureIndexTable));
        } else {
            dc = exec.createDataContainer(createDataTableSpecAsColumns(featureIndexTable));
        }

        Term lastTerm = null;
        List<DoubleCell> featureVector = initFeatureVector(featureIndexTable.size());
        int missingValueCount = 0;

        it = sortedTable.iterator();
        while (it.hasNext()) {
            exec.checkCanceled();
            final DataRow row = it.next();
            if (!row.getCell(m_documentColIndex).isMissing() && !row.getCell(m_termColIndex).isMissing()) {
                final Document currDoc = ((DocumentValue)row.getCell(m_documentColIndex)).getDocument();
                final Term currTerm = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
                double currValue = 1;
                if (colIndex > -1) {
                    currValue = ((DoubleValue)row.getCell(colIndex)).getDoubleValue();
                }

                // if current term is not equals last term, create new feature
                // vector for last term
                if (lastTerm != null) {
                    final boolean equals = m_ignoreTagsModel.getBooleanValue() ? currTerm.equalsWordsOnly(lastTerm)
                        : currTerm.equals(lastTerm);
                    if (!equals) {
                        // add old feature vector to table
                        DataRow newRow;
                        if (m_asCollectionModel.getBooleanValue()) {
                            newRow = createDataRowAsCollection(lastTerm, featureVector);
                        } else {
                            newRow = createDataRowAsColumns(lastTerm, featureVector);
                        }
                        dc.addRowToTable(newRow);
                        // create new feature vector
                        featureVector = initFeatureVector(featureIndexTable.size());
                    }
                }
                // add new document at certain index to feature vector
                int index = featureIndexTable.get(currDoc);
                featureVector.set(index, new DoubleCell(currValue));

                lastTerm = currTerm;
            } else {
                missingValueCount++;
            }
        }

        // add last term to data container
        if (featureVector.size() > 0) {
            DataRow newRow;
            if (m_asCollectionModel.getBooleanValue()) {
                newRow = createDataRowAsCollection(lastTerm, featureVector);
            } else {
                newRow = createDataRowAsColumns(lastTerm, featureVector);
            }
            dc.addRowToTable(newRow);
        }

        if (missingValueCount == 1) {
            setWarningMessage("One row has been ignored due to missing values.");
        } else if (missingValueCount > 1) {
            setWarningMessage(missingValueCount + " rows have been ignored due to missing values.");
        }

        dc.close();
        featureIndexTable.clear();

        return new BufferedDataTable[]{dc.getTable()};
    }

    private long m_rowKeyNr = 1;

    private DataRow createDataRowAsCollection(final Term term, final List<DoubleCell> featureVector) {
        final RowKey rowKey = RowKey.createRowKey(m_rowKeyNr);
        m_rowKeyNr++;
        final DataCell termCell = m_termFac.createDataCell(term);
        final DataCell collectionCell = CollectionCellFactory.createSparseListCell(featureVector, DEFAULT_CELL);
        return new DefaultRow(rowKey, new DataCell[]{termCell, collectionCell});
    }

    private DataRow createDataRowAsColumns(final Term term, final List<DoubleCell> featureVector) {
            final DataCell[] cells = new DataCell[featureVector.size() + 1];
            cells[0] = m_termFac.createDataCell(term);
            for (int i = 0; i < cells.length - 1; i++) {
                cells[i + 1] = featureVector.get(i);
            }

            final RowKey rowKey = RowKey.createRowKey(m_rowKeyNr);
            m_rowKeyNr++;
            final DataRow newRow = new DefaultRow(rowKey, cells);

            return newRow;
    }

    private DataTableSpec createDataTableSpecAsCollection(final Map<Document, Integer> featureIndexTable) {
        final Map<String, Integer> columnTitles = new HashMap<String, Integer>();
        final DataColumnSpec[] columnSpecs = new DataColumnSpec[2];

        // add document column
        DataColumnSpecCreator columnSpecCreator =
            new DataColumnSpecCreator(CommonColumnNames.DEF_TERM_COLNAME, m_termFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();

        // add feature vector columns
        columnSpecCreator = new DataColumnSpecCreator(CommonColumnNames.DEF_TERM_VECTOR_COLNAME,
            ListCell.getCollectionType(DoubleCell.TYPE));

        if (featureIndexTable != null) {
            final String[] featureNames = new String[featureIndexTable.size()];

            for (Entry<Document, Integer> entry : featureIndexTable.entrySet()) {
                final Document d = entry.getKey();
                final int index = entry.getValue();

                // avoid duplicate titles by adding numbers if titles are equal.
                final String origTitle = d.getTitle();
                String title = origTitle;
                Integer count = columnTitles.get(origTitle);
                // if title is used the first time initialize the count value
                // with 1
                if (count == null || count < 1) {
                    count = 1;
                    columnTitles.put(origTitle, count);

                    // if title occurs another time, add the count value
                } else if (count >= 1) {
                    count++;
                    title += " - #" + count;
                    columnTitles.put(origTitle, count);
                }
                featureNames[index] = title;
            }
            columnSpecCreator.setElementNames(featureNames);
        }
        columnSpecs[1] = columnSpecCreator.createSpec();

        columnTitles.clear();
        return new DataTableSpec(columnSpecs);
    }

    private DataTableSpec createDataTableSpecAsColumns(final Map<Document, Integer> featureIndexTable) {
        final Map<String, Integer> columnTitles = new HashMap<String, Integer>();

        final int featureCount = featureIndexTable.size();
        final DataColumnSpec[] columnSpecs = new DataColumnSpec[featureCount + 1];

        // add document column
        DataColumnSpecCreator columnSpecCreator = new DataColumnSpecCreator("Term", m_termFac.getDataType());
        columnSpecs[0] = columnSpecCreator.createSpec();

        // add feature vector columns
        for (final Entry<Document, Integer> entry : featureIndexTable.entrySet()) {
            final Document d = entry.getKey();
            int index = entry.getValue() + 1;

            // avoid duplicate titles by adding numbers if titles are equal.
            final String origTitle = d.getTitle();
            String title = origTitle;
            Integer count = columnTitles.get(origTitle);
            // if title is used the first time initialize the count value with 1
            if (count == null || count < 1) {
                count = 1;
                columnTitles.put(origTitle, count);

                // if title occurs another time, add the count value
            } else if (count >= 1) {
                count++;
                title += " - #" + count;
                columnTitles.put(origTitle, count);
            }

            columnSpecCreator = new DataColumnSpecCreator(title, DoubleCell.TYPE);
            columnSpecs[index] = columnSpecCreator.createSpec();
        }

        columnTitles.clear();
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
        m_ignoreTagsModel.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
        m_asCollectionModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_booleanModel.saveSettingsTo(settings);
        m_ignoreTagsModel.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
        m_asCollectionModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_booleanModel.validateSettings(settings);
        m_ignoreTagsModel.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
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

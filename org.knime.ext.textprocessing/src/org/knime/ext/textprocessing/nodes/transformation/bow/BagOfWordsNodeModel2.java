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
 * ---------------------------------------------------------------------
 *
 * History
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.bow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell2;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.CommonColumnNames;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model class of the Bag of word creator node. One column containing
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s is necessary to create a bag of words. The output table
 * contains a {@link TermCell2} column and additionally carries over columns selected in the {@code NodeDialog}.
 *
 * @author Kilian Thiel, University of Konstanz
 * @since 3.5
 */
class BagOfWordsNodeModel2 extends NodeModel {

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the column with the documents to create
     * the bag of words from.
     *
     * @return {@code SettingsModelString} containing the name of the document column.
     */
    static final SettingsModelString getDocumentColumnModel() {
        return new SettingsModelString(BagOfWordsConfigKeys2.CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * Creates and returns a {@link SettingsModelColumnFilter2} containing the name of the columns that will be carried
     * over to the output table.
     *
     * @return {@code SettingsModelColumnFilter2} containing the name of the columns that will be carried over to the
     *         output table.
     */
    static final SettingsModelColumnFilter2 getColumnSelectionModel() {
        return new SettingsModelColumnFilter2(BagOfWordsConfigKeys2.CFG_KEY_COLUMN_FILTER);
    }

    /**
     * Creates and returns a {@link SettingsModelString} containing the name of the term column that will be created.
     *
     * @return {@code SettingsModelString} containing the name of the term column.
     */
    static final SettingsModelString getTermColumnModel() {
        return new SettingsModelString(BagOfWordsConfigKeys2.CFG_KEY_TERM_COL, CommonColumnNames.DEF_TERM_COLNAME);
    }

    static boolean checkIncludes(final SettingsModelColumnFilter2 colFilter, final DataTableSpec spec,
        final String colName) {
        List<String> includes = Arrays.asList(colFilter.applyTo(spec).getIncludes());
        if (includes.contains(colName.trim())) {
            return true;
        }
        return false;
    }

    private final SettingsModelString m_docColModel = getDocumentColumnModel();

    private final SettingsModelColumnFilter2 m_colFilterModel = getColumnSelectionModel();

    private final SettingsModelString m_termColModel = getTermColumnModel();

    private final TextContainerDataCellFactory m_termFac = TextContainerDataCellFactoryBuilder.createTermCellFactory();

    private int m_documentColIndex = -1;

    /**
     * Creates a new instance of {@code BagOfWordsNodeModel2} with one in and one data table out port.
     */
    BagOfWordsNodeModel2() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);

        return new DataTableSpec[]{createDataTableSpec(inSpecs[0])};
    }

    private void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // verify that the incoming DataTableSpec contains at least one Document column
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        m_documentColIndex = spec.findColumnIndex(m_docColModel.getStringValue());

        // check if there already is a column named like the specified term column name
        if (checkIncludes(m_colFilterModel, spec, m_termColModel.getStringValue())) {
            throw new InvalidSettingsException("Can't create new column '" + m_termColModel.getStringValue()
                + "' as input spec already contains column named '" + m_termColModel.getStringValue() + "'!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        DataTableSpec inputSpec = inData[0].getDataTableSpec();
        checkDataTableSpec(inputSpec);

        // prepare data container
        final BufferedDataContainer bdc = exec.createDataContainer(createDataTableSpec(inputSpec));

        DataCell docCell = null;

        final long rowCount = inData[0].size();
        long currRow = 1;
        AtomicLong rowId = new AtomicLong(0);
        final RowIterator it = inData[0].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();

            // get terms for document
            docCell = row.getCell(m_documentColIndex);

            // get additional cells
            String[] includedColNames = m_colFilterModel.applyTo(inputSpec).getIncludes();
            DataCell[] additionalCells = new DataCell[includedColNames.length];
            for (int i = 0; i < includedColNames.length; i++) {
                additionalCells[i] = row.getCell(inputSpec.findColumnIndex(includedColNames[i]));
            }

            // if cell is not missing
            if (!docCell.isMissing()) {
                Document doc = ((DocumentValue)row.getCell(m_documentColIndex)).getDocument();
                addToBOW(setOfTerms(doc), additionalCells, bdc, rowId);
            } else {
                // set warning message
                setWarningMessage(
                    "Input table contains missing values in document column. Missing document values will be ignored.");
            }

            // report status
            double progress = (double)currRow / (double)rowCount;
            exec.setProgress(progress, "Processing document " + currRow + " of " + rowCount);
            exec.checkCanceled();
            currRow++;
        }

        bdc.close();
        return new BufferedDataTable[]{bdc.getTable()};
    }

    private void addToBOW(final Set<Term> terms, final DataCell[] additionalCells, final BufferedDataContainer bdc,
        final AtomicLong rowId) {
        for (Term t : terms) {
            final RowKey key = RowKey.createRowKey(rowId.getAndIncrement());
            final DataCell tc = m_termFac.createDataCell(t);
            // create new datacell array and add selected columns and term column
            DataCell[] newDataCells = new DataCell[bdc.getTableSpec().getNumColumns()];
            for (int i = 0; i < additionalCells.length; i++) {
                newDataCells[i] = additionalCells[i];
            }
            newDataCells[newDataCells.length - 1] = tc;
            bdc.addRowToTable(new DefaultRow(key, newDataCells));
        }
    }

    private Set<Term> setOfTerms(final Document doc) {
        Set<Term> termSet = null;
        if (doc != null) {
            termSet = new LinkedHashSet<Term>();

            Iterator<Sentence> it = doc.sentenceIterator();
            while (it.hasNext()) {
                Sentence sen = it.next();
                termSet.addAll(sen.getTerms());
            }
        }
        return termSet;
    }

    private final DataTableSpec createDataTableSpec(final DataTableSpec dataTableSpec) {
        // get column specs of selected input columns
        String[] includedNames = m_colFilterModel.applyTo(dataTableSpec).getIncludes();
        DataColumnSpec[] includedColumnSpecs = new DataColumnSpec[includedNames.length];
        for (int i = 0; i < includedNames.length; i++) {
            includedColumnSpecs[i] = dataTableSpec.getColumnSpec(includedNames[i]);
        }

        // create term column spec
        DataColumnSpecCreator terms =
            new DataColumnSpecCreator(m_termColModel.getStringValue(), m_termFac.getDataType());

        // create new data table with selected columns and term column
        return new DataTableSpec(new DataTableSpec(includedColumnSpecs), new DataTableSpec(terms.createSpec()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_colFilterModel.saveSettingsTo(settings);
        m_termColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_colFilterModel.validateSettings(settings);
        m_termColModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_colFilterModel.loadSettingsFrom(settings);
        m_termColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        //Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
    }
}

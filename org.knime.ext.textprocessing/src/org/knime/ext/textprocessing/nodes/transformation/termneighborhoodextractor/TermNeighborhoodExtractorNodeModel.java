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
 *   Jan 12, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.termneighborhoodextractor;

import java.io.File;
import java.io.IOException;
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
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.collection.ListCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
class TermNeighborhoodExtractorNodeModel extends NodeModel {

    static final int DEF_N_NEIGHBORHOOD = 3;

    static final boolean DEF_EXTRACT_SENTENCE = true;

    static final boolean DEF_TERMS_AS_STRINGS = false;

    static final boolean DEF_AS_COLLECTION = false;

    final SettingsModelString m_docColumnModel = TermNeighborhoodExtractorNodeDialog.getDocColumnModel();

    final SettingsModelIntegerBounded m_nNeighborhoodModel =
        TermNeighborhoodExtractorNodeDialog.getNNeighborhoodModel();

    final SettingsModelBoolean m_extractSentenceModel = TermNeighborhoodExtractorNodeDialog.getExtractSentenceModel();

    final SettingsModelBoolean m_termsAsStringsModel = TermNeighborhoodExtractorNodeDialog.getTermsAsStringsModel();

    final SettingsModelBoolean m_asCollectionModel = TermNeighborhoodExtractorNodeDialog.getAsCollectionModel();

    final TextContainerDataCellFactory m_termFac = TextContainerDataCellFactoryBuilder.createTermCellFactory();

    TermNeighborhoodExtractorNodeModel() {
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

    /**
     * @param dataTableSpec
     * @throws InvalidSettingsException
     */
    private final void checkDataTableSpec(final DataTableSpec dataTableSpec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(dataTableSpec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColumnModel, dataTableSpec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        //TODO: Check for column names term/sentence/Right neighbor etc.
    }

    private final DataTableSpec createDataTableSpec(final DataTableSpec dataTableSpec) {
        //create sentence column spec
        DataColumnSpecCreator sentenceCol = null;
        if (m_extractSentenceModel.getBooleanValue()) {
            sentenceCol = new DataColumnSpecCreator("Sentence", StringCell.TYPE);
        }

        // create term column spec
        DataColumnSpecCreator termsSpecCreator = new DataColumnSpecCreator("Term", m_termFac.getDataType());

        DataColumnSpec[] leftNeighborColSpecs = null;
        DataColumnSpec[] rightNeighborColSpecs = null;
        if (!m_asCollectionModel.getBooleanValue()) {
            leftNeighborColSpecs = new DataColumnSpec[m_nNeighborhoodModel.getIntValue()];
            rightNeighborColSpecs = new DataColumnSpec[m_nNeighborhoodModel.getIntValue()];
            for (int i = 0; i < m_nNeighborhoodModel.getIntValue(); i++) {
                leftNeighborColSpecs[i] = new DataColumnSpecCreator("Left Neighbor " + (i + 1),
                    m_termsAsStringsModel.getBooleanValue() ? StringCell.TYPE : m_termFac.getDataType()).createSpec();
                rightNeighborColSpecs[i] = new DataColumnSpecCreator("Right Neighbor " + (i + 1),
                    m_termsAsStringsModel.getBooleanValue() ? StringCell.TYPE : m_termFac.getDataType()).createSpec();
            }
        } else {
            leftNeighborColSpecs = new DataColumnSpec[1];
            rightNeighborColSpecs = new DataColumnSpec[1];
            leftNeighborColSpecs[0] =
                new DataColumnSpecCreator("Left Neighbors",
                    ListCell.getCollectionType(
                        m_termsAsStringsModel.getBooleanValue() ? m_termFac.getDataType() : StringCell.TYPE))
                            .createSpec();
            rightNeighborColSpecs[0] =
                new DataColumnSpecCreator("Right Neighbors",
                    ListCell.getCollectionType(
                        m_termsAsStringsModel.getBooleanValue() ? m_termFac.getDataType() : StringCell.TYPE))
                            .createSpec();
        }

        // create DataTableSpec from right and left neighbors
        DataTableSpec neighbors =
            new DataTableSpec(new DataTableSpec(leftNeighborColSpecs), new DataTableSpec(rightNeighborColSpecs));
        // create DataTableSpec from terms
        DataTableSpec terms = new DataTableSpec(termsSpecCreator.createSpec());

        if (sentenceCol != null) {
            return new DataTableSpec(dataTableSpec,
                new DataTableSpec(new DataTableSpec(sentenceCol.createSpec()), new DataTableSpec(terms, neighbors)));
        } else {
            return new DataTableSpec(dataTableSpec, new DataTableSpec(terms, neighbors));
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
        int docColIndex = inputSpec.findColumnIndex(m_docColumnModel.getStringValue());

        // prepare data container
        final BufferedDataContainer bdc = exec.createDataContainer(createDataTableSpec(inputSpec));

        final long rowCount = inData[0].size();
        long currRow = 1;
        AtomicLong rowId = new AtomicLong(0);
        final RowIterator it = inData[0].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();

            // get cells from original data table
            DataCell[] inputCells = new DataCell[inputSpec.getColumnNames().length];
            for (int i = 0; i < inputCells.length; i++) {
                inputCells[i] = row.getCell(inputSpec.findColumnIndex(inputSpec.getColumnNames()[i]));
            }
            DataCell docCell = inputCells[docColIndex];

            if (!docCell.isMissing()) {
                Document doc = ((DocumentValue)docCell).getDocument();
                extractInformation(setOfSentences(doc), inputCells, bdc, rowId);
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

    /**
     * @param doc
     * @param inputCells
     * @param bdc
     * @param rowId
     */
    private void extractInformation(final Set<Sentence> sentences, final DataCell[] inputCells,
        final BufferedDataContainer bdc, final AtomicLong rowId) {
        for (Sentence s : sentences) {
            List<Term> terms = s.getTerms();
            for (int i = 0; i < terms.size(); i++) {
                final RowKey key = RowKey.createRowKey(rowId.getAndIncrement());
                final DataCell tc = m_termFac.createDataCell(terms.get(i));
                DataCell[] newDataCells = new DataCell[bdc.getTableSpec().getNumColumns()];
                for (int k = 0; k < inputCells.length; k++) {
                    newDataCells[k] = inputCells[k];
                }
                if (m_extractSentenceModel.getBooleanValue()) {
                    newDataCells[inputCells.length] = new StringCell(s.getText());
                    newDataCells[inputCells.length + 1] = tc;
                } else {
                    newDataCells[inputCells.length] = tc;
                }
//                List<Term> leftNeighbors = new LinkedList<Term>();
//                List<Term> rightNeighbors = new LinkedList<Term>();
                for (int j = 1; j <= m_nNeighborhoodModel.getIntValue(); j++) {
                    if (i + 1 + m_nNeighborhoodModel.getIntValue() - j < terms.size()) {
                        if (!m_termsAsStringsModel.getBooleanValue()) {
                            newDataCells[newDataCells.length - j] =
                                    m_termFac.createDataCell(terms.get(i + 1 + m_nNeighborhoodModel.getIntValue() - j));
                        } else {
                            newDataCells[newDataCells.length - j] =
                                new StringCell((terms.get(i + 1 + m_nNeighborhoodModel.getIntValue() - j)).getText());
                        }
                    } else {
                        newDataCells[newDataCells.length - j] = DataType.getMissingCell();
                    }
                    if (i - 1 - m_nNeighborhoodModel.getIntValue() + j >= 0) {
                        if (!m_termsAsStringsModel.getBooleanValue()) {
                            newDataCells[newDataCells.length - m_nNeighborhoodModel.getIntValue() - j] =
                                    m_termFac.createDataCell(terms.get(i - 1 - m_nNeighborhoodModel.getIntValue() + j));
                        } else {
                            newDataCells[newDataCells.length - m_nNeighborhoodModel.getIntValue() - j] =
                                new StringCell((terms.get(i - 1 - m_nNeighborhoodModel.getIntValue() + j)).getText());
                        }
                    } else {
                        newDataCells[newDataCells.length - m_nNeighborhoodModel.getIntValue() - j] =
                            DataType.getMissingCell();
                    }
                }
                bdc.addRowToTable(new DefaultRow(key, newDataCells));
            }
        }
    }

    private Set<Sentence> setOfSentences(final Document document) {
        Set<Sentence> sentenceSet = null;

        if (document != null) {
            sentenceSet = new LinkedHashSet<Sentence>();
            Iterator<Sentence> it = document.sentenceIterator();
            while (it.hasNext()) {
                sentenceSet.add(it.next());
            }
        }
        return sentenceSet;
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
        m_docColumnModel.saveSettingsTo(settings);
        m_nNeighborhoodModel.saveSettingsTo(settings);
        m_extractSentenceModel.saveSettingsTo(settings);
        m_termsAsStringsModel.saveSettingsTo(settings);
        m_asCollectionModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColumnModel.validateSettings(settings);
        m_nNeighborhoodModel.validateSettings(settings);
        m_extractSentenceModel.validateSettings(settings);
        m_termsAsStringsModel.validateSettings(settings);
        m_asCollectionModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColumnModel.loadSettingsFrom(settings);
        m_nNeighborhoodModel.loadSettingsFrom(settings);
        m_extractSentenceModel.loadSettingsFrom(settings);
        m_termsAsStringsModel.loadSettingsFrom(settings);
        m_asCollectionModel.loadSettingsFrom(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

}

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
 *   20.11.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.sentenceextraction;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class SentenceExtractionNodeModel extends NodeModel {

    /**
     * The name of the column containing the number of terms.
     */
    static final String TERMCOUNT_COLNAME = "Number of terms";

    /**
     * The name of the column containing the sentence.
     */
    static final String SENTENCE_COLNAME = "Sentence";

    private int m_docColIndex = -1;

    private SettingsModelString m_documentColModel =
        SentenceExtractionNodeDialog.getDocumentColumnModel();


    /**
     * Creates a new instance of <code>SentenceStatisticsNodeModel</code>.
     */
    public SentenceExtractionNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);

        return new DataTableSpec[]{createOutDataTableSpec()};

    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier v = new DataTableSpecVerifier(spec);
        v.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier docVerifier =
            new ColumnSelectionVerifier(m_documentColModel, spec, DocumentValue.class);
        if (docVerifier.hasWarningMessage()) {
            setWarningMessage(docVerifier.getWarningMessage());
        }

        m_docColIndex = spec.findColumnIndex(m_documentColModel.getStringValue());

    }

    private DataTableSpec createOutDataTableSpec() {
        DataColumnSpecCreator docCreator = new DataColumnSpecCreator(
                DocumentDataTableBuilder.DEF_DOCUMENT_COLNAME,
                DocumentCell.TYPE);
        DataColumnSpecCreator sentenceCreator = new DataColumnSpecCreator(
                SENTENCE_COLNAME, StringCell.TYPE);
        DataColumnSpecCreator lengthCreator = new DataColumnSpecCreator(
                TERMCOUNT_COLNAME, IntCell.TYPE);

        return new DataTableSpec(docCreator.createSpec(),
                sentenceCreator.createSpec(), lengthCreator.createSpec());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        m_docColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_documentColModel.getStringValue());

        // create cache
        final TextContainerDataCellFactory docCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        docCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
        final DataCellCache docCache = new LRUDataCellCache(docCellFac);
        BufferedDataContainer dc = exec.createDataContainer(
                createOutDataTableSpec());

        try {
            long count = 0;
            RowIterator it = inData[0].iterator();
            while (it.hasNext()) {
                DataRow row = it.next();
                if (!row.getCell(m_docColIndex).isMissing()) {
                    Document doc = ((DocumentValue)row.getCell(m_docColIndex)).getDocument();
                    DataCell docCell = docCache.getInstance(doc);

                    Iterator<Sentence> si = doc.sentenceIterator();
                    while (si.hasNext()) {
                        exec.checkCanceled();

                        Sentence s = si.next();
                        String sentenceStr = s.getText();
                        int termCount = s.getTerms().size();
                        RowKey rowKey = RowKey.createRowKey(count);
                        DefaultRow newRow =
                            new DefaultRow(rowKey, docCell, new StringCell(sentenceStr), new IntCell(termCount));
                        dc.addRowToTable(newRow);
                        count++;
                    }
                } else {
                    dc.addRowToTable(new DefaultRow(RowKey.createRowKey(count), DataType.getMissingCell(),
                        DataType.getMissingCell(), DataType.getMissingCell()));
                    count++;
                }
            }
        } finally {
            dc.close();
            docCache.close();
        }

        return new BufferedDataTable[]{dc.getTable()};
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_documentColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_documentColModel.validateSettings(settings);
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
    throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
    throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
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
        DataTableSpecVerifier v = new DataTableSpecVerifier(inSpecs[0]);
        v.verifyMinimumDocumentCells(1, true);

        m_docColIndex = inSpecs[0].findColumnIndex(
                m_documentColModel.getStringValue());
        if (m_docColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! "
                    + "Check your settings!");
        }

        return new DataTableSpec[]{createOutDataTableSpec()};

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
        m_docColIndex = inData[0].getDataTableSpec().findColumnIndex(
                m_documentColModel.getStringValue());

        // create cache
        final TextContainerDataCellFactory docCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        docCellFac.prepare(exec);
        final DataCellCache docCache = new LRUDataCellCache(docCellFac);
        BufferedDataContainer dc = exec.createDataContainer(
                createOutDataTableSpec());

        try {
            int count = 1;
            RowIterator it = inData[0].iterator();
            while (it.hasNext()) {
                DataRow row = it.next();
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

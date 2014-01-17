/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.bow;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.swing.event.ChangeListener;
import org.knime.core.data.DataCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.DefaultSwitchEventListener;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The model class of the Bag of word creator node. The method
 * {@link BagOfWordsNodeModel#configure(DataTableSpec[])} validates the incoming
 * {@link org.knime.core.data.DataTableSpec}. Only one column containing
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s is allowed.
 * The output table contains two columns. The first consists of
 * {@link org.knime.ext.textprocessing.data.TermCell}s the second of
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s. A single row
 * represents a tupel of a term and a document meaning that the term is
 * contained in the document.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class BagOfWordsNodeModel extends NodeModel {

    /**
     * The default setting for appending the original document.
     * @since 2.9
     */
    public static final boolean DEF_APPEND_ORIGDOCUMENT = false;


    private final SettingsModelString m_docColModel = BagOfWordsNodeDialog.getDocumentColumnModel();

    private final SettingsModelString m_origDocColModel = BagOfWordsNodeDialog.getOrigDocumentColumnModel();

    private final SettingsModelBoolean m_appendOrigDocModel = BagOfWordsNodeDialog.getAppendIncomingDocument();


    private BagOfWordsDataTableBuilder m_dtBuilder = new BagOfWordsDataTableBuilder();

    private TextContainerDataCellFactory m_termFac = TextContainerDataCellFactoryBuilder.createTermCellFactory();

    private int m_rowId = 1;

    private int m_documentColIndex = -1;

    private int m_origDocumentColIndex = -1;

    /**
     * Creates a new instance of <code>BagOfWordsNodeModel</code> with one in
     * and one data table out port.
     */
    public BagOfWordsNodeModel() {
        super(1, 1);

        final ChangeListener cl = new DefaultSwitchEventListener(m_origDocColModel, m_appendOrigDocModel);
        m_appendOrigDocModel.addChangeListener(cl);
        cl.stateChanged(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec(m_appendOrigDocModel.getBooleanValue())};
    }

    private void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        // if only one document cell is available use it as doc column
        if (verifier.verifyDocumentCell(false)) {
            m_documentColIndex = verifier.getDocumentCellIndex();

        // if there are more than one document columns available check settings
        } else {
            final String docColName = m_docColModel.getStringValue();
            final String origDocColName = m_origDocColModel.getStringValue();
            m_documentColIndex = spec.findColumnIndex(docColName);
            m_origDocumentColIndex = spec.findColumnIndex(origDocColName);
        }

        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified document column is not valid! Check your settings!");
        }
        if (m_origDocumentColIndex < 0 && m_appendOrigDocModel.getBooleanValue()) {
            throw new InvalidSettingsException(
                "Index of specified original document column is not valid! Check your settings!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        // prepare data container
        final BufferedDataContainer bdc = exec.createDataContainer(m_dtBuilder.createDataTableSpec(
            m_appendOrigDocModel.getBooleanValue()));
        final Set<UUID> processedDocUUIDs = new HashSet<UUID>();

        DataCell docCell = null;
        DataCell origDocCell = null;

        final int rowCount = inData[0].getRowCount();
        int currRow = 1;
        final RowIterator it = inData[0].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();

            // get terms for document
            docCell = row.getCell(m_documentColIndex);
            Document doc = ((DocumentValue)row.getCell(m_documentColIndex)).getDocument();

            // get original document cell to append
            if (m_appendOrigDocModel.getBooleanValue()) {
                origDocCell = row.getCell(m_origDocumentColIndex);
            }

            if (!processedDocUUIDs.contains(doc.getUUID())) {
                Set<Term> terms = setOfTerms(doc);
                addToBOW(terms, docCell, origDocCell, bdc);
                processedDocUUIDs.add(doc.getUUID());
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

    private void addToBOW(final Set<Term> terms, final DataCell docCell, final DataCell origDocCell,
        final BufferedDataContainer bdc) {
        for (Term t : terms) {
            final RowKey key = RowKey.createRowKey(m_rowId);
            final DataCell tc = m_termFac.createDataCell(t);

            DataRow newRow;
            if (origDocCell == null) {
                newRow = new DefaultRow(key, tc, docCell);
            } else {
                newRow = new DefaultRow(key, tc, docCell, origDocCell);
            }

            bdc.addRowToTable(newRow);
            m_rowId++;
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


    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_appendOrigDocModel.saveSettingsTo(settings);
        m_origDocColModel.saveSettingsTo(settings);
        m_docColModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            m_appendOrigDocModel.validateSettings(settings);
            m_origDocColModel.validateSettings(settings);
            m_docColModel.validateSettings(settings);
        } catch (InvalidSettingsException e) {
            // do nothing, just catch to ensure backwards compatibility
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        try {
            m_appendOrigDocModel.loadSettingsFrom(settings);
            m_origDocColModel.loadSettingsFrom(settings);
            m_docColModel.loadSettingsFrom(settings);
        } catch (InvalidSettingsException e) {
            // do nothing, just catch to ensure backwards compatibility
        }
    }



    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_rowId = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }
}

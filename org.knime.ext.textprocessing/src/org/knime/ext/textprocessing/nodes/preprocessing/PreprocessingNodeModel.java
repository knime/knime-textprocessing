/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   19.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.event.ChangeListener;

/**
 * This class represents the super class of all text preprocessing node models
 * which apply filtering or modification of terms. Classes which extend
 * <code>PreprocessingNodeModel</code>  have to implement the method
 * {@link PreprocessingNodeModel#initPreprocessing()} and take care of a
 * proper initialization of the used
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing}
 * instance. A stop word filter i.e. requires a file containing the stop words,
 * a case converter requires information about the case to convert the terms to
 * and so on. The configure and execute procedure is done by the
 * <code>PreprocessingNodeModel</code>, classes extending this model do not
 * need to care about that. Once the used <code>Preprocessing</code> instance
 * is initialized properly the rest is done automatically.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class PreprocessingNodeModel extends NodeModel {

    /**
     * The default settings for prerprocessing unmodifiable terms.
     */
    public static final boolean DEF_PREPRO_UNMODIFIABLE = false;
    
    /**
     * The default setting for deep preprocessing (<code>true</code>).
     */
    public static final boolean DEF_DEEP_PREPRO = true;

    /**
     * The default setting for appending the incoming document
     * (<code>true</code>).
     */
    public static final boolean DEF_APPEND_INCOMING = true;
    
    

    private int m_documentColIndex = -1;

    private int m_origDocumentColIndex = -1;

    private int m_termColIndex = -1;

    private SettingsModelBoolean m_deepPreproModel;

    private SettingsModelBoolean m_appendIncomingModel;

    private SettingsModelString m_documentColModel;

    private SettingsModelString m_origDocumentColModel;
    
    private SettingsModelBoolean m_preproUnModifiable;

    /**
     * The <code>Preprocessing</code> instance to use for term preprocessing.
     */
    protected Preprocessing m_preprocessing;
    
    private BufferedDataContainer m_dc = null;
    
    
    private TextContainerDataCellFactory m_docCellFac;
    
    private TextContainerDataCellFactory m_termCellFac;
    
    private BagOfWordsDataTableBuilder m_fac;
    
    private Hashtable<Document, DataCell> m_preprocessedDocuments;
    
    
    private int m_noRows = 0;
    
    private AtomicInteger m_currRow = new AtomicInteger(0);
    
    private ExecutionContext m_exec;

    /**
     * The constructor of <code>PreprocessingNodeModel</code>.
     */
    public PreprocessingNodeModel() {
        super(1, 1);

        m_docCellFac = 
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        m_termCellFac =
            TextContainerDataCellFactoryBuilder.createTermCellFactory();
        m_fac = new BagOfWordsDataTableBuilder();
        
        
        m_deepPreproModel =
            PreprocessingNodeSettingsPane.getDeepPrepressingModel();
        m_appendIncomingModel =
            PreprocessingNodeSettingsPane.getAppendIncomingDocument();
        m_documentColModel =
            PreprocessingNodeSettingsPane.getDocumentColumnModel();
        m_origDocumentColModel =
            PreprocessingNodeSettingsPane.getOrigDocumentColumnModel();
        m_preproUnModifiable = 
            PreprocessingNodeSettingsPane.getPreprocessUnmodifiableModel();
        
        ChangeListener cl1 = new DefaultSwitchEventListener(m_documentColModel,
                m_deepPreproModel);
        m_deepPreproModel.addChangeListener(cl1);
        cl1.stateChanged(null);

        ChangeListener cl2 = new DefaultSwitchEventListener(
                m_origDocumentColModel, m_appendIncomingModel);
        m_appendIncomingModel.addChangeListener(cl2);
        cl2.stateChanged(null);
    }

    private final void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{m_fac.createDataTableSpec(
                m_appendIncomingModel.getBooleanValue())};
    }

    /**
     * Initializes the <code>Preprocessing</code> instance.
     */
    protected abstract void initPreprocessing();

    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(
            final BufferedDataTable[] inData, final ExecutionContext exec)
    throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());

        // search indices of document and original document columns.
        String docColName = m_documentColModel.getStringValue();
        String origDocColName = m_origDocumentColModel.getStringValue();
        m_documentColIndex =
            inData[0].getDataTableSpec().findColumnIndex(docColName);
        m_origDocumentColIndex =
            inData[0].getDataTableSpec().findColumnIndex(origDocColName);

        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                    "Index of specified document column is not valid! " 
                    + "Check your settings!");
        }
        if (m_origDocumentColIndex < 0) {
            throw new InvalidSettingsException(
                   "Index of specified original document column is not valid!" 
                    + " Check your settings!");
        }        
        
        
        // initialize the underlying preprocessing
        initPreprocessing();
        if (m_preprocessing == null) {
            throw new NullPointerException(
                    "Preprocessing instance may not be null!");
        }
        m_dc = exec.createDataContainer(m_fac.createDataTableSpec(
                        m_appendIncomingModel.getBooleanValue()));
        
        // handle chunks
        m_currRow = new AtomicInteger(0);
        m_noRows = inData[0].getRowCount();
        m_exec = exec;
        
        m_preprocessedDocuments = new Hashtable<Document, DataCell>();
        int count = 0;
        
        RowIterator i = inData[0].iterator();
        while(i.hasNext()) {
            exec.checkCanceled();
            count++;
            DataRow row = i.next();
            
            setProgress();
            processRow(row);
        }
        
        m_dc.close();
        return new BufferedDataTable[]{m_dc.getTable()};
    }

        
    public void processRow(final DataRow row) {
        DataCell newDocCell = null;
        RowKey rowKey = row.getKey();
        DataCell termcell = row.getCell(m_termColIndex);
        DataCell doccell = row.getCell(m_documentColIndex);
        DataCell origDocCell = row.getCell(m_origDocumentColIndex);

        // handle missing value (ignore rows with missing values)
        if (termcell.isMissing() || doccell.isMissing()) {
            return;
        }
        Term term = ((TermValue)termcell).getTermValue();

        //
        // do the preprocessing twist
        //
        // is the term unmodifiable ???
        if (!term.isUnmodifiable() || m_preproUnModifiable.getBooleanValue()) {
            term = m_preprocessing.preprocess(term);

            // if term is null or empty continue with next term !
            if (term == null || term.getText().length() <= 0) {
                return;
            }
        }
        // do we have to preprocess the documents itself too ?
        if (m_deepPreproModel.getBooleanValue()) {
            Document doc = ((DocumentValue)doccell).getDocument();
            newDocCell = m_preprocessedDocuments.get(doc);

            if (newDocCell == null) {
                // preprocess doc here !!!
                DocumentBuilder builder = new DocumentBuilder(doc);
                for (Section s : doc.getSections()) {
                    for (Paragraph p : s.getParagraphs()) {
                        for (Sentence sen : p.getSentences()) {
                            for (Term t : sen.getTerms()) {
                                if (!t.isUnmodifiable()) {
                                    t = m_preprocessing.preprocess(t);
                                }
                                if (t != null && t.getText().length() > 0) {
                                    builder.addTerm(t);
                                }
                            }
                            builder.createNewSentence();
                        }
                        builder.createNewParagraph();
                    }
                    builder.createNewSection(s.getAnnotation());
                }
                Document newDoc = builder.createDocument();
                newDocCell = m_docCellFac.createDataCell(newDoc);
                m_preprocessedDocuments.put(doc, newDocCell);
            }
        } else {
            // new doc is the same as the old doc
            newDocCell = doccell;
        }
        addRowToContainer(rowKey, term, newDocCell, origDocCell);
    }

    private void setProgress() {
        int curr = m_currRow.incrementAndGet();
        double prog = (double)curr / (double)m_noRows;
        m_exec.setProgress(prog, "Preprocesing row " + curr + " of "
                        + m_noRows);
    }
    
    private synchronized void addRowToContainer(RowKey rk, Term t, 
            DataCell preprocessedDoc, DataCell origDoc) {
        DataRow row;
        if (m_appendIncomingModel.getBooleanValue()) {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), 
                    preprocessedDoc, origDoc);
        } else {
            row = new DefaultRow(rk, m_termCellFac.createDataCell(t), 
                    preprocessedDoc);
        }
        m_dc.addRowToTable(row);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_deepPreproModel.loadSettingsFrom(settings);
        m_appendIncomingModel.loadSettingsFrom(settings);
        m_documentColModel.loadSettingsFrom(settings);
        m_origDocumentColModel.loadSettingsFrom(settings);
        m_preproUnModifiable.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_deepPreproModel.saveSettingsTo(settings);
        m_appendIncomingModel.saveSettingsTo(settings);
        m_documentColModel.saveSettingsTo(settings);
        m_origDocumentColModel.saveSettingsTo(settings);
        m_preproUnModifiable.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_deepPreproModel.validateSettings(settings);
        m_appendIncomingModel.validateSettings(settings);
        m_documentColModel.validateSettings(settings);
        m_origDocumentColModel.validateSettings(settings);
        m_preproUnModifiable.validateSettings(settings);
    }
}

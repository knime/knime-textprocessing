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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableBuilderFactory;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

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
     * The default setting for deep preprocessing (<code>false</code>).
     */
    public static final boolean DEF_DEEP_PREPRO = false;
    
    private int m_documentColIndex = -1;
    
    private int m_termColIndex = -1;
    
    private BagOfWordsDataTableBuilder m_dtBuilder = 
        DataTableBuilderFactory.createBowDataTableBuilder();
    
    private SettingsModelBoolean m_deepPreproModel = 
        PreprocessingNodeSettingsPane.getDeepPrepressingModel();
    
    /**
     * The <code>Preprocessing</code> instance to use for term preprocessing.
     */
    protected Preprocessing m_preprocessing;
    
    /**
     * The constructor of <code>PreprocessingNodeModel</code>.
     */
    public PreprocessingNodeModel() {
        super(1, 1);
    }
    
    private final void checkDataTableSpec(final DataTableSpec spec) 
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyDocumentCell(true);
        verifier.verifyTermCell(true);
        m_documentColIndex = verifier.getDocumentCellIndex();
        m_termColIndex = verifier.getTermCellIndex();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    /**
     * Initializes the <code>Preprocessing</code> instance.
     */
    protected abstract void initPreprocessing();
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        checkDataTableSpec(inData[0].getDataTableSpec());
        
        // initialize the underlying preprocessing
        initPreprocessing();
        if (m_preprocessing == null) {
            throw new NullPointerException(
                    "Preprocessing instance may not be null!");
        }
        
        Hashtable<Document, Set<Term>> docTerms = 
            new Hashtable<Document, Set<Term>>();
        Hashtable<Document, Document> preprocessedDoc = 
            new Hashtable<Document, Document>();
        
        ExecutionMonitor subExec = exec.createSubProgress(0.5);
        int rowCount = inData[0].getRowCount();
        int currRow = 1;
        RowIterator i = inData[0].iterator();
        while(i.hasNext()) {
            // report status
            double progress = (double)currRow / (double)rowCount;
            subExec.setProgress(progress, "Processing row " + currRow + " of " 
                    + rowCount);
            exec.checkCanceled();
            currRow++;    
            
            DataRow row = i.next();
            Term term = ((TermValue)row.getCell(m_termColIndex)).getTermValue();
            Document doc = ((DocumentValue)row.getCell(m_documentColIndex)).
                            getDocument();
            Document newDoc = null;
            
            //
            // do the preprocessing twist
            //
            
            // is the term unmodifiable ???
            if (!term.isUnmodifiable()) {
                term = m_preprocessing.preprocess(term);
                
                // if term is null or empty continue with next term !
                if (term == null || term.getText().length() <= 0) {
                    continue;
                }
            }
            // do we have to preprocess the documents itself too ?
            if (m_deepPreproModel.getBooleanValue()) {                
                // preprocess document only if it was not preprocessed till now
                if (preprocessedDoc.containsKey(doc)) {
                    newDoc = preprocessedDoc.get(doc);
                } else {
                    // preprocess doc here !!!
                    DocumentBuilder builder = new DocumentBuilder(doc);
                    List<Section> sections = doc.getSections();
                    for (Section s : sections) {
                        List<Paragraph> paragraphs = s.getParagraphs();
                        for (Paragraph p : paragraphs) {
                            List<Sentence> sentences = p.getSentences();
                            for (Sentence sen : sentences) {
                                List<Term> senTerms = sen.getTerms();
                                for (Term t : senTerms) {
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
                    newDoc = builder.createDocument();
                    
                    // add new document to cache
                    preprocessedDoc.put(doc, newDoc);
                }
            } else {
                // new doc is the same as the old doc
                newDoc = doc;
            }
            
            //
            // save new document and term to hashtable
            //
            Set<Term> terms = null;
            
            // if no term available for the current document
            if (!docTerms.containsKey(newDoc)) {
                terms = new HashSet<Term>();
            } else {
                terms = docTerms.get(newDoc);
            }
            terms.add(term);
            docTerms.put(newDoc, terms);         
        }
        
        preprocessedDoc.clear();
        // build data table
        ExecutionContext subContext = exec.createSubExecutionContext(0.5);
        return new BufferedDataTable[]{m_dtBuilder.createDataTable(
                subContext, docTerms, false)};
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_deepPreproModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_deepPreproModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_deepPreproModel.validateSettings(settings);
    }    
}

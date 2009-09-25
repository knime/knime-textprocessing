/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The <code>AbstractPreprocessor</code> provides the basic fields and members
 * of preprocessor classes. Since they all work on bag of words data tables
 * it contains the index of term and document columns, flags defining whether
 * deep preprocessing has to be applied or not or unmodifiable terms have to be
 * preprocessed as well. Additionally term and document cell factories, a
 * data container and execution monitor is provided.
 * 
 * To implement another preprocessor strategy, the method 
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.AbstractPreprocessor#checkPreprocessing()}
 * needs to be implemented in order to check the type of the preprocessing
 * instance to use. Additionally the method
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.AbstractPreprocessor#applyPreprocessing(BufferedDataTable, ExecutionContext)} 
 * needs to be implemented in which the preprocessor strategy has to be 
 * specified.
 * 
 * @author Kilian Thiel, University of Konstanz
 *
 */
public abstract class AbstractPreprocessor {
    
    /**
     * The index of the document column.
     */
    protected int m_documentColIndex = -1;

    /**
     * The index of the original document column.
     */
    protected int m_origDocumentColIndex = -1;

    /**
     * The index of the term column.
     */
    protected int m_termColIndex = -1;
    
    
    /**
     * The preprocessing method to apply.
     */
    protected Preprocessing m_preprocessing;
    
    /**
     * Flag specifying whether deep preprocessing has to be applied. 
     */
    protected boolean m_deepPreprocessing;
    
    /**
     * Flag specifying whether original documents have to be applied.
     */
    protected boolean m_appendIncomingDocument;
    
    /**
     * Flag specifying whether unmodifiable terms have to be preprocessed as 
     * well.
     */
    protected boolean m_preprocessUnmodifiable;
    
    
    /**
     * 
     */
    protected TextContainerDataCellFactory m_docCellFac;
    
    /**
     * The term cell factory.
     */
    protected TextContainerDataCellFactory m_termCellFac;
    
    /**
     * The document cell factory.
     */
    protected BagOfWordsDataTableBuilder m_fac;
    
    /**
     * The data contained to add rows.
     */
    protected BufferedDataContainer m_dc = null;
    
    /**
     * The execution context.
     */
    protected ExecutionContext m_exec;
        
    private boolean m_isInitialized = false;
    
    /**
     * Empty constructor of <code>AbstractPreprocessor</code>.
     */
    public AbstractPreprocessor() { }
    
    /**
     * Initialized the preprocessor by setting all the given parameters.
     * 
     * @param documentColIndex The index of the document column.
     * @param origDocumentColIndex The index of the original document column.
     * @param termColIndex The index of the term column.
     * @param deepPrepro If <code>true</code> deep preprocessing will be 
     * applied.
     * @param appendOrigDoc If <code>true</code> original document will be 
     * appended.
     * @param preproUnmodifiable If <code>true</code> unmodifiable terms will 
     * be preprocessed.
     * @param prepro The preprocessing to apply.
     * @throws InvalidSettingsException If given parameters are somehow invalid.
     */
    public void initialize(final int documentColIndex, 
            final int origDocumentColIndex, final int termColIndex, 
            final boolean deepPrepro, final boolean appendOrigDoc, 
            final boolean preproUnmodifiable, final Preprocessing prepro)
    throws InvalidSettingsException {
        if (prepro == null) {
            throw new InvalidSettingsException(
                    "Preprocessing type may not be null!");
        }
        if (documentColIndex < 0) {
            throw new InvalidSettingsException("Index of document column [" 
                    + documentColIndex + "] is not valid!");
        }
        if (origDocumentColIndex < 0 && appendOrigDoc) {
            throw new InvalidSettingsException(
                    "Index of original document column [" 
                    + origDocumentColIndex + "] is not valid!");
        }
        if (termColIndex < 0) {
            throw new InvalidSettingsException("Index of term column [" 
                    + termColIndex + "] is not valid!");
        }
        
        m_preprocessing = prepro;
        checkPreprocessing();
        
        m_documentColIndex = documentColIndex;
        m_origDocumentColIndex = origDocumentColIndex;
        m_termColIndex = termColIndex;
        m_deepPreprocessing = deepPrepro;
        m_appendIncomingDocument = appendOrigDoc;
        m_preprocessUnmodifiable = preproUnmodifiable;
        
        m_docCellFac = 
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        m_termCellFac =
            TextContainerDataCellFactoryBuilder.createTermCellFactory();
        m_fac = new BagOfWordsDataTableBuilder();
        
        m_isInitialized = true;
    }
    
    /**
     * Checks if the type of the specified preprocessing instance is compatible
     * with the underlying preprocessor implementation. 
     * 
     * @throws InvalidSettingsException If the type of the specified 
     * preprocessing instance is not compatible.
     */
    public abstract void checkPreprocessing() throws InvalidSettingsException;
    
    /**
     * Checks if the instance is initialized well and runs the preprocessing 
     * twist on the given data table if so, otherwise an exception will be 
     * thrown.
     * 
     * @param inData The incoming data table 
     * @param exec The execution context.
     * @return The output data table.
     * @throws Exception If something happens.
     */
    public BufferedDataTable doPreprocessing(final BufferedDataTable inData,
            final ExecutionContext exec) throws Exception {
        if (!m_isInitialized) {
            throw new IllegalStateException(
                    "Preprocessor has not been initialzed yet!");
        }
        return applyPreprocessing(inData, exec);
    }
    
    /**
     * Specifies how the preprocessing twist is applied and works (row by row,
     * or chunk wise etc.).
     * 
     * @param inData The incoming data table 
     * @param exec The execution context.
     * @return The output data table.
     * @throws Exception If something happens.
     */
    protected abstract BufferedDataTable applyPreprocessing(
            final BufferedDataTable inData, final ExecutionContext exec) 
    throws Exception;
}

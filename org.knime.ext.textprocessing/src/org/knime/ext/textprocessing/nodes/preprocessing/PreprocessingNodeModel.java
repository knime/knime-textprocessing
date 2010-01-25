/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   19.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import javax.swing.event.ChangeListener;

/**
 * This class represents the super class of all text preprocessing node models
 * which apply filtering or modification or any kind of preprocessing of terms 
 * and documents. Classes which extend <code>PreprocessingNodeModel</code>  
 * have to implement the method
 * {@link PreprocessingNodeModel#initPreprocessing()} and take care of a
 * proper initialization of the used
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing}
 * instance. 
 *
 * A stop word filter i.e. requires a file containing the stop words,
 * a case converter requires information about the case to convert the terms to
 * and so on. The configure and execute procedure is done by the
 * <code>PreprocessingNodeModel</code>, classes extending this model do not
 * need to care about that. Once the used <code>Preprocessing</code> instance
 * is initialized properly the rest is done automatically.
 *
 * There exists Two ways of preprocessing a bag of words. The first is row
 * by row. A row consists of a term an a document, and a preprocessing instance
 * i.e. a StopWordFilter can apply the preprocessing step (filtering stop words)
 * for each row separately.
 * The second way of preprocessing is chunk wise preprocessing. Here chunks
 * will be passed over to the preprocessing instance.
 * The preprocessor instance decides which kind of preprocessing strategy
 * (row by row or chunk wise) is applied. By default the
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.RowPreprocessor}
 * is used. To change the strategy i.e. to 
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.ChunkPreprocessor}
 * The constructor must be overwritten and the preprocessor which have to be 
 * used must be specified as parameter.
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
    
    /**
     * The preprocessor to use.
     */
    protected AbstractPreprocessor m_preprocessor;
    
    private BagOfWordsDataTableBuilder m_fac;

    /**
     * The constructor of <code>PreprocessingNodeModel</code> with the specified
     * preprocessor to use.
     * @param preprocessor The preprocessor to use.
     */
    public PreprocessingNodeModel(final AbstractPreprocessor preprocessor) {
        super(1, 1);

        if (preprocessor == null) {
            m_preprocessor = new RowPreprocessor();
        } else {
            m_preprocessor = preprocessor;
        }
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

    /**
     * The constructor of <code>PreprocessingNodeModel</code>.
     * The <code>RowPreprocessor</code> is used by default.
     */
    public PreprocessingNodeModel() {
        this(new RowPreprocessor());
    }
    
    
    private final void checkDataTableSpec(final DataTableSpec spec)
    throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier.verifyTermCell(true);
        m_termColIndex = verifier.getTermCellIndex();
        
        String docColName = m_documentColModel.getStringValue();
        String origDocColName = m_origDocumentColModel.getStringValue();
        m_documentColIndex = spec.findColumnIndex(docColName);
        m_origDocumentColIndex = spec.findColumnIndex(origDocColName);

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
        // search indices of document and original document columns.
        checkDataTableSpec(inData[0].getDataTableSpec());        
        
        // initialize the underlying preprocessing
        initPreprocessing();
        if (m_preprocessing == null) {
            throw new NullPointerException(
                    "Preprocessing instance may not be null!");
        }
        
        // initialize the underlying preprocessor
        if (m_preprocessor == null) {
            throw new NullPointerException(
                    "Preprocessor instance may not be null!");
        }
        m_preprocessor.initialize(
                m_documentColIndex, m_origDocumentColIndex, m_termColIndex,
                m_deepPreproModel.getBooleanValue(), 
                m_appendIncomingModel.getBooleanValue(),
                m_preproUnModifiable.getBooleanValue(),
                m_preprocessing);
        
        return new BufferedDataTable[]{
                m_preprocessor.doPreprocessing(inData[0], exec)};
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

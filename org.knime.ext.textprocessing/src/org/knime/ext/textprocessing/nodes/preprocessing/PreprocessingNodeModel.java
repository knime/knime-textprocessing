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
 *   19.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import javax.swing.event.ChangeListener;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.ProcessingFactory;

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

    private SettingsModelBoolean m_deepPreproModel = PreprocessingNodeSettingsPane.getDeepPrepressingModel();

    private SettingsModelBoolean m_appendIncomingModel = PreprocessingNodeSettingsPane.getAppendIncomingDocument();

    private SettingsModelString m_documentColModel = PreprocessingNodeSettingsPane.getDocumentColumnModel();

    private SettingsModelString m_origDocumentColModel = PreprocessingNodeSettingsPane.getOrigDocumentColumnModel();

    private SettingsModelBoolean m_preproUnModifiable = PreprocessingNodeSettingsPane.getPreprocessUnmodifiableModel();

    /**
     * The <code>Preprocessing</code> instance to use for term preprocessing.
     */
    protected Preprocessing m_preprocessing;

    /**
     * The preprocessor to use.
     */
    private AbstractPreprocessor m_preprocessor;

    /**
     * The constructor of {@link PreprocessingNodeModel} creatin one in port and one out port.
     */
    public PreprocessingNodeModel() {
        super(1, 1);
    }

    /**
     * The constructor of <code>PreprocessingNodeModel</code> with the specified preprocessor to use.
     *
     * @param preprocessor The preprocessor to use.
     * @deprecated use {@link PreprocessingNodeModel#PreprocessingNodeModel()} instead and overwrite
     *             {@link PreprocessingNodeModel#getPreprocessorForBowPP()} and
     *             {@link PreprocessingNodeModel#getPreprocessorForDirectPP()} to specify certain preprocessors if
     *             needed.
     */
    @Deprecated
    public PreprocessingNodeModel(final AbstractPreprocessor preprocessor) {
        this(1, preprocessor);
    }

    /**
     * Constructor of {@link PreprocessingNodeModel} with given inports to set. Ifyou use this constructor, be aware
     * that the in port at index 0 is preserved as in port for the bag of words data table.
     *
     * @param inPorts The number of in ports.
     * @since 2.6
     */
    public PreprocessingNodeModel(final int inPorts) {
        super(inPorts, 1);
        initSettingsListener();
    }

    /**
     * The constructor of <code>PreprocessingNodeModel</code> with the specified preprocessor to use and the specified
     * number of in ports. If you use this constructor, be aware that the in port at index 0 is preserved as in port for
     * the bag of words data table.
     *
     * @param inPorts The number of in ports.
     * @param preprocessor The preprocessor to use.
     * @since 2.6
     * @deprecated use {@link PreprocessingNodeModel#PreprocessingNodeModel(int)} instead and overwrite
     *             {@link PreprocessingNodeModel#getPreprocessorForBowPP()} and
     *             {@link PreprocessingNodeModel#getPreprocessorForDirectPP()} to specify certain preprocessors if
     *             needed.
     */
    @Deprecated
    public PreprocessingNodeModel(final int inPorts, final AbstractPreprocessor preprocessor) {
        super(inPorts, 1);

        if (preprocessor == null) {
            m_preprocessor = ProcessingFactory.getPrecessing();
        } else {
            m_preprocessor = preprocessor;
        }

        initSettingsListener();
    }


    /**
     * Initializes settings listener to enable or disable column selection if deep preprocessing is on or off.
     */
    private final void initSettingsListener() {
        ChangeListener cl1 = new DefaultSwitchEventListener(m_documentColModel, m_deepPreproModel);
        m_deepPreproModel.addChangeListener(cl1);
        cl1.stateChanged(null);

        ChangeListener cl2 = new DefaultSwitchEventListener(m_origDocumentColModel, m_appendIncomingModel);
        m_appendIncomingModel.addChangeListener(cl2);
        cl2.stateChanged(null);
    }


    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        m_termColIndex = verifier.getTermCellIndex();

        final String docColName = m_documentColModel.getStringValue();
        final String origDocColName = m_origDocumentColModel.getStringValue();
        m_documentColIndex = spec.findColumnIndex(docColName);
        m_origDocumentColIndex = spec.findColumnIndex(origDocColName);

        if (m_documentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified document column is not valid! Check your settings!");
        }
        if (m_origDocumentColIndex < 0) {
            throw new InvalidSettingsException(
                "Index of specified original document column is not valid! Check your settings!");
        }
    }

    /**
     * Creates and returns a {@link RowPreprocessor} as default for preprocessing of bag of words. In order to
     * specify an other preprocessor this method has to be overwritten.
     * @return a {@link RowPreprocessor} as default for preprocessing of bag of words.
     * @since 2.9
     */
    protected AbstractPreprocessor getPreprocessorForBowPP() {
        return new RowPreprocessor();
    }

    /**
     * Creates and returns a {@link DocumentPreprocessor} as default for direct preprocessing of document lists. In
     * order to specify an other preprocessor this method has to be overwritten. If the underlying preprocessing
     * strategy is not applicable for direct preprocessing {@code null} should be returned.
     * @return a {@link DocumentPreprocessor} as default for direct preprocessing of document lists.
     * @since 2.9
     */
    protected AbstractPreprocessor getPreprocessorForDirectPP() {
        return new DocumentPreprocessor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);
        internalConfigure(inSpecs);

        // if spec contains no term column switch to direct preprocessing (preprocessing on document lists)
        if (m_termColIndex < 0) {
            m_preprocessor = getPreprocessorForDirectPP();
            if (m_preprocessor == null) {
                throw new IllegalStateException("Preprocessing can not be applied directly on a document list. "
                    + "It has to be aplpied on a bag of words.");
            }

            // not all preprocessing strategies can be applied on a list of documents via DocumentPreprocessor, thus a
            // check is necessary. Therefore the preprocessing needs to be created, which is done during
            // initPreprocessing(). Afterwards the DocumentPreprocessor needs to be check the given preprocessing, which
            // is done during initialize().
            initPreprocessing();
            m_preprocessor.initialize(m_documentColIndex, m_origDocumentColIndex, m_termColIndex,
                m_deepPreproModel.getBooleanValue(), m_appendIncomingModel.getBooleanValue(),
                m_preproUnModifiable.getBooleanValue(), m_preprocessing);

        // if spec contains term column switch to regular bag of word preprocessing
        } else {
            m_preprocessor = getPreprocessorForBowPP();
            if (m_preprocessor == null) {
                throw new IllegalStateException("Preprocessing can not be applied on a bag of words.");
            }
        }

        // preprocessor needs to check if it can work on data table with given spec.
        m_preprocessor.validateDataTableSpec(inSpecs[0]);

        // preprocessor needs to check if it can work with specified settings
        m_preprocessor.validateSettings(m_documentColIndex, m_origDocumentColIndex, m_termColIndex,
            m_deepPreproModel.getBooleanValue(), m_appendIncomingModel.getBooleanValue(),
            m_preproUnModifiable.getBooleanValue());

        return new DataTableSpec[]{m_preprocessor.createDataTableSpec(m_appendIncomingModel.getBooleanValue())};
    }

    /**
     * This method is empty and called by
     * {@link PreprocessingNodeModel#configure(DataTableSpec[])}.
     * It can be overwritten if additional checks during the configure
     * procedure have to be done. The data table specs can not be chanced only
     * additional parameter checks can be applied.
     * @param inSpecs The input data table specs.
     * @throws InvalidSettingsException Is thrown if specified settings are
     * invalid.
     * @since 2.6
     */
    protected void internalConfigure(final DataTableSpec[] inSpecs)
    throws InvalidSettingsException {
        /* empty method, can be used to override and thus apply additional
         * checks.
         */
    }

    /**
     * Initializes the <code>Preprocessing</code> instance.
     */
    protected abstract void initPreprocessing();

    /**
     * This method is empty and called by
     * {@link PreprocessingNodeModel#execute(BufferedDataTable[], ExecutionContext)}.
     * It can be overwritten if additional computation during the execute have
     * to be done. This method is called before the regular preprocessing
     * computation is done and before the preprocessor is initialized. It can
     * also be used to initialize the preprocessor instead of using
     * {@link PreprocessingNodeModel#initPreprocessing()}.
     * @param inData The input data tables.
     * @param exec The execution context.
     * @throws Exception If something goes terribly wrong
     * @since 2.6
     */
    protected void internalExecute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        /* empty method, can be used to override and thus apply additional
         * checks and computation.
         */
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected final BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        // search indices of document and original document columns.
        checkDataTableSpec(inData[0].getDataTableSpec());

        // internal execute
        internalExecute(inData, exec);

        // initialize the underlying preprocessing
        initPreprocessing();
        if (m_preprocessing == null) {
            throw new IllegalStateException("Preprocessing instance has not been initialized!");
        }

        // initialize the underlying preprocessor
        if (m_preprocessor == null) {
            throw new IllegalStateException("Preprocessor instance has not been initialized!");
        }
        m_preprocessor.initialize(m_documentColIndex, m_origDocumentColIndex, m_termColIndex,
            m_deepPreproModel.getBooleanValue(), m_appendIncomingModel.getBooleanValue(),
            m_preproUnModifiable.getBooleanValue(), m_preprocessing);

        return new BufferedDataTable[]{m_preprocessor.doPreprocessing(inData[0], exec)};
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

/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * -------------------------------------------------------------------
 *
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.ext.textprocessing.util.BagOfWordsDataTableBuilder;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * The <code>AbstractPreprocessor</code> provides the basic fields and members of preprocessor classes. Since they all
 * work on bag of words data tables it contains the index of term and document columns, flags defining whether deep
 * preprocessing has to be applied or not or unmodifiable terms have to be preprocessed as well. Additionally term and
 * document cell factories, a data container and execution monitor is provided.
 *
 * To implement another preprocessor strategy, the method
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.AbstractPreprocessor#checkPreprocessing()} needs to be
 * implemented in order to check the type of the preprocessing instance to use. Additionally the method
 * {@link org.knime.ext.textprocessing.nodes.preprocessing.AbstractPreprocessor#applyPreprocessing(BufferedDataTable, ExecutionContext)}
 * needs to be implemented in which the preprocessor strategy has to be specified.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated use {@link StreamablePreprocessingNodeModel} instead.
 *
 */
@Deprecated
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
     * The document cell factory.
     */
    protected TextContainerDataCellFactory m_docCellFac =
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();

    /**
     * The term cell factory.
     */
    protected TextContainerDataCellFactory m_termCellFac =
            TextContainerDataCellFactoryBuilder.createTermCellFactory();

    /**
     * The bag of words cell factory.
     */
    protected BagOfWordsDataTableBuilder m_fac = new BagOfWordsDataTableBuilder();

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
            throw new InvalidSettingsException("Preprocessing type may not be null!");
        }
        m_preprocessing = prepro;

        // concrete preprocessor implementations need to check if they can work properly with the specified settings.
        validateSettings(documentColIndex, origDocumentColIndex, termColIndex, deepPrepro, appendOrigDoc,
            preproUnmodifiable);

        // concrete preprocessor implementations need to check if the provided preprocessing strategy can be applied.
        checkPreprocessing();

        m_documentColIndex = documentColIndex;
        m_origDocumentColIndex = origDocumentColIndex;
        m_termColIndex = termColIndex;
        m_deepPreprocessing = deepPrepro;
        m_appendIncomingDocument = appendOrigDoc;
        m_preprocessUnmodifiable = preproUnmodifiable;

        m_isInitialized = true;
    }

    /**
     * Creates the spec of the data table created by
     * {@link AbstractPreprocessor#doPreprocessing(BufferedDataTable, ExecutionContext)}.
     *
     * @param appendIncomingDocument If {@code true} the original incoming document will be appended.
     * @return The spec of the data table to create.
     * @since 2.9
     */
    public abstract DataTableSpec createDataTableSpec(final boolean appendIncomingDocument);

    /**
     * Checks if preprocessor can work on table with given spec. If preprocessor is not suitable for a data table with
     * given spec an InvalidSettingsException is thrown.
     *
     * @param spec The spec of the data table to check.
     * @throws InvalidSettingsException If preprocessor cannot work a data table with spec to check.
     * @since 2.9
     */
    public abstract void validateDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException;

    /**
     * Validates given preprocessor settings and throws InvalidSettingsException if settings are invalid or cannot
     * be applied to concrete preprocessor.
     *
     * @param documentColIndex The index of the document column.
     * @param origDocumentColIndex The index of the original document column.
     * @param termColIndex The index of the term column.
     * @param deepPrepro If {@code true} deep preprocessing will be applied.
     * @param appendOrigDoc If {@code true} original document will be appended.
     * @param preproUnmodifiable If {@code true} unmodifiable terms will be preprocessed.
     * @throws InvalidSettingsException if settings are invalid or cannot be applied on concrete preprocessor.
     * @since 2.9
     */
    public abstract void validateSettings(final int documentColIndex, final int origDocumentColIndex,
            final int termColIndex, final boolean deepPrepro, final boolean appendOrigDoc,
            final boolean preproUnmodifiable) throws InvalidSettingsException;

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

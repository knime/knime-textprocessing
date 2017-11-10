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
 *   May 3, 2016 (hermann): created
 */

package org.knime.ext.textprocessing.nodes.preprocessing.dictionaryfilter;

import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamablePreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter.StopWordFilter;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * This is the model implementation of DictionaryFilter.
 *
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class DictionaryFilterNodeModel extends StreamablePreprocessingNodeModel {

    private final SettingsModelString m_wordToFilterModel = DictionaryFilterNodeDialog.getColumnToReadModel();

    private SettingsModelBoolean m_caseModel = DictionaryFilterNodeDialog.getCaseSensitiveModel();

    /** The default setting for the use of case sensitivity. */
    public static final boolean DEF_CASE_SENSITIVE = false;

    private Set<String> m_terms;

    /**
     * Constructor of {@link DictionaryFilterNodeModel}.
     */
    public DictionaryFilterNodeModel() {
        super(2, new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});
        m_terms = new HashSet<String>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[1]);
    }

    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumStringCells(1, true);

        // set and verify column selection and set warning message if present
        ColumnSelectionVerifier.verifyColumn(m_wordToFilterModel, spec, StringValue.class, null)
            .ifPresent(a -> setWarningMessage(a));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preparePreprocessing(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws InvalidSettingsException {
        checkDataTableSpec(inData[1].getDataTableSpec());
        final int colIndex = inData[1].getDataTableSpec().findColumnIndex(m_wordToFilterModel.getStringValue());
        m_terms = new HashSet<String>();
        RowIterator iterator = inData[1].iterator();
        while (iterator.hasNext()) {
            DataRow row = iterator.next();
            if (!row.getCell(colIndex).isMissing()) {
                String value = ((StringValue)row.getCell(colIndex)).toString();
                m_terms.add(value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {

        if (m_terms == null) {
            throw new IllegalStateException("Dictionary of words to filter may not be null!");
        }

        return new StopWordFilter(m_terms, m_caseModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        m_terms.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_wordToFilterModel.loadSettingsFrom(settings);
        m_caseModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_wordToFilterModel.saveSettingsTo(settings);
        m_caseModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_wordToFilterModel.validateSettings(settings);
        m_caseModel.validateSettings(settings);
    }

}

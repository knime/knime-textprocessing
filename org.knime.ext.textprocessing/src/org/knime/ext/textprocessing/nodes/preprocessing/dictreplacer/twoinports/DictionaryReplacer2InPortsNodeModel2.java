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
 *   12.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.twoinports;

import java.util.HashMap;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamablePreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.DictionaryReplacer;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class DictionaryReplacer2InPortsNodeModel2 extends StreamablePreprocessingNodeModel {

    private final SettingsModelString m_replaceColumModel =
        DictionaryReplacer2InPortsNodeDialog2.getReplaceColumnModel();

    private final SettingsModelString m_replacementColumModel =
        DictionaryReplacer2InPortsNodeDialog2.getReplacementColumnModel();

    private HashMap<String, String> m_replacementDict;

    private final SettingsModelString m_tokenizerModel = DictionaryReplacer2InPortsNodeDialog2.getTokenizerModel();

    /**
     * Constructor of {@link DictionaryReplacer2InPortsNodeModel2}.
     */
    public DictionaryReplacer2InPortsNodeModel2() {
        super(2, new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});
        m_replacementDict = new HashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preparePreprocessing(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws InvalidSettingsException {

        final int replaceColIndex = inData[1].getDataTableSpec().findColumnIndex(m_replaceColumModel.getStringValue());
        final int replacementColIndex =
            inData[1].getDataTableSpec().findColumnIndex(m_replacementColumModel.getStringValue());

        m_replacementDict = new HashMap<>();

        RowIterator it = inData[1].iterator();
        while (it.hasNext()) {
            DataRow row = it.next();
            if (!row.getCell(replaceColIndex).isMissing() && !row.getCell(replacementColIndex).isMissing()) {
                String key = ((StringValue)row.getCell(replaceColIndex)).toString();
                String value = ((StringValue)row.getCell(replacementColIndex)).toString();
                m_replacementDict.put(key, value);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(inSpecs[0]);
        DataTableSpecVerifier dictTableSpecVerifier = new DataTableSpecVerifier(inSpecs[1]);
        dataTableSpecVerifier.verifyMinimumDocumentCells(1, true);
        dictTableSpecVerifier.verifyMinimumStringCells(2, true);

        DataTableSpec dictTableSpec = inSpecs[1];

        // initialize search string and replacement string columns
        ColumnSelectionVerifier.verifyColumn(m_replaceColumModel, dictTableSpec, StringValue.class,
            m_replacementColumModel.getStringValue()).ifPresent(msg -> setWarningMessage(msg));
        ColumnSelectionVerifier.verifyColumn(m_replacementColumModel, dictTableSpec, StringValue.class,
            m_replaceColumModel.getStringValue()).ifPresent(msg -> setWarningMessage(msg));

        if (m_replacementColumModel.getStringValue().equals(m_replaceColumModel.getStringValue())) {
            throw new InvalidSettingsException("Lookup column cannot be replacement column at the same time. "
                + "Select different columns for lookup and replacement column.");
        }

        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizerModel.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizerModel.getStringValue());
        }
        if (!dataTableSpecVerifier.verifyTokenizer(m_tokenizerModel.getStringValue())) {
            setWarningMessage(dataTableSpecVerifier.getTokenizerWarningMsg());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {
        return new DictionaryReplacer(m_replacementDict, m_tokenizerModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        m_replacementDict.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_replaceColumModel.loadSettingsFrom(settings);
        m_replacementColumModel.loadSettingsFrom(settings);
        // only load if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_replaceColumModel.saveSettingsTo(settings);
        m_replacementColumModel.saveSettingsTo(settings);
        m_tokenizerModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_replaceColumModel.validateSettings(settings);
        m_replacementColumModel.validateSettings(settings);
        // only validate if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizerModel.getKey())) {
            m_tokenizerModel.validateSettings(settings);
        }
        String replaceColName =
            ((SettingsModelString)m_replaceColumModel.createCloneWithValidatedValue(settings)).getStringValue();
        String replacementColName =
            ((SettingsModelString)m_replacementColumModel.createCloneWithValidatedValue(settings)).getStringValue();
        if (replaceColName.equals(replacementColName)) {
            throw new InvalidSettingsException("Lookup column cannot be replacement column at the same time. "
                + "Select different columns for lookup and replacement column.");
        }
    }
}

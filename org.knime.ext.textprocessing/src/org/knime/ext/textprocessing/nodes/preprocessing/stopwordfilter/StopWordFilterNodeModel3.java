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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   02.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

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
import org.knime.core.node.port.PortType;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamablePreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;

/**
 * The {@code NodeModel} for the Stop Word Filter node. This node has two in ports. The first contains the data table to
 * filter and the second (optional) one contains a list of stop words. It is also possible to chose a built-in stop word
 * list in the node dialog.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
final class StopWordFilterNodeModel3 extends StreamablePreprocessingNodeModel {

    /** The default setting for the use of case sensitivity. */
    static final boolean DEF_CASE_SENSITIVE = false;

    /** The default setting for the usage of build in lists. */
    static final boolean DEF_USE_BUILTIN_LIST = true;

    /** The default setting for the usage of build in lists. */
    static final boolean DEF_USE_CUSTOM_LIST = false;

    private SettingsModelString m_colModel = StopWordFilterNodeDialog3.getStopWordColumnModel();

    private SettingsModelBoolean m_caseModel = StopWordFilterNodeDialog3.getCaseSensitiveModel();

    private SettingsModelBoolean m_useBuiltInListModel = StopWordFilterNodeDialog3.getUseBuiltInListModel();

    private SettingsModelString m_builtInListModel = StopWordFilterNodeDialog3.getBuiltInListModel();

    private SettingsModelBoolean m_useCustomListModel = StopWordFilterNodeDialog3.getUseCustomListModel();

    private Set<String> m_stopWords;

    private boolean m_hasStopWordInput = true;

    /**
     * Constructor of {@link StopWordFilterNodeModel3}.
     */
    StopWordFilterNodeModel3() {
        super(new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE_OPTIONAL},
            new PortType[]{BufferedDataTable.TYPE}, new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {
        if (m_stopWords == null) {
            m_stopWords = new HashSet<String>();
        }
        return new StopWordFilter(m_stopWords, m_caseModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preparePreprocessing(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws InvalidSettingsException {

        m_stopWords = new HashSet<String>();

        if (m_useBuiltInListModel.getBooleanValue()) {
            Set<String> stopWordList =
                BuildInStopwordListFactory.getInstance().getStopwordListByName(m_builtInListModel.getStringValue());
            m_stopWords.addAll(stopWordList);
        }
        if (m_useCustomListModel.getBooleanValue()) {
            int colIndex = inData[1].getDataTableSpec().findColumnIndex(m_colModel.getStringValue());

            RowIterator iter = inData[1].iterator();
            while (iter.hasNext()) {
                DataRow row = iter.next();
                if (!row.getCell(colIndex).isMissing()) {
                    m_stopWords.add(((StringValue)row.getCell(colIndex)).getStringValue().trim());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        // check column selection
        if (inSpecs[1] != null) {
            if (inSpecs[1].getNumColumns() == 0) {
                setWarningMessage("Second input table does not contain any columns.");
            }
            ColumnSelectionVerifier.verifyColumn(m_colModel, inSpecs[1], StringValue.class, null)
                .ifPresent(msg -> setWarningMessage(msg));
        }

        // check if optional port has content
        if ((inSpecs[1] == null || !inSpecs[1].containsCompatibleType(StringValue.class))) {
            m_hasStopWordInput = false;
        } else {
            m_hasStopWordInput = true;
        }

        // throw exception if custom list model should be used, but table is not connected anymore.
        if (!m_hasStopWordInput && m_useCustomListModel.getBooleanValue()) {
            throw new InvalidSettingsException("Second input port disconnected. Please reconfigure.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_stopWords.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_colModel.loadSettingsFrom(settings);
        m_caseModel.loadSettingsFrom(settings);
        m_useBuiltInListModel.loadSettingsFrom(settings);
        m_builtInListModel.loadSettingsFrom(settings);
        m_useCustomListModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_colModel.saveSettingsTo(settings);
        m_caseModel.saveSettingsTo(settings);
        m_useBuiltInListModel.saveSettingsTo(settings);
        m_builtInListModel.saveSettingsTo(settings);
        m_useCustomListModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_colModel.validateSettings(settings);
        m_caseModel.validateSettings(settings);
        m_useBuiltInListModel.validateSettings(settings);
        m_builtInListModel.validateSettings(settings);
        m_useCustomListModel.validateSettings(settings);
    }
}

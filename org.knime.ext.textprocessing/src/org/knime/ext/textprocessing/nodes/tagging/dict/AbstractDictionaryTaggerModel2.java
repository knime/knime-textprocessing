/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 * Created on 04.05.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.nodes.tagging.dict;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.StreamableTaggerNodeModel2;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * This abstract class provides common functionality for {@code DictionaryTaggerNodeModel2} and
 * {@code WildcardTaggerNodeModel2}.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 3.5
 */
public abstract class AbstractDictionaryTaggerModel2 extends StreamableTaggerNodeModel2 {

    /**
     * The default value of the terms unmodifiable flag.
     */
    static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default value of the case sensitive setting.
     */
    static final boolean DEFAULT_CASE_SENSITIVE = true;

    /**
     * The default value of the default tag.
     */
    static final String DEFAULT_TAG = NamedEntityTag.UNKNOWN.getTag().getTagValue();

    /**
     * The default tag type.
     */
    static final String DEFAULT_TAG_TYPE = "NE";

    /**
     * Default dictionary table index.
     */
    public static final int DICT_TABLE_INDEX = 1;

    /**
     * Default document table index.
     */
    public static final int DATA_TABLE_INDEX = 0;

    private Set<String> m_dictionary;

    private SettingsModelBoolean m_setUnmodifiableModel =
        CommonDictionaryTaggerSettingModels.createSetUnmodifiableModel();

    private SettingsModelString m_tagModel = CommonDictionaryTaggerSettingModels.createTagModel();

    private SettingsModelString m_tagTypeModel = CommonDictionaryTaggerSettingModels.createTagTypeModel();

    private SettingsModelBoolean m_caseSensitiveModel = CommonDictionaryTaggerSettingModels.createCaseSensitiveModel();

    private SettingsModelString m_columnModel = CommonDictionaryTaggerSettingModels.createColumnModel();

    /**
     * Creates a new instance of {@code AbstractDictionaryTaggerModel2} with two table in ports and one out port.
     */
    public AbstractDictionaryTaggerModel2() {
        super(2, new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});
    }

    /**
     * Checks if spec of second input data table contains a string column that can be used as dictionary.
     *
     * @param inSpecs The specs of the input data tables.
     * @throws InvalidSettingsException If settings or specs of input data tables are invalid.
     */
    @Override
    protected final void checkInputDataTableSpecs(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpecVerifier verfier = new DataTableSpecVerifier(inSpecs[DICT_TABLE_INDEX]);
        verfier.verifyStringCell(true);
    }

    /**
     * Reads strings of string column of second input data table to build dictionary.
     *
     * @param inData Input data tables.
     * @param exec The execution context of the node.
     * @throws Exception If tagger cannot be prepared.
     */
    @Override
    protected final void prepareTagger(final BufferedDataTable[] inData, final ExecutionContext exec) throws Exception {
        // Read table with dictionary
        final int dictIndex =
            inData[DICT_TABLE_INDEX].getDataTableSpec().findColumnIndex(m_columnModel.getStringValue());
        m_dictionary = new LinkedHashSet<String>();
        final RowIterator it = inData[DICT_TABLE_INDEX].iterator();
        while (it.hasNext()) {
            final DataRow row = it.next();
            if (!row.getCell(dictIndex).isMissing()) {
                m_dictionary.add(((StringValue)row.getCell(dictIndex)).getStringValue());
            }
        }
    }

    /**
     * Creates a new instance of {@code DocumentTagger} with the specified settings. This tagger instance is used to tag
     * the documents of the input table.
     *
     * @param dictionary The dictionary to use for tagging.
     * @return The tagger instance to use for tagging.
     */
    protected abstract DocumentTagger createDocumentTagger(final Set<String> dictionary);

    /**
     * {@inheritDoc}
     */
    @Override
    public final DocumentTagger createTagger() throws Exception {
        return createDocumentTagger(m_dictionary);
    }

    /**
     * @return The unmodifiable setting.
     */
    protected boolean getUnmodifiableSetting() {
        return m_setUnmodifiableModel.getBooleanValue();
    }

    /**
     * @return The case sensitive setting.
     */
    protected boolean getCaseSensitiveSetting() {
        return m_caseSensitiveModel.getBooleanValue();
    }

    /**
     * @return The tag to set.
     */
    protected Tag getTagSetting() {
        String tagTypeStr = m_tagTypeModel.getStringValue();
        String tagStr = m_tagModel.getStringValue();
        return TagFactory.getInstance().getTagSetByType(tagTypeStr).buildTag(tagStr);
    }

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#saveSettingsTo(org.knime.core.node.NodeSettingsWO)
     */
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);

        m_caseSensitiveModel.saveSettingsTo(settings);
        m_tagModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);

        saveSettingsToInternal(settings);
    }

    /**
     * @param settings node model settings to save further settings to.
     */
    protected abstract void saveSettingsToInternal(final NodeSettingsWO settings);

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#validateSettings(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);

        m_caseSensitiveModel.validateSettings(settings);
        m_tagModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);

        validateSettingsInternal(settings);
    }

    /**
     * @param settings node model settings to validate further settings.
     * @throws InvalidSettingsException If further settings are invalid.
     */
    protected abstract void validateSettingsInternal(final NodeSettingsRO settings) throws InvalidSettingsException;

    /* (non-Javadoc)
     * @see org.knime.core.node.NodeModel#loadValidatedSettingsFrom(org.knime.core.node.NodeSettingsRO)
     */
    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);

        m_caseSensitiveModel.loadSettingsFrom(settings);
        m_tagModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_setUnmodifiableModel.loadSettingsFrom(settings);

        loadValidatedSettingsFromInternal(settings);
    }

    /**
     * @param settings node model settings to load further settings from.
     * @throws InvalidSettingsException If further settings are invalid.
     */
    protected abstract void loadValidatedSettingsFromInternal(final NodeSettingsRO settings)
        throws InvalidSettingsException;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

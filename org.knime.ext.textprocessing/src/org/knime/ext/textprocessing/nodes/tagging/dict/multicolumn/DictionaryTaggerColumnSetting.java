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
 *   Apr 11, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.dict.multicolumn;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.NamedEntityTag;

/**
 * Helper class that combines settings as to what should be happen with one column. That is one object of this
 * responsible for only one column!
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin Germany
 */
class DictionaryTaggerColumnSetting {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DictionaryTaggerColumnSetting.class);

    private static final boolean DEFAULT_CASE_SENSITIVITY = true;

    private static final boolean DEFAULT_EXACT_MATCH = true;

    private static final String DEFAULT_TAG_TYPE = "NE";

    private static final String DEFAULT_TAG_VALUE = NamedEntityTag.UNKNOWN.toString();

    private final String m_columnName;

    private boolean m_caseSensitivity;

    private boolean m_exactMatch;

    private String m_tagType;

    private String m_tagValue;

    DictionaryTaggerColumnSetting(final String colName) {
        m_columnName = colName;
        m_caseSensitivity = DEFAULT_CASE_SENSITIVITY;
        m_exactMatch = DEFAULT_EXACT_MATCH;
        m_tagType = DEFAULT_TAG_TYPE;
        m_tagValue = DEFAULT_TAG_VALUE;
    }

    boolean caseSensitive() {
        return m_caseSensitivity;
    }

    void setCaseSensitivity(final boolean caseSensitive) {
        m_caseSensitivity = caseSensitive;
    }

    boolean exactMatch() {
        return m_exactMatch;
    }

    void setExactMatch(final boolean exactMatch) {
        m_exactMatch = exactMatch;
    }

    String tagType() {
        return m_tagType;
    }

    void setTagType(final String tagType) {
        m_tagType = tagType;
    }

    String tagValue() {
        return m_tagValue;
    }

    void setTagValue(final String tagValue) {
        m_tagValue = tagValue;
    }

    String getColumnName() {
        return m_columnName;
    }

    void loadSettingsFrom(final NodeSettingsRO settings) {
        String columnName;
        try {
            columnName = settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_COLUMNNAME);
        } catch (InvalidSettingsException ise) {
            // this method is called from the dialog which inits "this" first
            // and immediately calls this method, name should (must) match
            LOGGER.warn("Can't safely update settings for column \"" + m_columnName + "\": No matching identifier.",
                ise);
            columnName = m_columnName;
        }

        if (!m_columnName.equals(columnName)) {
            LOGGER.warn("Can't update settings for column \"" + m_columnName + "\": got NodeSettings for \""
                + columnName + "\"");
        }

        try {
            setCaseSensitivity(settings.getBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_CASESENSITIVE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn("Can't update case sensitivity setting for column \"" + columnName
                + "\". Value has been set to default.");
            setCaseSensitivity(DEFAULT_CASE_SENSITIVITY);
        }

        try {
            setExactMatch(settings.getBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_EXACTMATCH));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update exact match setting for column \"" + columnName + "\". Value has been set to default.");
            setExactMatch(DEFAULT_EXACT_MATCH);
        }

        try {
            setTagType(settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGTYPE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update tag type setting for column \"" + columnName + "\". Value has been set to default.");
            setTagType(DEFAULT_TAG_TYPE);
        }

        try {
            setTagValue(settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGVALUE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update tag type setting for column \"" + columnName + "\". Value has been set to default.");
            setTagValue(DEFAULT_TAG_VALUE);
        }
    }

    public void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_COLUMNNAME, m_columnName);
        settings.addBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_CASESENSITIVE, m_caseSensitivity);
        settings.addBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_EXACTMATCH, m_exactMatch);
        settings.addString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGTYPE, m_tagType);
        settings.addString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGVALUE, m_tagValue);
    }

    public static DictionaryTaggerColumnSetting createFrom(final NodeSettingsRO settings)
        throws InvalidSettingsException {

        String colName = settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_COLUMNNAME);
        boolean caseSensitive = settings.getBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_CASESENSITIVE);
        boolean exactMatch = settings.getBoolean(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_EXACTMATCH);
        String tagType = settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGTYPE);
        String tagValue = settings.getString(DictionaryTaggerMultiColumnConfigKeys.CFGKEY_TAGVALUE);

        DictionaryTaggerColumnSetting result = new DictionaryTaggerColumnSetting(colName);
        result.setCaseSensitivity(caseSensitive);
        result.setExactMatch(exactMatch);
        result.setTagType(tagType);
        result.setTagValue(tagValue);
        return result;
    }

}

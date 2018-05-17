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
 *   Apr 18, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;

/**
 * The {@code DocumentTaggerConfiguration} class contains all properties a tagger instance needs to tag documents. This
 * class also provides getters and setters for the member variables, as well as helper methods for the loading and
 * saving processes of node settings.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.6
 */
public class DocumentTaggerConfiguration {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentTaggerConfiguration.class);

    /**
     * The default value for the case sensitivity flag.
     */
    private static final boolean DEFAULT_CASE_SENSITIVITY = true;

    /**
     * The default value for the exact match flag.
     */
    private static final boolean DEFAULT_EXACT_MATCH = true;

    /**
     * The default tag type.
     */
    private static final String DEFAULT_TAG_TYPE = NamedEntityTag.TAG_TYPE;

    /**
     * The default tag type.
     */
    private static final String DEFAULT_TAG_VALUE = NamedEntityTag.UNKNOWN.toString();

    private boolean m_caseSensitivity;

    private boolean m_exactMatch;

    private String m_tagType;

    private String m_tagValue;

    /**
     * Creates a new instance of {@code DocumentTaggerConfiguration} with a given column name. Sets default values for
     * case sensitivity, exact match, tag type and tag value.
     *
     * @param colName The column.
     */
    public DocumentTaggerConfiguration() {
        this(DEFAULT_CASE_SENSITIVITY, DEFAULT_EXACT_MATCH, DEFAULT_TAG_TYPE, DEFAULT_TAG_VALUE);
    }

    /**
     * Creates a new instance of {@code DocumentTaggerConfiguration} with a given column name and values for case
     * sensitivity, exact match, tag type and tag value.
     *
     * @param colName The column name.
     * @param caseSensitivity Boolean value for case sensitivity.
     * @param exactMatch Boolean value for exact matching behaviour.
     * @param tagType The tag type.
     * @param tagValue The tag value.
     *
     */
    public DocumentTaggerConfiguration(final boolean caseSensitivity, final boolean exactMatch,
        final String tagType, final String tagValue) {

        setCaseSensitivityOption(caseSensitivity);
        setExactMatchOption(exactMatch);
        setTagType(tagType);
        setTagValue(tagValue);
    }

    /**
     * Creates a new instance of {@code DocumentTaggerConfiguration} with a given column name, values for case
     * sensitivity, exact match and a given {@link Tag}.
     *
     * @param caseSensitivity
     * @param exactMatch
     * @param tag
     *
     */
    public DocumentTaggerConfiguration(final boolean caseSensitivity, final boolean exactMatch,
        final Tag tag) {
        this(caseSensitivity, exactMatch, tag.getTagType(), tag.getTagValue());
    }

    /**
     * @return The case sensitivity option.
     */
    public boolean getCaseSensitivityOption() {
        return m_caseSensitivity;
    }

    /**
     * @param caseSensitivity Set true for case sensitive matching.
     */
    public void setCaseSensitivityOption(final boolean caseSensitivity) {
        m_caseSensitivity = caseSensitivity;
    }

    /**
     * @return The exact match option.
     */
    public boolean getExactMatchOption() {
        return m_exactMatch;
    }

    /**
     * @param exactMatch Set true for exact matching.
     */
    public void setExactMatchOption(final boolean exactMatch) {
        m_exactMatch = exactMatch;
    }

    /**
     * @return The tag type.
     */
    public String getTagType() {
        return m_tagType;
    }

    /**
     * @param tagType The tag type to set.
     */
    public void setTagType(final String tagType) {
        m_tagType = tagType;
    }

    /**
     * @return The tag value.
     */
    public String getTagValue() {
        return m_tagValue;
    }

    /**
     * @param tagValue The tag value to set.
     */
    public void setTagValue(final String tagValue) {
        m_tagValue = tagValue;
    }

    /**
     * @return The tag build from tag type and tag value.
     */
    public Tag getTag() {
        return new Tag(m_tagValue, m_tagType);
    }

    /**
     * @param tag The tag that contains tag value and tag type information that will be set.
     */
    public void setTag(final Tag tag) {
        setTagType(tag.getTagType());
        setTagValue(tag.getTagValue());
    }

    /**
     * This methods sets all settings based on a {@link NodeSettingsRO} instance.
     *
     * @param settings The {@code NodeSettingsRO} instance containing {@code DocumentTaggerConfiguration} specific
     *            settings.
     */
    public void loadSettingsFrom(final NodeSettingsRO settings) {
        try {
            setCaseSensitivityOption(settings.getBoolean(MultiTaggerConfigKeys.CFGKEY_CASESENSITIVE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn("Can't update case sensitivity setting. Value has been set to default.");
            setCaseSensitivityOption(DEFAULT_CASE_SENSITIVITY);
        }

        try {
            setExactMatchOption(settings.getBoolean(MultiTaggerConfigKeys.CFGKEY_EXACTMATCH));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update exact match setting. Value has been set to default.");
            setExactMatchOption(DEFAULT_EXACT_MATCH);
        }

        try {
            setTagType(settings.getString(MultiTaggerConfigKeys.CFGKEY_TAGTYPE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update tag type setting. Value has been set to default.");
            setTagType(DEFAULT_TAG_TYPE);
        }

        try {
            setTagValue(settings.getString(MultiTaggerConfigKeys.CFGKEY_TAGVALUE));
        } catch (InvalidSettingsException e) {
            LOGGER.warn(
                "Can't update tag type setting. Value has been set to default.");
            setTagValue(DEFAULT_TAG_VALUE);
        }
    }

    /**
     * Saves the settings of the {@code DocumentTaggerConfiguration} to an instance of {@link NodeSettingsWO}.
     *
     * @param settings The {@code NodeSettingsWO} instance to save the settings to.
     */
    public void saveSettingsTo(final NodeSettingsWO settings) {
        settings.addBoolean(MultiTaggerConfigKeys.CFGKEY_CASESENSITIVE, m_caseSensitivity);
        settings.addBoolean(MultiTaggerConfigKeys.CFGKEY_EXACTMATCH, m_exactMatch);
        settings.addString(MultiTaggerConfigKeys.CFGKEY_TAGTYPE, m_tagType);
        settings.addString(MultiTaggerConfigKeys.CFGKEY_TAGVALUE, m_tagValue);
    }

    /**
     * Static method to create a {@code DocumentTaggerConfiguration} from an instance of {@link NodeSettingsRO}.
     *
     * @param settings The instance of {@code NodeSettingsRO} to create the {@code DocumentTaggerConfiguration} instance
     *            from.
     * @throws InvalidSettingsException If settings could not be retrieved.
     * @return Returns an instance of {@code DocumentTaggerConfiguration}.
     */
    public static DocumentTaggerConfiguration createFrom(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        boolean caseSensitive = settings.getBoolean(MultiTaggerConfigKeys.CFGKEY_CASESENSITIVE);
        boolean exactMatch = settings.getBoolean(MultiTaggerConfigKeys.CFGKEY_EXACTMATCH);
        String tagType = settings.getString(MultiTaggerConfigKeys.CFGKEY_TAGTYPE);
        String tagValue = settings.getString(MultiTaggerConfigKeys.CFGKEY_TAGVALUE);

        return new DocumentTaggerConfiguration(caseSensitive, exactMatch, tagType, tagValue);
    }

}

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
 *   12.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter2;

import static java.util.stream.Collectors.toSet;

import java.util.Set;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.tag.TagSet;
import org.knime.ext.textprocessing.data.tag.TagSets;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamableFunctionPreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public class TagFilterNodeModel2 extends StreamableFunctionPreprocessingNodeModel {

    /** The default value of the "strict filtering" setting. */
    public static final boolean DEF_STRICT = false;

    /** The default value of the "filter matching" setting. */
    public static final boolean DEF_FILTER_MATCHING = false;

    private final SettingsModelBoolean m_filterMatchingModel = TagFilterNodeDialog2.getFilterMatchingModel();

    private final SettingsModelBoolean m_strictFilteringModel = TagFilterNodeDialog2.getStrictFilteringModel();

    private final SettingsModelString m_tagTypeModel = TagFilterNodeDialog2.getTagTypeModel();

    private final SettingsModelStringArray m_validTagsModel = TagFilterNodeDialog2.getValidTagsModel();

    @Override
    protected TermPreprocessing createPreprocessing(final DataColumnSpec docCol) throws Exception {
        final var tagType = m_tagTypeModel.getStringValue();
        final var tagSet = TagSets.getTagSets(docCol).stream()//
            .filter(t -> tagType.equals(t.getType()))//
            .findFirst()//
            .orElseThrow(() -> new InvalidSettingsException(
                String.format("The selected tag type '%s' is not among the tagsets used in %s.", tagType, docCol)));
        final Set<String> validTagValues = Set.of(m_validTagsModel.getStringArrayValue());
        final Set<Tag> validTags = tagSet.getTags().stream()//
            .filter(t -> validTagValues.contains(t.getTagValue()))//
            .collect(toSet());
        return new TagFilter(validTags, tagSet.getType(), m_strictFilteringModel.getBooleanValue(),
            m_filterMatchingModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        var tableSpec = inSpecs[0];
        var docColumn = getDocumentColumn();
        var columnSpec = tableSpec.getColumnSpec(docColumn);
        CheckUtils.checkSettingNotNull(columnSpec, "The selectedColumn '%s' is missing from the input table.",
            docColumn);
        var selectedTagType = m_tagTypeModel.getStringValue();
        CheckUtils.checkSetting(
            TagSets.getTagSets(columnSpec).stream().map(TagSet::getType).anyMatch(selectedTagType::equals),
            "Selected tag type \"%s\" could not be found.\n"
            + "This might happend because it is either a dynamic TagSet that is undefined for the selected column '%s'\n"
            + "or it is a TagSet defined by a missing language extension!\n"
            + "Install additional language extensions at File->Install KNIME Extensions.", selectedTagType, docColumn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_strictFilteringModel.loadSettingsFrom(settings);
        m_validTagsModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        m_filterMatchingModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_strictFilteringModel.saveSettingsTo(settings);
        m_validTagsModel.saveSettingsTo(settings);
        m_filterMatchingModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_strictFilteringModel.validateSettings(settings);
        m_validTagsModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
        m_filterMatchingModel.validateSettings(settings);
    }
}

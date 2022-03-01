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

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ListSelectionModel;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.tag.TagSet;
import org.knime.ext.textprocessing.data.tag.TagSets;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public class TagFilterNodeDialog2 extends PreprocessingNodeSettingsPane2 {

    /**
     * @return Creates and returns a new instance of {@link SettingsModelBoolean} which specifies if strict filtering is
     *         truned on or off.
     */
    public static SettingsModelBoolean getStrictFilteringModel() {
        return new SettingsModelBoolean(TagFilterConfigKeys2.CFGKEY_STRICT, TagFilterNodeModel2.DEF_STRICT);
    }

    /**
     * @return Creates and returns a new instance of {@link SettingsModelBoolean} which specifies if terms that have the
     *         selected tags should be filtered or not.
     */
    public static SettingsModelBoolean getFilterMatchingModel() {
        return new SettingsModelBoolean(TagFilterConfigKeys2.CFGKEY_FILTER_MATCHING,
            TagFilterNodeModel2.DEF_FILTER_MATCHING);
    }

    /**
     * @return Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the
     *         tag type to assign to each found named entity.
     */
    public static final SettingsModelString getTagTypeModel() {
        return new SettingsModelString(TagFilterConfigKeys2.CFGKEY_TAGTYPE, NamedEntityTag.TAG_TYPE);
    }

    /**
     * @return Creates and returns a new instance of {@link SettingsModelStringArray} which contains the set of
     *         specified valid tags.
     */
    public static SettingsModelStringArray getValidTagsModel() {
        return new SettingsModelStringArray(TagFilterConfigKeys2.CFGKEY_VALIDTAGS, new String[]{});
    }

    private final SettingsModelString m_tagtypemodel;

    private final DialogComponentStringListSelection m_tagValuesSelectionList;

    private final DialogComponentStringSelection m_tagTypes;

    private DataTableSpec m_lastTableSpec;

    private Map<String, TagSet> m_currentTagSets = toTagSetMap(TagSets.getInstalledTagSets());

    /**
     * Creates a new instance of {@link TagFilterNodeDialog2}
     */
    public TagFilterNodeDialog2() {
        super();

        createNewTab("Tag filter options");
        setSelected("Tag filter options");

        addDialogComponent(new DialogComponentBoolean(getStrictFilteringModel(), "Strict filtering"));
        addDialogComponent(new DialogComponentBoolean(getFilterMatchingModel(), "Filter matching"));

        m_tagtypemodel = getTagTypeModel();

        getDocumentColumnSettingsModel().addChangeListener(e -> updateTagModels());

        m_tagTypes = new DialogComponentStringSelection(m_tagtypemodel, "Tag type", m_currentTagSets.keySet());

        addDialogComponent(m_tagTypes);

        m_tagValuesSelectionList = new DialogComponentStringListSelection(getValidTagsModel(), "Tags", getTagList(),
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION, true, 10);

        m_tagtypemodel.addChangeListener(e -> m_tagValuesSelectionList.replaceListItems(getTagList(), ""));
        addDialogComponent(m_tagValuesSelectionList);
    }

    private static Map<String, TagSet> toTagSetMap(final Set<TagSet> tagSets) {
        return tagSets.stream().collect(Collectors.toMap(TagSet::getType, Function.identity()));
    }

    private boolean updateTagModels() {
        if (m_lastTableSpec != null) {
            var columnName = getDocumentColumnSettingsModel().getStringValue();
            var column = m_lastTableSpec.getColumnSpec(columnName);
            var tagSets = TagSets.getTagSets(column);
            var newTagSets = toTagSetMap(tagSets);
            boolean tagSetsChanged = !m_currentTagSets.equals(newTagSets);
            m_currentTagSets = toTagSetMap(tagSets);
            var selectedType = m_tagtypemodel.getStringValue();
            var tagTypes = m_currentTagSets.keySet();
            final var newSelection = tagTypes.contains(selectedType) ? selectedType : tagTypes.iterator().next();
            m_tagTypes.replaceListItems(tagTypes, newSelection);
            m_tagtypemodel.setStringValue(newSelection);
            return tagSetsChanged;
        }
        return false;
    }

    private List<String> getTagList() {
        final var selectedTagType = m_tagtypemodel.getStringValue();
        final var tagSet = m_currentTagSets.get(selectedTagType);
        // might be null during load
        if (tagSet == null) {
            return List.of("");
        } else {
            return tagSet.getTags().stream()//
                .map(Tag::getTagValue)//
                .sorted()//
                .collect(toList());
        }
    }

    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final DataTableSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);
        m_lastTableSpec = specs[0];
        if (updateTagModels()) {
            // loads the settings again in case we have different tag sets available
            var upcastedSpecs = Stream.of(specs).toArray(PortObjectSpec[]::new);
            loadSettingsFrom(settings, upcastedSpecs);
        }
    }

}

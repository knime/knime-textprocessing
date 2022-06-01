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
 *   Feb 9, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.data.tag;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataType;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.data.TagFactory;

/**
 * Acts as facade to functionality revolving around {@link TagSet TagSets}. The difference to {@link TagFactory} is that
 * this class also allows access to dynamically registered tag sets that are attached to the meta data of a
 * {@link DataColumnSpec}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 * @since 4.6
 */
public final class TagSets {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TagSets.class);

    private static final Map<String, TagSet> INSTALLED_TAG_SETS = retrieveInstalledTagsets();

    private static Map<String, TagSet> retrieveInstalledTagsets() {
        try {
            return Collections.unmodifiableMap(TagFactory.getInstance().getTagSet().stream()//
                .collect(Collectors.toMap(TagBuilder::getType, InstalledTagSet::new)));
        } catch (Throwable t) {// NOSONAR any kind of exception would lead to a misleading NoClassDefFoundException
            LOGGER.error("Failed to retrieve the installed tag sets.", t);
            return Map.of();
        }
    }

    /**
     * Retrieves the installed TagSets as well as any dynamic TagSets that are stored as meta data in columnSpec.
     *
     * @param columnSpec to retrieve TagSets from
     * @return the TagSets in the meta data of columnSpec
     */
    public static Set<TagSet> getTagSets(final DataColumnSpec columnSpec) {
        return Stream.concat(//
            INSTALLED_TAG_SETS.values().stream(), //
            extractDynamicTagSets(columnSpec))//
            .collect(toCollection(LinkedHashSet::new));
    }

    /**
     * Retrieves the dynamic TagSets that are stored as meta data in columnSpec.
     *
     * @param columnSpec to retrieve dynamic TagSets from
     * @return the dynamic TagSets in the meta data of columnSpec
     */
    public static Set<TagSet> getDynamicTagSets(final DataColumnSpec columnSpec) {
        return extractDynamicTagSets(columnSpec).collect(toCollection(LinkedHashSet::new));
    }

    private static Stream<TagSet> extractDynamicTagSets(final DataColumnSpec columnSpec) {
        return columnSpec.getMetaDataOfType(TaggedValueMetaData.class).stream()//
                .map(TaggedValueMetaData::getTagSets)//
                .flatMap(Set::stream);
    }

    /**
     * @param type of the TagSet
     * @return the TagSet registered for type
     */
    public static Optional<TagSet> getTagSet(final String type) {
        return Optional.ofNullable(INSTALLED_TAG_SETS.get(type));
    }

    static boolean isInstalledTagSet(final String type) {
        return INSTALLED_TAG_SETS.containsKey(type);
    }

    /**
     * @return the TagSets registered at the extension point
     */
    public static Set<TagSet> getInstalledTagSets() {
        return Set.copyOf(INSTALLED_TAG_SETS.values());
    }

    /**
     * Adds the provided tagSets to the meta data in columnSpecCreator.
     *
     * @param columnSpecCreator to add meta data to
     * @param tagBuilders to add
     * @throws IllegalArgumentException if the type of columnSpecCreator is not a {@link TaggedValue} type
     */
    public static void addTagBuildersToMetaData(final DataColumnSpecCreator columnSpecCreator,
        final Set<TagBuilder> tagBuilders) {
        addTagSetsToMetaData(columnSpecCreator, toTagSets(tagBuilders));
    }

    private static Set<TagSet> toTagSets(final Set<TagBuilder> tagSets) {
        return tagSets.stream()//
            .map(TagSets::toTagSet)//
            .collect(toSet());
    }

    private static TagSet toTagSet(final TagBuilder tagBuilder) {
        return getTagSet(tagBuilder.getType()).orElseGet(() -> SnapshotTagSet.fromBuilder(tagBuilder));
    }

    private static void checkTypeIsTaggedValue(final DataColumnSpecCreator columnSpecCreator) {
        final var type = columnSpecCreator.getType();
        CheckUtils.checkArgument(isTaggedValue(type),
            "The provided DataColumnSpecCreator of type '%s' is not compatible with TaggedValue.", type);
    }

    private static void checkTypeIsTaggedValue(final DataColumnSpec columnSpec) {
        CheckUtils.checkArgument(isTaggedValue(columnSpec.getType()),
            "The provided column %s is not compatible with TaggedValue.", columnSpec);
    }

    /**
     * Convenience method that creates a copy of the provided {@link DataColumnSpec} containing the TagSets
     * corresponding to the provided {@link TagBuilder TagBuilders} in its meta data.
     *
     * @param columnSpec to copy and add tagSets to
     * @param tagBuilders to add
     * @return a copy of columnSpec whose meta data also contains the provided TagSets
     * @throws IllegalArgumentException if columnSpec is not of a {@link TaggedValue} type
     */
    public static DataColumnSpec addTagBuildersToMetaData(final DataColumnSpec columnSpec,
        final Set<TagBuilder> tagBuilders) {
        return addTagSetsToMetaData(columnSpec, toTagSets(tagBuilders));
    }

    /**
     * Adds the provided {@link TagSet TagSets} to the meta data of the provided {@link DataColumnSpecCreator}.
     *
     * @param columnSpecCreator to add tagSets to
     * @param tagSets to add
     * @throws IllegalArgumentException if the type of columnSpecCreator is not a {@link TaggedValue} type
     */
    public static void addTagSetsToMetaData(final DataColumnSpecCreator columnSpecCreator, final Set<TagSet> tagSets) {
        checkTypeIsTaggedValue(columnSpecCreator);
        checkForTypeDuplicates(tagSets.stream().map(TagSet::getType));
        addTagSetsToCreator(columnSpecCreator, tagSets);
    }

    private static void addTagSetsToCreator(final DataColumnSpecCreator columnSpecCreator, final Set<TagSet> tagSets) {
        var dynamicTagSets = tagSets.stream()//
            .filter(SnapshotTagSet.class::isInstance)//
            .map(SnapshotTagSet.class::cast)//
            .collect(toCollection(LinkedHashSet::new));
        columnSpecCreator.addMetaData(new TaggedValueMetaData(dynamicTagSets), false);
    }

    /**
     * Convenience method that creates a copy of the provided {@link DataColumnSpec} containing the provided
     * {@link TagSet TagSets} in its meta data.
     *
     * @param columnSpec to copy and add tagSets to
     * @param tagSets to add
     * @return a copy of columnSpec whose meta data also contains the provided TagSets
     * @throws IllegalArgumentException if columnSpec is not of a {@link TaggedValue} type
     */
    public static DataColumnSpec addTagSetsToMetaData(final DataColumnSpec columnSpec, final Set<TagSet> tagSets) {
        checkTypeIsTaggedValue(columnSpec);
        checkForTypeDuplicates(tagSets.stream().map(TagSet::getType));
        var colCreator = new DataColumnSpecCreator(columnSpec);
        addTagSetsToCreator(colCreator, tagSets);
        return colCreator.createSpec();
    }

    private static void checkForTypeDuplicates(final Stream<String> types) {
        var uniqueTypes = new HashSet<String>();
        for (var iterator = types.iterator(); iterator.hasNext();) {
            var type = iterator.next();
            CheckUtils.checkArgument(uniqueTypes.add(type),
                "Among the provided tag sets are at least two with the same type '%s'.");
        }
    }

    private static boolean isTaggedValue(final DataType type) {
        return type.isCompatible(TaggedValue.class);
    }

    private TagSets() {
    }
}

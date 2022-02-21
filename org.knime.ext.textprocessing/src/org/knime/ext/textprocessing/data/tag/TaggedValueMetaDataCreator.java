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
 *   Feb 8, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.data.tag;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.meta.DataColumnMetaDataCreator;
import org.knime.ext.textprocessing.data.Tag;

/**
 * Used to create TaggedValueMetaData from data or merge multiple TaggedValueMetaData objects.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class TaggedValueMetaDataCreator implements DataColumnMetaDataCreator<TaggedValueMetaData> {

    private final Map<String, TagSetBuilder> m_tagSetBuilders = new LinkedHashMap<>();

    @Override
    public Class<TaggedValueMetaData> getMetaDataClass() {
        return TaggedValueMetaData.class;
    }

    @Override
    public void update(final DataCell cell) {
        if (!cell.isMissing()) {
            updateTagTypes((TaggedValue)cell);
        }
    }

    private void updateTagTypes(final TaggedValue value) {
        value.getTagStream().forEach(this::consumeTag);
    }

    private void consumeTag(final Tag tag) {
        final var tagType = tag.getTagType();
        if (!TagSets.isInstalledTagSet(tagType)) {
            m_tagSetBuilders.computeIfAbsent(tagType, TagSetBuilder::new).addTag(tag);
        }
    }

    @Override
    public TaggedValueMetaData create() {
        return new TaggedValueMetaData(getCurrentTagSets());
    }

    private Set<SnapshotTagSet> getCurrentTagSets() {
        return m_tagSetBuilders.values().stream()//
            .map(TagSetBuilder::build)//
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public TaggedValueMetaDataCreator copy() {
        var copy = new TaggedValueMetaDataCreator();
        m_tagSetBuilders.entrySet()
            .forEach(e -> copy.m_tagSetBuilders.put(e.getKey(), e.getValue().copy()));
        return copy;
    }

    @Override
    public TaggedValueMetaDataCreator merge(final TaggedValueMetaData other) {
        other.getTagSets()
            .forEach(t -> m_tagSetBuilders.computeIfAbsent(t.getType(), TagSetBuilder::new).addAll(t));
        return this;
    }

    @Override
    public TaggedValueMetaDataCreator merge(final DataColumnMetaDataCreator<TaggedValueMetaData> other) {
        if (other instanceof TaggedValueMetaDataCreator) {
            mergeOther((TaggedValueMetaDataCreator)other);
            return this;
        } else {
            return merge(other.create());
        }
    }

    private void mergeOther(final TaggedValueMetaDataCreator other) {
        other.m_tagSetBuilders
            .forEach((k, v) -> m_tagSetBuilders.merge(k, v, TagSetBuilder::merge));
    }

    private static final class TagSetBuilder {

        static TagSetBuilder merge(final TagSetBuilder left, final TagSetBuilder right) {
            var result = new TagSetBuilder(left.m_type);
            result.m_tags.addAll(left.m_tags);
            result.m_tags.addAll(right.m_tags);
            return result;
        }

        private final String m_type;

        private final Set<String> m_tags = new LinkedHashSet<>();

        TagSetBuilder(final String type) {
            m_type = type;
        }

        void addTag(final Tag tag) {
            m_tags.add(tag.getTagValue());
        }

        void addAll(final SnapshotTagSet tagSet) {
            assert m_type.equals(tagSet.getType()) : "The TagSet types don't match: " + m_type + " vs. "
                + tagSet.getType();
            m_tags.addAll(tagSet.asStringList());
        }

        SnapshotTagSet build() {
            return SnapshotTagSet.fromStrings(m_type, m_tags.toArray(String[]::new));
        }

        TagSetBuilder copy() {
            var copy = new TagSetBuilder(m_type);
            copy.m_tags.addAll(m_tags);
            return copy;
        }
    }

}

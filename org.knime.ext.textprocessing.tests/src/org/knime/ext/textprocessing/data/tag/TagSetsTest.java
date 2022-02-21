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
 *   Feb 17, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.data.tag;

import static java.util.stream.Collectors.toCollection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Test;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

import com.google.common.collect.Sets;

/**
 * Provides unit tests for the {@link TagSets} class.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("javadoc")
public class TagSetsTest {

    @Test
    public void testAddTagSetsToColumn() {
        var builder = mockTagBuilder("foobar", "foo", "bar");
        var colWithMeta = columnSpecWithDynamicTagSets("col", builder);
        var meta = colWithMeta.getMetaDataOfType(TaggedValueMetaData.class);
        var tagSet = SnapshotTagSet.fromStrings("foobar", "foo", "bar");
        assertThat(meta).isPresent();
        var dynamicTagSets = meta.get().getTagSets();
        assertThat(dynamicTagSets).containsExactly(tagSet);
    }

    @Test
    public void testAddTagSetsToColumnCreator() {
        var creator = new DataColumnSpecCreator("col", DocumentCell.TYPE);
        var builder = mockTagBuilder("barz", "bar", "baz");
        TagSets.addTagBuildersToMetaData(creator, Set.of(builder));
        var col = creator.createSpec();
        var meta = col.getMetaDataOfType(TaggedValueMetaData.class);
        var tagSet = SnapshotTagSet.fromStrings("barz", "bar", "baz");
        assertThat(meta).isPresent();
        assertThat(meta.get().getTagSets()).containsExactly(tagSet);
    }

    private static DataColumnSpec columnSpecWithDynamicTagSets(final String columnName, final TagBuilder... builders) {
        var col = new DataColumnSpecCreator(columnName, DocumentCell.TYPE).createSpec();
        return TagSets.addTagBuildersToMetaData(col, Set.of(builders));
    }

    @Test
    public void testAddAndGetTagSetsFromMetaData() throws Exception {
        var builder = mockTagBuilder("barfoo", "bar", "foo");
        var colWithMeta = columnSpecWithDynamicTagSets("col", builder);
        var dynamicTagSet = SnapshotTagSet.fromStrings("barfoo", "bar", "foo");
        var tagSets = TagSets.getTagSets(colWithMeta);
        var expected = Sets.union(Set.of(dynamicTagSet), TagSets.getInstalledTagSets());
        assertThat(tagSets).containsExactlyInAnyOrder(expected.toArray(TagSet[]::new));
    }

    @Test
    public void testNameConflictsInUserProvidedTagSets() throws Exception {
        var first = SnapshotTagSet.fromStrings("foobar", "foo");
        var second = SnapshotTagSet.fromStrings("foobar", "bar");
        var col = new DataColumnSpecCreator("col", DocumentCell.TYPE).createSpec();
        assertThrows(IllegalArgumentException.class, () -> TagSets.addTagSetsToMetaData(col, Set.of(first, second)));
    }

    private static TagBuilder mockTagBuilder(final String name, final String ... tags) {
        var builder = mock(TagBuilder.class);
        when(builder.getType()).thenReturn(name);
        var asTags = Stream.of(tags)
                .map(t -> new Tag(t, name))
                .collect(toCollection(LinkedHashSet::new));
        when(builder.getTags()).thenReturn(asTags);
        return builder;
    }

}

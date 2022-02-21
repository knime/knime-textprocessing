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
 *   Feb 14, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.data.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.core.data.DataCell;
import org.knime.core.data.MissingCell;
import org.knime.ext.textprocessing.data.PartOfSpeechTag;
import org.knime.ext.textprocessing.data.Tag;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Contains unit tests for TaggedValueMetaDataCreator.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@SuppressWarnings("javadoc")
@RunWith(MockitoJUnitRunner.class)
public final class TaggedValueMetaDataCreatorTest {

    private static final SnapshotTagSet FOOBAR = SnapshotTagSet.fromStrings("foobar", "foo", "bar");

    private TaggedValueMetaDataCreator m_creator;

    @Before
    public void setup() {
        m_creator = new TaggedValueMetaDataCreator();
    }

    @Test
    public void testUpdateWithMissingValue() {
        m_creator.update(new MissingCell(""));
        assertThat(m_creator.create())//
            .extracting(TaggedValueMetaData::getTagSets)//
            .matches(Set::isEmpty);
    }

    private static Tag tag(final String type, final String value) {
        return new Tag(value, type);
    }

    @Ignore // fails on the build system because TagFactory can't load the installed tag factories there
    @Test
    public void testCreatorIgnoresInstalledTagSets() throws Exception {
        m_creator.update(new MockTaggedCell(PartOfSpeechTag.DT.getTag()));
        assertThat(m_creator.create().getTagSets()).isEmpty();
    }

    @Test
    public void testUpdateWithNonMissingValueAndNoExistingTagSets() throws Exception {
        m_creator.update(new MockTaggedCell(tag("foobar", "foo")));
        final var tagSet1 = SnapshotTagSet.fromStrings("foobar", "foo");
        assertContains(m_creator, tagSet1);
        m_creator.update(new MockTaggedCell(tag("barbaz", "baz")));
        var tagSet2 = SnapshotTagSet.fromStrings("barbaz", "baz");
        assertContains(m_creator, tagSet1, tagSet2);
    }

    @Test
    public void testCopy() throws Exception {
        m_creator.merge(new TaggedValueMetaData(Set.of(FOOBAR)));
        assertContains(m_creator, FOOBAR);

        // the copy contains everything added to m_creator up to now
        final var copy = m_creator.copy();
        assertContains(copy, FOOBAR);

        // updating the copy doesn't change m_creator
        copy.update(new MockTaggedCell(tag("barfoo", "bar")));
        assertContains(m_creator, FOOBAR);
        var barfooTagSet = SnapshotTagSet.fromStrings("barfoo", "bar");
        assertContains(copy, FOOBAR, barfooTagSet);

        // updating m_creator doesn't change copy
        m_creator.update(new MockTaggedCell(tag("baz", "baz")));
        var bazTagSet = SnapshotTagSet.fromStrings("baz", "baz");
        assertContains(m_creator, FOOBAR, bazTagSet);
        assertContains(copy, FOOBAR, barfooTagSet);
    }

    @Test
    public void testMergeMetaData() {
        m_creator.update(new MockTaggedCell(tag("bla", "bla")));
        var blaTagSet = SnapshotTagSet.fromStrings("bla", "bla");
        var otherMetaData = new TaggedValueMetaData(Set.of(FOOBAR));
        m_creator.merge(otherMetaData);
        assertContains(m_creator, blaTagSet, FOOBAR);
    }

    @Test
    public void testMergeCreator() {
        m_creator.update(new MockTaggedCell(tag("foo", "bar")));
        var otherCreator = new TaggedValueMetaDataCreator();
        otherCreator.update(new MockTaggedCell(tag("foo", "baz"), tag("bar", "bla")));
        var firstFooTagSet = SnapshotTagSet.fromStrings("foo", "bar");
        var secondFooTagSet = SnapshotTagSet.fromStrings("foo", "baz");
        var blaTagSet = SnapshotTagSet.fromStrings("bar", "bla");
        assertContains(m_creator, firstFooTagSet);
        assertContains(otherCreator, secondFooTagSet, blaTagSet);
        m_creator.merge(otherCreator);
        var mergedFooTagSet = SnapshotTagSet.fromStrings("foo", "bar", "baz");
        assertContains(m_creator, mergedFooTagSet, blaTagSet);
    }

    private static void assertContains(final TaggedValueMetaDataCreator creator, final SnapshotTagSet ... tagSets) {
        final var containedTagSets = creator.create().getTagSets();
        assertThat(containedTagSets).containsExactlyInAnyOrder(tagSets);
    }

    private static final class MockTaggedCell extends DataCell implements TaggedValue {

        private static final long serialVersionUID = 1L;

        private final Set<Tag> m_tagSets;

        MockTaggedCell(final Tag... tags) {
            m_tagSets = Set.of(tags);
        }

        @Override
        public Stream<Tag> getTagStream() {
            return m_tagSets.stream();
        }

        @Override
        public String toString() {
            return m_tagSets.toString();
        }

        @Override
        protected boolean equalsDataCell(final DataCell dc) {
            var other = (MockTaggedCell)dc;
            return m_tagSets.equals(other.m_tagSets);
        }

        @Override
        public int hashCode() {
            return m_tagSets.hashCode();
        }

    }

}

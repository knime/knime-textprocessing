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
 *   Feb 16, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.ext.textprocessing.data.tag;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagBuilder;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A {@link TagSet} that is a snapshot of a {@link TagBuilder} i.e. contains all of its tags at the moment that
 * SnapshotTagSet is created.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class SnapshotTagSet implements TagSet {

    private static final Cache<SnapshotTagSet, SnapshotTagSet> CACHE = CacheBuilder.newBuilder()//
        .weakKeys()//
        .weakValues()//
        .build();

    private final Set<Tag> m_tags;

    private final String m_type;

    private final int m_hashCode;

    private SnapshotTagSet(final String type, final Set<Tag> tags) {
        m_type = type;
        m_tags = Collections.unmodifiableSet(tags);
        m_hashCode = Objects.hashCode(type, tags);
    }

    static SnapshotTagSet fromBuilder(final TagBuilder builder) {
        return canonicalize(new SnapshotTagSet(builder.getType(), new HashSet<>(builder.getTags())));
    }

    private static SnapshotTagSet canonicalize(final SnapshotTagSet tagSet) {
        try {
            return CACHE.get(tagSet, () -> tagSet);
        } catch (ExecutionException e) {
            // unreachable because the supplier only returns the already existing object
            throw new IllegalStateException(
                String.format("Failed to update cache with tagset %s.", tagSet), e);
        }
    }

    static SnapshotTagSet fromStrings(final String type, final String... tags) {
        var tagSet = new SnapshotTagSet(type, Stream.of(tags)//
            .map(t -> new Tag(t, type))//
            .collect(toSet()));
        return canonicalize(tagSet);
    }

    @Override
    public List<String> asStringList() {
        return m_tags.stream()//
            .map(Tag::getTagValue)//
            .collect(toList());
    }

    @Override
    public Set<Tag> getTags() {
        return m_tags;
    }

    @Override
    public String getType() {
        return m_type;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof SnapshotTagSet) {
            var other = (SnapshotTagSet)obj;
            return m_type.equals(other.m_type) && m_tags.equals(other.m_tags);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return m_hashCode ;
    }

    @Override
    public String toString() {
        return String.format("{%s: %s}", m_type, m_tags);
    }

}

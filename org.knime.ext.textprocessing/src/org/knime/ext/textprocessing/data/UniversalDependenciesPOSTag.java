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
 *   Mar 26, 2018 (julian): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Julian Bunzel, KNIME AG, Berlin, Germany
 * @since 3.6
 */
public enum UniversalDependenciesPOSTag implements TagBuilder {

    /** Unknown type. */
    UNKNOWN,

    /** Adjective. */
    ADJ,
    /** Adposition. */
    ADP,
    /** Adverb. */
    ADV,
    /** Auxiliary. */
    AUX,
    /** Coordinating conjunction. */
    CCONJ,
    /** Determiner. */
    DET,
    /** Interjection. */
    INTJ,
    /** Noun. */
    NOUN,
    /** Numeral. */
    NUM,
    /** Particle. */
    PART,
    /** Pronoun. */
    PRON,
    /** Proper noun. */
    PROPN,
    /** Punctuation. */
    PUNCT,
    /** Subordinating conjunction. */
    SCONJ,
    /** Symbol. */
    SYM,
    /** Verb. */
    VERB,
    /** Other. */
    X;

    private final Tag m_tag;

    /**
     * The constant for POS tag types.
     */
    public static final String TAG_TYPE = "UDPOS";

    private UniversalDependenciesPOSTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified {@code UniversalDependenciesPOSTag}.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag buildTag(final String value) {
        return UniversalDependenciesPOSTag.stringToTag(value);
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the
     * given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * {@code UNKNOWN} tag is returned.
     *
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the
     *         given string.
     */
    private static Tag stringToTag(final String str) {
        for (UniversalDependenciesPOSTag pos : values()) {
            if (pos.getTag().getTagValue().equals(str)) {
                return pos.getTag();
            }
        }
        return PartOfSpeechTag.UNKNOWN.getTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> asStringList() {
        Enum<UniversalDependenciesPOSTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * @return The default "UNKNOWN" {@code UniversalDependenciesPOSTag} as
     *         {@code TagBuilder}.
     */
    public static TagBuilder getDefault() {
        return UniversalDependenciesPOSTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new LinkedHashSet<Tag>(values().length);
        for (UniversalDependenciesPOSTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TAG_TYPE;
    }

}

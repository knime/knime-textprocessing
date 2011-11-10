/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as 
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public enum NamedEntityTag implements TagBuilder {
    /** Unkown type. */
    UNKNOWN,
    /** A location. */
    LOCATION,
    /** A person. */
    PERSON,
    /** A organization. */
    ORGANIZATION,
    /** A date. */
    DATE,
    /** A time. */
    TIME,
    /** Money. */
    MONEY;

    private final Tag m_tag;

    /**
     * The constant for ABNER tag types.
     */
    public static final String TAG_TYPE = "NE";

    /**
     * Creates new instance of <code>NamedEntityTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified named
     * antity tag.
     */
    private NamedEntityTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     *         to the specified <code>NamedEntityTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the enum fields as a String list of their names.
     * 
     * @return - the enum fields as a String list of their names.
     */
    @Override
    public List<String> asStringList() {
        Enum<NamedEntityTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to the
     * given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * <code>UNKNOWN</code> tag is returned.
     * 
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to the
     *         given string.
     */
    public static Tag stringToTag(final String str) {
        for (NamedEntityTag ne : values()) {
            if (ne.getTag().getTagValue().equals(str)) {
                return ne.getTag();
            }
        }
        return NamedEntityTag.UNKNOWN.getTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tag buildTag(final String value) {
        return NamedEntityTag.stringToTag(value);
    }

    /**
     * @return The default "UNKNOWN" <code>NamedEntityTag</code> as
     *         <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return NamedEntityTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType() {
        return TAG_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Tag> getTags() {
        Set<Tag> tagSet = new HashSet<Tag>(values().length);
        for (NamedEntityTag tag : values()) {
            tagSet.add(tag.getTag());
        }
        return tagSet;
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 *   Aug 5, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;


/**
 * TagBuilder for multi-word terms.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public enum MultiwordTermTag implements TagBuilder {
    
    /** Unkown type. */
    UNKNOWN,
    /** n-gram. */
    MULTIWORDTERM;

    private final Tag m_tag;

    /**
     * The constant for multi word term  tags.
     */
    public static final String TAG_TYPE = "MWT";

    /**
     * Creates new instance of <code>MultiwordTermTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified
     * named antity tag.
     */
    private MultiwordTermTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     * to the specified <code>MultiwordTermTag</code>.
     */
    public Tag getTag() {
        return m_tag;
    }

    /**
     * Returns the enum fields as a String list of their names.
     *
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        Enum<MultiwordTermTag>[] values = values();
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.Tag} related to
     * the given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.Tag} is available the
     * <code>UNKNOWN</code> tag is returned.
     * @param str The string representing a
     * {@link org.knime.ext.textprocessing.data.Tag}.
     * @return The related {@link org.knime.ext.textprocessing.data.Tag} to
     * the given string.
     */
    public static Tag stringToTag(final String str) {
        for (MultiwordTermTag ne : values()) {
            if (ne.getTag().getTagValue().equals(str)) {
                return ne.getTag();
            }
        }
        return MultiwordTermTag.UNKNOWN.getTag();
    }

    /**
     * {@inheritDoc}
     */
    public Tag buildTag(final String type, final String value) {
        if (type.equals(TAG_TYPE)) {
            return MultiwordTermTag.stringToTag(value);
        }
        return null;
    }

    /**
     * @return The default "UNKNOWN" <code>NGramTag</code> as
     * <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return MultiwordTermTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TAG_TYPE;
    }
}

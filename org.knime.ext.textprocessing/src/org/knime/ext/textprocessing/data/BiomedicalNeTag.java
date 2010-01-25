/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   27.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public enum BiomedicalNeTag implements TagBuilder {

    /** Unkown type. */
    UNKNOWN,

    /** A Protein name. **/
    PROTEIN,
    /** DNA. **/
    DNA,
    /** RNA. **/
    RNA,
    /** A GENE name. **/
    GENE,
    /** A cell line name. **/
    CELL_LINE,
    /** A cell type name. **/
    CELL_TYPE;


    private final Tag m_tag;

    /**
     * The constant for ABNER tag types.
     */
    public static final String TAG_TYPE = "ABNER";

    /**
     * Creates new instance of <code>BiomedicalNeTag</code> and
     * {@link org.knime.ext.textprocessing.data.Tag} with the specified
     * ABNER tag.
     */
    private BiomedicalNeTag() {
        m_tag = new Tag(name(), TAG_TYPE);
    }

    /**
     * @return The {@link org.knime.ext.textprocessing.data.Tag} corresponding
     * to the specified <code>BiomedicalNeTag</code>.
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
        Enum<BiomedicalNeTag>[] values = values();
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
        for (BiomedicalNeTag ne : values()) {
            if (ne.getTag().getTagValue().equals(str)) {
                return ne.getTag();
            }
        }
        return BiomedicalNeTag.UNKNOWN.getTag();
    }

    /**
     * {@inheritDoc}
     */
    public Tag buildTag(final String type, final String value) {
        if (type.equals(TAG_TYPE)) {
            return BiomedicalNeTag.stringToTag(value);
        }
        return null;
    }

    /**
     * @return The default "UNKNOWN" <code>BiomedicalNeTag</code> as
     * <code>TagBuilder</code>.
     */
    public static TagBuilder getDefault() {
        return BiomedicalNeTag.UNKNOWN;
    }

    /**
     * {@inheritDoc}
     */
    public String getType() {
        return TAG_TYPE;
    }
}

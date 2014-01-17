/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public enum SectionAnnotation {

    /** The UNKNOWN annotation. **/
    UNKNOWN,
    /** The title of a document. **/
    TITLE,
    /** The abstract of a document. **/
    ABSTRACT,
    /** A chapter of a document. **/
    CHAPTER,
    /** The title of the journal the document was published at. **/
    JOURNAL_TITLE,
    /** The title of the conference the document was published at. **/
    CONFERENCE_TITLE,
    /**
     * Meta information of the document.
     * @since 2.7
     **/
    META_INFORMATION;

    /**
     * Returns the {@link org.knime.ext.textprocessing.data.SectionAnnotation}
     * related to the given string. If no corresponding
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} is available
     * the <code>UNKNOWN</code> annotation is returned.
     *
     * @param str The string representing a
     *            {@link org.knime.ext.textprocessing.data.SectionAnnotation}.
     * @return The related
     *         {@link org.knime.ext.textprocessing.data.SectionAnnotation} to
     *         the given string.
     */
    public static SectionAnnotation stringToAnnotation(final String str) {
        return valueOf(str);
    }

    /**
     * Returns the enum fields as a String list of their names.
     *
     * @return - the enum fields as a String list of their names.
     */
    public static List<String> asStringList() {
        final Enum<SectionAnnotation>[] values = values();
        final List<String> list = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            list.add(values[i].name());
        }
        return list;
    }
}

/*
 * ------------------------------------------------------------------------
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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

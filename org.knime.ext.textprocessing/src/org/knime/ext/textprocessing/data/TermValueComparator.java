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
 *   04.01.2007 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.Comparator;
import java.util.List;

import org.knime.core.data.DataValue;
import org.knime.core.data.DataValueComparator;

/**
 * Comparator returned by the
 * {@link org.knime.ext.textprocessing.data.TermValue} interface.
 *
 * @see org.knime.ext.textprocessing.data.TermValue#UTILITY
 * @see org.knime.ext.textprocessing.data.TermValue.TermUtilityFactory
 * @author Kilian Thiel, University of Konstanz
 */
public class TermValueComparator extends DataValueComparator {

    private static final Comparator<Tag> TAG_CMP = (t1, t2) -> {
        if (t1.equals(t2)) {
            return 0;
        }
        final var tt1 = t1.getTagType();
        final var tt2 = t2.getTagType();
        if (tt1.equals(tt2)) {
            return t1.getTagValue().compareTo(t2.getTagValue());
        }
        return tt1.compareTo(tt2);
    };

    private static final Comparator<List<Tag>> TAGS_CMP = (tags1, tags2) -> {
        if (tags1.size() != tags2.size()) {
            return tags1.size() - tags2.size();
        }
        for (var i = 0; i < tags1.size(); i++) {
            final int cmp = TAG_CMP.compare(tags1.get(i), tags2.get(i));
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    };

    /**
     * Compares two {@link org.knime.ext.textprocessing.data.TermValue}s based
     * on their words and tags.
     *
     * {@inheritDoc}
     */
    @Override
    protected int compareDataValues(final DataValue v1, final DataValue v2) {
        final var t1 = ((TermValue)v1).getTermValue();
        final var t2 = ((TermValue)v2).getTermValue();
        if (t1.equals(t2)) {
            return 0;
        }
        final var str1 = t1.getText();
        final var str2 = t2.getText();
        if (str1.equals(str2)) {
            return Comparator.comparing(Term::getTags, TAGS_CMP).compare(t1, t2);
        }
        return str1.compareTo(str2);
    }

}

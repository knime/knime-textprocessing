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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter2;

import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter.StopWordFilter;

/**
 * A tag filter, filtering terms with not specified tags.
 * See {@link StopWordFilter#preprocessTerm(Term)} for details to filter terms.
 * <code>TagFilter</code> implements <code>Preprocessing</code> and can be
 * used as a preprocessing step. The preprocessing method
 * {@link StopWordFilter#preprocessTerm(Term)} returns null if the term
 * was filtered out, an the given unmodified term if not.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFilter implements TermPreprocessing {

    private final Set<Tag> m_validTags;

    private final String m_validTagType;

    private final boolean m_strict;

    private final boolean m_filterMatching;

    /**
     * Creates a new instance of <code>TagFilter</code> with a given set of
     * valid tags, the type of the valid tags and the flag which specifies
     * of strict filtering is turned on or off.
     *
     * @param validTags The set of valid tags.
     * @param validTagType The type of the valid tags.
     * @param strict If <code>true</code>, strict filtering is used otherwise
     * not.
     * @param filterMatching If <code>true</code> matching terms are filtered
     */
    public TagFilter(final Set<Tag> validTags, final String validTagType,
            final boolean strict, final boolean filterMatching) {
        m_validTags = validTags;
        m_validTagType = validTagType;
        m_strict = strict;
        m_filterMatching = filterMatching;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        boolean allValid = true;

        final List<Tag> tags = term.getTags();
        if (tags.isEmpty()) {
            if (m_filterMatching) {
              //the term does not contains any tag and only matching
              //should be filtered
              return term;
            }
            return null;
        }
        for (final Tag t : tags) {
            if (t.getTagType().equals(m_validTagType)) {
                if (m_filterMatching) {
                    if (m_validTags.contains(t)) {
                        if (!m_strict) {
                            return null;
                        }
                    } else {
                        allValid = false;
                    }
                } else {
                    if (m_validTags.contains(t)) {
                        if (!m_strict) {
                            return term;
                        }
                    } else {
                        allValid = false;
                    }
                }
            } else {
                allValid = false;
            }
        }

        if ((m_strict && allValid) || (m_filterMatching && !allValid)) {
            return term;
        }
        return null;
    }
}

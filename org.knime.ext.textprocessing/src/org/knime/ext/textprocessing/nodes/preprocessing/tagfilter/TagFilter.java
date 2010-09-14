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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter.StopWordFilter;

import java.util.List;
import java.util.Set;

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
    public Term preprocessTerm(final Term term) {
        boolean allValid = true;

        final List<Tag> tags = term.getTags();
        if (tags.isEmpty()) {
            if (m_filterMatching) {
              //the term does not contains any tag and only matching
              //should filtered
              return term;
            }
            return null;
        }
        for (final Tag t : tags) {
            if (t.getTagType().equals(m_validTagType)) {
                if (m_filterMatching) {
                    if (!m_validTags.contains(t)) {
                        if (!m_strict) {
                            return term;
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
                if (m_filterMatching) {
                    if (!m_strict) {
                        return term;
                    }
                } else {
                    allValid = false;
                }
            }
        }

        if (m_strict && allValid) {
            return term;
        }
        return null;
    }

}

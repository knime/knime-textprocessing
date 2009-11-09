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
 *   24.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.tagfilter;

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
 * {@link StopWordFilter#preprocessTerm(Term)} returns null if the term was filtered 
 * out, an the given unmodified term if not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFilter implements TermPreprocessing {

    private Set<Tag> m_validTags;
    
    private String m_validTagType;
    
    private boolean m_strict;
    
    /**
     * Creates a new instance of <code>TagFilter</code> with a given set of
     * valid tags, the type of the valid tags and the flag which specifies
     * of strict filtering is turned on or off.
     * 
     * @param validTags The set of valid tags.
     * @param validTagType The type of the valid tags.
     * @param strict If <code>true</code>, strict filtering is used otherwise
     * not.
     */
    public TagFilter(final Set<Tag> validTags, final String validTagType,
            final boolean strict) {
        m_validTags = validTags;
        m_validTagType = validTagType;
        m_strict = strict;
    }
    
    /**
     * {@inheritDoc}
     */
    public Term preprocessTerm(final Term term) {
        boolean allValid = true;
        boolean oneValid = false;
        
        List<Tag> tags = term.getTags();
        for (Tag t : tags) {
            if (t.getTagType().equals(m_validTagType)) {
                if (m_validTags.contains(t)) {
                    oneValid = true;
                } else {
                    allValid = false;
                }
            }
        }
        
        if (m_strict && allValid && oneValid) {
            return term;
        } else if (!m_strict && oneValid) {
            return term;
        }
        return null;
    }

}

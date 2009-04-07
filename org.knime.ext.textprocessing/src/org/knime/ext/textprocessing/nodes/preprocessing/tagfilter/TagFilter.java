/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
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
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;
import org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter.StopWordFilter;

/**
 * A tag filter, filtering terms with not specified tags. 
 * See {@link StopWordFilter#preprocess(Term)} for details to filter terms.
 * <code>TagFilter</code> implements <code>Preprocessing</code> and can be
 * used as a preprocessing step. The preprocessing method 
 * {@link StopWordFilter#preprocess(Term)} returns null if the term was filtered 
 * out, an the given unmodified term if not. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFilter implements Preprocessing {

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
    public Term preprocess(final Term term) {
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

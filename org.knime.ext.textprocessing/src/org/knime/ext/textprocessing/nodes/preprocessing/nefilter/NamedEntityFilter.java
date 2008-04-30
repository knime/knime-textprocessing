/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.nefilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.Preprocessing;

/**
 * The filter class of the named entity filter node. Provides methods to filter
 * terms which are modifiable or unmodifiable, respectively.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class NamedEntityFilter implements Preprocessing {

    private boolean m_filterModifiable;
    
    /**
     * Creates a new instance of <code>NamedEntityFilter</code> with given
     * flag specifying if modifiable or unmodifiable terms are filtered.
     * 
     * @param filterModifiable If <code>true</code> modifiable terms are 
     * filtered, otherwise unmodifiable.
     */
    public NamedEntityFilter(final boolean filterModifiable) {
        m_filterModifiable = filterModifiable;
    }
    
    /**
     * {@inheritDoc}
     */
    public Term preprocess(final Term term) {
        if (m_filterModifiable && term.isUnmodifiable()) {
            return term;
        } else if(!m_filterModifiable && !term.isUnmodifiable()) {
            return term;
        }
        return null;
    }

}

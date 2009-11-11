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
 * -------------------------------------------------------------------
 * 
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Term;

import java.util.Collections;
import java.util.Set;

/**
 * Bundles a document and a set of terms into a chunk. The data can not be
 * changed once the chunk has been created.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentChunk {

    private Document m_doc;
    
    private Set<Term> m_terms;
    
    /**
     * Creates a new instance of <code>DocumentChunk</code> with given document
     * and set of terms to set.
     * 
     * @param doc The document to set.
     * @param terms The set of terms to set.
     */
    public DocumentChunk(final Document doc, final Set<Term> terms) {
        m_doc = doc;
        m_terms = terms;
    }
    
    /**
     * @return The document.
     */
    public Document getDocument() {
        return m_doc;
    }
    
    /**
     * @return The set of terms.
     */
    public Set<Term> getTerms() {
        return Collections.unmodifiableSet(m_terms);
    }
}

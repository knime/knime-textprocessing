/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
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

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.util.Collections;
import java.util.List;

/**
 * Contains a complete paragraph as a list of 
 * {@link org.knime.ext.textprocessing.data.Sentence}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Paragraph implements TextContainer {

    private List<Sentence> m_sentences;
    
    /**
     * Creates new instance of <code>Paragraph</code> with the given list of
     * {@link org.knime.ext.textprocessing.data.Sentence}s to set. The list
     * may not be <code>null</code> otherwise a 
     * <code>NullPointerException</code> will be thrown.
     * 
     * @param sentences The list of sentences to set.
     * @throws NullPointerException If the given list of sentences is
     * <code>null</code>
     */
    public Paragraph(final List<Sentence> sentences) 
    throws NullPointerException {
        if (sentences == null) {
            throw new NullPointerException(
                    "List of sentences may not be null!");
        }
        m_sentences = sentences;
    }

    /**
     * @return the sentences The list of sentences the paragraph consists of.
     */
    public List<Sentence> getSentences() {
        return Collections.unmodifiableList(m_sentences);
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_sentences.size(); i++) {
            sb.append(m_sentences.get(i).getText());
            if (i < m_sentences.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_sentences.size(); i++) {
            sb.append(m_sentences.get(i).toString());
            if (i < m_sentences.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Paragraph)) {
            return false;
        }
        Paragraph p = (Paragraph)o;
        if (!p.getSentences().equals(m_sentences)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int fac = 119;
        int div = 13;
        int hash = 0;
        for (Sentence s : m_sentences) {
            hash += fac * s.hashCode() / div; 
        }
        return hash;
    } 
}

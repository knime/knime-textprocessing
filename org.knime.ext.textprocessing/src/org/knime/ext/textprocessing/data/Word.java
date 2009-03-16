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
 *   12.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;


/**
 * Contains a single word and represents the atomic unit of a 
 * {@link org.knime.ext.textprocessing.data.Document}.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Word implements TextContainer {
    
    private String m_word;
    
    /**
     * Creates a new instance of <code>Word</code> with given string to set as
     * word.
     * 
     * @param word The word to set.
     * @throws NullPointerException If the given string to set as word is null
     * an exception will be thrown.
     */
    public Word(final String word) throws NullPointerException {
        if (word == null) {
            throw new NullPointerException("A word must not be null!");
        }
        m_word = word;
    }
    
    /**
     * @return The word.
     */
    public String getWord() {
        return m_word;
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        return getWord();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getWord();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Word)) {
            return false;
        }
        Word w = (Word)o;
        if (!w.getWord().equals(getWord())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getWord().hashCode();
    }
}

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
 *   12.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Contains one or more words (at least one) and groups them to a meaning of
 * a higher-level according to the grouping algorithms (like named entity
 * recognition, protein or gene name recognition, etc.). In addition a list of
 * {@link org.knime.ext.textprocessing.data.Tag}s can be assigned to a
 * <code>Term</code>, which label the different meanings of it, i.e.
 * Part-Of-Speech tags, Named Entity tags, etc. Further a term can be set
 * unmodifiable with the effect that it may not be filtered out or transformed
 * in any way by any node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Term implements TextContainer, Serializable {

    /**
     * The default string which separates the words, the term contains. This
     * separator is used i.e. in
     * {@link org.knime.ext.textprocessing.data.Term#getText()} to separate the
     * words and create a single string representing the term.
     */
    public static final String WORD_SEPARATOR = " ";

    private List<Word> m_words;

    private List<Tag> m_tags;

    private boolean m_unmodifiable = false;

    private int m_hashCode = -1;

    /**
     * Creates a new instance of <code>Term</code> with the given list of
     * {@link org.knime.ext.textprocessing.data.Word}s representing the term,
     * the list of {@link org.knime.ext.textprocessing.data.Tag}s and the
     * unmodifiable flag.
     *
     * @param words The list of words the term consist of.
     * @param tags The tags representing the meanings of the term.
     * @param unmodifiable If set <code>true</code> the term is set unmodifiable
     * and is not affected by filter or transformer nodes.
     * @throws NullPointerException Will be thrown if the word list is null.
     */
    public Term(final List<Word> words, final List<Tag> tags,
            final boolean unmodifiable) throws NullPointerException {
        if (words == null) {
            throw new NullPointerException("The ist of words may not be null!");
        }
        m_words = words;

        if (tags == null) {
            m_tags = new ArrayList<Tag>(0);
        } else {
            m_tags = tags;
        }

        m_unmodifiable = unmodifiable;
    }

    /**
     * @return the unmodifiable list of
     * {@link org.knime.ext.textprocessing.data.Word}s the
     * {@link org.knime.ext.textprocessing.data.Word}sterm consist of.
     */
    public List<Word> getWords() {
        return Collections.unmodifiableList(m_words);
    }

    /**
     * @return the unmodifiable list of
     * {@link org.knime.ext.textprocessing.data.Tag}s assigned to the term.
     */
    public List<Tag> getTags() {
        return Collections.unmodifiableList(m_tags);
    }

    /**
     * @return the unmodifiable flag.
     */
    public boolean isUnmodifiable() {
        return m_unmodifiable;
    }

    /**
     * {@inheritDoc}
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_words.size(); i++) {
            sb.append(m_words.get(i).getText());
            if (i < m_words.size() - 1) {
                sb.append(WORD_SEPARATOR);
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
        sb.append(getText());
        sb.append("[");
        for (int i = 0; i < m_tags.size(); i++) {
            sb.append(m_tags.get(i).getTagValue());
            sb.append("(");
            sb.append(m_tags.get(i).getTagType());
            sb.append(")");
            if (i < m_tags.size() - 1) {
                sb.append(WORD_SEPARATOR);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Term)) {
            return false;
        }
        Term t = (Term)o;
        if (this == t) {
            return true;
        }

        if (!t.getWords().equals(getWords())) {
            return false;
        }
        if (!t.getTags().equals(getTags())) {
            return false;
        }
        if (t.isUnmodifiable() != m_unmodifiable) {
            return false;
        }

        return true;
    }

    /**
     * Compares <code>this</code> with the given object. Returns
     * <code>true</code> if given object is an instance of a <code>Term</code>
     * and if the list of words of the given term is equal to the internal list
     * of words. Otherwise <code>false</code> is returned. Attributes like
     * tags or modifiability is not compared, therefore use
     * {@link Term#equals(Object)}.
     *
     * @param o The object to compare with.
     * @return <code>true</code> if given object is an instance of
     * <code>Term</code> and the list of words is equal to the list of words of
     * the given term.
     */
    public boolean equalsWordsOnly(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Term)) {
            return false;
        }
        Term t = (Term)o;
        if (!t.getWords().equals(getWords())) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (m_hashCode == -1) {
            m_hashCode = 0;
            int fac = 119;

            for (Word w : m_words) {
                m_hashCode += fac * w.hashCode();
            }

            for (Tag t : m_tags) {
                m_hashCode -= fac * t.hashCode();
            }

            m_hashCode += fac * new Boolean(m_unmodifiable).hashCode();
        }

        return m_hashCode;
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(TermCell.getSerializationString(this));
    }

    
    private void readObject(final ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (!(o instanceof String)) {
            throw new ClassNotFoundException(
                    "Serialized object is not a String!");
        }
        String termStr = (String)o;
        try {            
            Term term = TermCell.createTerm(termStr);
            m_tags = term.getTags();
            m_unmodifiable = term.isUnmodifiable();
            m_words = term.getWords();
        } catch (Exception e) {
            throw new IOException("Could not deserialize term! " 
                    + e.getMessage()); 
        }
    }    
}

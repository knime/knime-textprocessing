/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   12.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.EOFException;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Contains a single word and represents the atomic unit of a
 * {@link org.knime.ext.textprocessing.data.Document}.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Word implements TextContainer, Externalizable {

    private static final long serialVersionUID = 472002663573125545L;

    private static final String DEFAULT_WHITESPACE_SUFFIX = " ";


    private String m_word;

    private String m_whiteSpaceSuffix;

    /**
     * Creates empty instance of <code>Word</code> with <code>null</code> value
     * for word.
     */
    public Word() {
        m_word = null;
        m_whiteSpaceSuffix = null;
    }

    /**
     * Creates a new instance of <code>Word</code> with given word and whitespace suffix to set.
     *
     * @param word The word to set.
     * @param whiteSpaceSuffix The whitespace suffix to set.
     * @throws IllegalArgumentException If the word or the whitespace suffix to set is null.
     * @since 2.8
     */
    public Word(final String word, final String whiteSpaceSuffix) throws IllegalArgumentException {
        if (word == null) {
            throw new IllegalArgumentException("A word must not be null!");
        }
        if (whiteSpaceSuffix == null) {
            throw new IllegalArgumentException("The whitespace suffix must not be null!");
        }
        m_word = word;
        m_whiteSpaceSuffix = whiteSpaceSuffix;
    }

    /**
     * Creates a new instance of <code>Word</code> with given string to set as
     * word.
     *
     * @param word The word to set.
     * @throws NullPointerException If the given string to set as word is null
     *             an exception will be thrown.
     * @deprecated use {@link Word#Word(String, String)} instead. The whitespace suffix should be set explicitly.
     */
    @Deprecated
    public Word(final String word) throws NullPointerException {
        if (word == null) {
            throw new NullPointerException("A word must not be null!");
        }
        m_word = word;
        m_whiteSpaceSuffix = DEFAULT_WHITESPACE_SUFFIX;
    }

    /**
     * @return The word.
     */
    public String getWord() {
        return m_word;
    }

    /**
     * @return The whitespace suffix.
     * @since 2.8
     */
    public String getWhitespaceSuffix() {
        return m_whiteSpaceSuffix;
    }

    /**
     * Sets the given string as white space suffix.
     * @param whiteSpaceSuffix String to set as white space suffix.
     * @since 2.9
     */
    public void setWhiteSpaceSuffix(final String whiteSpaceSuffix) {
        m_whiteSpaceSuffix = whiteSpaceSuffix;
    }

    /**
     * Adds the given string to white space suffix.
     * @param whiteSpaceSuffix String to add to white space suffix.
     * @since 2.9
     */
    public void addWhiteSpaceSuffix(final String whiteSpaceSuffix) {
        m_whiteSpaceSuffix += whiteSpaceSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return m_word;
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getTextWithWsSuffix() {
        return m_word + m_whiteSpaceSuffix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return m_word;
    }

    /**
     * {@inheritDoc}
     * Note that two words are equal if their word is equal even if their whitespace suffixes are not equal. For
     * comparison only the words itself are considered. The same holds for the hash code of words.
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
     * Note that two words have the same hash code if their words are equal even if their whitespace suffixes are not
     * equal. For hash code generation only the word strings are used. The same holds for {@link Word#equals(Object)}.
     */
    @Override
    public int hashCode() {
        return m_word.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(m_word);
        out.writeUTF(m_whiteSpaceSuffix);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_word = in.readUTF();

        // try to de-serialize the whitespace suffix
        try {
            m_whiteSpaceSuffix = in.readUTF();
        // if suffix was not serialized and thus can not be de-serialized set default value
        } catch (EOFException e) {
            m_whiteSpaceSuffix = DEFAULT_WHITESPACE_SUFFIX;
        }
    }
}

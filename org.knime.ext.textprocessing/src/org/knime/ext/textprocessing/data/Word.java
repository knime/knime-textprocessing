/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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
        // if suffix was not serialized and thus cannot be de-serialized set default value
        } catch (EOFException e) {
            m_whiteSpaceSuffix = DEFAULT_WHITESPACE_SUFFIX;
        }
    }
}

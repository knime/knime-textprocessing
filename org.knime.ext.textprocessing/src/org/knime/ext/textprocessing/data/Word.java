/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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

    private String m_word;

    /**
     * Creates empty instance of <code>Word</code> with <code>null</code> value
     * for word.
     */
    public Word() {
        m_word = null;
    }

    /**
     * Creates a new instance of <code>Word</code> with given string to set as
     * word.
     * 
     * @param word The word to set.
     * @throws NullPointerException If the given string to set as word is null
     *             an exception will be thrown.
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
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeUTF(m_word);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_word = in.readUTF();
    }
}

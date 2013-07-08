/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knime.ext.textprocessing.util.TextContainers;

/**
 * Contains one or more words (at least one) and groups them to a meaning of a
 * higher-level according to the grouping algorithms (like named entity
 * recognition, protein or gene name recognition, etc.). In addition a list of
 * {@link org.knime.ext.textprocessing.data.Tag}s can be assigned to a
 * <code>Term</code>, which label the different meanings of it, i.e.
 * Part-Of-Speech tags, Named Entity tags, etc. Further a term can be set
 * unmodifiable with the effect that it may not be filtered out or transformed
 * in any way by any node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Term implements TextContainer, Externalizable {

    /**
     * The default string which separates the words, which is used e.g. in {@link Term#toString()}.
     */
    public static final String WORD_SEPARATOR = " ";

    private static final long serialVersionUID = -4594861599001630681L;

    private List<Word> m_words;

    private List<Tag> m_tags;

    private boolean m_unmodifiable = false;

    private int m_hashCode = -1;


    /**
     * Creates empty instance of <code>Term</code> with all <code>null</code>
     * values.
     */
    public Term() {
        m_words = null;
        m_tags = null;
        m_hashCode = -1;
        m_unmodifiable = false;
    }

    /**
     * Creates a new instance of <code>Term</code> with the given list of
     * {@link org.knime.ext.textprocessing.data.Word}s representing the term,
     * the list of {@link org.knime.ext.textprocessing.data.Tag}s and the
     * unmodifiable flag.
     *
     * @param words The list of words the term consist of.
     * @param tags The tags representing the meanings of the term.
     * @param unmodifiable If set <code>true</code> the term is set unmodifiable
     *            and is not affected by filter or transformer nodes.
     * @throws NullPointerException Will be thrown if the word list is null.
     */
    public Term(final List<Word> words, final List<Tag> tags,
            final boolean unmodifiable) throws NullPointerException {
        if (words == null) {
            throw new NullPointerException(
                    "The list of words must not be null!");
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
     *         {@link org.knime.ext.textprocessing.data.Word}s the
     *         {@link org.knime.ext.textprocessing.data.Word}sterm consist of.
     */
    public List<Word> getWords() {
        return Collections.unmodifiableList(m_words);
    }

    /**
     * @return the unmodifiable list of
     *         {@link org.knime.ext.textprocessing.data.Tag}s assigned to the
     *         term.
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
    @Override
    public String getText() {
        return TextContainers.getText(m_words);
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getTextWithWsSuffix() {
        return TextContainers.getTextWithWsSuffix(m_words);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getText().trim());
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
     * of words. Otherwise <code>false</code> is returned. Attributes like tags
     * or modifiability is not compared, therefore use
     * {@link Term#equals(Object)}.
     *
     * @param o The object to compare with.
     * @return <code>true</code> if given object is an instance of
     *         <code>Term</code> and the list of words is equal to the list of
     *         words of the given term.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeBoolean(m_unmodifiable);
        out.writeInt(m_hashCode);

        out.writeInt(m_words.size());
        for (Word w : m_words) {
            out.writeObject(w);
        }
        out.writeInt(m_tags.size());
        for (Tag t : m_tags) {
            out.writeObject(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_unmodifiable = in.readBoolean();
        m_hashCode = in.readInt();

        int size = in.readInt();
        m_words = new ArrayList<Word>(size);
        for (int i = 0; i < size; i++) {
            m_words.add((Word)in.readObject());
        }
        size = in.readInt();
        m_tags = new ArrayList<Tag>(size);
        for (int i = 0; i < size; i++) {
            m_tags.add((Tag)in.readObject());
        }
    }
}

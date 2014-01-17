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
 *   13.02.2008 (thiel): created
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
 * Contains a complete paragraph as a list of
 * {@link org.knime.ext.textprocessing.data.Sentence}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Paragraph implements TextContainer, Externalizable {

    private static final long serialVersionUID = 8317307915043783911L;

    private List<Sentence> m_sentences;

    private int m_hashCode = -1;

    /**
     * Creates empty instance of <code>Paragraph</code> with all
     * <code>null</code> values.
     */
    public Paragraph() {
        m_sentences = null;
    }

    /**
     * Creates new instance of <code>Paragraph</code> with the given list of
     * {@link org.knime.ext.textprocessing.data.Sentence}s to set. The list may
     * not be <code>null</code> otherwise a <code>NullPointerException</code>
     * will be thrown.
     *
     * @param sentences The list of sentences to set.
     * @throws NullPointerException If the given list of sentences is
     *             <code>null</code>
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
    @Override
    public String getText() {
        return TextContainers.getText(m_sentences);
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getTextWithWsSuffix() {
        return TextContainers.getTextWithWsSuffix(m_sentences);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Sentence sentence : m_sentences) {
            sb.append(sentence.toString());
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
        if (m_hashCode == -1) {
            int fac = 119;
            int div = 13;
            m_hashCode = 0;
            for (Sentence s : m_sentences) {
                m_hashCode += fac * s.hashCode() / div;
            }
        }
        return m_hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(m_hashCode);
        out.writeInt(m_sentences.size());
        for (Sentence s : m_sentences) {
            out.writeObject(s);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_hashCode = in.readInt();
        int size = in.readInt();
        m_sentences = new ArrayList<Sentence>(size);
        for (int i = 0; i < size; i++) {
            m_sentences.add((Sentence)in.readObject());
        }
    }
}

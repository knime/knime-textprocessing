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

/**
 * Contains all corresponding
 * {@link org.knime.ext.textprocessing.data.Paragraph}s as a list as well as a
 * annotation ({@link org.knime.ext.textprocessing.data.SectionAnnotation})
 * marking out the position and rolw of the section, i.e. title, abstract,
 * chapter, etc.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Section implements TextContainer, Externalizable {

    private List<Paragraph> m_paragraphs;

    private SectionAnnotation m_annotation;

    private int m_hashCode = -1;

    /**
     * Creates empty instance of <code>Section</code> with all <code>null</code>
     * values.
     */
    public Section() {
        m_paragraphs = null;
        m_annotation = null;
    }

    /**
     * Creates new instance of <code>Section</code> with given list of
     * {@link org.knime.ext.textprocessing.data.Paragraph}s and the given
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} to set. If
     * one of these parameters is <code>null</code> a
     * <code>NullPointerException</code> will be thrown.
     * 
     * @param paragraphs The list of paragraphs to set.
     * @param annotation The annotation to set.
     * @throws NullPointerException If the given list of paragraphs or the
     *             annotation is <code>null</code>.
     */
    public Section(final List<Paragraph> paragraphs,
            final SectionAnnotation annotation) throws NullPointerException {
        if (paragraphs == null) {
            throw new NullPointerException(
                    "List of paragraphs may not be null!");
        } else if (annotation == null) {
            throw new NullPointerException("Annotation may not be null!");
        }

        m_paragraphs = paragraphs;
        m_annotation = annotation;
    }

    /**
     * Creates new instance of <code>Section</code> with given list of
     * {@link org.knime.ext.textprocessing.data.Paragraph}s. The annotation is
     * set to <code>UNKONWON</code> by default. If the given list of paragraphs
     * is <code>null</code> a <code>NullPointerException</code> will be thrown.
     * 
     * @param paragraphs The list of paragraphs to set.
     * @throws NullPointerException If the given list of paragraphs or is
     *             <code>null</code>.
     */
    public Section(final List<Paragraph> paragraphs)
            throws NullPointerException {
        this(paragraphs, SectionAnnotation.UNKNOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_paragraphs.size(); i++) {
            sb.append(m_paragraphs.get(i).getText());
            if (i < m_paragraphs.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * @return the paragraphs of the section.
     */
    public List<Paragraph> getParagraphs() {
        return Collections.unmodifiableList(m_paragraphs);
    }

    /**
     * @return the annotation
     */
    public SectionAnnotation getAnnotation() {
        return m_annotation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_paragraphs.size(); i++) {
            sb.append(m_paragraphs.get(i).toString());
            if (i < m_paragraphs.size() - 1) {
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
        } else if (!(o instanceof Section)) {
            return false;
        }
        Section s = (Section)o;
        if (!s.getParagraphs().equals(m_paragraphs)) {
            return false;
        } else if (!s.getAnnotation().equals(m_annotation)) {
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
            int div = 19;
            m_hashCode = 0;
            for (Paragraph p : m_paragraphs) {
                m_hashCode += fac * p.hashCode() / div;
            }
            m_hashCode -= div * m_annotation.hashCode();
        }
        return m_hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(final ObjectOutput out) throws IOException {
        out.writeInt(m_hashCode);
        out.writeInt(m_paragraphs.size());
        for (Paragraph p : m_paragraphs) {
            out.writeObject(p);
        }
        out.writeObject(m_annotation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(final ObjectInput in) throws IOException,
            ClassNotFoundException {
        m_hashCode = in.readInt();
        int size = in.readInt();
        m_paragraphs = new ArrayList<Paragraph>(size);
        for (int i = 0; i < size; i++) {
            m_paragraphs.add((Paragraph)in.readObject());
        }
        m_annotation = (SectionAnnotation)in.readObject();
    }
}

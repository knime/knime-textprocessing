/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   13.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knime.ext.textprocessing.util.TextContainers;

/**
 * Contains a complete sentence as a list of
 * {@link org.knime.ext.textprocessing.data.Term}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Sentence implements org.knime.ext.textprocessing.data.hittisau.Sentence {

    private static final long serialVersionUID = 2908976905629595643L;

    private List<Term> m_terms;

    private int m_hashCode = -1;

    /**
     * Creates empty instance of <code>Sentence</code> with all
     * <code>null</code> values.
     */
    public Sentence() {
        m_terms = null;
    }

    /**
     * Creates a new instance of <code>Sentence</code> with the given list of
     * {@link org.knime.ext.textprocessing.data.Term}s as its words . If it is
     * set to <code>null</code> a <code>NullPointerException</code> is thrown.
     *
     * @param sentence The list of terms to set as sentence.
     * @throws NullPointerException If the list of terms is <code>null</code>.
     */
    public Sentence(final List<Term> sentence) throws NullPointerException {
        if (sentence == null) {
            throw new NullPointerException(
                    "Term list \"sentence\" may not be null!");
        }

        m_terms = sentence;
    }

    /**
     * @return the unmodifiable list of terms representing the sentence.
     */
    @Override
    public List<Term> getTerms() {
        return Collections.unmodifiableList(m_terms);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return TextContainers.getText(m_terms);
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getTextWithWsSuffix() {
        return TextContainers.getTextWithWsSuffix(m_terms);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Term term : m_terms) {
            sb.append(term.toString());
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
        } else if (!(o instanceof Sentence)) {
            return false;
        }
        Sentence s = (Sentence)o;
        if (!s.getTerms().equals(getTerms())) {
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
            for (Term t : m_terms) {
                m_hashCode += fac * t.hashCode() / div;
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
        out.writeInt(m_terms.size());
        for (Term t : m_terms) {
            out.writeObject(t);
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
        m_terms = new ArrayList<Term>(size);
        for (int i = 0; i < size; i++) {
            m_terms.add((Term)in.readObject());
        }
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * -------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.cooccurrencecounter;

import org.knime.ext.textprocessing.data.Term;


/**
 * This class holds a term and compares it depending on the given flag that
 * indicates if the tags should be considered during comparison of two term
 * containers.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class TermContainer {

    private int m_hashCode = -1;
    private final boolean m_checkTags;
    private final Term m_term;

    /**Constructor for class TermContainer.
     * @param checkTags <code>true</code> if the term tags should be checked
     * @param term the term
     */
    public TermContainer(final boolean checkTags, final Term term) {
        m_checkTags = checkTags;
        m_term = term;
    }

    /**
     * @return the text representation of the term
     */
    public String getText() {
        return m_term.getText();
    }

    /**
     * @return the term itself
     */
    public Term getTerm() {
        return m_term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (m_hashCode == -1) {
            if (m_checkTags) {
                m_hashCode = m_term.hashCode();
            } else {
                m_hashCode = m_term.getWords().hashCode();
            }
        }
        return m_hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TermContainer other = (TermContainer)obj;
        if (m_checkTags != other.m_checkTags) {
            return false;
        }
        if (m_checkTags) {
            return m_term.equals(other.m_term);
        }
        return m_term.equalsWordsOnly(other.m_term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TermContainer [m_checkTags=" + m_checkTags + ", m_term="
                + m_term + "]";
    }

}

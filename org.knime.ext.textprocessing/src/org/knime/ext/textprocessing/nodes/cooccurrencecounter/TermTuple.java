/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 * -------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.cooccurrencecounter;

import org.knime.ext.textprocessing.data.Term;


/**
 * Class that holds two terms and the number of their co-occurrences within
 * different parts of a document.
 *
 * @author Tobias Koetter, University of Konstanz
 */
public class TermTuple {

    private final TermContainer m_term1;
    private final TermContainer m_term2;
    private int m_document = 0;
    private int m_section = 0;
    private int m_paragraph = 0;
    private int m_sentence = 0;
    private int m_neighbor = 0;
    private int m_title = 0;
    private final boolean m_ignoreTermOrder;

    /**Constructor for class TermTuple.
     * @param termContainer the first term
     * @param termContainer2 the second term
     */
    public TermTuple(final TermContainer termContainer, final TermContainer termContainer2) {
        this(termContainer, termContainer2, true);
    }
    /**Constructor for class TermTuple.
     * @param term1 the first term
     * @param term2 the second term
     * @param ignoreTermOrder <code>true</code> if the term order is ignored
     */
    public TermTuple(final TermContainer term1, final TermContainer term2,
        final boolean ignoreTermOrder) {
        if (term1 == null) {
            throw new NullPointerException("term1 must not be null");
        }
        if (term2 == null) {
            throw new NullPointerException("term2 must not be null");
        }
        m_ignoreTermOrder = ignoreTermOrder;
        //ensure that the first term is lexicographical smaller than the second
        if (!m_ignoreTermOrder || term1.getText().compareTo(term2.getText()) <= 0) {
            m_term1 = term1;
            m_term2 = term2;
        } else {
            m_term1 = term2;
            m_term2 = term1;
        }
    }


    /**
     * @return the term1
     */
    public Term getTerm1() {
        return m_term1.getTerm();
    }


    /**
     * @return the term2
     */
    public Term getTerm2() {
        return m_term2.getTerm();
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms in a
     * document
     */
    public void incDocument(final int noOfCooc) {
        m_document += noOfCooc;
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms in the
     * title of a document
     */
    public void incTitle(final int noOfCooc) {
        m_title += noOfCooc;
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms in a
     * document section
     */
    public void incSection(final int noOfCooc) {
        m_section += noOfCooc;
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms in a
     * document paragraph
     */
    public void incParagraph(final int noOfCooc) {
        m_paragraph += noOfCooc;
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms in a
     * document sentence
     */
    public void incSentence(final int noOfCooc) {
        m_sentence += noOfCooc;
    }

    /**
     * @param noOfCooc the number of co-occurrences of the two terms as
     * neighbors
     */
    public void incNeighbors(final int noOfCooc) {
        m_neighbor += noOfCooc;
    }


    /**
     * Increments the neighbor counter of the two terms by 1.
     */
    public void incNeighbors() {
        m_neighbor++;
    }


    /**
     * @return the number of co-occurrences of the two terms in the document
     */
    public int getDocument() {
        return m_document;
    }


    /**
     * @return the the number of co-occurrences of the two terms in the title
     *  of a document
     */
    public int getTitle() {
        return m_title;
    }


    /**
     * @return the the number of co-occurrences of the two terms in the
     * sections of a document
     */
    public int getSection() {
        return m_section;
    }


    /**
     * @return the the number of co-occurrences of the two terms in the
     * paragraphs of a document
     */
    public int getParagraph() {
        return m_paragraph;
    }


    /**
     * @return the the number of co-occurrences of the two terms in the
     * sentences of a document
     */
    public int getSentence() {
        return m_sentence;
    }


    /**
     * @return the number of occurrences of the two terms as neighbors
     */
    public int getNeighbor() {
        return m_neighbor;
    }

    /**
     * @param occurrence the type where both term co-occurred
     * @param term1Oc the number of occurrences of the first term
     * @param term2Oc the number of occurrences of the second term
     */
    public void inc(final CooccurrenceLevel occurrence,
            final int term1Oc, final int term2Oc) {
        final int noOfCooc = Math.min(term1Oc, term2Oc);
        switch (occurrence) {
        case DOCUMENT:
            incDocument(noOfCooc);
            return;
        case TITLE:
            incTitle(noOfCooc);
            return;
        case SECTION:
            incSection(noOfCooc);
            return;
        case PARAGRAPH:
            incParagraph(noOfCooc);
            return;
        case SENTENCE:
            incSentence(noOfCooc);
            return;
        case NEIGHBOR:
            incNeighbors(noOfCooc);
            return;
        default:
            return;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = prime * (m_term1.hashCode() + m_term2.hashCode());
        return result;
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
        final TermTuple other = (TermTuple)obj;
        if ((m_term1.equals(other.m_term1) && m_term2.equals(other.m_term2))
                //check both ways if the order should be ignored
                || (m_ignoreTermOrder && m_term1.equals(other.m_term2) && m_term2.equals(other.m_term1))) {
            return true;
        }
        return false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "TermTuple [m_term1=" + m_term1 + ", m_term2=" + m_term2
                + ", m_document=" + m_document + ", m_title=" + m_title
                + ", m_section=" + m_section + ", m_paragraph=" + m_paragraph
                + ", m_sentence=" + m_sentence + "]";
    }
}

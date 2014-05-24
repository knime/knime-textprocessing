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
 * Created on 01.02.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TextContainer;
import org.knime.ext.textprocessing.data.Word;

/**
* A {@link NGramIterator} to create word ngrams.
*
* @author Kilian Thiel, KNIME.com, Zurich, Switzerland
* @since 2.8
*/
public final class NGramWordIterator extends NGramIterator {

    /**
     * The default word separator (one whitespace).
     */
    static final String DEFAULT_WORD_SEPARATOR = " ";

    private Iterator<Sentence> m_sentenceIterator = null;

    private List<Word> m_words = null;

    private final String m_tokenSeparator;

    private int m_wordIndex = 0;

    /**
     * Creates new instance of {@link NGramWordIterator} with given N and
     * word separator to set.
     *
     * @param n the N value.
     * @param tokenSeparator the string to use as word separator of word ngrams.
     */
    public NGramWordIterator(final int n, final String tokenSeparator) {
        super(n);

        if (tokenSeparator != null) {
            m_tokenSeparator = tokenSeparator;
        } else {
            m_tokenSeparator = DEFAULT_WORD_SEPARATOR;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextNGram() {
        if (m_words != null && m_wordIndex + getN() <= m_words.size()) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String nextNGram() throws NoSuchElementException {
        if (m_words == null || m_words.size() < m_wordIndex + getN()) {
            throw new NoSuchElementException("No next n gram!");
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < getN(); i++) {
            sb.append(m_words.get(i + m_wordIndex).getText());

            if (i < getN() - 1) {
                sb.append(m_tokenSeparator);
            }
        }
        m_wordIndex++;

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocument(final Document document) {
        super.setDocument(document);
        m_sentenceIterator = getDocument().sentenceIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextBlock() {
        if (m_sentenceIterator != null) {
            return m_sentenceIterator.hasNext();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextContainer nextBlock() throws NoSuchElementException {
        if (m_sentenceIterator == null || !m_sentenceIterator.hasNext()) {
            throw new NoSuchElementException(
                    "No next block (sentence) to step into!");
        }

        Sentence nextSentence = m_sentenceIterator.next();
        m_words = new ArrayList<Word>();
        for (Term t : nextSentence.getTerms()) {
            m_words.addAll(t.getWords());
        }
        m_wordIndex = 0;

        return nextSentence;
    }
}

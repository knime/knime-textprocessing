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
 * ---------------------------------------------------------------------
 *
 * Created on 02.02.2013 by kilian
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
* A {@link NGramIterator} to create character ngrams.
*
* @author Kilian Thiel, KNIME AG, Zurich, Switzerland
* @since 2.8
*/
public class NGramCharacterIterator extends NGramIterator {

    private Iterator<Sentence> m_sentenceIterator = null;

    private Iterator<Word> m_wordIterator = null;

    private List<Word> m_words;

    private char[] m_characters;

    private int m_charIndex = 0;

    /**
     * Creates new instance of {@link NGramCharacterIterator} with given N
     * to set.
     * @param n The N value to set.
     */
    public NGramCharacterIterator(final int n) {
        super(n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextNGram() {
        if (m_characters != null
                && m_charIndex + getN() <= m_characters.length) {
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String nextNGram() throws NoSuchElementException {
        if (m_characters == null
                || m_characters.length < m_charIndex + getN()) {
            throw new NoSuchElementException("No next n gram!");
        }

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < getN(); i++) {
            sb.append(m_characters[i + m_charIndex]);
        }
        m_charIndex++;

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNextBlock() {
        if (m_wordIterator != null && m_sentenceIterator != null) {
            if (m_wordIterator.hasNext()) {
                return true;
            }
            return m_sentenceIterator.hasNext();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TextContainer nextBlock() throws NoSuchElementException {
        if (m_sentenceIterator == null || m_wordIterator == null) {
            throw new NoSuchElementException(
                    "No next block (sentence) to step into!");
        }

        // if next word is available
        if (m_wordIterator.hasNext()) {
            Word nextWord = m_wordIterator.next();
            m_characters = nextWord.getText().toCharArray();
            m_charIndex = 0;

            return nextWord;
        }

        // if not, step into next sentence
        boolean nextElementFound = false;
        while (!nextElementFound) {
            if (m_sentenceIterator.hasNext()) {
                Sentence nextSentence = m_sentenceIterator.next();

                // extract all words
                m_words = new ArrayList<Word>();
                for (Term t : nextSentence.getTerms()) {
                    m_words.addAll(t.getWords());
                }
                m_wordIterator = m_words.iterator();

                if (m_wordIterator.hasNext()) {
                    Word nextWord = m_wordIterator.next();
                    m_characters = nextWord.getText().toCharArray();
                    m_charIndex = 0;

                    return nextWord;
                }
            } else {
                throw new NoSuchElementException(
                        "No next block (sentence) to step into!");
            }
        }

        throw new NoSuchElementException(
                "No next block (sentence) to step into!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocument(final Document document) {
        super.setDocument(document);

        m_sentenceIterator = getDocument().sentenceIterator();
        Sentence nextSentence = m_sentenceIterator.next();

        m_words = new ArrayList<Word>();
        for (Term t : nextSentence.getTerms()) {
            m_words.addAll(t.getWords());
        }
        m_wordIterator = m_words.iterator();
    }
}

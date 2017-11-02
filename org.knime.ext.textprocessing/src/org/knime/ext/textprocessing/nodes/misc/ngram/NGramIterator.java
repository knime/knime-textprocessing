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
 * Created on 28.01.2013 by kilian
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

import java.util.NoSuchElementException;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * The {@link NGramIterator} allows to iterate over sentences of documents in
 * order to get ngrams. The type of the ngram (word or character ...) is
 * defined by the underlying implementation. After a document is set by
 * {@link NGramIterator#setDocument(Document)} it can be iterated over blocks,
 * which may be words or sentences and ngrams inside these blocks.
 *
 * <p>
 * Example how to use the iterator:
 * <pre>
 * NGramIterator nGramIterator = ...
 * Document doc = ...
 *
 * //first set new document to iterate over
 * nGramIterator.setDocument(doc);
 *
 * //iterate over all blocks (sentences or words)
 * while (m_nGramIterator.hasNextBlock()) {
 *     m_nGramIterator.nextBlock();
 *
 *     // generate all ngrams of current block
 *     while (m_nGramIterator.hasNextNGram()) {
 *         String nGram = m_nGramIterator.nextNGram();
 *     }
 * }
 * </pre>
 * </p>
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.8
 */
public abstract class NGramIterator {

    private final int m_n;

    private Document m_document;

    /**
     * Creates new instance of {@link NGramIterator} with given n to
     * set.
     * @param n The N to set for g gram extraction.
     */
    public NGramIterator(final int n) {
        if (n <= 0) {
            throw new IllegalArgumentException(
                "N value may not be null for n gram extraction!");
        }

        m_n = n;
    }

    /**
     * @return The n value.
     */
    public int getN() {
        return m_n;
    }

    /**
     * @param document The document to iterate over.
     */
    public void setDocument(final Document document) {
        m_document = document;
    }

    /**
     * @return The document to iterate over.
     */
    public Document getDocument() {
        return m_document;
    }

    /**
     * Returns {@code true} if the iteration has more n grams,
     * otherwise {@code false}.
     *
     * @return {@code true} if the iteration has more n grams.
     */
    public abstract boolean hasNextNGram();

    /**
     * Returns the next n gram of the iteration.
     *
     * @return the next n gram of the iteration
     * @throws NoSuchElementException if the iteration has no more n grams.
     */
    public abstract String nextNGram() throws NoSuchElementException;


    /**
     * Returns {@code true} if the iteration has more blocks, otherwise
     * {@code false}.
     *
     * @return {@code true} if the iteration has more blocks.
     */
    public abstract boolean hasNextBlock();

    /**
     * Returns the next block as {@link TextContainer}, which can be e.g. a
     * sentence or a word.
     *
     * @return the next block of the iteration.
     * @throws NoSuchElementException if the iteration has no more n blocks.
     */
    public abstract TextContainer nextBlock() throws NoSuchElementException;
}



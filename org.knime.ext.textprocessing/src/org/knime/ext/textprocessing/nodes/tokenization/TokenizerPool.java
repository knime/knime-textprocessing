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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 * Created on 07.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.tokenization;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.nodes.tokenization.tokenizer.sentence.OpenNlpSentenceTokenizer;

/**
 * Provides a pool of tokenizer instances. The pool size is the number of available word and sentence tokenizers. All
 * tokenizer instances are created in the constructor of the pool.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 3.3
 */
public class TokenizerPool {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TokenizerPool.class);

    private final Tokenizer[] m_wordTokenizer;

    private final OpenNlpSentenceTokenizer[] m_sentenceTokenizer;

    private final int m_poolSize;

    private final String m_tokenizerName;

    private int m_wordIndex = 0;

    private int m_sentenceIndex = 0;

    /**
     * Constructor for class OpenNLPTokenizerPool.
     * @param tokenizerName The name of the word tokenizer.
     * @param poolSize The number of word and sentence tokenizers of the pool.
     */
    TokenizerPool(final int poolSize, final String tokenizerName) {
        if (poolSize < 1) {
            throw new IllegalArgumentException("Tokenizer pool size must be larger than 0!");
        }

        m_tokenizerName = tokenizerName;
        if (TokenizerFactoryRegistry.getTokenizerFactoryMap().get(m_tokenizerName).forceMaxPoolSize()) {
            m_poolSize = TokenizerFactoryRegistry.getTokenizerFactoryMap().get(m_tokenizerName).getMaxPoolSize();
        } else {
            m_poolSize = poolSize;
        }
        m_wordTokenizer = new Tokenizer[m_poolSize];
        m_sentenceTokenizer = new OpenNlpSentenceTokenizer[m_poolSize];

        LOGGER.debug("Initializing tokenizer pool with " + m_poolSize + " tokenizers.");
        for (int i = 0; i < m_poolSize; i++) {
            m_wordTokenizer[i] = TokenizerFactoryRegistry.getTokenizerFactoryMap().get(m_tokenizerName).getTokenizer();
            m_sentenceTokenizer[i] = new OpenNlpSentenceTokenizer();
        }
    }

    /**
     * @return The next available word tokenizer.
     */
    synchronized Tokenizer nextWordTokenizer() {
        return m_wordTokenizer[m_wordIndex++ % m_poolSize];
    }

    /**
     * @return The next available sentence tokenizer of the pool.
     */
    synchronized OpenNlpSentenceTokenizer nextSentenceTokenizer() {
        return m_sentenceTokenizer[m_sentenceIndex++ % m_poolSize];
    }

    /**
     * @return the poolSize
     */
    int getPoolSize() {
        return m_poolSize;
    }

    /**
     * @return The name of the tokenizer used for word tokenization.
     */
    String getTokenizerName() {
        return m_tokenizerName;
    }

}

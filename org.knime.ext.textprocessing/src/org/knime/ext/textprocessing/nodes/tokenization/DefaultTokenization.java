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
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;

import java.util.HashMap;
import java.util.Map;

import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * Is a utility class which provides methods for the default tokenization of
 * {@link org.knime.ext.textprocessing.data.Document}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class DefaultTokenization {
    private static Map<String, TokenizerPool> m_tokenizerPoolMap = new HashMap<String, TokenizerPool>();

    private DefaultTokenization() {
    }

    /**
     * @return Creates and returns a new {@code TokenizerPool} for the specified tokenizer.
     * @param tokenizerName The name of the tokenizer used for word tokenization.
     * @since 3.3
     */
    private static TokenizerPool createTokenizerPool(final String tokenizerName) {
        return new TokenizerPool(TextprocessingPreferenceInitializer.tokenizerPoolSize(), tokenizerName);
    }

    /**
     * @param tokenizerName The specified tokenizer used for word tokenization.
     * @return Returns the sentence tokenizer taken from the same TokenizerPool as the word tokenizer.
     * @since 3.3
     */
    public static final synchronized Tokenizer getSentenceTokenizer(final String tokenizerName) {
        if (m_tokenizerPoolMap.get(tokenizerName) == null) {
            m_tokenizerPoolMap.put(tokenizerName, createTokenizerPool(tokenizerName));
        }
        return m_tokenizerPoolMap.get(tokenizerName).nextSentenceTokenizer();
    }

    /**
     * @return The default sentence tokenizer.
     * @deprecated Use {@link #getSentenceTokenizer(String)} instead to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public static final synchronized Tokenizer getSentenceTokenizer() {
        if (m_tokenizerPoolMap.get("OpenNLP English WordTokenizer") != null) {
            return m_tokenizerPoolMap.get("OpenNLP English WordTokenizer").nextSentenceTokenizer();
        } else {
            m_tokenizerPoolMap.put("OpenNLP English WordTokenizer",
                createTokenizerPool("OpenNLP English WordTokenizer"));
            return m_tokenizerPoolMap.get("OpenNLP English WordTokenizer").nextSentenceTokenizer();
        }
    }

    /**
     * @param tokenizerName The specified tokenizer used for word tokenization
     * @return Returns the specified word tokenizer.
     * @since 3.3
     */
    public static final synchronized Tokenizer getWordTokenizer(final String tokenizerName) {
        if (m_tokenizerPoolMap.get(tokenizerName) == null) {
            m_tokenizerPoolMap.put(tokenizerName, createTokenizerPool(tokenizerName));
        }
        return m_tokenizerPoolMap.get(tokenizerName).nextWordTokenizer();
    }

    /**
     * @return The default word tokenizer.
     * @deprecated Use {@link #getWordTokenizer(String)} instead to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public static final synchronized Tokenizer getWordTokenizer() {
        if (m_tokenizerPoolMap.get("OpenNLP English WordTokenizer") != null) {
            return m_tokenizerPoolMap.get("OpenNLP English WordTokenizer").nextWordTokenizer();
        } else {
            m_tokenizerPoolMap.put("OpenNLP English WordTokenizer",
                createTokenizerPool("OpenNLP English WordTokenizer"));
            return m_tokenizerPoolMap.get("OpenNLP English WordTokenizer").nextWordTokenizer();
        }
    }
}

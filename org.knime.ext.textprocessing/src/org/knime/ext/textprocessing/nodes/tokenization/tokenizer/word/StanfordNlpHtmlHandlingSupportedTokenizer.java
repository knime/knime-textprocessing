/*
 * ------------------------------------------------------------------------
 *
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
 * History
 *   Oct 9, 2018 (julian): created
 */
package org.knime.ext.textprocessing.nodes.tokenization.tokenizer.word;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 * This class is an abstract super class for integrated StanfordNLP tokenizer that have to be handled the same way. E.g.
 * the {@code StanfordNlpPTBTokenizer} and the {@code StanfordNlpSpanishTokenizer} both need a specific handling of HTML
 * entities which is provided by this class.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 * @since 3.7
 */
public abstract class StanfordNlpHtmlHandlingSupportedTokenizer implements Tokenizer {

    /**
     * The {@code TokenizerFactory} instance which is defined in the specific implementation of this class.
     */
    private final TokenizerFactory<CoreLabel> m_tokenizer;

    /**
     * Creates a new instance of {@code StanfordNlpHtmlHandlingSupportedTokenizer}.
     *
     * @param tokenizer The {@code TokenizerFactory}.
     */
    public StanfordNlpHtmlHandlingSupportedTokenizer(final TokenizerFactory<CoreLabel> tokenizer) {
        m_tokenizer = tokenizer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> tokenize(final String sentence) {
        if (m_tokenizer != null) {
            try (final StringReader readString = new StringReader(sentence)) {
                final edu.stanford.nlp.process.Tokenizer<CoreLabel> tokenizer = m_tokenizer.getTokenizer(readString);
                final List<CoreLabel> tokList = tokenizer.tokenize();
                final List<String> tokenList = new ArrayList<>();
                int previousTokenEnd = 0;
                for (final CoreLabel tok : tokList) {
                    final String token = tok.originalText();
                    final int currentTokenStart = sentence.indexOf(token, previousTokenEnd);
                    if (currentTokenStart < 0) {
                        throw new RuntimeException(
                            "The token " + token + " cannot be found in the sentence: \"" + sentence + "\"!");
                    }

                    // check if there is an untokenized part in front of the current token
                    if (currentTokenStart > previousTokenEnd) {
                        final String skippedPart = sentence.substring(previousTokenEnd, currentTokenStart);
                        if (!skippedPart.trim().isEmpty()) {
                            // process the skipped part if it is not just a whitespace
                            splitSkippedWordAndAdd(skippedPart, tokenList);
                        }
                    }
                    tokenList.add(token);
                    previousTokenEnd = currentTokenStart + token.length();
                }
                return tokenList;
            }
        } else {
            return null;
        }
    }

    /**
     * Splits a string around matches of whitespace or ;& (html) and adds them to the provided token list.
     *
     * @param skippedWord the skipped word to be split
     * @param tokenList the list the split strings are added to
     */
    private static void splitSkippedWordAndAdd(final String skippedWord, final List<String> tokenList) {
        final String[] split = skippedWord.split("\\s+");
        for (final String token : split) {
            if (token.contains(";&")) {
                tokenList.addAll(rebuildHTMLEntity(token.split(";&")));
            } else {
                tokenList.add(token);
            }
        }
    }

    /**
     * Rebuilds a html entity from a given array of tokens.
     *
     * @param tokens the array of tokens
     * @return a list containing the re-constructed html entries
     */
    private static List<String> rebuildHTMLEntity(final String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0) {
                tokens[i] = tokens[i] + ";";
            } else if (i == (tokens.length - 1)) {
                tokens[i] = "&" + tokens[i];
            } else {
                tokens[i] = "&" + tokens[i] + ";";
            }
        }
        return Arrays.asList(tokens);
    }

}

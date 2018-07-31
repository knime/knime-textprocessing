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
 *   30.08.2016 (Julian): created
 */
package org.knime.ext.textprocessing.language.spanish.nodes.tokenization.tokenizer.word;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;

import edu.stanford.nlp.international.spanish.process.SpanishTokenizer;
import edu.stanford.nlp.international.spanish.process.SpanishTokenizer.SpanishTokenizerFactory;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * A tokenizer that detects Spanish words. It provides each word as one token.
 * This word tokenizer is based on the "StanfordNLP Spanish Tokenizer" model, which is created on top of the
 * "StanfordNLP PTB Tokenizer" model.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
public class StanfordNlpSpanishTokenizer implements Tokenizer {

    private SpanishTokenizerFactory<CoreLabel> m_tokenizer;

    /**
     * Creates a new instance of {@code StanfordNlpSpanishTokenizer}
     */
    public StanfordNlpSpanishTokenizer() {
        m_tokenizer = (SpanishTokenizerFactory<CoreLabel>)SpanishTokenizer.coreLabelFactory();
        m_tokenizer.setOptions(
            "normalizeAmpersandEntity=false,"
            + "normalizeFractions=false,"
            + "normalizeParentheses=false,"
            + "normalizeOtherBrackets=false,"
            + "ptb3Ellipsis=false,"
            + "unicodeEllipsis=false,"
            + "ptb3Dashes=false,"
            + "escapeForwardSlashAsterisk=false,"
            + "splitAll=false,"
            + "tokenizeNLs=false,"
            + "strictTreebank3=true");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> tokenize(final String sentence) {
        if (m_tokenizer != null) {
            StringReader readString = new StringReader(sentence);
            SpanishTokenizer<CoreLabel> tokenizer = (SpanishTokenizer<CoreLabel>)m_tokenizer.getTokenizer(readString);
            List<CoreLabel> coreLabelList = tokenizer.tokenize();
            List<String> tokenList = new ArrayList<String>();
            String cpySentence = sentence;
            for (CoreLabel cl : coreLabelList) {
                String token = cl.originalText();
                cpySentence = cpySentence.trim();

                // check if the next part of the sentence is the current word and add it if it is the case
                if (token.equals(cpySentence.substring(0, token.length()))) {
                    tokenList.add(token);
                    cpySentence = cpySentence.substring(token.length(), cpySentence.length());
                } else {
                    // else normalize the word
                    String normToken = StringEscapeUtils.escapeHtml4(token);
                    // check if the next part of the sentence is the normalized word and add it if it is the case
                    if (normToken.equals(cpySentence.substring(0, normToken.length()))) {
                        tokenList.add(normToken);
                        cpySentence = cpySentence.substring(normToken.length(), cpySentence.length());
                    } else {
                        // if the sentence doesn't start with the tokenized word or the normalized word, look up the
                        // position of the word / normalized word add the skipped part as tokens as well as token that
                        // has been found behind the skipped part
                        cpySentence = handleSkippedParts(readString, tokenList, cpySentence, token, normToken);
                    }
                }
            }
            return tokenList;
        } else {
            return null;
        }
    }

    private static String handleSkippedParts(StringReader sr, List<String> tokenList, String sentence,
        String token, String normalizedToken) {
        // add untokenized parts as token (this happens if there is a whitespace HTML entity in the text
        // e.g. "&nbsp;" - the tokenizer will detect it as a whitespace and will not add to the word
        // list, but we want it as a token, so we add the skipped part manually)
        // -> get indices of word and normalized word
        int wordStart = sentence.indexOf(token);
        int normWordStart = sentence.indexOf(normalizedToken);

        // if normalized word exists in sentence and its index is smaller than the index of the
        // tokenized word, check for the normalized word. also check for the normalized word, if only
        // the normalized word exists in the text. in other cases (normWordStart < 0 or normWordStart >
        // wordStart) the tokenized word will be used for checking.
        if ((wordStart >= 0 && normWordStart >= 0 && wordStart > normWordStart)
            || (wordStart < 0 && normWordStart >= 0)) {
            wordStart = normWordStart;
            token = normalizedToken;
        } else if (wordStart < 0 && normWordStart < 0) {
            // if neither the tokenized word nor the normalized word could be found throw an exception
            sr.close();
            throw new RuntimeException("The token " + token + " / " + normalizedToken
                + " cannot be found in the sentence: \"" + sentence + "\"!");
        }
        // get the skipped part
        String skippedWord = sentence.substring(0, wordStart).trim();
        // transform the skipped part into useful tokens and add them to the token list
        splitSkippedWordAndAdd(skippedWord, tokenList);

        tokenList.add(token);
        return sentence.substring(wordStart + token.length());
    }

    // split by whitespace and ;& for the case of html entities
    private static void splitSkippedWordAndAdd(String skippedWord, List<String> tokenList) {
        String[] split = skippedWord.split("\\s+");
        for (String token : split) {
            if (token.contains(";&")) {
                tokenList.addAll(rebuildHTMLEntity(token.split(";&")));
            } else {
                tokenList.add(token);
            }
        }
    }

    private static List<String> rebuildHTMLEntity(String[] tokens) {
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0) {
                tokens[i] = tokens[i] + ";";
            } else if (i == tokens.length - 1) {
                tokens[i] = "&" + tokens[i];
            } else {
                tokens[i] = "&" + tokens[i] + ";";
            }
        }
        return Arrays.asList(tokens);
    }
}

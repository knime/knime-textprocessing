/*
 * ------------------------------------------------------------------------
 *
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
 *   10.03.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.data.hittisau.legancy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.TagBuilder;
import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;

/**
 *
 * @author Kilian
 */
public final class DocumentBuilderLegacy {

    public static final DocumentLegacy createDocument(final String title, final String text) {

        if (text != null && !text.isEmpty()) {

            final UUID uuid = UUID.randomUUID();
            final String tit;
            if (title == null) {
                tit = uuid.toString();
            } else {
                tit = title;
            }

            final DocumentMetaInfo metaInfo = new DocumentMetaInfo();

            int numberOfTerms = 0;
            InternalTerm[][] internalSentences;
            TagBuilder[] internalTagBuilder = new TagBuilder[0];
            String[] terms;
            String[] whiteSpaces;

            // tokenize sentences
            List<String> strSentences = DefaultTokenization.getSentenceTokenizer().tokenize(text);

            internalSentences = new InternalTerm[strSentences.size()][];
            int currentTermIndex = 0;
            int currentWsIndex = 0;
            Map<String, Integer> uniqueTerms = new LinkedHashMap<>();
            Map<String, Integer> uniqueWs = new LinkedHashMap<>();

            String cpyText = text;
            for (int i = 0; i < strSentences.size(); i++) {
                final String sentence = strSentences.get(i);
                final String whiteSpaceSuffixSentence;
                String nextSentence = null;
                if (i < strSentences.size() - 1) {
                    nextSentence = strSentences.get(i + 1);
                }

                // extract whitespace suffix characters
                int tokenStart = cpyText.indexOf(sentence);
                cpyText = cpyText.substring(tokenStart + sentence.length());

                if (nextSentence != null) {
                    int nextTokenStart = cpyText.indexOf(nextSentence);
                    whiteSpaceSuffixSentence = cpyText.substring(0, nextTokenStart);
                    cpyText = cpyText.substring(nextTokenStart);
                } else {
                    if (cpyText.length() > 0) {
                        whiteSpaceSuffixSentence = cpyText;
                    } else {
                        whiteSpaceSuffixSentence = "";
                    }
                }

                final String sentenceStrWithWs = sentence + whiteSpaceSuffixSentence;

                // tokenize words
                List<String> tokens = DefaultTokenization.getWordTokenizer().tokenize(sentenceStrWithWs);

                // internal sentence
                InternalTerm[] sentenceIT = new InternalTerm[tokens.size()];
                numberOfTerms += tokens.size();

                String cpySentence = sentenceStrWithWs;
                for (int j = 0; j < tokens.size(); j++) {
                    final String token = tokens.get(i);
                    final String whiteSpaceSuffix;
                    String nextToken = null;
                    if (i < tokens.size() - 1) {
                        nextToken = tokens.get(i + 1);
                    }

                    // extract whitespace suffix characters
                    tokenStart = cpySentence.indexOf(token);
                    cpySentence = cpySentence.substring(tokenStart + token.length());

                    if (nextToken != null) {
                        int nextTokenStart = cpySentence.indexOf(nextToken);
                        whiteSpaceSuffix = cpySentence.substring(0, nextTokenStart);
                        cpySentence = cpySentence.substring(nextTokenStart);
                    } else {
                        if (cpySentence.length() > 0) {
                            whiteSpaceSuffix = cpySentence;
                        } else {
                            whiteSpaceSuffix = "";
                        }
                    }

                    // add word if it does not exist
                    if (!uniqueTerms.containsKey(token)) {
                        uniqueTerms.put(token, currentTermIndex);
                        currentTermIndex++;
                    }
                    // add white space if it does not exist
                    if (!uniqueWs.containsKey(whiteSpaceSuffix)) {
                        uniqueWs.put(whiteSpaceSuffix, currentWsIndex);
                        currentWsIndex++;
                    }

                    final int tidx = uniqueTerms.get(token);
                    final int widx = uniqueWs.get(whiteSpaceSuffix);
                    sentenceIT[j] = new InternalTerm(tidx, widx, null, false);
                }

                internalSentences[i] = sentenceIT;
            }

            terms = new String[uniqueTerms.size()];
            for (Entry<String, Integer> e : uniqueTerms.entrySet()) {
                terms[e.getValue()] = e.getKey();
            }

            whiteSpaces = new String[uniqueWs.size()];
            for (Entry<String, Integer> e : uniqueWs.entrySet()) {
                whiteSpaces[e.getValue()] = e.getKey();
            }

            return new DocumentLegacy(uuid, title, numberOfTerms, metaInfo, terms, whiteSpaces, internalTagBuilder,
                internalSentences);
        }

        return null;
    }
}

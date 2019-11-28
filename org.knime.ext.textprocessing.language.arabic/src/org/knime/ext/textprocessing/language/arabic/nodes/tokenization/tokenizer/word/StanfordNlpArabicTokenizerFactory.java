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
 *   22.11.2019 (Julian Bunzel, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.ext.textprocessing.language.arabic.nodes.tokenization.tokenizer.word;

import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactory;

/**
 * This factory class creates instances of the Arabic word tokenizer {@code StanfordNlpArabicTokenizer}.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public final class StanfordNlpArabicTokenizerFactory implements TokenizerFactory {

    private static final String TOKENIZER_NAME = "StanfordNLP ArabicTokenizer";

    private static final String TOKENIZER_DESC =
        "Arabic is a root-and-template language with abundant bound clitics. \n"
            + "These clitics include possessives, pronouns, and discourse connectives. \n"
            + "The Arabic segmenter segments clitics from words (only). Segmenting clitics attached to words reduces lexical sparsity and simplifies syntactic analysis.\r\n"
            + "The Arabic segmenter model processes raw text according to the Penn Arabic Treebank 3 (ATB) standard.";

    private static final String TOKENIZER_DESC_LINK = "https://nlp.stanford.edu/software/segmenter.html";

    @Override
    public Tokenizer getTokenizer() {
        return new StanfordNlpArabicTokenizer();
    }

    @Override
    public String getTokenizerName() {
        return TOKENIZER_NAME;
    }

    @Override
    public String getTokenizerDescription() {
        return TOKENIZER_DESC;
    }

    @Override
    public String getTokenizerDescLink() {
        return TOKENIZER_DESC_LINK;
    }

}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * History
 *   05.09.2016 (Julian): created
 */
package org.knime.ext.textprocessing.language.spanish.nodes.tokenization.tokenizer.word;

import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactory;

/**
 * This factory class creates instances of the Spanish word tokenizer {@code StanfordNlpSpanishTokenizer}.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
public class StanfordNlpSpanishTokenizerFactory implements TokenizerFactory {

    /**
     * Creates a new instance of the {@code StanfordNlpSpanishTokenizerFactory}.
     */
    public StanfordNlpSpanishTokenizerFactory() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tokenizer getTokenizer() {
        return new StanfordNlpSpanishTokenizer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenizerName() {
        return "StanfordNLP SpanishTokenizer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenizerDescription() {
        return "\"Tokenizer for raw Spanish text. This tokenization scheme is a derivative of PTB tokenization, \n"
            + "but with extra rules for Spanish contractions and assimilations. \n"
            + "It is based heavily on the FrenchTokenizer. \n"
            + "The tokenizer tokenizes according to the modified AnCora corpus tokenization standards, \n"
            + "so the rules are a little different from PTB.\"";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenizerDescLink() {
        return "http://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/international/spanish/process/SpanishTokenizer.html";
    }

}

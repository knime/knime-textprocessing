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
 *   05.04.2017 (Julian): created
 */
package org.knime.ext.textprocessing.language.chinese.nodes.tokenization.tokenizer.word;

import java.util.List;
import java.util.Properties;

import org.knime.ext.textprocessing.language.chinese.TextprocessingChineseLanguagePack;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * A tokenizer that detects Chinese words. It provides each word as one token.
 * This word tokenizer is based on the "StanfordNLP Chinese Tokenizer" model, which is created on top of the
 * "StanfordNLP PTB Tokenizer" model.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
public class StanfordNlpChineseTokenizer implements Tokenizer {
    private final CRFClassifier<CoreLabel> m_tokenizer;

    private static final String BASEDIR =
        TextprocessingChineseLanguagePack.resolvePath("models/stanfordmodels/tokenizer/data").getAbsolutePath();

    private static final Properties PROPERTIES = createProperties();

    /**
     * Creates a new tokenizer.
     */
    public StanfordNlpChineseTokenizer() {
        m_tokenizer = new CRFClassifier<>(PROPERTIES);
        m_tokenizer.loadClassifierNoExceptions(BASEDIR + "/ctb.gz", PROPERTIES);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<String> tokenize(final String sentence) {
        return m_tokenizer.segmentString(sentence);
    }

    private static Properties createProperties() {
        Properties props = new Properties();
        props.setProperty("sighanCorporaDict", BASEDIR);
        props.setProperty("NormalizationTable", BASEDIR + "/norm.simp.utf8");
        props.setProperty("normTableEncoding", "UTF-8");
        // below is needed because CTBSegDocumentIteratorFactory accesses it
        props.setProperty("serDictionary", BASEDIR + "/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");
        props.setProperty("keepAllWhitespaces", "true");
        return props;
    }
}

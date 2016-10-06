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
package org.knime.ext.textprocessing.nodes.tokenization.tokenizer.sentence;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

/**
 * A tokenizer which is able to detect sentences and and provides a tokenization
 * resulting each sentence as one token.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class OpenNlpSentenceTokenizer implements Tokenizer {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            OpenNlpSentenceTokenizer.class);

    private SentenceDetector m_tokenizer;

    /**
     * Creates a new instance of <code>OpenNlpSentenceTokenizer</code>.
     */
    public OpenNlpSentenceTokenizer() {
        try {
            String modelPath = OpenNlpModelPaths.getOpenNlpModelPaths()
            .getSentenceModelFile();
            InputStream is = new FileInputStream(new File(modelPath));
            SentenceModel model = new SentenceModel(is);
            m_tokenizer = new SentenceDetectorME(model);
        } catch (IOException e) {
            LOGGER.error("Could not create OpenNlpSentenceTokenizer since"
                    + "model could not be red!");
            LOGGER.error(e.getStackTrace());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<String> tokenize(final String text) {
        if (m_tokenizer != null) {
            return Arrays.asList(m_tokenizer.sentDetect(text));
        }
        return null;
    }

}

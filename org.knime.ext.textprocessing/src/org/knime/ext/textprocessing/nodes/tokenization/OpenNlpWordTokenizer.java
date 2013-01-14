/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.tokenize.TokenizerModel;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

/**
 * A tokenizer which is able to detect words and and provides a tokenization
 * resulting each word as one token.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class OpenNlpWordTokenizer implements Tokenizer {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(
            OpenNlpWordTokenizer.class);

    private opennlp.tools.tokenize.Tokenizer m_tokenizer;

    /**
     * Creates new instance of <code>OpenNlpWordTokenizer</code>.
     */
    public OpenNlpWordTokenizer() {
        try {
            String modelPath = OpenNlpModelPaths.getOpenNlpModelPaths()
            .getTokenizerModelFile();
            InputStream is = new FileInputStream(new File(modelPath));
            TokenizerModel model = new TokenizerModel(is);
            m_tokenizer = new opennlp.tools.tokenize.TokenizerME(model);
        } catch (IOException e) {
            LOGGER.error("Sentence tokenizer model could not be red!");
            LOGGER.error(e.getStackTrace());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<String> tokenize(final String sentence) {
        if (m_tokenizer != null) {
            return Arrays.asList(m_tokenizer.tokenize(sentence));
        }
        return null;
    }

}

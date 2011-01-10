/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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

import opennlp.tools.lang.english.SentenceDetector;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
            m_tokenizer = new SentenceDetector(modelPath);
        } catch (IOException e) {
            LOGGER.error("Could not create OpenNlpSentenceTokenizer since"
                    + "model could not be red!");
            LOGGER.error(e.getStackTrace());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized List<String> tokenize(final String text) {
        if (m_tokenizer != null) {
            return Arrays.asList(m_tokenizer.sentDetect(text));
        }
        return null;
    }

}

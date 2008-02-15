/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 * 
 * History
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tokenization;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.lang.english.SentenceDetector;

import org.knime.core.node.NodeLogger;
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
            m_tokenizer = new SentenceDetector(
                    OpenNlpModelPaths.SENTENCE_MODEL_FILE);
        } catch (IOException e) {
            LOGGER.error("Could not create OpenNlpSentenceTokenizer since" +
            		"model could not be red!");
            LOGGER.error(e.getStackTrace());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public List<String> tokenize(final String text) {
        if (m_tokenizer != null) {
            return Arrays.asList(m_tokenizer.sentDetect(text));
        }
        return null;
    }

}

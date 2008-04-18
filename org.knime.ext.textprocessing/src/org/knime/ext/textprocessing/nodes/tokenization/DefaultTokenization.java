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

import java.util.List;


/**
 * Is a utility class which provides methods for the default tokenization of
 * {@link org.knime.ext.textprocessing.data.Document}s. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DefaultTokenization {
    
    /**
     * The default sentence tokenizer.
     */
    public static final Tokenizer SENTENCE_TOKENIZER = 
        new OpenNlpSentenceTokenizer();
    
    /**
     * The default word tokenizer.
     */
    public static final Tokenizer WORD_TOKENIZER = 
        new OpenNlpWordTokenizer();
    
    
    private DefaultTokenization() { }
    
    
    /**
     * Provides the default sentence tokenization, which detects sentences
     * in the given text  and returns them as a list of strings, each sentences 
     * as one string.
     * 
     * @param text The text to tokenize.
     * @return The list of sentences as strings.
     */
    public static List<String> detectSentences(final String text) {
        return SENTENCE_TOKENIZER.tokenize(text);
    }
    
    /**
     * Provides the default word tokenization, which detects the words of the
     * given sentence and returns them as a list of strings, each word as one
     * string.
     * 
     * @param sentence The sentence to tokenize.
     * @return The tokenzied sentence as a list of tokens.
     */
    public static List<String> tokenizeSentence(final String sentence) {
        return WORD_TOKENIZER.tokenize(sentence);
    }
}

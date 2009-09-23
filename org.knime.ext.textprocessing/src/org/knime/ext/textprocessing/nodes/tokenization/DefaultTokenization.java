/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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



/**
 * Is a utility class which provides methods for the default tokenization of
 * {@link org.knime.ext.textprocessing.data.Document}s. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DefaultTokenization {
        
    private static final OpenNlpWordTokenizer WORD_TOKENIZER = 
        new OpenNlpWordTokenizer();
    
    private static final OpenNlpSentenceTokenizer SENTENCE_TOKENIZER = 
        new OpenNlpSentenceTokenizer();
    
    private DefaultTokenization() { }
    
    /**
     * @return The default sentence tokenizer.
     */
    public static final Tokenizer getSentenceTokenizer() {
        return SENTENCE_TOKENIZER;
    }

    /**
     * @return The default word tokenizer.
     */
    public static final Tokenizer getWordTokenizer() {
        return WORD_TOKENIZER; 
    }
}

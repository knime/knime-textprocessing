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
package org.knime.ext.textprocessing.util;

/**
 * Provides the paths to the models used by the OpenNlp library.
 * 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class OpenNlpModelPaths {

    private OpenNlpModelPaths() { }
    
    /**
     * The path to the sentence detection model.
     */
    public static final String SENTENCE_MODEL_FILE = 
        "E:\\downloads\\Textmining\\OpenNLP\\Models\\sentdetect\\EnglishSD.bin";
    
    /**
     * The path to the tokenizer model file.
     */
    public static final String TOKENIZER_MODEL_FILE = 
        "E:\\downloads\\Textmining\\OpenNLP\\Models\\tokenize\\EnglishTok.bin";
}

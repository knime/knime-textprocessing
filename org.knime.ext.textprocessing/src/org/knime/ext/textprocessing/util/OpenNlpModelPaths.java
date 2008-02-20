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
 *   14.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.TextprocessingPlugin;



/**
 * Provides the paths to the models used by the OpenNlp library. The paths 
 * are based on the root directory of the Textprocessing plugin, which is
 * held in the plugin's activator class 
 * {@link org.knime.ext.textprocessing.TextprocessingPlugin}. Without the 
 * activation of the plugin (which is usually done automatically by eclipse 
 * by the creation of an instance of the activator class) the plugin root path
 * and the model paths can not be created / provided.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class OpenNlpModelPaths {

    private static OpenNlpModelPaths instance = null;

    private static final String SENTENCE_MODEL_POSTFIX = 
        "/resources/opennlpmodels/sentdetect/EnglishSD.bin"; 

    private static final String TOKENIZATION_MODEL_POSTFIX = 
        "/resources/opennlpmodels/tokenize/EnglishTok.bin";    
    
    /**
     * The path to the sentence detection model.
     */
    private String m_sentenceModelFile;
    
    /**
     * The path to the tokenizer model file.
     */
    private String m_tokenizerModelFile;    
    
    
    /**
     * @return The singleton <code>OpenNlpModelPaths</code> instance holding
     * the paths to the OpenNlp models.
     */
    public static OpenNlpModelPaths getOpenNlpModelPaths() {
        if (instance == null) {
            TextprocessingPlugin plugin = TextprocessingPlugin.getDefault();
            String pluginPath = plugin.getPluginRootPath();
            String sentModelPath = pluginPath + SENTENCE_MODEL_POSTFIX;
            String tokModelPath = pluginPath + TOKENIZATION_MODEL_POSTFIX;
            
            instance = new OpenNlpModelPaths(sentModelPath, tokModelPath);
        }
        return instance;
    }
    
    private OpenNlpModelPaths(final String tokModel, final String sentDetModel) 
    { 
        m_tokenizerModelFile = tokModel;
        m_sentenceModelFile = sentDetModel;
    }

    /**
     * @return the model file of the sentence detection model.
     */
    public String getSentenceModelFile() {
        return m_sentenceModelFile;
    }

    /**
     * @return the model file of the tokenization model.
     */
    public String getTokenizerModelFile() {
        return m_tokenizerModelFile;
    }
    

}

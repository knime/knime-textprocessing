/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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

import org.knime.ext.textprocessing.TextprocessingCorePlugin;



/**
 * Provides the paths to the models used by the OpenNlp library. The paths 
 * are based on the root directory of the Textprocessing plugin, which is
 * held in the plugin's activator class 
 * {@link org.knime.ext.textprocessing.TextprocessingCorePlugin}. Without the 
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
    
    private static final String POS_MODEL_POSTFIX = 
        "/resources/opennlpmodels/pos/tag.bin";
    
    private static final String POS_DICT_POSTFIX = 
        "/resources/opennlpmodels/pos/tagdict";
    
    /**
     * The base path to the models.
     */
    private String m_basePath;
    
    /**
     * @return The singleton <code>OpenNlpModelPaths</code> instance holding
     * the paths to the OpenNlp models.
     */
    public static OpenNlpModelPaths getOpenNlpModelPaths() {
        if (instance == null) {
            TextprocessingCorePlugin plugin = 
                TextprocessingCorePlugin.getDefault();
            String pluginPath = plugin.getPluginRootPath();            
            instance = new OpenNlpModelPaths(pluginPath);
        }
        return instance;
    }
    
    private OpenNlpModelPaths(final String basePath) { 
        m_basePath = basePath;
    }

    /**
     * @return the model file of the sentence detection model.
     */
    public String getSentenceModelFile() {
        return m_basePath + SENTENCE_MODEL_POSTFIX;
    }

    /**
     * @return the model file of the tokenization model.
     */
    public String getTokenizerModelFile() {
        return m_basePath + TOKENIZATION_MODEL_POSTFIX;
    }
    
    /**
     * @return the model file of the pos tagger model.
     */
    public String getPosTaggerModelFile() {
        return m_basePath + POS_MODEL_POSTFIX;
    }
    
    /**
     * @return the model file of the pos tagger tag dictionary.
     */
    public String getPosTaggerDictFile() {
        return m_basePath + POS_DICT_POSTFIX;
    }    
}

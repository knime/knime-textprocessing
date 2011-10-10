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
 *   14.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.TextprocessingCorePlugin;



/**
 * Provides the paths to the models used by the Stanford POS library. The paths 
 * are based on the root directory of the Textprocessing plugin, which is
 * held in the plugin's activator class 
 * {@link org.knime.ext.textprocessing.TextprocessingCorePlugin}. Without the 
 * activation of the plugin (which is usually done automatically by eclipse 
 * by the creation of an instance of the activator class) the plugin root path
 * and the model paths can not be created / provided.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class StanfordModelPaths {

    private static StanfordModelPaths instance = null;

    private static final String BIDIRECTIONAL_POS = 
        "/resources/stanfordmodels/bidirectional-distsim-wsj-0-18.tagger";

    private static final String LEFT3WORDS_POS = 
        "/resources/stanfordmodels/left3words-wsj-0-18.tagger";
    
    private static final String LEFT3WORDS_DISTSIM_POS = 
        "/resources/stanfordmodels/left3words-distsim-wsj-0-18.tagger";
    
    private static final String GERMAN_FAST_POS = 
        "/resources/stanfordmodels/german-fast.tagger";
    
    private static final String GERMAN_ACCURATE_POS = 
        "/resources/stanfordmodels/german-accurate.tagger";    
    
    /**
     * The base path to the models.
     */
    private String m_basePath;
    
    /**
     * @return The singleton <code>StanfordModelPaths</code> instance holding
     * the paths to the OpenNlp models.
     */
    public static StanfordModelPaths getStanfordModelPaths() {
        if (instance == null) {
            TextprocessingCorePlugin plugin = 
                TextprocessingCorePlugin.getDefault();
            String pluginPath = plugin.getPluginRootPath();            
            instance = new StanfordModelPaths(pluginPath);
        }
        return instance;
    }
    
    private StanfordModelPaths(final String basePath) { 
        m_basePath = basePath;
    }

    /**
     * @return the model file of the german accurate tagger.
     */
    public String getGermanAccuratePosModelFile() {
        return m_basePath + GERMAN_ACCURATE_POS;
    }     
    
    /**
     * @return the model file of the german fast tagger.
     */
    public String getGermanFastPosModelFile() {
        return m_basePath + GERMAN_FAST_POS;
    }     
    
    /**
     * @return the model file of the left 3 words distsim tagger.
     */
    public String getLeft3WordsDistSimPosModelFile() {
        return m_basePath + LEFT3WORDS_DISTSIM_POS;
    }    
    
    /**
     * @return the model file of the left 3 words tagger.
     */
    public String getLeft3WordsPosModelFile() {
        return m_basePath + LEFT3WORDS_POS;
    }
    
    /**
     * @return the model file of the bidirectional pos tagger.
     */
    public String getBidirectionalPosModelFile() {
        return m_basePath + BIDIRECTIONAL_POS;
    }
}

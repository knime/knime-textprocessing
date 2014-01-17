/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

    private static final String GERMAN_FAST_POS =
        "/resources/stanfordmodels/german-fast.tagger";

    private static final String GERMAN_DEWAC_POS =
        "/resources/stanfordmodels/german-dewac.tagger";

    private static final String GERMAN_HGC_POS =
            "/resources/stanfordmodels/german-hgc.tagger";

    private static final String FRENCH_POS =
            "/resources/stanfordmodels/french.tagger";

    private static final String ENGLISH_BIDIRECTIONAL_POS =
            "/resources/stanfordmodels/english-bidirectional-distsim.tagger";

    private static final String ENGLISH_CASELESS_LEFT3WORDS_POS =
            "/resources/stanfordmodels/english-caseless-left3words-distsim.tagger";

    private static final String ENGLISH_LEFT3WORDS_POS =
            "/resources/stanfordmodels/english-left3words-distsim.tagger";


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
     * @return the model file of the french tagger.
     * @since 2.8
     */
    public String getFrenchPosModelFile() {
        return m_basePath + FRENCH_POS;
    }

    /**
     * @return the model file of the german accurate tagger. The model is the
     * same as the hgc model.
     * @deprecated use {@link StanfordModelPaths#getGermanHgcPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getGermanAccuratePosModelFile() {
        return m_basePath + GERMAN_HGC_POS;
    }

    /**
     * @return the model file of the german hgc tagger.
     * @since 2.8
     */
    public String getGermanHgcPosModelFile() {
        return m_basePath + GERMAN_HGC_POS;
    }

    /**
     * @return the model file of the german hgc tagger.
     * @since 2.8
     */
    public String getGermanDewacPosModelFile() {
        return m_basePath + GERMAN_DEWAC_POS;
    }

    /**
     * @return the model file of the german fast tagger.
     */
    public String getGermanFastPosModelFile() {
        return m_basePath + GERMAN_FAST_POS;
    }

    /**
     * @return the model file of the english left 3 words tagger.
     * @since 2.8
     */
    public String getEnglishLeft3WordsCaselessPosModelFile() {
        return m_basePath + ENGLISH_CASELESS_LEFT3WORDS_POS;
    }

    /**
     * @return the model file of the left 3 words distsim tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishLeft3WordsPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getLeft3WordsDistSimPosModelFile() {
        return m_basePath + ENGLISH_LEFT3WORDS_POS;
    }

    /**
     * @return the model file of the english left 3 words tagger.
     * @since 2.8
     */
    public String getEnglishLeft3WordsPosModelFile() {
        return m_basePath + ENGLISH_LEFT3WORDS_POS;
    }

    /**
     * @return the model file of the left 3 words tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishLeft3WordsPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getLeft3WordsPosModelFile() {
        return m_basePath + ENGLISH_LEFT3WORDS_POS;
    }

    /**
     * @return the model file of the english bidirectional pos tagger.
     * @since 2.8
     */
    public String getEnglishBidirectionalPosModelFile() {
        return m_basePath + ENGLISH_BIDIRECTIONAL_POS;
    }

    /**
     * @return the model file of the bidirectional pos tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishBidirectionalPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getBidirectionalPosModelFile() {
        return m_basePath + ENGLISH_BIDIRECTIONAL_POS;
    }
}

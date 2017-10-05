/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   14.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModel;
import org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger.nermodels.English3ClassesDistsimModel;

/**
 * Provides the paths to the models used by the Stanford POS library. The paths
 * are based on the root directory of the Textprocessing plugin, which is
 * held in the plugin's activator class
 * {@link org.knime.ext.textprocessing.TextprocessingCorePlugin}. Without the
 * activation of the plugin (which is usually done automatically by eclipse
 * by the creation of an instance of the activator class) the plugin root path
 * and the model paths cannot be created / provided.
 *
 * @author Kilian Thiel, University of Konstanz
 * @deprecated Use {@link StanfordTaggerModel} interface and extension point to provide Stanford tagger models instead
 * of using static variables (e.g. {@link English3ClassesDistsimModel}.
 */
@Deprecated
public final class StanfordModelPaths {
    private static final StanfordModelPaths INSTANCE = new StanfordModelPaths();

    private static final String GERMAN_FAST_POS = "stanfordmodels/pos/german-fast.tagger";

    private static final String GERMAN_DEWAC_POS = "stanfordmodels/pos/german-dewac.tagger";

    private static final String GERMAN_HGC_POS = "stanfordmodels/pos/german-hgc.tagger";

    private static final String FRENCH_POS = "stanfordmodels/pos/french.tagger";

    private static final String ENGLISH_BIDIRECTIONAL_POS = "stanfordmodels/pos/english-bidirectional-distsim.tagger";

    private static final String ENGLISH_CASELESS_LEFT3WORDS_POS =
        "stanfordmodels/pos/english-caseless-left3words-distsim.tagger";

    private static final String ENGLISH_LEFT3WORDS_POS = "stanfordmodels/pos/english-left3words-distsim.tagger";

    /**
     * @return The singleton <code>StanfordModelPaths</code> instance holding
     * the paths to the OpenNlp models.
     */
    public static StanfordModelPaths getStanfordModelPaths() {
        return INSTANCE;
    }

    private StanfordModelPaths() {}

    /**
     * @return the model file of the french tagger.
     * @since 2.8
     */
    public String getFrenchPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(FRENCH_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the german accurate tagger. The model is the
     * same as the hgc model.
     * @deprecated use {@link StanfordModelPaths#getGermanHgcPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getGermanAccuratePosModelFile() {
        return TextprocessingCorePlugin.resolvePath(GERMAN_HGC_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the german hgc tagger.
     * @since 2.8
     */
    public String getGermanHgcPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(GERMAN_HGC_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the german hgc tagger.
     * @since 2.8
     */
    public String getGermanDewacPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(GERMAN_DEWAC_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the german fast tagger.
     */
    public String getGermanFastPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(GERMAN_FAST_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the english left 3 words tagger.
     * @since 2.8
     */
    public String getEnglishLeft3WordsCaselessPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_CASELESS_LEFT3WORDS_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the left 3 words distsim tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishLeft3WordsPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getLeft3WordsDistSimPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_LEFT3WORDS_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the english left 3 words tagger.
     * @since 2.8
     */
    public String getEnglishLeft3WordsPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_LEFT3WORDS_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the left 3 words tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishLeft3WordsPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getLeft3WordsPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_LEFT3WORDS_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the english bidirectional pos tagger.
     * @since 2.8
     */
    public String getEnglishBidirectionalPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_BIDIRECTIONAL_POS).getAbsolutePath();
    }

    /**
     * @return the model file of the bidirectional pos tagger.
     * @deprecated use
     * {@link StanfordModelPaths#getEnglishBidirectionalPosModelFile()}
     * instead.
     */
    @Deprecated
    public String getBidirectionalPosModelFile() {
        return TextprocessingCorePlugin.resolvePath(ENGLISH_BIDIRECTIONAL_POS).getAbsolutePath();
    }
}

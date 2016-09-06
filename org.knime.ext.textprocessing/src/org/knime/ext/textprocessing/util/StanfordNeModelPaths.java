/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   30.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.3
 */
public final class StanfordNeModelPaths {

    private static StanfordNeModelPaths instance = null;

    private static final String ENGLISH_ALL_3CLASS_DISTSIM =
        "/resources/stanfordmodels/nermodels/english.all.3class.distsim.crf.ser.gz";

    private static final String ENGLISH_CONLL_4CLASS_DISTSIM =
        "/resources/stanfordmodels/nermodels/english.conll.4class.distsim.crf.ser.gz";

    private static final String ENGLISH_MUC_7CLASS_DISTSIM =
        "/resources/stanfordmodels/nermodels/english.muc.7class.distsim.crf.ser.gz";

    private static final String ENGLISH_ALL_3CLASS_NODISTSIM =
        "/resources/stanfordmodels/nermodels/english.all.3class.nodistsim.crf.ser.gz";

    private static final String ENGLISH_CONLL_4CLASS_NODISTSIM =
        "/resources/stanfordmodels/nermodels/english.conll.4class.nodistsim.crf.ser.gz";

    private static final String ENGLISH_MUC_7CLASS_NODISTSIM =
        "/resources/stanfordmodels/nermodels/english.muc.7class.nodistsim.crf.ser.gz";

    private static final String ENGLISH_NOWIKI_3CLASS_CASELESS_DISTSIM =
        "/resources/stanfordmodels/nermodels/english.nowiki.3class.caseless.distsim.crf.ser.gz";

    private static final String GERMAN_DEWAC_175M_600 =
        "/resources/stanfordmodels/nermodels/german.dewac_175m_600.crf.ser.gz";

    private static final String GERMAN_HGC_175M_600 =
        "/resources/stanfordmodels/nermodels/german.hgc_175m_600.crf.ser.gz";

    private static final String SPANISH_ANCORA_DISTSIM =
        "/resources/stanfordmodels/nermodels/spanish.ancora.distsim.s512.crf.ser.gz";

    /**
     * The base path to the models.
     */
    private String m_basePath;

    /**
     * @return The singleton {@code StanfordNeModelPaths} instance holding the paths to the StanfordNLP NE models.
     */
    public static StanfordNeModelPaths getStanfordNeModelPaths() {
        if (instance == null) {
            TextprocessingCorePlugin plugin = TextprocessingCorePlugin.getDefault();
            String pluginPath = plugin.getPluginRootPath();
            instance = new StanfordNeModelPaths(pluginPath);
        }
        return instance;
    }

    private StanfordNeModelPaths(final String basePath) {
        m_basePath = basePath;
    }

    /**
     * @return the model file of the English 3 class classifier (distsim)
     */
    public String getEnglishAll3ClassDistSimModelFile() {
        return m_basePath + ENGLISH_ALL_3CLASS_DISTSIM;
    }

    /**
     * @return the model file of the English 4 class classifier (distsim)
     */
    public String getEnglishConll4ClassDistSimModelFile() {
        return m_basePath + ENGLISH_CONLL_4CLASS_DISTSIM;
    }

    /**
     * @return the model file of the English 7 class classifier (distsim)
     */
    public String getEnglishMuc7ClassDistSimModelFile() {
        return m_basePath + ENGLISH_MUC_7CLASS_DISTSIM;
    }

    /**
     * @return the model file of the English 3 class classifier (nodistsim)
     */
    public String getEnglishAll3ClassNoDistSimModelFile() {
        return m_basePath + ENGLISH_ALL_3CLASS_NODISTSIM;
    }

    /**
     * @return the model file of the English 4 class classifier (nodistsim)
     */
    public String getEnglishConll4ClassNoDistSimModelFile() {
        return m_basePath + ENGLISH_CONLL_4CLASS_NODISTSIM;
    }

    /**
     * @return the model file of the English 7 class classifier (nodistsim)
     */
    public String getEnglishMuc7ClassNoDistSimModelFile() {
        return m_basePath + ENGLISH_MUC_7CLASS_NODISTSIM;
    }

    /**
     * @return the model file of the English no wiki 3 class classifier (distsim)
     */
    public String getEnglishNoWiki3ClassCaselessDistSimModelFile() {
        return m_basePath + ENGLISH_NOWIKI_3CLASS_CASELESS_DISTSIM;
    }

    /**
     * @return the model file of the German dewac classifier
     */
    public String getGermanDewacModelFile() {
        return m_basePath + GERMAN_DEWAC_175M_600;
    }

    /**
     * @return the model file of the German hgc classifier
     */
    public String getGermanHgcModelFile() {
        return m_basePath + GERMAN_HGC_175M_600;
    }

    /**
     * @return the model file of the Spanish ancora classifier
     */
    public String getSpanishAncoraDistSimModelFile() {
        return m_basePath + SPANISH_ANCORA_DISTSIM;
    }

}

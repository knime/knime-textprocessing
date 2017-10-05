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
 * ------------------------------------------------------------------------
 *
 * History
 *   12.11.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.hyphenator;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.util.HyphenationPatternFilePaths;

/**
 *
 * @author thiel, University of Konstanz
 */
public final class HyphenationPatternFactory {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(HyphenationPatternFactory.class);

    private static HyphenationPatternFactory instance = null;

    private Map<String, HyphenationPatterns> m_models =
        new HashMap<String, HyphenationPatterns>();

    /**
     * Creates and returns a singleton instance of
     * <code>OpenNlpModelFactory</code>.
     *
     * @return a singleton instance of <code>OpenNlpModelFactory</code>.
     */
    public static final HyphenationPatternFactory getInstance() {
        if (instance == null) {
            instance = new HyphenationPatternFactory();
        }
        return instance;
    }

    private HyphenationPatternFactory() {
        LOGGER.info("Registering Hyphenation Patterns ...");

        HyphenationPatternFilePaths paths =
            HyphenationPatternFilePaths.getHyphenationPatternlPaths();

        Set<String> languages = paths.getLanguages();
        for (String l : languages) {
            try {
                m_models.put(l, new HyphenationPatterns(l, new File(paths
                        .getPatternFile(l))));
            } catch (InvalidSettingsException e) {
                LOGGER.warn("Could not load pattern for language [" + l + "]");
            }
        }
    }

    /**
     * Returns the patterns related to the given language.
     * @param lang The language to get the patterns for.
     * @return The related patterns.
     */
    public HyphenationPatterns getPatternsByLanguage(final String lang) {
        return m_models.get(lang);
    }

    /**
     * @return A set of all available languages.
     */
    public Set<String> getLanguages() {
        return m_models.keySet();
    }

    /**
     * @return The language of the default patterns.
     */
    public String getDefaultLanguage() {
        return "English";
    }
}

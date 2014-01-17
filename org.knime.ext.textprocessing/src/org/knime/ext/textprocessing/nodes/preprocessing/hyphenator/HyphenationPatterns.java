/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   13.11.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.hyphenator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;

/**
 *
 * @author thiel, University of Konstanz
 */
public class HyphenationPatterns {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(HyphenationPatterns.class);

    private static final String PATTERNS = "patterns";

    private static final String SHORTEST_PATTERN = "shortestPattern";

    private static final String LONGEST_PATTERN = "longestPattern";


    private Map<String, int[]> m_patterns;

    private String m_language;

    private File m_patternFile;

    /**
     * Creates a new instance of <code>HyphenationPatterns</code> with a given
     * language and file containing the patterns.
     * @param lang The language to set.
     * @param file The file to set containing the patterns.
     * @throws InvalidSettingsException If language or file is <code>null</code>
     * or file is not readable.
     */
    public HyphenationPatterns(final String lang, final File file)
    throws InvalidSettingsException {
        if (file == null) {
            throw new InvalidSettingsException(
                    "Specified pattern file may not be null");
        }
        if (!file.exists() || !file.canRead()) {
            throw new InvalidSettingsException("Specified pattern file ["
                    + file.getAbsolutePath() + "] is not valid!");
        }
        if (lang == null) {
            throw new InvalidSettingsException(
                    "Specified language may not be null!");
        }
        m_patternFile = file;
        m_language = lang;
    }

    /**
     * @return Returns a map with all available patterns.
     */
    public Map<String, int[]> getPatterns() {
        if (m_patterns == null) {
            m_patterns = new HashMap<String, int[]>();
            parsePatterns();
        }
        return m_patterns;
    }

    /**
     * @return The language.
     */
    public String getLanguage() {
        return m_language;
    }

    private void parsePatterns() {
        try {
            LOGGER.info("Loading pattern file ["
                    + m_patternFile.getAbsolutePath() + "] for language ["
                    + m_language + "]");

            JSONTokener jt = new JSONTokener(new FileReader(m_patternFile));
            JSONObject jo = new JSONObject(jt);

            int shortest = jo.getInt(SHORTEST_PATTERN);
            int longest = jo.getInt(LONGEST_PATTERN);
            JSONObject patternObj = jo.getJSONObject(PATTERNS);

            for (int i = shortest + 1; i < longest + 1; i++) {
                String allPatterns = patternObj.getString(Integer.toString(i));

                int start = 0;
                while (start < allPatterns.length()) {
                    if (start <= allPatterns.length()
                            && start + i <= allPatterns.length()) {
                        String pattern = allPatterns.substring(start,
                                start + i);
                        start += i;
                        addPattern(pattern);
                    } else {
                        break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            LOGGER.warn("Could not find pattern file ["
                    + m_patternFile.getAbsolutePath() + "]!");
        } catch (JSONException e) {
            LOGGER.warn("Could not parse pattern file ["
                    + m_patternFile.getAbsolutePath() + "]!");
            e.printStackTrace();
        }
    }

    private boolean addPattern(final String pattern) {
        String pat = pattern.replaceAll("[0-9]", "");
        String[] numbers = pattern.split("[^0-9]");
        int[] points = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i].equals("")) {
                points[i] = 0;
            } else {
                points[i] = Integer.parseInt(numbers[i]);
            }
        }
        m_patterns.put(pat, points);
        return true;

    }
}

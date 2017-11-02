/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 * -------------------------------------------------------------------
 *
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 * Singleton class reads build in stopword lists and provides them as <code>Set</code>.
 *
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class BuildInStopwordListFactory {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(BuildInStopwordListFactory.class);

    private static final String STOPWORDLIST_PATH = "stopwordlists";

    private static final BuildInStopwordListFactory INSTANCE = new BuildInStopwordListFactory();

    private final Map<String, Set<String>> m_buildInStopwordLists;

    private final String m_defaultName;

    /**
     * Accessor method for the singleton class. Creates a new instance of <code>BuildInStopwordListFactory</code> if it
     * does not exists yet and returns it.
     *
     * @return An instance of <code>BuildInStopwordListFactory</code>.
     */
    public static BuildInStopwordListFactory getInstance() {
        return INSTANCE;
    }

    private BuildInStopwordListFactory() {
        m_buildInStopwordLists = new HashMap<String, Set<String>>();

        File englishFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/English-Stopwords.txt");
        String englishName = "English";
        m_buildInStopwordLists.put(englishName, readList(englishFilename));
        m_defaultName = englishName;

        File germanFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/German-Stopwords.txt");
        m_buildInStopwordLists.put("German", readList(germanFilename));

        File italianFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Italian-Stopwords.txt");
        m_buildInStopwordLists.put("Italian", readList(italianFilename));

        File spanishFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Spanish-Stopwords.txt");
        m_buildInStopwordLists.put("Spanish", readList(spanishFilename));

        File frenchFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/French-Stopwords.txt");
        m_buildInStopwordLists.put("French", readList(frenchFilename));

        File bulgarianFilename =
            TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Bulgarian-Stopwords.txt");
        m_buildInStopwordLists.put("Bulgarian", readList(bulgarianFilename));

        File hungarianFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Hungarian-Stopwords.txt");
        m_buildInStopwordLists.put("Hungarian", readList(hungarianFilename));

        File polishFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Polish-Stopwords.txt");
        m_buildInStopwordLists.put("Polish", readList(polishFilename));

        File portugueseFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Portuguese-Stopwords.txt");
        m_buildInStopwordLists.put("Portuguese", readList(portugueseFilename));

        File roumanianFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Roumanian-Stopwords.txt");
        m_buildInStopwordLists.put("Roumanian", readList(roumanianFilename));

        File russianFilename = TextprocessingCorePlugin.resolvePath(STOPWORDLIST_PATH + "/Russian-Stopwords.txt");
        m_buildInStopwordLists.put("Russian", readList(russianFilename));
    }

    /**
     * Returns the stop word list corresponding to specified given name. If no list exists with the given name,
     * <code>null</code> is returned.
     *
     * @param name The name of the stop word list to return.
     * @return The stop word list with given name, or <code>null</code> if name does not exist.
     */
    public Set<String> getStopwordListByName(final String name) {
        return Collections.unmodifiableSet(m_buildInStopwordLists.get(name));
    }

    /**
     * @return The set of names of the available stop word lists.
     */
    public Set<String> getNames() {
        List<String> listName = new LinkedList<String>(m_buildInStopwordLists.keySet());
        Collections.sort(listName);
        return new LinkedHashSet<String>(listName);
    }

    /**
     * @return A map containing all stop word lists as values and their names as corresponding keys.
     * @since 2.9
     */
    public Map<String, Set<String>> getStopwordLists() {
        return Collections.unmodifiableMap(m_buildInStopwordLists);
    }

    /**
     * @return The name of the default stop word list.
     */
    public String getDefaultName() {
        return m_defaultName;
    }

    private Set<String> readList(final File file) {
        final Set<String> stopWords = new LinkedHashSet<String>();
        if (file.exists() && file.canRead() && file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    stopWords.add(line.trim());
                }
            } catch (FileNotFoundException e) {
                LOGGER.warn("Stop word file not found: " + file.getAbsolutePath());
            } catch (IOException e) {
                LOGGER.warn("Can't read from stop word file: " + file.getAbsolutePath());
            }
        }
        return stopWords;
    }
}

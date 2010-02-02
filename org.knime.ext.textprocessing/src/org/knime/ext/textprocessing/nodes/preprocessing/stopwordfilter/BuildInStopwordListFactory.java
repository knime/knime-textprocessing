/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 * -------------------------------------------------------------------
 * 
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Singleton class reads build in stopword lists and provides them as 
 * <code>Set</code>.
 * 
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class BuildInStopwordListFactory {
    
    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(BuildInStopwordListFactory.class);
    
    private static final String STOPWORDLIST_PATH = "/resources/stopwordlists";
    
    private static BuildInStopwordListFactory instance;
    
    private Hashtable<String, Set<String>> m_buildInStopwordLists;
    
    private String m_defaultName;
    
    private String m_basePath;
    
    /**
     * Accessor method for the singleton class. Creates a new instance of
     * <code>BuildInStopwordListFactory</code> if it exists not yet and returns 
     * it.
     * 
     * @return An instance of <code>BuildInStopwordListFactory</code>.
     */
    public static BuildInStopwordListFactory getInstance() {
        if (instance == null) {
            TextprocessingCorePlugin plugin = 
                TextprocessingCorePlugin.getDefault();
            String pluginPath = plugin.getPluginRootPath();
            instance = new BuildInStopwordListFactory(pluginPath);
        }
        return instance;
    }
    
    private BuildInStopwordListFactory(final String basePath) { 
        m_basePath = basePath;
        m_buildInStopwordLists = new Hashtable<String, Set<String>>();
        
        String englishFilename = m_basePath + STOPWORDLIST_PATH 
                               + "/English-Stopwords.txt";
        String englishName = "English";
        m_buildInStopwordLists.put(englishName, readList(englishFilename));
        m_defaultName = englishName;
        
        String germanFilename = m_basePath + STOPWORDLIST_PATH 
                              + "/German-Stopwords.txt";
        m_buildInStopwordLists.put("German", readList(germanFilename));
    }
    
    /**
     * Returns the stopword list corresponding to specified given name. If
     * no list exists with the given name, <code>null</code> is returned.
     * @param name The name of the stopword list to return.
     * @return The stopword list with given name, or <code>null</code> if
     * name does not exist.
     */
    public Set<String> getStopwordListByName(final String name) {
        return m_buildInStopwordLists.get(name);
    }
    
    /**
     * @return The set of names of the available stopword lists.
     */
    public Set<String> getNames() {
        return m_buildInStopwordLists.keySet();
    }
    
    /**
     * @return The <code>Hashtable</code> containing the names and the 
     * corresponding stopword lists.
     */
    public Hashtable<String, Set<String>> getNamesAndLists() {
        return m_buildInStopwordLists;
    }
    
    /**
     * @return The name of the default stopword list.
     */
    public String getDefaultName() {
        return m_defaultName;
    }
    
    private Set<String> readList(final String filename) {
        Set<String> stopWords = new HashSet<String>();
        File f = new File(filename);
        if (f.exists() && f.canRead() && f.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    stopWords.add(line.trim());
                }
                reader.close();
            } catch (FileNotFoundException e) {
                LOGGER.warn("Not such file !");
            } catch (IOException e) {
                LOGGER.warn("Cant read from file");
            }
        }
        return stopWords;
    }
}

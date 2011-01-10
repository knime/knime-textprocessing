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
 * -------------------------------------------------------------------
 * 
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.tartarus.snowball.SnowballStemmer;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class SnowballStemmerFactory {

    private static final String PACKAGE_PREFIX = "org.tartarus.snowball.ext.";
    
    private static final String PACKAGE_POSTFIX = "Stemmer";
    
    private Set<String> m_stemmerNames = new HashSet<String>();
    
    private Hashtable<String, SnowballStemmer> m_stemmer = 
        new Hashtable<String, SnowballStemmer>();
    
    private static SnowballStemmerFactory instance = null;
    
    private SnowballStemmerFactory() throws ClassNotFoundException,
    InstantiationException, IllegalAccessException {
        m_stemmerNames.add("Danish");
        m_stemmerNames.add("Dutch");
        m_stemmerNames.add("English");
        m_stemmerNames.add("Finnish");
        m_stemmerNames.add("French");
        m_stemmerNames.add("German");
        m_stemmerNames.add("Hungarian");
        m_stemmerNames.add("Italian");
        m_stemmerNames.add("Norwegian");
        m_stemmerNames.add("Porter");
        m_stemmerNames.add("Portuguese");
        m_stemmerNames.add("Romanian");
        m_stemmerNames.add("Russian");
        m_stemmerNames.add("Spanish");
        m_stemmerNames.add("Swedish");
        m_stemmerNames.add("Turkish");
        
        for (String name : m_stemmerNames) {
            Class stemClass = Class.forName(
                    PACKAGE_PREFIX + name.toLowerCase() + PACKAGE_POSTFIX);
            SnowballStemmer stemmer = (SnowballStemmer)stemClass.newInstance();
            m_stemmer.put(name, stemmer);
        }        
    }
    
    /**
     * @return The singleton instance of <code>SnowballStemmerFactory</code>.
     * @throws ClassNotFoundException If snowball stemmer class could not be 
     * found.
     * @throws InstantiationException If snowball stemmer class could not be 
     * instanciated.
     * @throws IllegalAccessException If snowball stemmer class could not be 
     * accessed.
     */
    public static SnowballStemmerFactory getInstance() throws 
    ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (instance == null) {
            instance = new SnowballStemmerFactory();
        }
        return instance;
    }
    
    /**
     * @return The set of available snowball stemmer names.
     */
    public Set<String> getStemmerNames() {
        return m_stemmer.keySet();
    }
    
    /**
     * Returns the Snowball stemmer corresponding to the given name.
     * 
     * @param name The name to get the Snowball stemmer for.
     * @return the Snowball stemmer corresponding to the given name.
     */
    public SnowballStemmer getStemmerByName(final String name) {
        return m_stemmer.get(name);
    }
}

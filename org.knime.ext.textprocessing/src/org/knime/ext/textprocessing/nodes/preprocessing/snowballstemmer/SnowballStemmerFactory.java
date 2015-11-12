/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   25.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.snowballstemmer;

import java.util.HashSet;
import java.util.Set;

import javax.naming.NameNotFoundException;

import org.tartarus.snowball.SnowballStemmer;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class SnowballStemmerFactory {

    private static final String PACKAGE_PREFIX = "org.tartarus.snowball.ext.";

    private static final String PACKAGE_POSTFIX = "Stemmer";

    private Set<String> m_stemmerNames = new HashSet<String>();

    /**
     * Creates an instance of <code>SnowballStemmerFactory</code>.
     *
     * @throws ClassNotFoundException If snowball stemmer classes could not be found.
     * @throws InstantiationException If snowball stemmer classes could not be instantiated.
     * @throws IllegalAccessException If snowball stemmer classes could not be accessed.
     * @since 2.8
     */
    public SnowballStemmerFactory() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
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
    }

    /**
     * @return The set of available snowball stemmer names.
     */
    public Set<String> getStemmerNames() {
        return m_stemmerNames;
    }

    /**
     * Creates a new snowball stemmer instance for the given name and returns it. If the name could not be found in the
     * list of valid names, or the stemmer instance could not be create an exception will be thrown.
     *
     * @param name The name of the Snowball stemmer to create.
     * @return the Snowball stemmer corresponding to the given name.
     * @throws NameNotFoundException If given name of snowball stemmer could not be found in the list of valid names.
     * @throws ClassNotFoundException If snowball stemmer classes could not be found.
     * @throws IllegalAccessException If snowball stemmer classes could not be instantiated.
     * @throws InstantiationException If snowball stemmer classes could not be instantiated.
     */
    public SnowballStemmer getStemmerByName(final String name)
        throws NameNotFoundException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (!m_stemmerNames.contains(name)) {
            throw new NameNotFoundException("Stemmer name \"" + name + "\" not found in Snowball library.");
        }

        @SuppressWarnings("unchecked")
        Class<SnowballStemmer> stemmerClass =
            (Class<SnowballStemmer>)Class.forName(PACKAGE_PREFIX + name.toLowerCase() + PACKAGE_POSTFIX);
        return stemmerClass.newInstance();
    }
}

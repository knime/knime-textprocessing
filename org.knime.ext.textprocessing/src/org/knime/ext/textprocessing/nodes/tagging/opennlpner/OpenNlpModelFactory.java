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
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

import java.util.Hashtable;
import java.util.Set;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public final class OpenNlpModelFactory {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(OpenNlpModelFactory.class);

    private static OpenNlpModelFactory instance = null;

    private Hashtable<String, OpenNlpModel> m_models =
        new Hashtable<String, OpenNlpModel>();

    /**
     * Creates and returns a singleton instance of
     * <code>OpenNlpModelFactory</code>.
     *
     * @return a singleton instance of <code>OpenNlpModelFactory</code>.
     */
    public static final OpenNlpModelFactory getInstance() {
        if (instance == null) {
            instance = new OpenNlpModelFactory();
        }
        return instance;
    }

    private OpenNlpModelFactory() {
        LOGGER.debug("Registering Maxent Models ...");

        OpenNlpModelPaths paths = OpenNlpModelPaths.getOpenNlpModelPaths();

        // PERSON
        String name = "Person";
        OpenNlpModel m = new OpenNlpModel(name, paths.getPersonNERModelFile(),
                NamedEntityTag.PERSON.toString());
        m_models.put(name, m);

        // LOCATION
        name = "Location";
        m = new OpenNlpModel(name, paths.getLocationNERModelFile(),
                NamedEntityTag.LOCATION.toString());
        m_models.put(name, m);

        // ORGANIZATION
        name = "Organization";
        m = new OpenNlpModel(name, paths.getOrganizationNERModelFile(),
                NamedEntityTag.ORGANIZATION.toString());
        m_models.put(name, m);

        // MONEY
        name = "Money";
        m = new OpenNlpModel(name, paths.getMoneyNERModelFile(),
                NamedEntityTag.MONEY.toString());
        m_models.put(name, m);

        // DATE
        name = "Date";
        m = new OpenNlpModel(name, paths.getDateNERModelFile(),
                NamedEntityTag.DATE.toString());
        m_models.put(name, m);

        // TIME
        name = "Time";
        m = new OpenNlpModel(name, paths.getTimeNERModelFile(),
                NamedEntityTag.TIME.toString());
        m_models.put(name, m);
    }

    /**
     * Returns the model related to the given name.
     * @param name The name to get the model for.
     * @return The related model.
     */
    public OpenNlpModel getModelByName(final String name) {
        return m_models.get(name);
    }

    /**
     * @return A set of all names of available models.
     */
    public Set<String> getModelNames() {
        return m_models.keySet();
    }

    /**
     * @return The name of the default model.
     */
    public String getDefaultName() {
        return "Person";
    }
}

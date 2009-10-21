/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.util.Hashtable;
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.NamedEntityTag;
import org.knime.ext.textprocessing.util.OpenNlpModelPaths;

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
        LOGGER.info("Registering Maxent Models ...");

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

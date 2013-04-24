/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 * ---------------------------------------------------------------------
 *
 * History
 *   13.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentGrabberFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentGrabberFactory.class);

    /**
     * The id of the {@link DocumentGrabber} extension point.
     * @since 2.8
     */
    public static final String EXT_POINT_ID =
            "org.knime.ext.textprocessing.DocumentGrabber";

    /**
     * The attribute of the DocumentGrabber extension point.
     * @since 2.8
     * */
    public static final String EXT_POINT_ATTR_DF = "DocumentGrabber";

    private static DocumentGrabberFactory instance = null;

    private final Map<String, DocumentGrabber> m_grabber = new Hashtable<String, DocumentGrabber>();

    /**
     * @return The singelton instance of <code>DocumentGrabberFactory</code>.
     */
    public static DocumentGrabberFactory getInstance() {
        if (instance == null) {
            instance = new DocumentGrabberFactory();
        }
        return instance;
    }

    private DocumentGrabberFactory() {
        registerDocumentGrabber(new PubMedDocumentGrabber());
        registerExtensionPoints();
    }

    /**
     * @param grabber the {@link DocumentGrabber} to register
     */
    private void registerDocumentGrabber(final DocumentGrabber grabber) {
        if (grabber == null) {
            throw new NullPointerException("grabber must not be null");
        }
        final String name = grabber.getName();
        final DocumentGrabber existingGrabber = m_grabber.get(name);
        if (existingGrabber != null) {
            throw new IllegalArgumentException("DocumentGrabber with name: " + name
                           + " already registered for class: " + existingGrabber.getClass().getName());
        }
        m_grabber.put(name, grabber);
    }

    /**
     * @return The names of the registered grabber.
     */
    public Set<String> getGrabberNames() {
        return m_grabber.keySet();
    }

    /**
     * Returns the grabber related to the given name. If no grabber is
     * available, <code>null</code> is returned.
     *
     * @param name The name of the grabber.
     * @return The grabber related to the given name.
     */
    public DocumentGrabber getGrabber(final String name) {
        DocumentGrabber grabber = m_grabber.get(name);

        DocumentGrabber newGrabberInstance = null;

        // New instance of grabber needs to be created here, to ensure thread safety.
        // Each node (thread in parallel chunk loop) must have its own instance.
        // One singleton instance would either block or run into concurrency problems.
        try {
            newGrabberInstance = grabber.getClass().newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("New DocumentGrabber instance " + grabber.getClass().toString() + " could not be created!");
            LOGGER.error(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.error("Empty Consructor of " + grabber.getClass().toString() + " is not accessible.");
            LOGGER.error(e.getMessage());
        }

        return newGrabberInstance;
    }

    /**
     * Registers all extension point implementations.
     */
    private void registerExtensionPoints() {
        try {
            final IExtensionRegistry registry = Platform.getExtensionRegistry();
            final IExtensionPoint point =
                registry.getExtensionPoint(EXT_POINT_ID);
            if (point == null) {
                LOGGER.error("Invalid extension point: " + EXT_POINT_ID);
                throw new IllegalStateException("ACTIVATION ERROR: "
                        + " --> Invalid extension point: " + EXT_POINT_ID);
            }
            for (final IConfigurationElement elem : point.getConfigurationElements()) {
                final String docGrabber = elem.getAttribute(EXT_POINT_ATTR_DF);
                final String decl = elem.getDeclaringExtension().getUniqueIdentifier();

                if (docGrabber == null || docGrabber.isEmpty()) {
                    LOGGER.error("The extension '" + decl + "' doesn't provide the required attribute '"
                            + EXT_POINT_ATTR_DF + "'");
                    LOGGER.error("Extension " + decl + " ignored.");
                    continue;
                }

                try {
                    final DocumentGrabber grabber =
                            (DocumentGrabber)elem.createExecutableExtension(EXT_POINT_ATTR_DF);
                    registerDocumentGrabber(grabber);
                } catch (final Throwable t) {
                    LOGGER.error("Problems during initialization of DocumentGrabber '"
                + docGrabber + "'.", t);
                    if (decl != null) {
                        LOGGER.error("Extension " + decl + " ignored.", t);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception while registering documentGrabber", e);
        }
    }
}

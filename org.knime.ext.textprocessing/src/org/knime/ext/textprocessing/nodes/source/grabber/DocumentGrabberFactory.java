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

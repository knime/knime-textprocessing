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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   03.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The main plugin class to be used in the desktop.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TextprocessingCorePlugin extends AbstractUIPlugin {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TextprocessingCorePlugin.class);

    private static final String PLUGIN_ID = "org.knime.ext.textprocessing";

    private static final String ASSETS_PLUGIN_ID = "org.knime.ext.textprocessing.assets";

    // The shared instance.
    private static TextprocessingCorePlugin plugin;

    /**
     * The constructor.
     */
    public TextprocessingCorePlugin() {
        plugin = this;
    }

    /**
     * This method is called when the plug-in is stopped.
     * @param context The bundle context.
     * @throws Exception If cause by super class.
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     *
     * @return The shared instance
     */
    public static TextprocessingCorePlugin getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Resolves a path relative to the plug-in or any fragment's root into an absolute path.
     *
     * @param relativePath a relative path
     * @return the resolved absolute path
     * @since 3.3
     */
    public static File resolvePath(final String relativePath) {
        final var myself = FrameworkUtil.getBundle(TextprocessingCorePlugin.class);
        final var relPath = new Path(relativePath);

        final var path = Optional.ofNullable(FileLocator.find(myself, relPath)) //
            .or(() -> Optional.ofNullable(FileLocator.find(Platform.getBundle(ASSETS_PLUGIN_ID), relPath)))
            .map(resourceUrl -> {
                try {
                    final var fileUrl = FileLocator.toFileURL(resourceUrl);
                    return fileUrl != null ? fileUrl.getPath() : "";
                } catch (final IOException e) {
                    LOGGER.error(() -> String.format("Could not resolve relative path '%s' to file URL: %s",
                        relativePath, e.getMessage() == null ? "reason unknown" : e.getMessage()), e);
                    return null;
                }
            }).orElse(""); // same behavior as before, the resolvePath method never returned `null`
        return new File(path);
    }
}

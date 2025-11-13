package org.knime.ext.textprocessing.language.arabic;

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
 * The activator class controls the plug-in life cycle
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class TextprocessingArabicLanguagePack extends AbstractUIPlugin {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TextprocessingArabicLanguagePack.class);

    // The plug-in ID
    private static final String PLUGIN_ID = "org.knime.features.ext.textprocessing.language.arabic";

    private static final String ASSETS_PLUGIN_ID = "org.knime.ext.textprocessing.language.arabic.assets";

    // The shared instance
    private static TextprocessingArabicLanguagePack plugin;

    /**
     * The constructor
     */
    public TextprocessingArabicLanguagePack() {
        plugin = this;
    }

    /**
     * This method is called when the plug-in is stopped.
     *
     * @param context The bundle context.
     * @throws Exception If cause by super class.
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TextprocessingArabicLanguagePack getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(final String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Resolves a path relative to the plug-in or any fragment's root into an absolute path.
     *
     * @param relativePath a relative path
     * @return the resolved absolute path
     */
    public static File resolvePath(final String relativePath) {
        final var myself = FrameworkUtil.getBundle(TextprocessingArabicLanguagePack.class);
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

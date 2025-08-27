package org.knime.ext.textprocessing.language.chinese;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
public class TextprocessingChineseLanguagePack extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.knime.features.ext.textprocessing.language.chinese";

    private static final String MODELS_PLUGIN_ID = "org.knime.ext.textprocessing.language.chinese.models";

    // The shared instance
    private static TextprocessingChineseLanguagePack plugin;

    /**
     * The constructor
     */
    public TextprocessingChineseLanguagePack() {
        plugin = this;
    }

    /**
     * This method is called when the plug-in is stopped.
     * @param context The bundle context.
     * @throws Exception If cause by super class.
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static TextprocessingChineseLanguagePack getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Resolves a path relative to the plug-in or any fragment's root into an absolute path.
     *
     * @param relativePath a relative path
     * @return the resolved absolute path
     */
    public static File resolvePath(final String relativePath) {
        final var modelsBundle = Platform.getBundle(MODELS_PLUGIN_ID);
        try {
            final var bundleUrl = FileLocator.find(modelsBundle, new Path(relativePath), null);
            return FileUtil.resolveToPath(FileLocator.toFileURL(bundleUrl)).toFile();
        } catch (IOException | URISyntaxException ex) {
            NodeLogger.getLogger(TextprocessingChineseLanguagePack.class)
                .error("Could not resolve relative path '" + relativePath + "': " + ex.getMessage(), ex);
            return new File("");
        }
    }
}

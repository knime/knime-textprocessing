package org.knime.ext.textprocessing.language.german;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.knime.core.node.NodeLogger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
public class TextprocessingGermanLanguagePack extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.knime.ext.textprocessing.language.german";

	// The shared instance
	private static TextprocessingGermanLanguagePack plugin;
	
	/**
	 * The constructor
	 */
	public TextprocessingGermanLanguagePack() {
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
	public static TextprocessingGermanLanguagePack getDefault() {
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
        Bundle myself = FrameworkUtil.getBundle(TextprocessingGermanLanguagePack.class);
        try {
            URL fileUrl = FileLocator.toFileURL(FileLocator.find(myself, new Path(relativePath), null));
            return new File(fileUrl.getPath());
        } catch (IOException ex) {
            NodeLogger.getLogger(TextprocessingGermanLanguagePack.class)
                .error("Could not resolve relativ path '" + relativePath + "': " + ex.getMessage(), ex);
            return new File("");
        }
    }
}

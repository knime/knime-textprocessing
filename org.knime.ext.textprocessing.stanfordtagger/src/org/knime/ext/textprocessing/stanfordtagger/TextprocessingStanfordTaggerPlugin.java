package org.knime.ext.textprocessing.stanfordtagger;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TextprocessingStanfordTaggerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.knime.ext.textprocessing.stanfordtagger";

	// The shared instance
	private static TextprocessingStanfordTaggerPlugin plugin;

    private String m_pluginRootPath;
	
	/**
	 * The constructor
	 */
	public TextprocessingStanfordTaggerPlugin() {
	    plugin = this;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
        super.start(context);
        final URL pluginURL = Platform.resolve(plugin.find(new Path(".")));
        final File tmpFile = new File(pluginURL.getPath());
        m_pluginRootPath = tmpFile.getAbsolutePath();
	}

	/**
	 * {@inheritDoc}
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
    public static TextprocessingStanfordTaggerPlugin getDefault() {
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
        return AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.knime.ext.textprocessing.stanfordtagger", path);
    }
    
    /**
     * @return the absolute root path of this plugin.
     */
    public String getPluginRootPath() {
        return m_pluginRootPath;
    } 	
}

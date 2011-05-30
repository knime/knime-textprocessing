package org.knime.ext.textprocessing.stanfordtagger;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class TextprocessingStanfordTaggerPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID =
	        "org.knime.ext.textprocessing.stanfordtagger";

	// The shared instance
	private static TextprocessingStanfordTaggerPlugin plugin;

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

}

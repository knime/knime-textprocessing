/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   18.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 * All different types of {@link org.knime.ext.textprocessing.data.Tag}s have to
 * be registered in this factory to be able to create the right tag instance
 * (i.e. PartOfSpeechTag) related to the type of the tag. Access the singleton
 * instance of the factory by calling
 * {@link org.knime.ext.textprocessing.data.TagFactory#getInstance()}. To add
 * your own tagset, specified in a xml file (see tagset.dtd) use the method
 * {@link org.knime.ext.textprocessing.data.TagFactory#addTagSet(File)}, which
 * registers the specified tags in this factory.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class TagFactory {

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(TagFactory.class);

    /** The id of the AggregationMethod extension point. */
    public static final String EXT_POINT_ID =
            "org.knime.ext.textprocessing.TagSet";

    /** The attribute of the aggregation method extension point. */
    public static final String EXT_POINT_ATTR_DF = "TagBuilder";

    /**
     * The path (postix) of the tagset.xml file relative to the plugin
     * directory.
     */
    public static final String TAGSET_XML_POSTFIX =
            "/resources/tagset/tagset.xml";

    /**
     * The path (postix) of the tagset.dtd file relative to the plugin
     * directory.
     */
    public static final String TAGSET_DTD_POSTFIX =
            "/resources/tagset/tagset.dtd";

    private static TagFactory instance;

    private Hashtable<String, TagBuilder> m_tagBuilder =
            new Hashtable<String, TagBuilder>();

    private TagFactory() {
        TextprocessingCorePlugin plugin = TextprocessingCorePlugin.getDefault();
        String pluginPath = plugin.getPluginRootPath();
        String tagSetPath = pluginPath + TAGSET_XML_POSTFIX;
        addTagSet(new File(tagSetPath));
        registerExtensionPoints();
    }

    /**
     * @return The singleton instance of <code>TagFactory</code>.
     */
    public static TagFactory getInstance() {
        if (instance == null) {
            instance = new TagFactory();
        }
        return instance;
    }

    /**
     * Adds the tagset specified in the xml file.
     * 
     * @param xmlTagSet The xml file containing the tagset to add.
     */
    public void addTagSet(final File xmlTagSet) {
        TagSetParser parser = new TagSetParser();
        Set<String> tagSet = parser.parse(xmlTagSet);
        addTags(tagSet);
    }

    /**
     * @return The so far registered tags as a set.
     */
    public Set<TagBuilder> getTagSet() {
        Set<TagBuilder> builder = new HashSet<TagBuilder>();
        for (TagBuilder tb : m_tagBuilder.values()) {
            builder.add(tb);
        }
        return Collections.unmodifiableSet(builder);
    }

    /**
     * @return The set of tag types.
     */
    public Set<String> getTagTypes() {
        return m_tagBuilder.keySet();
    }

    /**
     * @param type The type to return set of tags for.
     * @return The set of tags for given type
     */
    public TagBuilder getTagSetByType(final String type) {
        return m_tagBuilder.get(type);
    }

    @SuppressWarnings("unchecked")
    private void addTags(final Set<String> tagClassNames) {
        Class<? extends TagBuilder> tagClass;
        try {
            for (String tagName : tagClassNames) {
                tagName.trim();
                if (tagName.length() > 0) {
                    tagClass =
                            (Class<? extends TagBuilder>)Class.forName(tagName);

                    final Method method =
                            tagClass.getMethod("getDefault", new Class[]{});
                    TagBuilder tagBuilder =
                            (TagBuilder)method.invoke(new Object[]{},
                                    new Object[]{});

                    m_tagBuilder.put(tagBuilder.getType(), tagBuilder);
                    LOGGER.info("Added TagBuilder: "
                            + tagBuilder.getClass().toString());
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Class of tag could not be found!");
            LOGGER.warn(e.getMessage());
        } catch (SecurityException e) {
            LOGGER.warn("Security violation!");
            LOGGER.warn(e.getMessage());
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Method to create tag could not be found!");
            LOGGER.warn(e.getMessage());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Arguments of method to create tag are illegal!");
            LOGGER.warn(e.getMessage());
        } catch (IllegalAccessException e) {
            LOGGER.warn("Illegal access of method to create tag!");
            LOGGER.warn(e.getMessage());
        } catch (InvocationTargetException e) {
            LOGGER.warn("Error during invokation of method to create tag!");
            LOGGER.warn(e.getMessage());
        }
    }

    /**
     * Creates a valid instance of {@link org.knime.ext.textprocessing.data.Tag}
     * with given type and value.
     * 
     * @param type The type of the tag to create.
     * @param value The value of the tag to create.
     * @return A new instance of tag with given type and value.
     */
    public Tag createTag(final String type, final String value) {
        TagBuilder tb = m_tagBuilder.get(type);
        if (tb != null) {
            return tb.buildTag(value);
        }
        return null;
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
            for (final IConfigurationElement elem : point
                    .getConfigurationElements()) {
                final String operator = elem.getAttribute(EXT_POINT_ATTR_DF);
                final String decl =
                        elem.getDeclaringExtension().getUniqueIdentifier();

                if (operator == null || operator.isEmpty()) {
                    LOGGER.error("The extension '" + decl
                            + "' doesn't provide the required attribute '"
                            + EXT_POINT_ATTR_DF + "'");
                    LOGGER.error("Extension " + decl + " ignored.");
                    continue;
                }

                try {
                    final TagBuilder builder =
                            (TagBuilder)elem.createExecutableExtension(
                                    EXT_POINT_ATTR_DF);
                    if (builder != null) {
                        if (m_tagBuilder.containsKey(builder.getType())) {
                            LOGGER.error("Tag set with type \""
                                    + builder.getType() + "\" already exists!");
                        } else {
                            m_tagBuilder.put(builder.getType(), builder);
                        }
                    }
                } catch (final Throwable t) {
                    LOGGER.error("Problems during initialization of "
                            + "tag set (with id '" + operator + "'.)");
                    if (decl != null) {
                        LOGGER.error("Extension " + decl + " ignored.", t);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error("Exception while registering tag set extensions");
        }
    }
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
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
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingPlugin;

/**
 * All different types of {@link org.knime.ext.textprocessing.data.Tag}s have
 * to be registered in this factory to be able to create 
 * the right tag instance (i.e. PartOfSpeechTag) related to the type of the
 * tag. Access the singleton instance of the factory by calling
 * {@link org.knime.ext.textprocessing.data.TagFactory#getInstance()}. To add
 * your own tagset, specified in a xml file (see tagset.dtd) use the method
 * {@link org.knime.ext.textprocessing.data.TagFactory#addTagSet(File)}, which 
 * registers the specified tags in this factory.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFactory {

    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(TagFactory.class);
    
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
    
    private static TagFactory m_instance;
    
    private HashSet<TagBuilder> m_tagBuilder = new HashSet<TagBuilder>();
    
    
    private TagFactory() {
        TextprocessingPlugin plugin = TextprocessingPlugin.getDefault();
        String pluginPath = plugin.getPluginRootPath();
        String tagSetPath = pluginPath + TAGSET_XML_POSTFIX;
        addTagSet(new File(tagSetPath));
    }
    
    /**
     * @return The singleton instance of <code>TagFactory</code>.
     */
    public static TagFactory getInstance() {
        if (m_instance == null) {
            m_instance = new TagFactory();
        }
        return m_instance;
    }
    
    /**
     * Adds the tagset specified in the xml file.
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
        return Collections.unmodifiableSet(m_tagBuilder);
    }
    
    @SuppressWarnings("unchecked")
    private void addTags(final Set<String> tagClassNames) {
        Class<? extends TagBuilder> tagClass;
        try {
            for (String tagName : tagClassNames) {
                tagName.trim();
                if (tagName.length() > 0) {
                    tagClass = (Class<? extends TagBuilder>)
                    Class.forName(tagName);
            
                    final Method method = tagClass.getMethod(
                            "getDefault", new Class[]{});
                    TagBuilder tagBuilder = (TagBuilder)method.invoke(
                            new Object[]{}, new Object[]{});
                
                    m_tagBuilder.add(tagBuilder);
                    LOGGER.info("Added TagBuilder: " 
                            + tagBuilder.getClass().toString());
                }
            }
        } catch(ClassNotFoundException e) {
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
        for (TagBuilder tb : m_tagBuilder) {
            Tag t = tb.buildTag(type, value);
            if (t != null) {
                return t;
            }
        }
        return null;
    }   
}

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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingPlugin;

/**
 * All different kind of {@link org.knime.ext.textprocessing.data.Tag}s have
 * to be registered according their type in this factory to be able to create 
 * the right tag instance (i.e. PartOfSpeechTag) related to the type of the
 * tag. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagFactory {

    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(TagFactory.class);
    
    private static final HashSet<TagBuilder> TAG_BUILDER = 
        new HashSet<TagBuilder>();
    
    private static final String TAGSET_POSTFIX = "/resources/tagset/tagset.xml";
    
    static {
        TextprocessingPlugin plugin = TextprocessingPlugin.getDefault();
        String pluginPath = plugin.getPluginRootPath();
        String tagSetPath = pluginPath + TAGSET_POSTFIX;
            
        addTagSet(new File(tagSetPath));
    }
    
    public static void addTagSet(final File xmlTagSet) {
        TagSetParser parser = new TagSetParser();
        Set<String> tagSet = parser.parse(xmlTagSet);
        addTags(tagSet);
    }
    
    @SuppressWarnings("unchecked")
    private static void addTags(final Set<String> tagClassNames) {
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
                
                    TAG_BUILDER.add(tagBuilder);
                    LOGGER.error("Added TagBuilder: " 
                            + tagBuilder.getClass().toString());
                }
            }
        } catch(Exception e) {
            LOGGER.warn("Tags could not be loaded from tagset!");
            LOGGER.info(e.getMessage());
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
    public static Tag createTag(final String type, final String value) {
        for (TagBuilder tb : TAG_BUILDER) {
            Tag t = tb.buildTag(type, value);
            if (t != null) {
                return t;
            }
        }
        return null;
    }   
}

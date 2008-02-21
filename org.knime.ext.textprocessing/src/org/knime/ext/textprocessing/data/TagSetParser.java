/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   21.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.NodeLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TagSetParser extends DefaultHandler {
    
    /**
     * The name of the Tag tag.
     */
    public static final String TAG = "tag";
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(TagSetParser.class);
    
    private Set<String> m_tagClassNames;
    
    private String m_lastMember;
    
    private String m_tag = "";
    
    /**
     * Creates new empty instance of <code>TagSetParser</code>.
     */
    public TagSetParser() { }

    /**
     * Parses the given file consisting of the tagset data and creates
     * a set of all contained tags.
     * 
     * @param file The file to parse.
     * @return The set containing all parsed available tags.
     */
    public Set<String> parse(final File file) {
        try {
            m_tagClassNames = new HashSet<String>();      
            SAXParserFactory.newInstance().newSAXParser().parse(file, this);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Could not instanciate parser");
            LOGGER.info(e.getMessage());
        } catch (SAXException e) {
            LOGGER.error("Could not parse file");
            LOGGER.info(e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Could not read file");
            LOGGER.info(e.getMessage());
        }
        return m_tagClassNames;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, 
            final String qName, final Attributes attributes) {
        m_lastMember = qName.toLowerCase();
        if (m_lastMember.equals(TAG)) {
            m_tag = "";
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, 
            final String qName) {
        String name = qName.toLowerCase();
        if (name.equals(TAG)) {
            m_tagClassNames.add(m_tag);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (m_lastMember.equals(TAG)) {
            m_tag += new String(ch, start, length);
        }
    }
}

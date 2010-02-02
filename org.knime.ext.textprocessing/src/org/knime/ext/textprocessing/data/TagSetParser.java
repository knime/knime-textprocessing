/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   21.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
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
    
    /**
     * The public identifier for (sdml) xml files.
     */
    public static final String PUBLIC_IDENTIFIER = 
        "-//UNIKN//DTD KNIME TagSet 2.0//EN";    
    
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
            SAXParserFactory fac = SAXParserFactory.newInstance();
            fac.setValidating(true);
            fac.newSAXParser().parse(file, this);
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
    public InputSource resolveEntity(final String pubId,
            final String sysId) throws IOException, SAXException {
        if (pubId != null) {
            TextprocessingCorePlugin plugin = 
                TextprocessingCorePlugin.getDefault();
            String path = plugin.getPluginRootPath();
            if (pubId.equals(PUBLIC_IDENTIFIER)) {
                path += TagFactory.TAGSET_DTD_POSTFIX;
            }
            InputStream in = new FileInputStream(path);
            return new InputSource(in);
        }
        return super.resolveEntity(pubId, sysId);
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

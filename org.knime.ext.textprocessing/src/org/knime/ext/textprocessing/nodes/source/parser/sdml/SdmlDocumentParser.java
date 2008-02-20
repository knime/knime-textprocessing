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
 *   19.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.sdml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.util.DocumentBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} 
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)}
 * is able to parse the data of the given input stream representing <i>sdml</i>
 * (<b>S</b>imple <b>D</b>ocument <b>M</b>arkup <b>L</b>anguage) formatted text 
 * documents. See the <i>sdml.dtd</i> file for more details about the format. 
 * This format enables a simple representation of textual documents and can be 
 * used as a transfer format to get text documents formatted in various kinds 
 * of xml formats into knime without implementing an extra parser node for each 
 * format. The only thing what have to be done is to transform documents in
 * other xml formats via xslt transformation into sdml.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class SdmlDocumentParser extends DefaultHandler implements
        DocumentParser {

    /**
     * The name of the document tag.
     */
    public static final String DOCUMENT = "document";

    /**
     * The name of the title tag.
     */    
    public static final String TITLE = "title";    
    
    /**
     * The name of the section tag.
     */    
    public static final String SECTION = "section";
    
    /**
     * The name of the annotation attribute.
     */
    public static final String ANNOTATION = "annotation";
    
    /**
     * The name of the author tag.
     */
    public static final String AUTHOR = "author";
    
    /**
     * The name of the first name tag.
     */
    public static final String FIRSTNAME = "firstname";
    
    /**
     * The name of the last name tag.
     */
    public static final String LASTNAME = "lastname";
    
    /**
     * The name of the publication date tag.
     */
    public static final String PUBLICATIONDATE = "publicationdate";
    
    /**
     * The name of the day tag.
     */
    public static final String DAY = "day";
    
    /**
     * The name of the month tag.
     */
    public static final String MONTH = "month";
    
    /**
     * The name of the year tag.
     */
    public static final String YEAR = "year";    
    
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(SdmlDocumentParser.class);
    
    private List<Document> m_docs;
    
    private DocumentCategory m_category;
    
    private DocumentSource m_source;
    
    private String m_docPath;
    
    
    private DocumentBuilder m_currentDoc;
        
    private String m_lastTag;
    
    private String m_firstName = "";
    
    private String m_lastName = "";
    
    private String m_day = "";
    
    private String m_month = "";
    
    private String m_year = "";
    
    private String m_annotation = "";
    
    private String m_section = "";
    
    private String m_title = "";
    
    
    /**
     * Creates a new instance of <code>SdmlDocumentParser</code>. The documents
     * source, category and file path will be set to <code>null</code> be 
     * default.
     */
    public SdmlDocumentParser() {
        this(null, null, null);
    }    
    
    /**
     * Creates a new instance of <code>SdmlDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     * 
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public SdmlDocumentParser(final String docPath,
            final DocumentCategory category, final DocumentSource source) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
    }    
    
    
    /**
     * {@inheritDoc}
     */
    public List<Document> parse(InputStream is) {
        try {
            m_docs = new ArrayList<Document>();
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
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
        return m_docs;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, 
            final String qName, final Attributes attributes) {
        m_lastTag = qName.toLowerCase();
        
        if (m_lastTag.equals(DOCUMENT)) {
            m_currentDoc = new DocumentBuilder();
            if (m_category != null) {
                m_currentDoc.addDocumentCategory(m_category);
            }
            if (m_source != null) {
                m_currentDoc.addDocumentSource(m_source);
            }
            if (m_docPath != null) {
                File f = new File(m_docPath);
                if (f.exists()) {
                    m_currentDoc.setDocumentFile(f);
                }
            }
        } else if(m_lastTag.equals(FIRSTNAME)) {
            m_firstName = "";
        } else if(m_lastTag.equals(LASTNAME)) {
            m_lastName = "";
        } else if(m_lastTag.equals(DAY)) {
            m_day = "";
        } else if(m_lastTag.equals(MONTH)) {
            m_month = "";
        } else if(m_lastTag.equals(YEAR)) {
            m_year = "";
        } else if(m_lastTag.equals(TITLE)) {
            m_title = "";
        } else if (m_lastTag.equals(SECTION)) {
            m_annotation = attributes.getValue(ANNOTATION);
            m_section = "";
        }        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, 
            final String qName) {
        if (qName.equals(DOCUMENT) && m_currentDoc != null) {
            Document doc = m_currentDoc.createDocument();
            m_docs.add(doc);
            
            LOGGER.info("Build document " + doc.getTitle());
            LOGGER.info("Temp Dir: " + System.getProperty("java.io.tmpdir"));
            
            m_currentDoc = null;
        } else if(qName.equals(AUTHOR)) {
            Author a = new Author(m_firstName.trim(), m_lastName.trim());
            m_currentDoc.addAuthor(a);
        } else if (qName.equals(PUBLICATIONDATE)) {
            int day = Integer.parseInt(m_day.trim());
            int month = Integer.parseInt(m_month.trim());
            int year = Integer.parseInt(m_year.trim());
            PublicationDate pd;
            try {
                pd = new PublicationDate(year, month, day);
                m_currentDoc.setPublicationDate(pd);
            } catch (ParseException e) {
                LOGGER.warn("Publication date (" 
                        + year + "-" + month + "-" + day 
                        + ") could not be parsed !");
                LOGGER.info(e.getMessage());
            }
        } else if (qName.equals(TITLE)) {
            m_currentDoc.addTitle(m_title.trim());
        } else if (qName.equals(SECTION)) {
            m_currentDoc.addSection(m_section.trim(), 
                    SectionAnnotation.stringToAnnotation(m_annotation));
        }        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (m_lastTag.equals(FIRSTNAME)) {
            m_firstName += new String(ch, start, length);
        } else if (m_lastTag.equals(LASTNAME)) {
            m_lastName += new String(ch, start, length);
        } else if (m_lastTag.equals(DAY)) {
            m_day += new String(ch, start, length);
        } else if (m_lastTag.equals(MONTH)) {
            m_month += new String(ch, start, length);
        } else if (m_lastTag.equals(YEAR)) {
            m_year += new String(ch, start, length);
        } else if (m_lastTag.equals(TITLE)) {
            m_title += new String(ch, start, length);
        } else if (m_lastTag.equals(SECTION)) {
            m_section += new String(ch, start, length);
        }
    }
}

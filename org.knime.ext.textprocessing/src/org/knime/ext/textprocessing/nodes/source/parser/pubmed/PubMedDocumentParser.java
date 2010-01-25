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
 *   20.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.pubmed;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} 
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)}
 * is able to parse the data of the given input stream containing PubMed 
 * (http://www.pubmed.org) document search results. For more details
 * about the xml format used by PubMed to deliver search results see
 * (http://www.ncbi.nlm.nih.gov/entrez/query/DTD/pubmed_060101.dtd).
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PubMedDocumentParser extends DefaultHandler implements
        DocumentParser {

    /**
     * The default source of the pub med parser.
     */
    public static final String DEFAULT_SOURCE = "PubMed";
    
    /**
     * The name of the article tag.
     */
    public static final String PUBMEDARTICLE = "pubmedarticle";

    /**
     * The name of the abstract text tag.
     */
    public static final String ABSTRACTTEXT = "abstracttext";

    /**
     * The name of the article title tag.
     */
    public static final String ARTICLETITLE = "articletitle";
    
    /**
     * The name of the author tag.
     */
    public static final String AUTHOR = "author";
    
    /**
     * The name of the first name tag.
     */
    public static final String FIRSTNAME = "firstname";

    /**
     * The name of the fore name tag.
     */
    public static final String FORENAME = "forename";
    
    /**
     * The name of the last name tag.
     */
    public static final String LASTNAME = "lastname";    

    /**
     * The name of the publication date tag.
     */
    public static final String PUBDATE = "pubdate";

    /**
     * The name of the year tag.
     */
    public static final String YEAR = "year";
    
    /**
     * The name of the month tag.
     */
    public static final String MONTH = "month";
    
    /**
     * The name of the day tag.
     */
    public static final String DAY = "day";

    /**
     * The name of the journal tag.
     */
    public static final String JOURNAL = "journal";    

    /**
     * The name of the title tag.
     */
    public static final String TITLE = "title";    
    
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(PubMedDocumentParser.class);
    
    private List<Document> m_docs;
    
    private DocumentCategory m_category;
    
    private DocumentSource m_source;
    
    private DocumentType m_type;
    
    private String m_docPath;    
    
    private DocumentBuilder m_currentDoc;
    
    private String m_lastTag;
    
    private String m_abstract = "";
    
    private String m_title = "";
    
    private String m_firstName = "";
    
    private String m_lastName = "";
    
    private boolean m_pubDateFlag = false;
    
    private String m_day = "";
    
    private String m_month = "";
    
    private String m_year = "";
    
    private String m_journalTitle = "";
    
    private boolean m_journalFlag = false;
    
    
    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The 
     * document source is set to 
     * {@link org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser#DEFAULT_SOURCE} 
     * by default. The document category and file path will be set to 
     * <code>null</code> by default.
     */
    public PubMedDocumentParser() {
        this(null, null, new DocumentSource(DEFAULT_SOURCE));
    }
    
    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     * 
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public PubMedDocumentParser(final String docPath,
            final DocumentCategory category, final DocumentSource source) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
    } 
    
    /**
     * {@inheritDoc}
     */
    public List<Document> parse(final InputStream is) throws Exception {
        m_docs = new ArrayList<Document>();
        SAXParserFactory fac = SAXParserFactory.newInstance();
        fac.setValidating(true);
        fac.newSAXParser().parse(is, this);
        return m_docs;
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, 
            final String qName, final Attributes attributes) {
        m_lastTag = qName.toLowerCase();
        
        if (m_lastTag.equals(PUBMEDARTICLE)) {
            m_currentDoc = new DocumentBuilder();
            if (m_category != null) {
                m_currentDoc.addDocumentCategory(m_category);
            }
            if (m_source != null) {
                m_currentDoc.addDocumentSource(m_source);
            }
            if (m_type != null) {
                m_currentDoc.setDocumentType(m_type);
            }            
            if (m_docPath != null) {
                File f = new File(m_docPath);
                if (f.exists()) {
                    m_currentDoc.setDocumentFile(f);
                }
            }
        } else if (m_lastTag.equals(ABSTRACTTEXT)) {
            m_abstract = "";    
        } else if (m_lastTag.equals(ARTICLETITLE)) {
            m_title = "";
        } else if (m_lastTag.equals(FIRSTNAME) || qName.equals(FORENAME)) {
            m_firstName = "";
        } else if (m_lastTag.equals(LASTNAME)) {
            m_lastName = "";
        } else if (m_lastTag.equals(PUBDATE)) {
            m_pubDateFlag = true;
        } else if (m_lastTag.equals(YEAR)) {
            m_year = "";
        } else if (m_lastTag.equals(MONTH)) {
            m_month = "";
        } else if (m_lastTag.equals(DAY)) {
            m_day = "";
        } else if (m_lastTag.equals(JOURNAL)) {
            m_journalFlag = true;
        } else if (m_lastTag.equals(TITLE)) {
            m_journalTitle = "";
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, 
            final String qName) {
        String name = qName.toLowerCase();
        if (name.equals(PUBMEDARTICLE) && m_currentDoc != null) {
            Document doc = m_currentDoc.createDocument();
            m_docs.add(doc);
            m_currentDoc = null;
        } else if (name.equals(ABSTRACTTEXT)) {
            m_currentDoc.addSection(m_abstract.trim(), 
                    SectionAnnotation.ABSTRACT);
        } else if (name.equals(ARTICLETITLE)) {
            m_currentDoc.addTitle(m_title.trim());
        } else if (name.equals(AUTHOR)) {
            Author a = new Author(m_firstName.trim(), m_lastName.trim());
            m_currentDoc.addAuthor(a);
        } else if (name.equals(PUBDATE)) {
            m_pubDateFlag = false;
            int year = 0;
            int day = 0;
            if (m_year.length() > 0) {
                year = Integer.parseInt(m_year);
            }
            if (m_day.length() > 0) {
                day = Integer.parseInt(m_day);
            }
            try {
                PublicationDate pubDate = PublicationDate.createPublicationDate(
                        year, m_month, day);
                m_currentDoc.setPublicationDate(pubDate);
            } catch (Exception e) {
                LOGGER.warn("Publication date could not be created!");
                LOGGER.warn(e.getMessage());
                
                // set empty PublicationDate!
                m_currentDoc.setPublicationDate(new PublicationDate());
            }
        } else if (name.equals(TITLE)) {
            if (m_journalTitle.length() > 0) {
                m_currentDoc.addSection(m_journalTitle, 
                        SectionAnnotation.JOURNAL_TITLE);
            }
        } else if (name.equals(JOURNAL)) {
            m_journalFlag = false;
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
        } else if (m_lastTag.equals(ARTICLETITLE)) {
            m_title += new String(ch, start, length);
        } else if (m_lastTag.equals(ABSTRACTTEXT)) {
            m_abstract += new String(ch, start, length);
        } else if (m_lastTag.equals(YEAR) && m_pubDateFlag) {
            m_year += new String(ch, start, length);
        } else if (m_lastTag.equals(MONTH) && m_pubDateFlag) {
            m_month += new String(ch, start, length);
        } else if (m_lastTag.equals(DAY) && m_pubDateFlag) {
            m_day += new String(ch, start, length);
        } else if (m_lastTag.equals(TITLE) && m_journalFlag) {
            m_journalTitle += new String(ch, start, length);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentCategory(final DocumentCategory category) {
        m_category = category;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentSource(final DocumentSource source) {
        m_source = source;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentType(final DocumentType type) {
        m_type = type;
    } 
    
    /**
     * {@inheritDoc}
     */
    public void setDocumentFilepath(final String filePath) {
        m_docPath = filePath;
    }    
}

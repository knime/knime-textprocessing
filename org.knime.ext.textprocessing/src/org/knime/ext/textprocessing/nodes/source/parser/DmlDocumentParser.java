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
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.ByteArrayOutputStream;
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
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.util.DocumentBuilder;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Implements the 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} 
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DmlDocumentParser#parse(InputStream)}
 * is able to parse the data of the given input stream representing <i>dml</i>
 * (<b>D</b>ocument <b>M</b>arkup <b>L</b>anguage) formatted text documents. 
 * See the <i>dml.dtd</i> file for more details about the format. 
 * This format is also used to serialize the 
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s. Furthermore this 
 * class provides the method 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DmlDocumentParser#documentAsSdml(Document)}
 * which creates the serialized dml representation of the given document as 
 * a string.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DmlDocumentParser extends DefaultHandler implements
        DocumentParser {

    /**
     * The name of the document tag.
     */
    public static final String DOCUMENT = "Document";
    
    /**
     * The name of the term tag.
     */    
    public static final String TERM = "Term";
    
    /**
     * The name of the word tag.
     */
    public static final String WORD = "Word";
    
    /**
     * The name of the <i>tag</i> tag.
     */
    public static final String TAG = "Tag";
    
    /**
     * The name of the <i>tag</i> value tag.
     */    
    public static final String TAG_VALUE = "TagValue";
    
    /**
     * The name of the <i>tag</i> type tag.
     */    
    public static final String TAG_TYPE = "TagType";
    
    /**
     * The name of the sentence tag.
     */    
    public static final String SENTENCE = "Sentence";
    
    /**
     * The name of the paragraph tag.
     */    
    public static final String PARAGRAPH = "Paragraph";
    
    /**
     * The name of the section tag.
     */    
    public static final String SECTION = "Section";
    
    /**
     * The name of the annotation attribute.
     */
    public static final String ANNOTATION = "Annotation";
    
    /**
     * The name of the authors tag.
     */
    public static final String AUTHORS = "Authors";
    
    /**
     * The name of the author tag.
     */
    public static final String AUTHOR = "Author";
    
    /**
     * The name of the first name tag.
     */
    public static final String FIRSTNAME = "Firstname";
    
    /**
     * The name of the last name tag.
     */
    public static final String LASTNAME = "Lastname";
    
    /**
     * The name of the publication date tag.
     */
    public static final String PUBLICATIONDATE = "PublicationDate";
    
    /**
     * The name of the day tag.
     */
    public static final String DAY = "Day";
    
    /**
     * The name of the month tag.
     */
    public static final String MONTH = "Month";
    
    /**
     * The name of the year tag.
     */
    public static final String YEAR = "Year";
    
    
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(DmlDocumentParser.class);
    
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
    
    private String m_word = "";
    
    private String m_tagType = "";
    
    private String m_tagValue = "";
    
    private List<Word> m_words;
    
    private List<Tag> m_tags;
    
    private String m_annotation = "";
    
    
    
    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The documents
     * source, category and file path will be set to <code>null</code> be 
     * default.
     */
    public DmlDocumentParser() {
        this(null, null, null, null);
    }
    
    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The given
     * source, category and file path is set to the created documents. These
     * documents are kept in the given list.
     * 
     * @param docs The list to keep the created 
     * {@link org.knime.ext.textprocessing.data.Document} instances.
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public DmlDocumentParser(final List<Document> docs, final String docPath,
            final DocumentCategory category, final DocumentSource source) {
        if (docs == null) {
            m_docs = new ArrayList<Document>();
        } else {
            m_docs = docs;
        }
        m_category = category;
        m_source = source;
        m_docPath = docPath;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Document> parse(final InputStream is) {
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        } catch (ParserConfigurationException e) {
            LOGGER.error("Could not instanciate parser");
            LOGGER.warn(e.getStackTrace());
        } catch (SAXException e) {
            LOGGER.error("Could not parse file");
            LOGGER.warn(e.getStackTrace());
        } catch (IOException e) {
            LOGGER.error("Could not read file");
            LOGGER.warn(e.getStackTrace());
        }
        return m_docs;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, 
            final String qName, final Attributes attributes) {
        m_lastTag = qName;
        
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
        } else if(m_lastTag.equals(WORD)) {
            m_word = "";
        } else if(m_lastTag.equals(TAG_TYPE)) {
            m_tagType = "";
        } else if(m_lastTag.equals(TAG_VALUE)) {
            m_tagValue = "";
        } else if(m_lastTag.equals(TERM)) {
            m_words = new ArrayList<Word>();
            m_tags = new ArrayList<Tag>();
        } else if (m_lastTag.equals(SECTION)) {
            m_annotation = attributes.getValue(ANNOTATION);
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
            m_currentDoc = null;
        } else if(m_lastTag.equals(AUTHOR)) {
            Author a = new Author(m_firstName, m_lastName);
            m_currentDoc.addAuthor(a);
        } else if (m_lastTag.equals(PUBLICATIONDATE)) {
            int day = Integer.parseInt(m_day);
            int month = Integer.parseInt(m_month);
            int year = Integer.parseInt(m_year);
            PublicationDate pd;
            try {
                pd = new PublicationDate(year, month, day);
                m_currentDoc.setPublicationDate(pd);
            } catch (ParseException e) {
                LOGGER.warn("Publication date (" 
                        + year + "-" + month + "-" + day 
                        + ") could not be parsed !");
                LOGGER.warn(e.getStackTrace());
            }
        } else if(m_lastTag.equals(WORD)) {
            if (m_words != null && m_word != null) {
                Word w = new Word(m_word);
                m_words.add(w);
            }
        } else if(m_lastTag.equals(TAG)) {
            if (m_tags != null && m_tagType != null && m_tagValue != null) {
                Tag t = TagFactory.createTag(m_tagType, m_tagValue);
                m_tags.add(t);
            }
        } else if(m_lastTag.equals(TERM)) {
            if (m_words != null && m_tags != null) {
                Term t = new Term(m_words, m_tags);
                m_currentDoc.addTerm(t);
            }
        } else if (m_lastTag.equals(SENTENCE)) {
            m_currentDoc.createNewSentence();
        } else if (m_lastTag.equals(PARAGRAPH)) {
            m_currentDoc.createNewParagraph();
        } else if (m_lastTag.equals(SECTION)) {
            m_currentDoc.createNewSection(
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
        } else if (m_lastTag.equals(WORD)) {
            m_word += new String(ch, start, length);
        } else if (m_lastTag.equals(TAG_TYPE)) {
            m_tagType += new String(ch, start, length);
        } else if (m_lastTag.equals(TAG_VALUE)) {
            m_tagValue += new String(ch, start, length);
        }
    }
    
    
    
    
    
    
    
    
    
    /**
     * Creates a <i>dml</i> out of the given document. See the <i>dml.dtd</i>
     * for more details about the format.
     * 
     * @param doc The document to create the <i>dml</i> representation for.
     * @return The <i>dml</i> representation of the given document.
     */
    public static String documentAsSdml(final Document doc) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // header
        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setIndenting(true);
        // of.setDoctype(null, "dml.dtd");

        try {
            XMLSerializer serializer = new XMLSerializer(os, of);
            ContentHandler hd = serializer.asContentHandler();

            hd.startDocument();
            AttributesImpl atts = new AttributesImpl();
            hd.startElement("", "", DOCUMENT, atts);

            // Authors
            if (doc.getAuthors().size() > 0) {
                atts.clear();
                hd.startElement("", "", AUTHORS, atts);
                for (Author a : doc.getAuthors()) {
                    atts.clear();
                    hd.startElement("", "", AUTHOR, atts);
                    if (a.getFirstName().length() > 0) {
                        atts.clear();
                        hd.startElement("", "", FIRSTNAME, atts);
                        hd.characters(a.getFirstName().toCharArray(), 0, a
                                .getFirstName().length());
                        hd.endElement("", "", FIRSTNAME);
                    }
                    if (a.getLastName().length() > 0) {
                        atts.clear();
                        hd.startElement("", "", LASTNAME, atts);
                        hd.characters(a.getLastName().toCharArray(), 0, a
                                .getLastName().length());
                        hd.endElement("", "", LASTNAME);
                    }
                    hd.endElement("", "", AUTHOR);
                }
                hd.endElement("", "", AUTHORS);
            }

            // PublicationDate
            atts.clear();
            hd.startElement("", "", PUBLICATIONDATE, atts);
            // Day
            atts.clear();
            hd.startElement("", "", DAY, atts);
            hd.characters(Integer.toString(doc.getPubDate().getDay())
                    .toCharArray(), 0, Integer.toString(
                    doc.getPubDate().getDay()).length());
            hd.endElement("", "", DAY);
            // Month
            atts.clear();
            hd.startElement("", "", MONTH, atts);
            hd.characters(Integer.toString(doc.getPubDate().getMonth())
                    .toCharArray(), 0, Integer.toString(
                    doc.getPubDate().getMonth()).length());
            hd.endElement("", "", MONTH);
            // Year
            atts.clear();
            hd.startElement("", "", YEAR, atts);
            hd.characters(Integer.toString(doc.getPubDate().getYear())
                    .toCharArray(), 0, Integer.toString(
                    doc.getPubDate().getYear()).length());
            hd.endElement("", "", YEAR);
            hd.endElement("", "", PUBLICATIONDATE);

            // Sections (except title section)
            for (Section s : doc.getSections()) {
                atts.clear();
                atts.addAttribute("", "", ANNOTATION, "CDATA", s
                        .getAnnotation().toString());
                hd.startElement("", "", SECTION, atts);

                // Paragraphs
                for (Paragraph p : s.getParagraphs()) {
                    atts.clear();
                    hd.startElement("", "", PARAGRAPH, atts);

                    // Sentences
                    for (Sentence sn : p.getSentences()) {
                        atts.clear();
                        hd.startElement("", "", SENTENCE, atts);

                        // Terms
                        for (Term t : sn.getTerms()) {
                            atts.clear();
                            hd.startElement("", "", TERM, atts);

                            // Words
                            for (Word w : t.getWords()) {
                                atts.clear();
                                hd.startElement("", "", WORD, atts);
                                hd.characters(w.getWord().toCharArray(), 0, w
                                        .getWord().length());
                                hd.endElement("", "", WORD);
                            }

                            // Tags
                            for (Tag tag : t.getTags()) {
                                atts.clear();
                                hd.startElement("", "", TAG, atts);
                                // TagValue
                                hd.startElement("", "", TAG_VALUE, atts);
                                hd.characters(tag.getTagValue().toCharArray(),
                                        0, tag.getTagValue().length());
                                hd.endElement("", "", TAG_VALUE);
                                // TagType
                                hd.startElement("", "", TAG_TYPE, atts);
                                hd.characters(tag.getTagType().toCharArray(),
                                        0, tag.getTagType().length());
                                hd.endElement("", "", TAG_TYPE);
                                hd.endElement("", "", TAG);
                            }
                            hd.endElement("", "", TERM);
                        }
                        hd.endElement("", "", SENTENCE);
                    }
                    hd.endElement("", "", PARAGRAPH);
                }
                hd.endElement("", "", SECTION);
            }

            hd.endElement("", "", DOCUMENT);
            hd.endDocument();
            os.close();
        } catch (SAXException e1) {
            LOGGER.error("Could not create xml output!");
            LOGGER.warn(e1.getStackTrace());
        } catch (IOException e2) {
            LOGGER.error("Could not write xml output to output stream!");
            LOGGER.warn(e2.getStackTrace());
        }

        return os.toString();
    }
}

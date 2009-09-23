/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
package org.knime.ext.textprocessing.nodes.source.parser.dml;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * Implements the 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} 
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)}
 * is able to parse the data of the given input stream representing <i>dml</i>
 * (<b>D</b>ocument <b>M</b>arkup <b>L</b>anguage) formatted text documents. 
 * See the <i>dml.dtd</i> file for more details about the format. 
 * This format is also used to serialize the 
 * {@link org.knime.ext.textprocessing.data.DocumentCell}s. Furthermore this 
 * class provides the method 
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#documentAsDml(Document)}
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
    public static final String DOCUMENT = "document";

    /**
     * The name of the file name.
     */
    public static final String FILENAME = "filename";
    
    /**
     * The document's category.
     */
    public static final String CATEGORY = "category";
    
    /**
     * The document's source.
     */
    public static final String SOURCE = "source";
    
    /**
     * The document's type.
     */
    public static final String DOCUMENT_TYPE = "documenttype";
    
    /**
     * The name of the term tag.
     */    
    public static final String TERM = "term";
    
    /**
     * The name of the word tag.
     */
    public static final String WORD = "word";

    /**
     * The name of the modifiability tag.
     */
    public static final String MODIFIABILITY = "modifiability";    
    
    /**
     * The name of the <i>tag</i> tag.
     */
    public static final String TAG = "tag";
    
    /**
     * The name of the <i>tag</i> value tag.
     */    
    public static final String TAG_VALUE = "tagvalue";
    
    /**
     * The name of the <i>tag</i> type tag.
     */    
    public static final String TAG_TYPE = "tagtype";
    
    /**
     * The name of the sentence tag.
     */    
    public static final String SENTENCE = "sentence";
    
    /**
     * The name of the paragraph tag.
     */    
    public static final String PARAGRAPH = "paragraph";
    
    /**
     * The name of the section tag.
     */    
    public static final String SECTION = "section";
    
    /**
     * The name of the annotation attribute.
     */
    public static final String ANNOTATION = "annotation";
    
    /**
     * The name of the authors tag.
     */
    public static final String AUTHORS = "authors";
    
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
    
    /**
     * The path (postfix) of the dml.dtd file relative to the plugin 
     * directory.
     */
    public static final String DML_DTD_POSTFIX = 
        "/resources/documentformat/dml.dtd";
    
    /**
     * The public identifier for (dml) xml files.
     */
    public static final String PUBLIC_IDENTIFIER = 
        "-//UNIKN//DTD KNIME Dml 2.0//EN";
    
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(DmlDocumentParser.class);
    
    private List<Document> m_docs;
    
    private DocumentCategory m_category;
    
    private String m_currentCategory = "";
    
    private DocumentSource m_source;
    
    private String m_currentSource = "";
    
    private DocumentType m_type;
    
    private String m_currentType = "";
    
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
    
    private String m_modifiability = "";
    
    private String m_annotation = "";
    
    
    
    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The documents
     * source, category and file path will be set to <code>null</code> by 
     * default.
     */
    public DmlDocumentParser() {
        this(null, null, null);
    }
    
    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     * 
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public DmlDocumentParser(final String docPath,
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
        SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        return m_docs;
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
                path += DML_DTD_POSTFIX;
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
        m_lastTag = qName.toLowerCase();
        
        if (m_lastTag.equals(DOCUMENT)) {
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
        } else if (m_lastTag.equals(FIRSTNAME)) {
            m_firstName = "";
        } else if (m_lastTag.equals(LASTNAME)) {
            m_lastName = "";
        } else if (m_lastTag.equals(DAY)) {
            m_day = "";
        } else if (m_lastTag.equals(MONTH)) {
            m_month = "";
        } else if (m_lastTag.equals(YEAR)) {
            m_year = "";
        } else if (m_lastTag.equals(WORD)) {
            m_word = "";
        } else if (m_lastTag.equals(TAG_TYPE)) {
            m_tagType = "";
        } else if (m_lastTag.equals(TAG_VALUE)) {
            m_tagValue = "";
        } else if (m_lastTag.equals(MODIFIABILITY)) { 
            m_modifiability = "";
        } else if (m_lastTag.equals(TERM)) {
            m_words = new ArrayList<Word>();
            m_tags = new ArrayList<Tag>();
        } else if (m_lastTag.equals(SECTION)) {
            m_annotation = attributes.getValue(ANNOTATION);
        } else if (m_lastTag.equals(FILENAME)) {
            m_docPath = "";
        } else if (m_lastTag.equals(CATEGORY)) {
            m_currentCategory = "";
        } else if (m_lastTag.equals(SOURCE)) {
            m_currentSource = "";
        } else if (m_lastTag.equals(DOCUMENT_TYPE)) {
            m_currentType = "";
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
        } else if (qName.equals(AUTHOR)) {
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
                LOGGER.warn(e.getStackTrace());
            }
        } else if (qName.equals(WORD)) {
            if (m_words != null && m_word != null) {
                Word w = new Word(m_word.trim());
                m_words.add(w);
            }
        } else if (qName.equals(TAG)) {
            if (m_tags != null && m_tagType != null && m_tagValue != null) {
                Tag t = TagFactory.getInstance().createTag(m_tagType.trim(), 
                        m_tagValue.trim());
                m_tags.add(t);
            }
        } else if (qName.equals(TERM)) {
            if (m_words != null && m_tags != null) {
                boolean mod = new Boolean(m_modifiability.trim());
                Term t = new Term(m_words, m_tags, mod);
                m_currentDoc.addTerm(t);
            }
        } else if (qName.equals(SENTENCE)) {
            m_currentDoc.createNewSentence();
        } else if (qName.equals(PARAGRAPH)) {
            m_currentDoc.createNewParagraph();
        } else if (qName.equals(SECTION)) {
            m_currentDoc.createNewSection(
                    SectionAnnotation.stringToAnnotation(m_annotation));
        } else if (qName.equals(FILENAME)) {
            File f = new File(m_docPath);
            if (f.exists()) {
                m_currentDoc.setDocumentFile(f);
            }
        } else if (qName.equals(CATEGORY)) {
            DocumentCategory cat = new DocumentCategory(m_currentCategory);
            m_currentDoc.addDocumentCategory(cat);
        } else if (qName.equals(SOURCE)) {
            DocumentSource source = new DocumentSource(m_currentSource);
            m_currentDoc.addDocumentSource(source);
        } else if (qName.equals(DOCUMENT_TYPE)) {
            if (m_type == null) {
                DocumentType type =
                        DocumentType.stringToDocumentType(m_currentType);
                m_currentDoc.setDocumentType(type);
            }
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
        } else if (m_lastTag.equals(FILENAME)) {
            m_docPath += new String(ch, start, length);
        } else if (m_lastTag.equals(MODIFIABILITY)) {
            m_modifiability +=  new String(ch, start, length);
        } else if (m_lastTag.equals(CATEGORY)) {
            m_currentCategory +=  new String(ch, start, length);
        } else if (m_lastTag.equals(SOURCE)) {
            m_currentSource +=  new String(ch, start, length);
        } else if (m_lastTag.equals(DOCUMENT_TYPE)) {
            m_currentType +=  new String(ch, start, length);
        }
    }

    
    
    
    
    
    /**
     * Creates a <i>dml</i> out of the given document. See the <i>dml.dtd</i>
     * for more details about the format.
     * 
     * @param doc The document to create the <i>dml</i> representation for.
     * @return The <i>dml</i> representation of the given document.
     */
    public static String documentAsDml(final Document doc) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // header
        OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
        of.setIndent(1);
        of.setIndenting(true);
        of.setDoctype(PUBLIC_IDENTIFIER, "./dml.dtd");

        try {
            XMLSerializer serializer = new XMLSerializer(os, of);
            ContentHandler hd = serializer.asContentHandler();

            hd.startDocument();
            AttributesImpl atts = new AttributesImpl();
            hd.startElement("", "", DOCUMENT, atts);

            // Filename
            if (doc.getDocFile() != null && doc.getDocFile().length() > 0) {
                atts.clear();
                hd.startElement("", "", FILENAME, atts);
                String filename = stripNonValidXMLCharacters(
                        doc.getDocFile().getAbsolutePath()); 
                hd.characters(filename.toCharArray(), 0, filename.length());
                hd.endElement("", "", FILENAME);
            }
            
            // DocumentCategroy
            for (DocumentCategory cat : doc.getCategories()) {
                atts.clear();
                hd.startElement("", "", CATEGORY, atts);
                String category = stripNonValidXMLCharacters(
                        cat.getCategoryName()); 
                hd.characters(category.toCharArray(), 0, category.length());
                hd.endElement("", "", CATEGORY);                
            }
            
            // DocumentSource
            for (DocumentSource source : doc.getSources()) {
                atts.clear();
                hd.startElement("", "", SOURCE, atts);
                String docSource = stripNonValidXMLCharacters(
                        source.getSourceName()); 
                hd.characters(docSource.toCharArray(), 0, docSource.length());
                hd.endElement("", "", SOURCE);                
            }
            
            // Document Type
            if (doc.getType() != null) {
                atts.clear();
                hd.startElement("", "", DOCUMENT_TYPE, atts);
                String type = stripNonValidXMLCharacters(
                        doc.getType().toString());
                hd.characters(type.toCharArray(), 0, type.length());
                hd.endElement("", "", DOCUMENT_TYPE);                
            }
            
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
                        String firstname = stripNonValidXMLCharacters(
                                a.getFirstName()); 
                        hd.characters(firstname.toCharArray(), 0, 
                                firstname.length());
                        hd.endElement("", "", FIRSTNAME);
                    }
                    if (a.getLastName().length() > 0) {
                        atts.clear();
                        hd.startElement("", "", LASTNAME, atts);
                        String lastname = stripNonValidXMLCharacters(
                                a.getLastName()); 
                        hd.characters(lastname.toCharArray(), 0, 
                                lastname.length());
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
            String day = stripNonValidXMLCharacters(
                    Integer.toString(doc.getPubDate().getDay()));
            hd.characters(day.toCharArray(), 0, day.length());
            hd.endElement("", "", DAY);
            // Month
            atts.clear();
            hd.startElement("", "", MONTH, atts);
            String month = stripNonValidXMLCharacters(
                    Integer.toString(doc.getPubDate().getMonth())); 
            hd.characters(month.toCharArray(), 0, month.length());
            hd.endElement("", "", MONTH);
            // Year
            atts.clear();
            hd.startElement("", "", YEAR, atts);
            String year = stripNonValidXMLCharacters(
                    Integer.toString(doc.getPubDate().getYear()));
            hd.characters(year.toCharArray(), 0, year.length());
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
                            
                            // Modifiability
                            hd.startElement("", "", MODIFIABILITY, atts);
                            String mod = Boolean.toString(t.isUnmodifiable());
                            hd.characters(mod.toCharArray(), 0, mod.length());
                            hd.endElement("", "", MODIFIABILITY);
                            
                            // Words
                            for (Word w : t.getWords()) {
                                atts.clear();
                                hd.startElement("", "", WORD, atts);
                                String word = stripNonValidXMLCharacters(
                                        w.getWord());
                                hd.characters(word.toCharArray(), 0, 
                                        word.length());
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
            LOGGER.error("Could not create xml output of documemnt " 
                    + "file:" + doc.getDocFile() 
                    + " / title:" + doc.getTitle());
            LOGGER.info(e1.getMessage());
            e1.printStackTrace();
        } catch (IOException e2) {
            LOGGER.error("Could not write xml output to output stream " 
                    + "of document file:" + doc.getDocFile() 
                    + " / title:" + doc.getTitle());
            LOGGER.info(e2.getMessage());
            e2.printStackTrace();
        }

        return os.toString();
    }
    
    /**
     * Strips non valid XML characters and returns stripped string.
     * 
     * @param in String with non valid XML characters which have to be removed
     * @return Stripped string containing only valid XML characters
     */
    public static String stripNonValidXMLCharacters(final String in) {
        StringBuffer out = new StringBuffer();
        char curr;
        if (in == null || (in.equals(""))) {
            return "";
        }
        for (int i = 0; i < in.length(); i++) {
            curr = in.charAt(i);
            if ((curr == 0x9) 
                    || (curr == 0xA) 
                    || (curr == 0xD) 
                    || ((curr >= 0x20) && (curr <= 0xD7FF)) 
                    || ((curr >= 0xE000) && (curr <= 0xFFFD)) 
                    || ((curr >= 0x10000) && (curr <= 0x10FFFF))) {
                out.append(curr);
            }
        }
        return out.toString();
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

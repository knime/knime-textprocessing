/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.dml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

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
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)} is able to parse
 * the data of the given input stream representing <i>dml</i> (<b>D</b>ocument <b>M</b>arkup <b>L</b>anguage) formatted
 * text documents. See the <i>dml.dtd</i> file for more details about the format. This format is also used to serialize
 * the {@link org.knime.ext.textprocessing.data.DocumentCell}s. Furthermore this class provides the method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#documentAsDml(Document)} which creates
 * the serialized dml representation of the given document as a string.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DmlDocumentParser extends DefaultHandler implements DocumentParser {

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
     * The path (postfix) of the dml.dtd file relative to the plugin directory.
     */
    private static final String DML_DTD_POSTFIX = "documentformat/dml.dtd";

    /**
     * The public identifier for (dml) xml files.
     */
    public static final String PUBLIC_IDENTIFIER = "-//UNIKN//DTD KNIME Dml 2.0//EN";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DmlDocumentParser.class);

    private static final SAXTransformerFactory TRANSFORMER_FACTORY =
        (SAXTransformerFactory)TransformerFactory.newInstance();

    private static final String DEF_WHITESPACE_SUFFIX = " ";

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

    private boolean m_storeInList = false;

    // initialize the tokenizer with the old standard tokenizer for backwards compatibility
    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The documents source, category and file path will be
     * set to <code>null</code> by default.
     *
     * @deprecated Use {@link #DmlDocumentParser(String)} instead to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public DmlDocumentParser() {
        this(null, null, null, TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates a new instance of {@code DmlDocumentParser}. The document source, category and file path will be set to
     * {@code null} by default. The parser instance will use the given tokenizer.
     *
     * @param tokenizerName The tokenizer used to tokenize words.
     * @since 3.3
     */
    public DmlDocumentParser(final String tokenizerName) {
        this(null, null, null, tokenizerName);
    }

    /**
     * Creates a new instance of <code>DmlDocumentParser</code>. The given source, category and file path is set to the
     * created documents. The parser instance will use the given tokenizer.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param tokenizerName The tokenizer used to tokenize words.
     * @since 3.3
     */
    public DmlDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final String tokenizerName) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_tokenizerName = tokenizerName;
        m_listener = new ArrayList<DocumentParsedEventListener>();
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public List<Document> parse(final InputStream is) throws Exception {
        m_docs = new ArrayList<Document>();
        m_storeInList = true;
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        } catch (SAXException e) {
            LOGGER.warn("Could not parse DML documents, XML is not valid!");
            throw (e);
        } catch (IOException e) {
            LOGGER.warn("Could not read DML documents!");
            throw (e);
        }
        return m_docs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clean() {
        if (m_docs != null) {
            m_docs.clear();
        }
        if (m_words != null) {
            m_words.clear();
        }
        if (m_tags != null) {
            m_tags.clear();
        }
        m_currentDoc = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputSource resolveEntity(final String pubId, final String sysId) throws IOException, SAXException {
        if (Objects.equals(pubId, PUBLIC_IDENTIFIER)) {
            InputStream in = new FileInputStream(TextprocessingCorePlugin.resolvePath(DML_DTD_POSTFIX));
            return new InputSource(in);
        }
        return super.resolveEntity(pubId, sysId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) {
        m_lastTag = qName.toLowerCase();

        if (m_lastTag.equals(DOCUMENT)) {
            m_currentDoc = new DocumentBuilder(m_tokenizerName);
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
    public void endElement(final String uri, final String localName, final String qName) {
        String endTag = qName.toLowerCase();
        if (endTag.equals(DOCUMENT) && m_currentDoc != null) {
            Document doc = m_currentDoc.createDocument();

            // due to memory issues documents are not all stored in list anymore
            // but handed out via listener mechanism
            if (m_storeInList) {
                m_docs.add(doc);
            }
            notifyAllListener(new DocumentParsedEvent(doc, this));

            m_currentDoc = null;
        } else if (endTag.equals(AUTHOR)) {
            Author a = new Author(m_firstName.trim(), m_lastName.trim());
            m_currentDoc.addAuthor(a);
        } else if (endTag.equals(PUBLICATIONDATE)) {
            int day = Integer.parseInt(m_day.trim());
            int month = Integer.parseInt(m_month.trim());
            int year = Integer.parseInt(m_year.trim());
            PublicationDate pd;
            try {
                //pd = new PublicationDate(year, month, day);
                pd = PublicationDate.createPublicationDate(year, month, day);
                m_currentDoc.setPublicationDate(pd);
            } catch (ParseException e) {
                LOGGER.warn("Publication date (" + year + "-" + month + "-" + day + ") could not be parsed !");
                LOGGER.warn(e.getStackTrace());
            }
        } else if (endTag.equals(WORD)) {
            if (m_words != null && m_word != null) {
                Word w = new Word(m_word.trim(), DEF_WHITESPACE_SUFFIX);
                m_words.add(w);
            }
        } else if (endTag.equals(TAG)) {
            if (m_tags != null && m_tagType != null && m_tagValue != null) {
                Tag t = TagFactory.getInstance().createTag(m_tagType.trim(), m_tagValue.trim());
                m_tags.add(t);
            }
        } else if (endTag.equals(TERM)) {
            if (m_words != null && m_tags != null) {
                boolean mod = new Boolean(m_modifiability.trim());
                Term t = new Term(m_words, m_tags, mod);
                m_currentDoc.addTerm(t);
            }
        } else if (endTag.equals(SENTENCE)) {
            m_currentDoc.createNewSentence();
        } else if (endTag.equals(PARAGRAPH)) {
            m_currentDoc.createNewParagraph();
        } else if (endTag.equals(SECTION)) {
            m_currentDoc.createNewSection(SectionAnnotation.stringToAnnotation(m_annotation));
        } else if (endTag.equals(FILENAME)) {
            File f = new File(m_docPath);
            if (f.exists()) {
                m_currentDoc.setDocumentFile(f);
            }
        } else if (endTag.equals(CATEGORY)) {
            DocumentCategory cat = new DocumentCategory(m_currentCategory);
            m_currentDoc.addDocumentCategory(cat);
        } else if (endTag.equals(SOURCE)) {
            DocumentSource source = new DocumentSource(m_currentSource);
            m_currentDoc.addDocumentSource(source);
        } else if (endTag.equals(DOCUMENT_TYPE)) {
            if (m_type == null) {
                DocumentType type = DocumentType.stringToDocumentType(m_currentType);
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
            m_modifiability += new String(ch, start, length);
        } else if (m_lastTag.equals(CATEGORY)) {
            m_currentCategory += new String(ch, start, length);
        } else if (m_lastTag.equals(SOURCE)) {
            m_currentSource += new String(ch, start, length);
        } else if (m_lastTag.equals(DOCUMENT_TYPE)) {
            m_currentType += new String(ch, start, length);
        }
    }

    /**
     * Creates a <i>dml</i> out of the given document. See the <i>dml.dtd</i> for more details about the format.
     *
     * @param doc The document to create the <i>dml</i> representation for.
     * @return The <i>dml</i> representation of the given document.
     */
    public static String documentAsDml(final Document doc) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        // header
        String str = null;
        try {
            TransformerHandler hd = TRANSFORMER_FACTORY.newTransformerHandler();
            Transformer transformer = hd.getTransformer();

            final String encoding = "UTF-8";
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
            transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, PUBLIC_IDENTIFIER);
            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "./dml.dtd");

            hd.startDocument();
            AttributesImpl atts = new AttributesImpl();
            hd.startElement("", "", DOCUMENT, atts);

            // Filename
            if (doc.getDocFile() != null && doc.getDocFile().length() > 0) {
                atts.clear();
                hd.startElement("", "", FILENAME, atts);
                String filename = stripNonValidXMLCharacters(doc.getDocFile().getAbsolutePath());
                hd.characters(filename.toCharArray(), 0, filename.length());
                hd.endElement("", "", FILENAME);
            }

            // DocumentCategroy
            for (DocumentCategory cat : doc.getCategories()) {
                atts.clear();
                hd.startElement("", "", CATEGORY, atts);
                String category = stripNonValidXMLCharacters(cat.getCategoryName());
                hd.characters(category.toCharArray(), 0, category.length());
                hd.endElement("", "", CATEGORY);
            }

            // DocumentSource
            for (DocumentSource source : doc.getSources()) {
                atts.clear();
                hd.startElement("", "", SOURCE, atts);
                String docSource = stripNonValidXMLCharacters(source.getSourceName());
                hd.characters(docSource.toCharArray(), 0, docSource.length());
                hd.endElement("", "", SOURCE);
            }

            // Document Type
            if (doc.getType() != null) {
                atts.clear();
                hd.startElement("", "", DOCUMENT_TYPE, atts);
                String type = stripNonValidXMLCharacters(doc.getType().toString());
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
                        String firstname = stripNonValidXMLCharacters(a.getFirstName());
                        hd.characters(firstname.toCharArray(), 0, firstname.length());
                        hd.endElement("", "", FIRSTNAME);
                    }
                    if (a.getLastName().length() > 0) {
                        atts.clear();
                        hd.startElement("", "", LASTNAME, atts);
                        String lastname = stripNonValidXMLCharacters(a.getLastName());
                        hd.characters(lastname.toCharArray(), 0, lastname.length());
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
            String day = stripNonValidXMLCharacters(Integer.toString(doc.getPubDate().getDay()));
            hd.characters(day.toCharArray(), 0, day.length());
            hd.endElement("", "", DAY);
            // Month
            atts.clear();
            hd.startElement("", "", MONTH, atts);
            String month = stripNonValidXMLCharacters(Integer.toString(doc.getPubDate().getMonth()));
            hd.characters(month.toCharArray(), 0, month.length());
            hd.endElement("", "", MONTH);
            // Year
            atts.clear();
            hd.startElement("", "", YEAR, atts);
            String year = stripNonValidXMLCharacters(Integer.toString(doc.getPubDate().getYear()));
            hd.characters(year.toCharArray(), 0, year.length());
            hd.endElement("", "", YEAR);
            hd.endElement("", "", PUBLICATIONDATE);

            // Sections (except title section)
            for (Section s : doc.getSections()) {
                atts.clear();
                atts.addAttribute("", "", ANNOTATION, "CDATA", s.getAnnotation().toString());
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
                                String word = stripNonValidXMLCharacters(w.getWord());
                                hd.characters(word.toCharArray(), 0, word.length());
                                hd.endElement("", "", WORD);
                            }

                            // Tags
                            for (Tag tag : t.getTags()) {
                                atts.clear();
                                hd.startElement("", "", TAG, atts);
                                // TagValue
                                hd.startElement("", "", TAG_VALUE, atts);
                                hd.characters(tag.getTagValue().toCharArray(), 0, tag.getTagValue().length());
                                hd.endElement("", "", TAG_VALUE);
                                // TagType
                                hd.startElement("", "", TAG_TYPE, atts);
                                hd.characters(tag.getTagType().toCharArray(), 0, tag.getTagType().length());
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

            str = os.toString("UTF-8");
        } catch (SAXException e1) {
            LOGGER.error("Could not create xml output of documemnt " + "file:" + doc.getDocFile() + " / title:"
                + doc.getTitle());
            LOGGER.info(e1.getMessage());
            e1.printStackTrace();
        } catch (IOException e2) {
            LOGGER.error("Could not write xml output to output stream " + "of document file:" + doc.getDocFile()
                + " / title:" + doc.getTitle());
            LOGGER.info(e2.getMessage());
            e2.printStackTrace();
        } catch (TransformerConfigurationException ex) {
            LOGGER.error("Could not create xml output of documemnt " + "file:" + doc.getDocFile() + " / title:"
                + doc.getTitle());
            LOGGER.info(ex.getMessage());
            ex.printStackTrace();
        }

        return str;
    }

    /**
     * Strips non valid XML characters and returns stripped string.
     *
     * @param in String with non valid XML characters which have to be removed
     * @return Stripped string containing only valid XML characters
     */
    public static String stripNonValidXMLCharacters(final String in) {
        StringBuilder out = new StringBuilder();
        char curr;
        if (in == null || (in.equals(""))) {
            return "";
        }
        for (int i = 0; i < in.length(); i++) {
            curr = in.charAt(i);
            if ((curr == 0x9) || (curr == 0xA) || (curr == 0xD) || ((curr >= 0x20) && (curr <= 0xD7FF))
                || ((curr >= 0xE000) && (curr <= 0xFFFD)) || ((curr >= 0x10000) && (curr <= 0x10FFFF))) {
                out.append(curr);
            }
        }
        return out.toString();
    }

    //    private final static Charset UTF8_CHARSET = Charset.forName("UTF-8");

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentCategory(final DocumentCategory category) {
        m_category = category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentSource(final DocumentSource source) {
        m_source = source;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentType(final DocumentType type) {
        m_type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocumentFilepath(final String filePath) {
        m_docPath = filePath;
    }

    /**
     * {@inheritDoc}
     *
     * The given charset is ignored since the SAX parser takes it from the xml file.
     */
    @Override
    public void setCharset(final Charset charset) {
    }

    /**
     * List of listeners.
     */
    private List<DocumentParsedEventListener> m_listener;

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseDocument(final InputStream is) throws Exception {
        m_storeInList = false;
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(is, this);
        } catch (SAXException e) {
            LOGGER.warn("Could not parse DML documents, XML is not valid!");
            throw (e);
        } catch (IOException e) {
            LOGGER.warn("Could not read DML documents!");
            throw (e);
        }
    }

    /**
     * Notifies all registered listeners with given event.
     *
     * @param event Event to notify listener with
     */
    public void notifyAllListener(final DocumentParsedEvent event) {
        for (DocumentParsedEventListener l : m_listener) {
            l.documentParsed(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDocumentParsedListener(final DocumentParsedEventListener listener) {
        m_listener.add(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDocumentParsedListener(final DocumentParsedEventListener listener) {
        m_listener.remove(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllDocumentParsedListener() {
        m_listener.clear();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.1
     */
    @Override
    public void setFilenameAsTitle(final boolean filenameAsTitle) {
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    @Override
    public void setTokenizerName(final String tokenizerName) {
        m_tokenizerName = tokenizerName;
    }
}

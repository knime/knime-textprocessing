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
 *   20.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.pubmed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)} is able to parse
 * the data of the given input stream containing PubMed (http://www.pubmed.org) document search results. For more
 * details about the xml format used by PubMed to deliver search results see
 * (http://www.ncbi.nlm.nih.gov/entrez/query/DTD/pubmed_060101.dtd).
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class PubMedDocumentParser extends DefaultHandler implements DocumentParser {

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

    /**
     * The pub med id.
     *
     * @since 2.7
     */
    public static final String PMID = "pmid";

    /**
     * The chemical list.
     *
     * @since 2.7
     */
    public static final String CHEMICAL_LIST = "chemicallist";

    /**
     * The chemical name.
     *
     * @since 2.7
     */
    public static final String NAME_OF_SUBSTANCE = "nameofsubstance";

    /**
     * The mesh heading list.
     *
     * @since 2.7
     */
    public static final String MESH_HEADING_LIST = "meshheadinglist";

    /**
     * The mesh descriptor name.
     *
     * @since 2.7
     */
    public static final String DESCRIPTOR_NAME = "descriptorname";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(PubMedDocumentParser.class);

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

    private String m_pmid = "";

    private boolean m_pmidIsSet = false;

    private List<String> m_meshHeadingList = new ArrayList<String>();

    private List<String> m_chemicalList = new ArrayList<String>();

    private String m_chemicalEntry = "";

    private String m_meshEntry = "";

    private boolean m_journalFlag = false;

    private boolean m_storeInList = false;

    private boolean m_extractMetaData = false;

    // initialize the tokenizer with the old standard tokenizer for backwards compatibility
    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The document source is set to
     * {@link org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser#DEFAULT_SOURCE} by default.
     * The document category and file path will be set to <code>null</code> by default.
     *
     * @deprecated Use {@link #PubMedDocumentParser(String)} instead to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public PubMedDocumentParser() {
        this(null, null, new DocumentSource(DEFAULT_SOURCE), false);
    }

    /**
     * Creates a new instance of {@code PubMedDocumentParser}. The document source is set to
     * {@link org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser#DEFAULT_SOURCE} by default.
     * The document category and file path will be set to <code>null</code> by default.
     *
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public PubMedDocumentParser(final String tokenizerName) {
        this(null, null, new DocumentSource(DEFAULT_SOURCE), false, tokenizerName);
    }

    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The document source is set to
     * {@link org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser#DEFAULT_SOURCE} by default.
     * The document category and file path will be set to <code>null</code> by default.
     *
     * @param extractMetaData The flag whether meta data (mesh info and pub med id) are extracted or not.
     * @since 2.7
     * @deprecated Use {@link #PubMedDocumentParser(boolean, String)} instead to define the tokenizer used for word
     *             tokenization.
     */
    @Deprecated
    public PubMedDocumentParser(final boolean extractMetaData) {
        this(null, null, new DocumentSource(DEFAULT_SOURCE), extractMetaData);
    }

    /**
     * Creates a new instance of {@code PubMedDocumentParser}. The document source is set to
     * {@link org.knime.ext.textprocessing.nodes.source.parser.pubmed.PubMedDocumentParser#DEFAULT_SOURCE} by default.
     * The document category and file path will be set to <code>null</code> by default.
     *
     * @param extractMetaData The flag whether meta data (mesh info and pub med id) are extracted or not.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public PubMedDocumentParser(final boolean extractMetaData, final String tokenizerName) {
        this(null, null, new DocumentSource(DEFAULT_SOURCE), extractMetaData, tokenizerName);
    }

    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The given source, category and file path is set to
     * the created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @deprecated Use {@link #PubMedDocumentParser(String, DocumentCategory, DocumentSource, String)} instead to define
     *             the tokenizer used for word tokenization.
     */
    @Deprecated
    public PubMedDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source) {
        this(docPath, category, source, false);
    }

    /**
     * Creates a new instance of {@code PubMedDocumentParser}. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public PubMedDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final String tokenizerName) {
        this(docPath, category, source, false, tokenizerName);
    }

    /**
     * Creates a new instance of <code>PubMedDocumentParser</code>. The given source, category and file path is set to
     * the created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param extractMetaData The flag whether meta data (mesh info and pub med id) are extracted or not.
     * @since 2.7
     * @deprecated Use {@link #PubMedDocumentParser(String, DocumentCategory, DocumentSource, boolean, String)} instead
     *             to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public PubMedDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final boolean extractMetaData) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_listener = new ArrayList<DocumentParsedEventListener>();
        m_extractMetaData = extractMetaData;
    }

    /**
     * Creates a new instance of {@code PubMedDocumentParser}. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param extractMetaData The flag whether meta data (mesh info and pub med id) are extracted or not.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public PubMedDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final boolean extractMetaData, final String tokenizerName) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_listener = new ArrayList<DocumentParsedEventListener>();
        m_extractMetaData = extractMetaData;
        m_tokenizerName = tokenizerName;
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
        SAXParserFactory fac = SAXParserFactory.newInstance();
        fac.setValidating(true);
        try {
            fac.newSAXParser().parse(is, this);
        } catch (SAXException e) {
            LOGGER.warn("Could not parse PubMed documents, XML is not valid!");
            throw (e);
        } catch (IOException e) {
            LOGGER.warn("Could not read PubMed documents!");
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
        m_currentDoc = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName,
        final Attributes attributes) {
        m_lastTag = qName.toLowerCase();

        if (m_lastTag.equals(PUBMEDARTICLE)) {
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
        } else if (m_lastTag.equals(ABSTRACTTEXT)) {
            m_abstract = "";
        } else if (m_lastTag.equals(ARTICLETITLE)) {
            m_title = "";
        } else if (m_lastTag.equals(FIRSTNAME) || m_lastTag.equals(FORENAME)) {
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
        } else if (m_lastTag.equals(PMID)) {
            m_pmid = "";
        } else if (m_lastTag.equals(DESCRIPTOR_NAME)) {
            m_meshEntry = "";
        } else if (m_lastTag.equals(NAME_OF_SUBSTANCE)) {
            m_chemicalEntry = "";
        } else if (m_lastTag.equals(MESH_HEADING_LIST)) {
            m_meshHeadingList = new ArrayList<String>();
        } else if (m_lastTag.equals(CHEMICAL_LIST)) {
            m_chemicalList = new ArrayList<String>();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) {
        String name = qName.toLowerCase();
        if (name.equals(PUBMEDARTICLE) && m_currentDoc != null) {
            Document doc = m_currentDoc.createDocument();

            // due to memory issues documents are not all stored in list anymore
            // but handed out via listener mechanism
            //m_docs.add(doc);
            if (m_storeInList) {
                m_docs.add(doc);
            }
            notifyAllListener(new DocumentParsedEvent(doc, this));

            m_currentDoc = null;
            // set empty strings to delete data from previous document
            m_abstract = "";
            m_title = "";
            m_firstName = "";
            m_lastName = "";
            m_day = "";
            m_month = "";
            m_year = "";
            m_journalTitle = "";
            m_pmid = "";
            m_chemicalEntry = "";
            m_meshEntry = "";
            m_pmidIsSet = false;
        } else if (name.equals(ABSTRACTTEXT)) {
            if (m_currentDoc != null) {
                m_currentDoc.addSection(m_abstract.trim(), SectionAnnotation.ABSTRACT);
            } else {
                LOGGER.info(
                    "No <" + PUBMEDARTICLE + "> start Element: " + "Abstract (" + ABSTRACTTEXT + ") cannot be set.");
            }
        } else if (name.equals(ARTICLETITLE)) {
            if (m_currentDoc != null) {
                m_currentDoc.addTitle(m_title.trim());
            } else {
                LOGGER.info("No <" + PUBMEDARTICLE + "> start Element: " + "Title (" + ARTICLETITLE + ":"
                    + m_title.trim() + ") cannot be set.");
            }
        } else if (name.equals(AUTHOR) && m_currentDoc != null) {
            if (m_currentDoc != null) {
                Author a = new Author(m_firstName.trim(), m_lastName.trim());
                m_currentDoc.addAuthor(a);
            } else {
                LOGGER.info("No <" + PUBMEDARTICLE + "> start Element: " + "Author (" + AUTHOR + ":"
                    + m_firstName.trim() + " " + m_lastName.trim() + ") cannot be set.");
            }
        } else if (name.equals(PUBDATE)) {
            if (m_currentDoc != null) {
                m_year = m_year.trim();
                m_month = m_month.trim();
                m_day = m_day.trim();

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
                    PublicationDate pubDate = PublicationDate.createPublicationDate(year, m_month, day);
                    m_currentDoc.setPublicationDate(pubDate);
                } catch (Exception e) {
                    LOGGER.info("Publication date could not be created!");
                    LOGGER.info(e.getMessage());

                    // set empty PublicationDate!
                    m_currentDoc.setPublicationDate(new PublicationDate());
                }
            } else {
                LOGGER.info("No <" + PUBMEDARTICLE + "> start Element: " + "Date (" + PUBDATE + ") cannot be set.");
            }
        } else if (name.equals(TITLE)) {
            if (m_journalTitle.length() > 0) {
                if (m_currentDoc != null) {
                    m_currentDoc.addSection(m_journalTitle.trim(), SectionAnnotation.JOURNAL_TITLE);
                }
            } else {
                LOGGER.info("No <" + PUBMEDARTICLE + "> start Element: " + "Journal title (" + TITLE + ":"
                    + m_journalTitle.trim() + ") cannot be set.");
            }
        } else if (name.equals(JOURNAL)) {
            m_journalFlag = false;
        } else if (name.equals(PMID)) {
            if (m_extractMetaData) {
                if (m_pmid.length() > 0 && !m_pmidIsSet) {
                    if (m_currentDoc != null) {
                        m_currentDoc.addMetaInformation(PMID, m_pmid);
                    }
                    m_pmidIsSet = true;
                }
            }
        } else if (name.equals(NAME_OF_SUBSTANCE)) {
            if (m_chemicalEntry.length() > 0 && m_chemicalList != null) {
                m_chemicalList.add(m_chemicalEntry);
            }
        } else if (name.equals(DESCRIPTOR_NAME)) {
            if (m_meshEntry.length() > 0 && m_meshHeadingList != null) {
                m_meshHeadingList.add(m_meshEntry);
            }
        } else if (name.equals(CHEMICAL_LIST)) {
            if (m_extractMetaData) {
                if (m_chemicalList != null && m_chemicalList.size() > 0) {
                    if (m_currentDoc != null) {
                        StringBuilder chemicalInfo = new StringBuilder();
                        for (String chem : m_chemicalList) {
                            chemicalInfo.append(chem + "; ");
                        }
                        m_currentDoc.addMetaInformation(CHEMICAL_LIST, chemicalInfo.toString());
                    }
                }
            }
        } else if (name.equals(MESH_HEADING_LIST)) {
            if (m_extractMetaData) {
                if (m_meshHeadingList != null && m_meshHeadingList.size() > 0) {
                    if (m_currentDoc != null) {
                        StringBuilder meshInfo = new StringBuilder();
                        for (String chem : m_meshHeadingList) {
                            meshInfo.append(chem + "; ");
                        }
                        m_currentDoc.addMetaInformation(MESH_HEADING_LIST, meshInfo.toString());
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (m_lastTag.equals(FIRSTNAME) || m_lastTag.equals(FORENAME)) {
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
        } else if (m_lastTag.equals(PMID)) {
            m_pmid += new String(ch, start, length);
        } else if (m_lastTag.equals(NAME_OF_SUBSTANCE)) {
            m_chemicalEntry += new String(ch, start, length);
        } else if (m_lastTag.equals(DESCRIPTOR_NAME)) {
            m_meshEntry += new String(ch, start, length);
        }
    }

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
            final SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            final XMLReader reader = parser.getXMLReader();
            // disable validation and schema or dtd loading
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
            reader.setFeature("http://xml.org/sax/features/validation", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            reader.setContentHandler(this);
            // parse input stream
            reader.parse(new InputSource(is));
        } catch (SAXException e) {
            LOGGER.warn("Could not parse PubMed documents, XML is not valid!");
            throw (e);
        } catch (IOException e) {
            LOGGER.warn("Could not read PubMed documents!");
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
}

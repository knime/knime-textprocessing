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
 *   19.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.sdml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
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
import org.xml.sax.helpers.DefaultHandler;

/**
 * Implements the {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)} is able to parse
 * the data of the given input stream representing <i>sdml</i> (<b>S</b>imple <b>D</b>ocument <b>M</b>arkup <b>L</b>
 * anguage) formatted text documents. See the <i>sdml.dtd</i> file for more details about the format. This format
 * enables a simple representation of textual documents and can be used as a transfer format to get text documents
 * formatted in various kinds of xml formats into knime without implementing an extra parser node for each format. The
 * only thing what have to be done is to transform documents in other xml formats via xslt transformation into sdml.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class SdmlDocumentParser extends DefaultHandler implements DocumentParser {

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

    /**
     * The path (postfix) of the sdml.dtd file relative to the plugin directory.
     */
    public static final String SDML_DTD_POSTFIX = "/resources/documentformat/sdml.dtd";

    /**
     * The public identifier for (sdml) xml files.
     */
    public static final String PUBLIC_IDENTIFIER = "-//UNIKN//DTD KNIME Sdml 2.0//EN";

    private static final NodeLogger LOGGER = NodeLogger.getLogger(SdmlDocumentParser.class);

    private List<Document> m_docs;

    private DocumentCategory m_category;

    private DocumentSource m_source;

    private DocumentType m_type;

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

    private boolean m_storeInList = false;

    // initialize the tokenizer with the old standard tokenizer for backwards compatibility
    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     * Creates a new instance of <code>SdmlDocumentParser</code>. The documents source, category and file path will be
     * set to <code>null</code> by default.
     *
     * @deprecated Use {@link #SdmlDocumentParser(String)} instead to define the tokenizer used for word tokenization.
     */
    @Deprecated
    public SdmlDocumentParser() {
        this(null, null, null);
    }

    /**
     * Creates a new instance of {@code SdmlDocumentParser}. The documents source, category and file path will be set to
     * <code>null</code> by default.
     *
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public SdmlDocumentParser(final String tokenizerName) {
        this(null, null, null, tokenizerName);
    }

    /**
     * Creates a new instance of <code>SdmlDocumentParser</code>. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @deprecated Use {@link #SdmlDocumentParser(String, DocumentCategory, DocumentSource, String)} instead to define
     *             the tokenizer used for word tokenization.
     */
    @Deprecated
    public SdmlDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_listener = new ArrayList<DocumentParsedEventListener>();
    }

    /**
     * Creates a new instance of {@code SdmlDocumentParser}. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public SdmlDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final String tokenizerName) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_listener = new ArrayList<DocumentParsedEventListener>();
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
            LOGGER.warn("Could not parse SDML documents, XML is not valid!");
            throw (e);
        } catch (IOException e) {
            LOGGER.warn("Could not read SDML documents!");
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
    public InputSource resolveEntity(final String pubId, final String sysId) throws IOException, SAXException {
        if (pubId != null) {
            TextprocessingCorePlugin plugin = TextprocessingCorePlugin.getDefault();
            String path = plugin.getPluginRootPath();
            if (pubId.equals(PUBLIC_IDENTIFIER)) {
                path += SDML_DTD_POSTFIX;
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
        } else if (m_lastTag.equals(TITLE)) {
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
    public void endElement(final String uri, final String localName, final String qName) {
        String endTag = qName.toLowerCase();
        if (endTag.equals(DOCUMENT) && m_currentDoc != null) {
            Document doc = m_currentDoc.createDocument();

            // due to memory issues documents are not all stored in list anymore
            // but handed out via listener mechanism
            //m_docs.add(doc);
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
                pd = new PublicationDate(year, month, day);
                m_currentDoc.setPublicationDate(pd);
            } catch (ParseException e) {
                LOGGER.warn("Publication date (" + year + "-" + month + "-" + day + ") could not be parsed !");
                LOGGER.info(e.getMessage());
            }
        } else if (endTag.equals(TITLE)) {
            m_currentDoc.addTitle(m_title.trim());
        } else if (endTag.equals(SECTION)) {
            m_currentDoc.addSection(m_section.trim(), SectionAnnotation.stringToAnnotation(m_annotation));
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

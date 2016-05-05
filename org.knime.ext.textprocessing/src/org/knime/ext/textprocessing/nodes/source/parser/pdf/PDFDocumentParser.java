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
package org.knime.ext.textprocessing.nodes.source.parser.pdf;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.text.PDFTextStripper;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.AbstractDocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;
import org.knime.ext.textprocessing.util.AuthorUtil;

/**
 * Implements the {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#parse(InputStream)} is able to read the data
 * of the given input stream and store it as a {@link org.knime.ext.textprocessing.data.Document}s full text.
 *
 * To parse PDF files the PDFBox library is used.
 *
 * @author Kilian Thiel, University of Konstanz
 * @since 2.7
 */
public class PDFDocumentParser extends AbstractDocumentParser {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(PDFDocumentParser.class);

    private List<Document> m_docs;

    private DocumentBuilder m_currentDoc;

    /**
     * Creates a new instance of <code>PDFDocumentParser</code>. The document source, category and file path will be set
     * to <code>null</code> by default.
     */
    public PDFDocumentParser() {
        super(null, null, null);
    }

    /**
     * Creates a new instance of <code>PDFDocumentParser</code>. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public PDFDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source) {
        super(docPath, category, source);
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
     *
     * @deprecated
     */
    @Deprecated
    @Override
    public List<Document> parse(final InputStream is) throws Exception {
        m_docs = new ArrayList<Document>();
        m_docs.add(parseInternal(is));
        return m_docs;
    }

    private boolean checkTitle(final String title) {
        if (title == null) {
            return false;
        }
        String t = title.trim();
        if (t.equals("")) {
            return false;
        }
        return true;
    }

    private Document parseInternal(final InputStream is) throws Exception {
        m_currentDoc = new DocumentBuilder();
        m_currentDoc.setDocumentFile(new File(m_docPath));
        m_currentDoc.setDocumentType(m_type);
        m_currentDoc.addDocumentCategory(m_category);
        m_currentDoc.addDocumentSource(m_source);

        if (m_charset == null) {
            m_charset = Charset.defaultCharset();
        }

        PDDocument document = null;
        try {
            document = PDDocument.load(is);

            // extract text from pdf
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            m_currentDoc.addSection(text, SectionAnnotation.UNKNOWN);

            // extract meta data from pdf
            String title = null;
            String authors = null;

            if (m_filenameAsTitle) {
                title = m_docPath.toString().trim();
            }

            PDDocumentInformation information = document.getDocumentInformation();
            if (information != null) {
                if (!checkTitle(title)) {
                    title = information.getTitle();
                }
                authors = information.getAuthor();
            }

            // if title meta data does not exist use first sentence
            if (!checkTitle(title)) {
                List<Section> sections = m_currentDoc.getSections();
                if (sections.size() > 0) {
                    try {
                        title = sections.get(0).getParagraphs().get(0).getSentences().get(0).getText().trim();
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.debug("Parsed PDF document " + m_docPath + " is empty.");
                        title = "";
                    }
                }
            }
            // if no useful first sentence exist use filename
            if (!checkTitle(title)) {
                title = m_docPath.toString().trim();
            }
            m_currentDoc.addTitle(title);

            // use author meta data
            if (authors != null) {
                Set<Author> authSet = AuthorUtil.parseAuthors(authors);
                for (Author a : authSet) {
                    m_currentDoc.addAuthor(a);
                }
            }

            // add document to list
            return m_currentDoc.createDocument();
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseDocument(final InputStream is) throws Exception {
        Document d = parseInternal(is);
        notifyAllListener(new DocumentParsedEvent(d, this));
    }
}

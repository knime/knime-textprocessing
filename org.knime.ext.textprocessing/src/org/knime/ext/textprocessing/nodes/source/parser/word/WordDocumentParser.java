/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
package org.knime.ext.textprocessing.nodes.source.parser.word;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.AbstractDocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;

/**
 * Implements the {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser} interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#parse(InputStream)} is able to read the data
 * of the given input stream and store it as a {@link org.knime.ext.textprocessing.data.Document}s full text.
 *
 * To parse PDF files the Apache POI library is used.
 *
 * @author Kilian Thiel, University of Konstanz
 * @since 2.7
 */
public class WordDocumentParser extends AbstractDocumentParser {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(WordDocumentParser.class);

    private List<Document> m_docs;

    private DocumentBuilder m_currentDoc;

    /**
     * Creates a new instance of {@code WordDocumentParser}. The document source, category and file path will be set to
     * {@code null} by default.
     *
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public WordDocumentParser(final String tokenizerName) {
        super(null, null, null, tokenizerName);
    }

    /**
     * Creates a new instance of {@code WordDocumentParser}. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public WordDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final String tokenizerName) {
        super(docPath, category, source, tokenizerName);
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

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s]+");

    private static boolean onlyWhitepscaes(final String str) {
        if (WHITESPACE_PATTERN.matcher(str).matches()) {
            return true;
        }
        return false;
    }

    private boolean checkTitle(final String title) {
        if (title == null) {
            return false;
        }
        if (title.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    private Document parseInternal(final InputStream is) throws Exception {
        m_currentDoc = new DocumentBuilder(m_tokenizerName);
        m_currentDoc.setDocumentFile(new File(m_docPath));
        m_currentDoc.setDocumentType(m_type);
        m_currentDoc.addDocumentCategory(m_category);
        m_currentDoc.addDocumentSource(m_source);

        try {
            // doc files
            if (m_docPath.endsWith(".doc")) {
                // copy content of input stream into byte array since content have to be red twice unfortunately.
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                final byte[] buf = new byte[1024];
                int i = 0;
                while ((i = is.read(buf)) >= 0) {
                    baos.write(buf, 0, i);
                }
                final byte[] content = baos.toByteArray();

                // open stream with copied content to read text
                InputStream copiedInput = new ByteArrayInputStream(content);
                final HWPFDocument hdoc = new HWPFDocument(copiedInput);
                final WordExtractor extractor = new WordExtractor(hdoc);
                for (String p : extractor.getParagraphText()) {
                    p = p.trim();
                    if (!onlyWhitepscaes(p)) {
                        m_currentDoc.addParagraph(p);
                    }
                }

                // open stream again with copied content to read meta info
                copiedInput = new ByteArrayInputStream(content);
                final POIFSFileSystem poifs = new POIFSFileSystem(copiedInput);
                final DirectoryEntry dir = poifs.getRoot();
                final DocumentEntry siEntry = (DocumentEntry)dir.getEntry(SummaryInformation.DEFAULT_STREAM_NAME);
                final PropertySet ps = new PropertySet(new DocumentInputStream(siEntry));

                final SummaryInformation si = new SummaryInformation(ps);

                setAuthor(si.getAuthor());
                setPublicationDate(si.getCreateDateTime());

                // docx files
            } else if (m_docPath.endsWith(".docx") || m_docPath.endsWith(".docm")) {
                final XWPFDocument hdoc = new XWPFDocument(is);
                final List<XWPFParagraph> paragraphs = hdoc.getParagraphs();
                for (final XWPFParagraph paragraph : paragraphs) {
                    final String text = paragraph.getText();
                    if (!onlyWhitepscaes(text)) {
                        m_currentDoc.addParagraph(text);
                    }
                }

                setAuthor(hdoc.getProperties().getCoreProperties().getCreator());
                setPublicationDate(hdoc.getProperties().getCoreProperties().getCreated());
            }

            m_currentDoc.createNewSection(SectionAnnotation.CHAPTER);

            // find title
            String title = null;

            if (m_filenameAsTitle) {
                title = m_docPath.trim();
            } else {
                final List<Section> sections = m_currentDoc.getSections();
                if (sections.size() > 0) {
                    try {
                        title = sections.get(0).getParagraphs().get(0).getSentences().get(0).getText().trim();
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.debug("Parsed word document " + m_docPath + " is empty.");
                        title = "";
                    }
                }
            }
            if (!checkTitle(title)) {
                title = m_docPath.toString();
            }
            m_currentDoc.addTitle(title);

            return m_currentDoc.createDocument();
        } finally {
            is.close();
        }
    }

    private void setPublicationDate(final Date creationDate) throws ParseException {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(creationDate);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        m_currentDoc.setPublicationDate(new PublicationDate(year, month, day));
    }

    private void setAuthor(final String authorName) {
        final String trimmedName = authorName.trim();
        if (!trimmedName.isEmpty()) {
            m_currentDoc.addAuthor(new Author("", trimmedName));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseDocument(final InputStream is) throws Exception {
        notifyAllListener(new DocumentParsedEvent(parseInternal(is), this));
    }
}

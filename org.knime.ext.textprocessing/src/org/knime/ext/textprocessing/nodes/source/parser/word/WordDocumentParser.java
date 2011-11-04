/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
package org.knime.ext.textprocessing.nodes.source.parser.word;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.poi.hwpf.extractor.WordExtractor;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.AbstractDocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEvent;

/**
 * Implements the
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#parse(InputStream)}
 * is able to read the data of the given input stream and store it as a
 * {@link org.knime.ext.textprocessing.data.Document}s full text. 
 * 
 * To parse PDF files the Apache POI library is used.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class WordDocumentParser extends AbstractDocumentParser {

    private List<Document> m_docs;

    private DocumentBuilder m_currentDoc;


    /**
     * Creates a new instance of <code>PDFDocumentParser</code>.
     * The document source, category and file path will be set to
     * <code>null</code> by default.
     */
    public WordDocumentParser() {
        super(null, null, null);
    }

    /**
     * Creates a new instance of <code>PDFDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public WordDocumentParser(final String docPath,
            final DocumentCategory category, final DocumentSource source) {
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
     */
    @Override
    public List<Document> parse(final InputStream is) throws Exception {
        m_docs = new ArrayList<Document>();
        m_docs.add(parseInternal(is));
        return m_docs;
    }
    
    private static Pattern symbolPattern = Pattern.compile("[\\s]+");

    private static boolean onlyWhitepscaes(final String str) {
        if (symbolPattern.matcher(str).matches()) {
            return true;
        }
        return false;
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
        
        try {
            WordExtractor extractor = new WordExtractor(is);
            for (String p : extractor.getParagraphText()) {
                p = p.trim();
                if (!onlyWhitepscaes(p)) {
                    m_currentDoc.addParagraph(p);
                }
            }
            m_currentDoc.createNewSection(SectionAnnotation.UNKNOWN);
            
            String title = null;
            // if title meta data does not exist use first sentence
            if (!checkTitle(title)) {
                List<Section> sections = m_currentDoc.getSections();
                if (sections.size() > 0) {
                    title = sections.get(0).getParagraphs().get(0)
                            .getSentences().get(0).getText().trim();
                }
            }
            if (!checkTitle(title)) {
                title = m_docPath.toString();
            }
            m_currentDoc.addTitle(title);

            return m_currentDoc.createDocument();
        } finally {
            /* empty */
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void parseDocument(final InputStream is) throws Exception {
        Document d = parseInternal(is);
        notifyAllListener(new DocumentParsedEvent(d, this));
    }    
}

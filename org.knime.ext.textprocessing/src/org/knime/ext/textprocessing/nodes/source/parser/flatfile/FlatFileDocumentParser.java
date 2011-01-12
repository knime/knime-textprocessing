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
package org.knime.ext.textprocessing.nodes.source.parser.flatfile;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.source.parser.AbstractDocumentParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements the
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}
 * interface. The provided method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser#parse(InputStream)}
 * is able to read the data of the given input stream and store it as a
 * {@link org.knime.ext.textprocessing.data.Document}s full text. The title
 * of the document is the absolut name of the file containing the text data.
 * The file name has to be set before calling the parser method, otherwise no
 * title will be set. The complete data of the input stream is set as full text.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class FlatFileDocumentParser extends AbstractDocumentParser {

    private List<Document> m_docs;

    private DocumentBuilder m_currentDoc;


    /**
     * Creates a new instance of <code>FlatFileDocumentParser</code>.
     * The document source, category and file path will be set to
     * <code>null</code> by default.
     */
    public FlatFileDocumentParser() {
        super(null, null, null);
    }

    /**
     * Creates a new instance of <code>FlatFileDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public FlatFileDocumentParser(final String docPath,
            final DocumentCategory category, final DocumentSource source) {
        super(docPath, category, source);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Document> parse(final InputStream is) throws Exception {
        m_docs = new ArrayList<Document>();

        m_currentDoc = new DocumentBuilder();
        m_currentDoc.setDocumentFile(new File(m_docPath));
        m_currentDoc.setDocumentType(m_type);
        m_currentDoc.addDocumentCategory(m_category);
        m_currentDoc.addDocumentSource(m_source);

        if (m_charset == null) {
            m_charset = Charset.defaultCharset();
        }
        BufferedReader br = new BufferedReader(
                new InputStreamReader(is, m_charset));
        String line = null;
        StringBuilder text = new StringBuilder();
        while ((line = br.readLine()) != null) {
            text.append(line);
            text.append(Term.WORD_SEPARATOR);
        }

        int len = text.length();
        if (len > 0) {
            text.delete(len - (Term.WORD_SEPARATOR.length() + 1), len);
        }

        br.close();
        m_currentDoc.addSection(text.toString(), SectionAnnotation.UNKNOWN);

        String firstSentence = null;
        List<Section> sections = m_currentDoc.getSections();
        if (sections != null && sections.size() > 0) {
            List<Paragraph> paragraphs = sections.get(0).getParagraphs();
            if (paragraphs != null && paragraphs.size() > 0) {
                List<Sentence> sentences = paragraphs.get(0).getSentences();
                if (sentences != null && sentences.size() > 0) {
                    firstSentence = sentences.get(0).getText();
                }
            }
        }
        if (firstSentence != null) {
            m_currentDoc.addTitle(firstSentence);
        } else {
            m_currentDoc.addTitle(m_docPath);
        }

        m_docs.add(m_currentDoc.createDocument());
        return m_docs;
    }
}

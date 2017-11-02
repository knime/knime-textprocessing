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
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * This abstract class implements conveniently all setter methods of the interface
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}, such as
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentCategory(DocumentCategory)},
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentSource(DocumentSource)},
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentFilepath(String)} and
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentType(DocumentType)}. The parse
 * method is still not implemented since the parsing technique is still up to the concrete underlying parser.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class AbstractDocumentParser implements DocumentParser {

    /**
     * The category of the document.
     */
    protected DocumentCategory m_category;

    /**
     * The source of the document.
     */
    protected DocumentSource m_source;

    /**
     * The type of the document.
     */
    protected DocumentType m_type;

    /**
     * The path of the file containing the document.
     */
    protected String m_docPath;

    /**
     * The charset to use by the parser.
     */
    protected Charset m_charset;

    /**
     * If filename should be used as title or not.
     * @since 3.1
     */
    protected boolean m_filenameAsTitle;

    /**
     * List of listeners.
     */
    protected List<DocumentParsedEventListener> m_listener;

    /**
     * The name of the word tokenizer.
     * @since 3.3
     */
    protected String m_tokenizerName;


    /**
     * Constructor of <code>AbstractDocumentParser</code>. The document source, category and file path will be set to
     * <code>null</code> by default.
     */
    public AbstractDocumentParser() {
        this(null, null, null, TextprocessingPreferenceInitializer.tokenizerName());
    }


    /**
     * Constructor of <code>AbstractDocumentParser</code>. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @param tokenizerName The name of the word tokenizer.
     *
     * @since 3.3
     */
    public AbstractDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source,
        final String tokenizerName) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
        m_listener = new ArrayList<DocumentParsedEventListener>();
        m_tokenizerName = tokenizerName;
    }


    /**
     * Constructor of <code>AbstractDocumentParser</code>. The given source, category and file path is set to the
     * created documents.
     *
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     * @deprecated Use {@link #AbstractDocumentParser(String, DocumentCategory, DocumentSource, String)} to set word
     *             tokenizer.
     *
     */
    @Deprecated
    public AbstractDocumentParser(final String docPath, final DocumentCategory category, final DocumentSource source) {
        this(docPath, category, source, TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * {@inheritDoc}
     *
     * @deprecated use {@link AbstractDocumentParser#parseDocument(InputStream)} instead.
     */
    @Deprecated
    @Override
    public abstract List<Document> parse(final InputStream is) throws Exception;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void parseDocument(final InputStream is) throws Exception;

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
     */
    @Override
    public void setCharset(final Charset charset) {
        m_charset = charset;
    }

    /**
     * {@inheritDoc}
     * @since 3.1
     */
    @Override
    public void setFilenameAsTitle(final boolean filenameAsTitle) {
        m_filenameAsTitle = filenameAsTitle;
    }

    /**
     * {@inheritDoc}
     * @since 3.3
     */
    @Override
    public void setTokenizerName(final String tokenizerName) {
        m_tokenizerName = tokenizerName;
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
     * Notifies all registered listeners with given event.
     *
     * @param event Event to notify listener with
     */
    public void notifyAllListener(final DocumentParsedEvent event) {
        for (final DocumentParsedEventListener l : m_listener) {
            l.documentParsed(event);
        }
    }
}

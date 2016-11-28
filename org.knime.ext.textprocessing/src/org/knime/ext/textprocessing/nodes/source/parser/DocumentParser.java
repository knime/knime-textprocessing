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
 *   15.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;

/**
 * All parser, parsing various document formats, like Reuters, PubMed, etc. and create
 * {@link org.knime.ext.textprocessing.data.Document} instances, have to implement this interface, to enable
 * compatibility with classes using different parsers. The interface provides the method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#parse(InputStream)}. Underlying
 * implementations have to use the given <code>InputStream</code> and, parse its data and create a list of
 * {@link org.knime.ext.textprocessing.data.Document}s which is then returned.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentParser {

    /**
     * Parses the data of the given <code>InputStream</code> and creates instances of
     * {@link org.knime.ext.textprocessing.data.Document}s, which are finally returned as a list.
     *
     * @param is the <code>InputStream</code> providing the data to parse.
     * @return A list of documents parsed out of the input stream's data.
     * @throws Exception If something is not working properly.
     */
    @Deprecated
    public List<Document> parse(final InputStream is) throws Exception;

    /**
     * Parses the data of the given <code>InputStream</code> and creates instances of
     * {@link org.knime.ext.textprocessing.data.Document}s after each parsed document all
     * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener} are notified.
     *
     * @param is the <code>InputStream</code> providing the data to parse.
     * @throws Exception If something is not working properly.
     */
    public void parseDocument(final InputStream is) throws Exception;

    /**
     * Adds the given {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener} from the list
     * of listeners.
     *
     * @param listener Listener to add.
     */
    public void addDocumentParsedListener(final DocumentParsedEventListener listener);

    /**
     * Removes the given {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener} from the
     * list of listeners.
     *
     * @param listener Listener to remove.
     */
    public void removeDocumentParsedListener(final DocumentParsedEventListener listener);

    /**
     * Removes all {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParsedEventListener} from the list of
     * listeners.
     */
    public void removeAllDocumentParsedListener();

    /**
     * Cleans list of parsed documents.
     */
    public void clean();

    /**
     * @param category The category of the documents to set.
     */
    public void setDocumentCategory(final DocumentCategory category);

    /**
     * @param source The source of the documents to set.
     */
    public void setDocumentSource(final DocumentSource source);

    /**
     * @param type The type of the documents to set.
     */
    public void setDocumentType(final DocumentType type);

    /**
     * @param filePath The path to the file containing the document.
     */
    public void setDocumentFilepath(final String filePath);

    /**
     * @param charset The charset to use by the parser.
     */
    public void setCharset(final Charset charset);

    /**
     * @param tokenizerName The name of the word tokenizer.
     * @since 3.3
     */
    public void setTokenizerName(final String tokenizerName);

    /**
     * @param filenameAsTitle If {@code true} filename will be set as title, otherwise the concrete parser
     * implementation will define the title.
     * @since 3.1
     */
    public void setFilenameAsTitle(final boolean filenameAsTitle);
}

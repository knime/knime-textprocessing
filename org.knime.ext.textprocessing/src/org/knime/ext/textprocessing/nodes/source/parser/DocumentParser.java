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
 * All parser, parsing various document formats, like Reuters, PubMed, etc.
 * and create {@link org.knime.ext.textprocessing.data.Document} instances,
 * have to implement this interface, to enable compatibility with
 * classes using different parsers. The interface provides the method
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#parse(InputStream)}.
 * Underlying implementations have to use the given <code>InputStream</code>
 * and, parse its data and create a list of
 * {@link org.knime.ext.textprocessing.data.Document}s which is then returned.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentParser {

    /**
     * Parses the data of the given <code>InputStream</code> and creates
     * instances of {@link org.knime.ext.textprocessing.data.Document}s, which
     * are finally returned as a list.
     *
     * @param is the <code>InputStream</code> providing the data to parse.
     * @return A list of documents parsed out of the input stream's data.
     * @throws Exception If something is not working properly.
     */
    public List<Document> parse(final InputStream is) throws Exception;
    
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
}

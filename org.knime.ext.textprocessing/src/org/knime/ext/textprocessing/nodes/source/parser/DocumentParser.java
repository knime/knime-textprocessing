/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   15.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.InputStream;
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
}

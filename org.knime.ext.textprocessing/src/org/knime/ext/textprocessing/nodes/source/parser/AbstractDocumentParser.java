/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   20.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.InputStream;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;

/**
 * This abstract class implements conveniently all setter methods of the 
 * interface
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}, 
 * such as
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentCategory(DocumentCategory)},
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentSource(DocumentSource)},
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentFilepath(String)}
 * and
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser#setDocumentType(DocumentType)}.
 * The parse method is still not implemented since the parsing technique
 * is still up to the concrete underlying parser. 
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
     * Constructor of <code>AbstractDocumentParser</code>. 
     * The document source, category and file path will be set to 
     * <code>null</code> by default.
     */
    public AbstractDocumentParser() {
        this(null, null, null);
    }
    
    /**
     * Constructor of <code>AbstractDocumentParser</code>. The given
     * source, category and file path is set to the created documents.
     * 
     * @param docPath The path to the file containing the document.
     * @param category The category of the document to set.
     * @param source The source of the document to set.
     */
    public AbstractDocumentParser(final String docPath,
            final DocumentCategory category, final DocumentSource source) {
        m_category = category;
        m_source = source;
        m_docPath = docPath;
    }
    
    /**
     * {@inheritDoc}
     */
    public abstract List<Document> parse(final InputStream is) throws Exception;

    /**
     * {@inheritDoc}
     */
    public void setDocumentCategory(final DocumentCategory category) {
        m_category = category;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentSource(final DocumentSource source) {
        m_source = source;
    }

    /**
     * {@inheritDoc}
     */
    public void setDocumentType(final DocumentType type) {
        m_type = type;
    } 
    
    /**
     * {@inheritDoc}
     */
    public void setDocumentFilepath(final String filePath) {
        m_docPath = filePath;
    } 
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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

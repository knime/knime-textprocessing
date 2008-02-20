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
 *   20.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.flatfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.nodes.source.parser.AbstractDocumentParser;

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

    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(FlatFileDocumentParser.class);
    
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
    public List<Document> parse(final InputStream is) {
        m_docs = new ArrayList<Document>();
        
        m_currentDoc = new DocumentBuilder();
        m_currentDoc.setDocumentFile(new File(m_docPath));
        m_currentDoc.setDocumentType(m_type);
        m_currentDoc.addDocumentCategory(m_category);
        m_currentDoc.addDocumentSource(m_source);
        
        m_currentDoc.addTitle(m_docPath);
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            StringBuilder text = new StringBuilder();
            while((line = br.readLine()) != null) {
                text.append(line);
            }
            br.close();
            m_currentDoc.addSection(text.toString(), SectionAnnotation.UNKNOWN);
        } catch (IOException e) {
            LOGGER.warn("Input stream could not be red!");
            LOGGER.warn(e.getMessage());
        }
        
        m_docs.add(m_currentDoc.createDocument());
        return m_docs;
    }     
}

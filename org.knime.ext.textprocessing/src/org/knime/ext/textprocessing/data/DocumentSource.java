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
 *   22.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

/**
 * Contains the source of a {@link org.knime.ext.textprocessing.data.Document}, 
 * which can for instance be PubMed, Reuters, etc. The source specifies where a 
 * {@link org.knime.ext.textprocessing.data.Document} stems from.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentSource {

    /**
     * The default source name.
     */
    private static final String DEFAULT_SOURCE_NAME = "DefaultSource";
    
    private String m_sourceName;
    
    /**
     * Creates new instance of <code>DocumentSource</code> with given 
     * source name.
     * @param sourceName The name of the source to set.
     */
    public DocumentSource(final String sourceName) {
        if (sourceName == null) {
            m_sourceName = DocumentSource.DEFAULT_SOURCE_NAME;
        } else {
            m_sourceName = sourceName;
        }
    }
    
    /**
     * Creates empty <code>DocumentSource</code> instance with the default name.
     */
    public DocumentSource() {
        this(null);
    }
    
    /**
     * @return The name of the source.
     */
    public String getSourceName() {
        return m_sourceName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof DocumentSource)) {
            return false;
        }
        DocumentSource ds = (DocumentSource)o;
        if (!ds.getSourceName().equals(m_sourceName)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_sourceName.hashCode();
    }    
}

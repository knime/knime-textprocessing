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
 *   11.01.2007 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

/**
 * Contains the category of a 
 * {@link org.knime.ext.textprocessing.data.Document}, which can for instance be
 * artificial intelligence, presidential elections, breast cancer, etc. The 
 * category of a {@link org.knime.ext.textprocessing.data.Document} specifies 
 * its superior topic. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentCategory {

    /**
     * The default source name.
     */
    private static final String DEFAULT_CATEGORY = "";
    
    private String m_categoryName;
    
    /**
     * Creates new instance of <code>DocumentCategory</code> with the given 
     * name of the category.
     * @param categoryName The name of the source to set.
     */
    public DocumentCategory(final String categoryName) {
        if (categoryName == null) {
            m_categoryName = DocumentCategory.DEFAULT_CATEGORY;
        } else {
            m_categoryName = categoryName;
        }
    }
    
    /**
     * Creates empty <code>DocumentCategory</code> instance with the default 
     * name.
     */
    public DocumentCategory() {
        this(null);
    }
    
    /**
     * @return The name of the category.
     */
    public String getCategoryName() {
        return m_categoryName;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof DocumentCategory)) {
            return false;
        }
        DocumentCategory dc = (DocumentCategory)o;
        if (!dc.getCategoryName().equals(m_categoryName)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_categoryName.hashCode();
    }     
}

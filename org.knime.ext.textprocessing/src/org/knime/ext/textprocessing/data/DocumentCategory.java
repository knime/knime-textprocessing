/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

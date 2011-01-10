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

/*
========================================================================
 *
 *  Copyright (C) 2003 - 2013
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
 *   18.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Query {

    private String m_query;
    
    private int m_maxResults;
    
    /**
     * Creates a new <code>Query</code> instance with given query string and
     * number of maximal results.
     * 
     * @param query The query string.
     * @param maxResults The maximal query results.
     */
    public Query(final String query, final int maxResults) {
        m_query = query;
        m_maxResults = maxResults;
    }
    
    /**
     * @return The query string.
     */
    public String getQuery() {
        return m_query;
    }
    
    /**
     * @return The maximal number of query results.
     */
    public int getMaxResults() {
        return m_maxResults;
    }
}

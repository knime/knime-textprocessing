/*
 * ------------------------------------------------------------------ *
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

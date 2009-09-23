/*
 * ------------------------------------------------------------------ *
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
 *   05.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentGrabberConfigKeys {

    private DocumentGrabberConfigKeys() { }
    
    /**
     * Config Key for the query.
     */
    public static final String CFGKEY_QUERY = "Query";
    
    /**
     * Config Key for the database.
     */
    public static final String CFGKEY_DATATBASE = "Database";
    
    /**
     * Config Key for deleting file after parsing.
     */
    public static final String CFGKEY_DELETE = "DeleteAfterParse";
    
    /**
     * Config Key for number of maximal results.
     */
    public static final String CFGKEY_MAX_RESULTS = "MaximalResults";
    
    /**
     * Config Key for the directory to save the documents to.
     */
    public static final String CFGKEY_DIR = "Directory";
    
    /**
     * Config Key for the documents category.
     */
    public static final String CFGKEY_DOC_CAT = "DocumentCategrory";
    
    /**
     * Config Key for the document type.
     */
    public static final String CFGKEY_DOC_TYPE = "DocumentType";     
}

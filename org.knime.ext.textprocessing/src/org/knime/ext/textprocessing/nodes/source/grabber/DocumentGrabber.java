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
 *   18.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.io.File;
import java.util.List;

import org.knime.ext.textprocessing.data.Document;



/**
 * An interface which provides method declarations to get the number of results
 * of a specified query or download the result documents and parse them.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface DocumentGrabber {

    /**
     * Sends the given query to the corresponding bibliographic database and
     * stores the resulting documents into the specified directory.
     * To which database the query is send relates on the underlying
     * implementation of this interface. After downloading the related parser
     * id used to parse the documents and return them in a list.
     * 
     * @param directory The directory to save the documents to.
     * @param query The query to send to the bibliographic database.
     * @throws Exception If grabber can not connect to the server or something 
     * else goes wrong. 
     * @return The list of parsed documents.
     */
    public List < Document > grabDocuments(final File directory, 
            final Query query) throws Exception;
    
    /**
     * Sends the given query to the corresponding bibliographic database
     * and returns the number of resulting documents.  
     * To which database the query is send relates on the underlying
     * implementation of this interface.
     *  
     * @param query The query to send to the bibliographic database.
     * @return The number of resulting documents related to the given query.
     * @throws Exception If grabber can not connect to the server of something
     * else goes wrong.
     */
    public int numberOfResults(final Query query) throws Exception;
}

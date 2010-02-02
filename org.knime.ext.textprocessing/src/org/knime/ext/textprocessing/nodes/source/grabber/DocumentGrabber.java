/*
========================================================================
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

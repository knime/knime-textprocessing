/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   13.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.util.Hashtable;
import java.util.Set;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentGrabberFactory {

    private static DocumentGrabberFactory INSTANCE = null;
    
    private Hashtable<String, DocumentGrabber> m_grabber;
    
    private DocumentGrabberFactory() {
        m_grabber = new Hashtable<String, DocumentGrabber>();
        m_grabber.put("PUBMED", new PubMedDocumentGrabber());
    }
    
    /**
     * @return The singelton instance of <code>DocumentGrabberFactory</code>.
     */
    public static DocumentGrabberFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DocumentGrabberFactory();
        }
        return INSTANCE;
    }
    
    /**
     * @return The names of the registered grabber.
     */
    public Set<String> getGrabberNames() {
        return m_grabber.keySet();
    }
    
    /**
     * Returns the grabber related to the given name. If no grabber is 
     * available, <code>null</code> is returned.
     * 
     * @param name The name of the grabber.
     * @return The grabber related to the given name.
     */
    public DocumentGrabber getGrabber(final String name) {
        return m_grabber.get(name);
    }
}

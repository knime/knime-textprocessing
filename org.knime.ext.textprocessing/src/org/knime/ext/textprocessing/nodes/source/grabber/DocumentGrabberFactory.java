/*
 * ------------------------------------------------------------------------
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
 *   13.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

import java.util.Hashtable;
import java.util.Set;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentGrabberFactory {

    private static DocumentGrabberFactory instance = null;
    
    private Hashtable<String, DocumentGrabber> m_grabber;
    
    private DocumentGrabberFactory() {
        m_grabber = new Hashtable<String, DocumentGrabber>();
        m_grabber.put("PUBMED", new PubMedDocumentGrabber());
    }
    
    /**
     * @return The singelton instance of <code>DocumentGrabberFactory</code>.
     */
    public static DocumentGrabberFactory getInstance() {
        if (instance == null) {
            instance = new DocumentGrabberFactory();
        }
        return instance;
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

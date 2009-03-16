/* 
 * -------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 * 
 * History
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Serializable;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentAuthor implements Serializable {

    private String m_lastName = "-";

    private String m_firstName = "-";

    /**
     * Creates new instance of DocumentAuthor with given last name and first 
     * name.
     * 
     * @param lastName Last name to set.
     * @param firstName First name to set.
     */
    public DocumentAuthor(final String lastName, final String firstName) {
        super();
        
        if (lastName != null) {
            if (lastName.length() > 0) {
                setLastName(lastName);
            }
        }
        if (firstName != null) {
            if (firstName.length() > 0) {
                setFirstName(firstName);
            }
        }
    }

    /**
     * Empty default constructor. This constructor is used by hibernate.
     */
    DocumentAuthor() { 
        super();
    }
    
    /**
     * @return Returns the firstName.
     */
    public String getFirstName() {
        return m_firstName;
    }

    /**
     * @return Returns the lastName.
     */
    public String getLastName() {
        return m_lastName;
    } 
    

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Author)) {
            return false;
        }

        Author da = (Author)o;
        if (!m_firstName.equals(da.getFirstName())) {
            return false;
        } else if (!m_lastName.equals(da.getLastName())) {
            return false;
        }
        return true;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hashCode = 1;
        hashCode *= 37 + m_firstName.hashCode();
        hashCode *= 37 + m_lastName.hashCode();
        return hashCode;
    }
     
    
    //
    /// private setter for hibernate
    //

    /**
     * @param name the first name to set
     */
    private void setFirstName(final String name) {
        m_firstName = name;
    }

    /**
     * @param name the last name to set
     */
    private void setLastName(final String name) {
        m_lastName = name;
    }    
}

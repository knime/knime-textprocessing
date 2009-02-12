/* 
 * -------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 * 
 * History
 *   Apr 18, 2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.Serializable;

/**
 * Contains the first and last name of an author. Authors can be assigned to
 * {@link org.knime.ext.textprocessing.data.Document}
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Author implements Serializable {

    private String m_lastName = "-";

    private String m_firstName = "-";

    /**
     * Creates new instance of Author with given first and last name.
     * 
     * @param firstName First name to set.
     * @param lastName Last name to set.
     */
    public Author(final String firstName, final String lastName) {
        super();
        
        if (lastName != null && lastName.length() > 0) {
            m_lastName = lastName;
        }
        if (firstName != null && firstName.length() > 0) {
            m_firstName = firstName;
        }
    }
    
    /**
     * @return Returns the first name of the author.
     */
    public String getFirstName() {
        return m_firstName;
    }

    /**
     * @return Returns the last name of the author.
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
        hashCode *= 119 + m_firstName.hashCode();
        hashCode *= 119 + m_lastName.hashCode();
        return hashCode;
    }    
}

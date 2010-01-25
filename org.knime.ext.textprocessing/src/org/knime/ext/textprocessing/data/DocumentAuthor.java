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

/* 
========================================================================
 *
 *  Copyright (C) 2003 - 2011
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

/*
 * ------------------------------------------------------------------------
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

/**
 * Classes implementing this interface have to contain textual data in any
 * kind of representations. Therefore the method
 * {@link org.knime.ext.textprocessing.data.TextContainer#getText()} enables the
 * access of this textual data in an unified way.   
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TextContainer {

    /**
     * @return The textual data of the <code>TextContainer</code> as a 
     * single String. The difference to {@link java.lang.Object#toString()} is
     * that not a string representation of the instance is returned but only
     * the useful textual data as string.
     */
    public String getText();
}

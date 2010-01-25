/*
 * ------------------------------------------------------------------------
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
 *   21.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * Caches extending <code>DataCellCache</code> are caching various 
 * {@link org.knime.core.data.DataCell}s containing
 * text content as {@link org.knime.ext.textprocessing.data.TextContainer}. 
 * In order to enable to proper usability of this type of caches this abstract
 * class has to be extended. All caches need a
 * {@link org.knime.ext.textprocessing.util.TextContainerDataCellFactory} which
 * creates the proper {@link org.knime.core.data.DataCell}, i.e. for Term
 * a {@link org.knime.ext.textprocessing.data.TermCell} for documents a
 * {@link org.knime.ext.textprocessing.data.DocumentCell}. This factory
 * specifies which type of <code>DataCell</code>s the cache is caching. 
 * The method 
 * {@link org.knime.ext.textprocessing.util.DataCellCache#getInstance(TextContainer)}
 * returns a newly created <code>DataCell</code> if there exists no cell yet 
 * containing the given text container, otherwise the already existing cell 
 * is returned.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class DataCellCache {
    
    /**
     * The factory creating the proper <code>DataCell</code>.
     */
    protected TextContainerDataCellFactory m_dcFac;
    
    /**
     * Constructor of <code>DataCellCache</code> with given factory which 
     * creates the proper type of <code>DataCell</code>s.
     * @param fac The factory which creates the proper type of 
     * <code>DataCell</code>s.
     */
    public DataCellCache(final TextContainerDataCellFactory fac) {
        m_dcFac = fac;
    }
    
    /**
     * Returns a newly created <code>DataCell</code> if there exists no cell 
     * yet, containing the given <code>TextContainer</code>, otherwise the 
     * already existing cell is returned. The specific type of the
     * <code>DataCell</code>, i.e.
     * {@link org.knime.ext.textprocessing.data.TermCell} or 
     * {@link org.knime.ext.textprocessing.data.DocumentCell} depends on the
     * specified <code>TextContainerDataCellFactory</code>.
     * 
     * @param tc The <code>TextContainer</code> the data cell have to contain.
     * @return A <code>DataCell</code> containing the 
     * <code>TextContainer</code>.
     */
    public abstract DataCell getInstance(final TextContainer tc);
    
    /**
     * Resets the cache and deletes all references to the cached instances.   
     */
    public abstract void reset();
}

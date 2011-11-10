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
 *   22.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Hashtable;

import org.knime.core.data.DataCell;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * Caches all <code>TextContainer</code> and related <code>DataCell</code>s
 * containing the <code>TextContainer</code> in a <code>Hashtable</code>.
 * See {@link org.knime.ext.textprocessing.util.DataCellCache} for more details
 * about the functionality of a cache. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
@Deprecated
public class FullDataCellCache extends DataCellCache {

    private Hashtable<TextContainer, DataCell> m_cache;

    private static final int INIT_CACHE_SIZE = 1000;
    
    /**
     * Creates new instance of <code>FullDataCellCache</code> with a 
     * initial cache size of 1000 as default and the given 
     * <code>TextContainerDataCellFactory</code> to create the proper
     * type of <code>DataCell</code>s.
     * 
     * @param fac The <code>TextContainerDataCellFactory</code> to create the 
     * proper type of <code>DataCell</code>s. 
     */
    public FullDataCellCache(final TextContainerDataCellFactory fac) {
        this(INIT_CACHE_SIZE, fac);
    }
    
    /**
     * Creates new instance of <code>FullDataCellCache</code> with the 
     * given initial cache size and the given 
     * <code>TextContainerDataCellFactory</code> to create the proper
     * type of <code>DataCell</code>s.
     * 
     * @param initialCacheSize The initial size of the internal cache.
     * @param fac The <code>TextContainerDataCellFactory</code> to create the 
     * proper type of <code>DataCell</code>s.
     */
    public FullDataCellCache(final int initialCacheSize, 
            final TextContainerDataCellFactory fac) {
        super(fac);
        m_cache = new Hashtable<TextContainer, DataCell>(initialCacheSize);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getInstance(final TextContainer tc) {
        DataCell dc;
        dc = m_cache.get(tc);
        if (dc == null) {
            dc = m_dcFac.createDataCell(tc);
            m_cache.put(tc, dc);
        }
        return dc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_cache.clear();
    }
}

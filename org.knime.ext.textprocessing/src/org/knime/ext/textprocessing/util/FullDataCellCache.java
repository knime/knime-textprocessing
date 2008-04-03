/* ------------------------------------------------------------------
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
        if (m_cache.contains(tc)) {
            dc = m_cache.get(tc);
        } else {
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

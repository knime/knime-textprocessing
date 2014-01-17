/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   02.09.2013 (Kilian Thiel): created
 */

package org.knime.ext.textprocessing.util;

import java.lang.ref.SoftReference;

import org.knime.core.data.DataCell;
import org.knime.core.data.util.memory.MemoryWarningSystem;
import org.knime.core.data.util.memory.MemoryWarningSystem.MemoryWarningListener;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.LRUCache;
import org.knime.ext.textprocessing.data.TextContainer;

/**
 * A lru data cell cache for text containers with fixed size.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public final class LRUDataCellCache extends DataCellCache {

    /** Logger. */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(LRUDataCellCache.class);

    private static final int DEF_CACHE_SIZE = 1000;

    private LRUCache<TextContainer, SoftReference<DataCell>> m_cache;

    private int m_maxHistory;

    /** To check memory usage and react on low memory. */
    private final MemoryWarningListener m_memoryWarningListener = new MemoryWarningSystem.MemoryWarningListener() {
        @Override
        public void memoryUsageLow(final long usedMemory, final long maxMemory) {
            LOGGER.debug("Low memory encountered in Textprocessing, clearing "
                    + LRUDataCellCache.class.getSimpleName() + "( " + m_cache.size() + "element(s))");
            m_cache.clear();
        }
    };

    /**
     * Constructor for class {@link LRUDataCellCache} with given factory to create data cells with. The maximum
     * history of the cache is set to 1000.
     *
     * @param fac The factory to set and create data cells.
     */
    public LRUDataCellCache(final TextContainerDataCellFactory fac) {
        this(DEF_CACHE_SIZE, fac);
    }

    /**
     * Constructor for class {@link LRUDataCellCache} with given factory to create data cells and maximal number of
     * cells in cache history to set.
     * @param fac The factory to set and create data cells.
     * @param maxHistory the maximal number of data cells in cache history.
     */
    public LRUDataCellCache(final int maxHistory, final TextContainerDataCellFactory fac) {
        super(fac);
        m_maxHistory = maxHistory;
        m_cache = new LRUCache<TextContainer, SoftReference<DataCell>>(m_maxHistory);
        MemoryWarningSystem.getInstance().registerListener(m_memoryWarningListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getInstance(final TextContainer tc) {
        SoftReference<DataCell> srCell = m_cache.get(tc);
        DataCell cell;
        if (srCell == null || srCell.get() == null) {
            cell = m_dcFac.createDataCell(tc);
            m_cache.put(tc, new SoftReference<DataCell>(cell));
        } else {
            cell = srCell.get();
        }
        return cell;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_cache.clear();
    }

    /**
     * Unregisters memory warning listener.
     */
    @Override
    public void close() {
        LOGGER.debug("Closing lru data cell cache.");
        reset();
        MemoryWarningSystem.getInstance().removeListener(m_memoryWarningListener);
    }
}

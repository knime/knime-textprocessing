/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        reset();
    }
}

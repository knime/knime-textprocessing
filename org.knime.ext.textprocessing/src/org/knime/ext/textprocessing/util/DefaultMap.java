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
 *   Jun 12, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Wrapper around a map that returns a default value when a key is not present.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public abstract class DefaultMap<K, V> implements Map<K, V> {

    /**
     * The map that we wrap around.
     */
    Map<K, V> m_innermap;

    /**
     * Creates a DefaultMap based on a HashMap.
     */
    public DefaultMap() {
        this(new LinkedHashMap<K, V>());
    }

    /**
     * Constructs a DefaultMap that wraps around a given map instance.
     * @param map the map to wrap around
     */
    public DefaultMap(final Map<K, V> map) {
        m_innermap = map;
    }

    /**
     * Creates a new V instance as a default value to return when get is called
     * on a non-existent key. Be aware that returning the same instance every
     * time may result in unexpected behaviour, as any key with the same
     * reference as a value will mirror the changes.
     *
     * @return an instance of the default value for V.
     */
    public abstract V getDefaultValue();

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        m_innermap.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(final Object key) {
        return m_innermap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(final Object value) {
        return m_innermap.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return m_innermap.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(final Object key) {
        V value = m_innermap.get(key);
        if (value == null) {
            value = getDefaultValue();

            // Do not insert the value
            // Doing so may cause a ConcurrentModificationException if an
            // unsuccessful get is done during an iteration over the key/entry
            // set.
            //m_innermap.put((K)key, value);
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return m_innermap.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<K> keySet() {
        return m_innermap.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V put(final K key, final V value) {
        return m_innermap.put(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        m_innermap.putAll(m);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V remove(final Object key) {
        return m_innermap.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return m_innermap.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<V> values() {
        return m_innermap.values();
    }
}

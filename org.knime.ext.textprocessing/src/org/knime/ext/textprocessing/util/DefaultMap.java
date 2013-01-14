/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
 *   Jun 12, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Collection;
import java.util.HashMap;
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
        this(new HashMap<K, V>());
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

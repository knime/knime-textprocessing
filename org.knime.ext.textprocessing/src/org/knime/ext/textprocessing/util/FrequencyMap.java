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
 *   Jun 12, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Map;
import java.util.Set;

/**
 * Wrapper around a map to keep track of frequency counts. Will return a default
 * value of 0 when get() is called on a non-existent key.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <K> the type of the keys
 */
public class FrequencyMap<K> extends DefaultMap<K, Integer> {
    // Integers are immutable, no worries about sharing the reference here
    private static final Integer DEFAULT_VALUE = new Integer(0);

    /**
     * Creates a FrequencyMap with the default map type from DefaultMap.
     */
    public FrequencyMap() {
        super();
    }

    /**
     * Creates a frequency map that will wrap around a given Map instance.
     * @param map the map to wrap around
     */
    public FrequencyMap(final Map<K, Integer> map) {
        super(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getDefaultValue() {
        return DEFAULT_VALUE;
    }

    /**
     * @param keys the keys to search for
     * @return the total frequencies of the elements in the set
     */
    public Integer getTotalFrequency(final Set<K> keys) {
        int freqs = 0;

        for (K k : keys) {
            freqs += this.get(k);
        }

        return freqs;
    }

    /**
     * Increments the frequency count for object 'key'.
     *
     * @param key the object whose count should be incremented
     * @return the new value
     */
    public Integer increment(final K key) {
        return put(key, get(key) + 1);
    }

    /**
     * Increments the frequency count for object 'key' by n.
     *
     * @param key the object whose count should be incremented
     * @param n the increment value
     * @return the new value
     */
    public Integer increment(final K key, final int n) {
        return put(key, get(key) + n);
    }

    /**
     * Decrements the frequency count for object 'key'.
     *
     * @param key the object whose count should be decremented
     * @return the new value
     */
    public Integer decrement(final K key) {
        return put(key, get(key) - 1);
    }

    /**
     * Decrements the frequency count for object 'key' by n.
     *
     * @param key the object whose count should be decremented
     * @param n the decrement value
     * @return the new value
     */
    public Integer decrement(final K key, final int n) {
        return put(key, get(key) - n);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (m_innermap.size() > 0) {
            final StringBuilder sb = new StringBuilder();
            for (final Entry<K, Integer> e : m_innermap.entrySet()) {
                sb.append(e.getKey().toString());
                sb.append("::");
                sb.append(e.getValue().toString());
                sb.append(",");
            }

            return sb.toString().substring(0, sb.length() - 1);
        }
        return "";
    }
}

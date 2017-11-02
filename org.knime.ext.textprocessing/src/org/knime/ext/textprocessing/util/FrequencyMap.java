/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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

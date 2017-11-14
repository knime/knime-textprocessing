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
 *   Jun 12, 2008 (Pierre-Francois Laquerre, University of Konstanz): created
 */
package org.knime.ext.textprocessing.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Provides utility methods for maps.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public class Maps {

    /**
     * Returns the min(nbvalues, source.size()) top entries, ordered in
     * descending order of their value.
     *
     * @param <K> key type
     * @param <V> value type
     * @param source the map from which the values are to be extracted
     * @param n the maximum number of values to return
     *
     * The amount of items returned will be less than nbvalues if the source map
     * does not contain enough entries.
     *
     * @return a new map containing the top min(n, source.size()) entries.
     */
    @SuppressWarnings("unchecked")
    public static <K, V extends Comparable<V>> Map<K, V> getTopValues(
            final Map<K, V> source, final int n) {
        Map<K, V> topvalues = new LinkedHashMap<K, V>();
        int nbval = n > source.size() ? source.size() : n;

        Entry<K, V>[] entryset = new Entry[source.size()];
        source.entrySet().toArray(entryset);

        java.util.Arrays.sort(entryset, new ValueComparator());

        int i = source.size() - 1;
        int lowerbound = source.size() - nbval; // inclusive lower bound

        while (i >= lowerbound) {
            topvalues.put(entryset[i].getKey(), entryset[i].getValue());
            --i;
        }

        return topvalues;
    }

    /**
     * Compares entries based on their value.
     *
     * @author Pierre-Francois Laquerre, University of Konstanz
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class ValueComparator implements
            Comparator<Entry<?, ? extends Comparable>> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final Entry<?, ? extends Comparable> e1,
                final Entry<?, ? extends Comparable> e2) {
            return e1.getValue().compareTo(e2.getValue());
        }
    }

    /**
     * Only keeps elements in 'map' which match a given condition.
     *
     * @param <M> the type of the map
     * @param <K> the key type
     * @param <V> the value type
     *
     * @param map the map to modify
     * @param cond the condition that must be met for an element to be kept
     */
    public static <M extends Map<K, V>, K, V> void filter(
            final M map, final Condition<Entry<K, V>> cond) {
        Iterator<Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<K, V> e = it.next();

            if (!cond.test(e)) {
                it.remove();
            }
        }
    }
}

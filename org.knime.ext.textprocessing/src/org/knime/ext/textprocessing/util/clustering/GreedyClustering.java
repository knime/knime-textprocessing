/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 *   Jul 22, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.clustering;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.knime.ext.textprocessing.util.similarity.SimilarityMeasure;

/**
 * Clusters elements in a greedy way: start with 0 clusters and then, for each
 * element, assign it to the most similar cluster. If none is found, a new
 * cluster is created. This proceeds until all elements have been clustered.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the elements to cluster
 */
public class GreedyClustering<T> implements ClusteringAlgorithm<T> {

    /**
     * {@inheritDoc}
     */
    public Set<Cluster<T>> cluster(
            final Set<T> elements, final SimilarityMeasure<T> measure) {
        Set<Cluster<T>> clusters = new HashSet<Cluster<T>>();

        for (T e : elements) {
            try {
                Cluster<T> maxcluster = measure.getMostSimilar(e, clusters);
                maxcluster.add(e);
            } catch (NoSuchElementException ex) {
                Cluster<T> cluster = new Cluster<T>();
                cluster.add(e);
                clusters.add(cluster);
            }
        }

        return clusters;
    }
}

/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
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

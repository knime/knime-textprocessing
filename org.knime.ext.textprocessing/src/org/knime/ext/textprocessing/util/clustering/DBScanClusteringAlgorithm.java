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
 *   Jul 18, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.clustering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.knime.ext.textprocessing.util.similarity.SimilarityMeasure;

/**
 * DBSCAN (http://en.wikipedia.org/wiki/DBSCAN) is a recursive clustering
 * algorithm which does not have a preconception of the shape of the cluster.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to cluster
 */
public class DBScanClusteringAlgorithm<T> implements ClusteringAlgorithm<T> {

    private int m_minPts;

    /**
     * @param minPts points with less neighbours than minPts will be considered
     * as noise (returned as stand-alone clusters)
     */
    public DBScanClusteringAlgorithm(final int minPts) {
        m_minPts = minPts;
    }

    /**
     * {@inheritDoc}
     */
    public Set<Cluster<T>> cluster(
            final Set<T> elements, final SimilarityMeasure<T> measure) {
        Set<Cluster<T>> clusters = new HashSet<Cluster<T>>();

        Set<T> unvisited = new HashSet<T>(elements);
        while (!unvisited.isEmpty()) {
            T t = pop(unvisited);
            Cluster<T> c = new Cluster<T>();
            c.add(t);
            clusters.add(c);

            Set<T> neighbours = getNeighbours(t, elements, measure);
            if (neighbours.size() > m_minPts) {
                expandCluster(
                        c, clusters, neighbours, elements, unvisited, measure);
            }
        }
        return clusters;
    }


    /**
     * Expands cluster c with all non-noise points from unvisited that are
     * within reach.
     *
     * @param c the cluster to expand
     * @param clusters the set of all current clusters
     * @param candidates the candidate points
     * @param elements the set of all possible points
     * @param unvisited the subset of 'elements' that is unvisited as of yet
     * @param measure the similarity measure to use
     */
    private void expandCluster(
            final Cluster<T> c, final Set<Cluster<T>> clusters,
            final Set<T> candidates, final Set<T> elements,
            final Set<T> unvisited, final SimilarityMeasure<T> measure) {
        for (T t : candidates) {
            // only consider unvisited points
            if (unvisited.remove(t)) {
                Set<T> neighbours = getNeighbours(t, elements, measure);

                if (neighbours.size() > m_minPts) {
                    c.add(t);
                    expandCluster(c, clusters, neighbours,
                            elements, unvisited, measure);
                } else {
                    Cluster<T> c2 = new Cluster<T>();
                    c2.add(t);
                    clusters.add(c2);
                }
            }
        }
    }

    /**
     * @param e the element for which neighbours are requested
     * @param candidates the candidate neighbours
     * @param measure the similarity measure to use
     * @return all points considered similar to e
     */
    private static <T> Set<T> getNeighbours(final T e, final Set<T> candidates,
            final SimilarityMeasure<T> measure) {
        Set<T> neighbours = new HashSet<T>();

        for (T candidate : candidates) {
            if (measure.areSimilar(e, candidate)) {
                neighbours.add(candidate);
            }
        }

        return neighbours;
    }

    /**
     * Removes and returns the first element of a set.
     *
     * @param <T> the type of elements in the set
     * @param set the set
     * @return the item that was popped from the set
     */
    private static<T> T pop(final Set<T> set) {
        Iterator<T> it = set.iterator();
        if (it.hasNext()) {
            T e = it.next();
            set.remove(e);
            return e;
        }   
        return null;
    }
}

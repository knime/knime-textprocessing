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
package org.knime.ext.textprocessing.util.similarity;

import java.util.NoSuchElementException;
import java.util.Set;

import org.knime.ext.textprocessing.util.clustering.Cluster;

/**
 * Provides element-element and element-cluster similarity measurement.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of the elements to compare
 */
public abstract class SimilarityMeasure<T> {
    /**
     * Inclusive value over which two elements are considered similar
     */
    protected double m_threshold = 0.0;

    /**
     * @return the lowest value that this measure can return
     */
    public abstract double getLowerBound();

    /**
     * @return the highest value that this measure can return
     */
    public abstract double getHigherBound();


    /**
     * Empty constructor for the measures which require some processing before
     * setting the threshold.
     */
    public SimilarityMeasure() {
        // nothing.
    }

    /**
     * The threshold, min and max are all inclusive.
     * @param threshold inclusive similarity threshold
     */
    public SimilarityMeasure(final double threshold) {
        if (getLowerBound() > getHigherBound()) {
            throw new IllegalArgumentException("The lower bound must be " +
                    "less than or equal to the higher bound");
        }

        setThreshold(threshold);
    }

    /**
     * @param e1 the first element
     * @param e2 the second element
     * @return true if e1 is similar to e2 (the similarity measure exceeded the
     * threshold), false otherwise
     */
    public boolean areSimilar(final T e1, final T e2) {
        return getValue(e1, e2) >= m_threshold;
    }

    /**
     * @param e1 the first element
     * @param e2 the second element
     * @return a measure of similarity between e1 and e2, ranging inclusively
     * from m_lowerbound to m_higherbound
     */
    public abstract double getValue(final T e1, final T e2);

    /**
     * @param e the element
     * @param c the cluster against which the element is to be compared
     * @return the average similarity of e to c (0.0 if the cluster is empty)
     */
    public double getSimilarityValue(final T e, final Cluster<T> c) {
        double val = 0.0;

        if (!c.isEmpty()) {
            for (T e2 : c) {
                val += getValue(e, e2);
            }

            val /= c.size();
        }

        return val;
    }

    /**
     * @param e the element to compare
     * @param clusters the set of clusters to search through
     * @return the most similar cluster
     * @throws NoSuchElementException if none of the clusters are similar to e
     */
    public Cluster<T> getMostSimilar(
            final T e, final Set<Cluster<T>> clusters)
            throws NoSuchElementException {
        double maxval = getLowerBound();
        Cluster<T> maxcluster = null;

        for (Cluster<T> c : clusters) {
            double val = getSimilarityValue(e, c);
            if (impliesSimilarity(val)) {
                if (val > maxval) {
                    maxval = val;
                    maxcluster = c;
                }
            }
        }

        if (maxcluster == null) {
            throw new NoSuchElementException();
        }

        return maxcluster;
    }

    /**
     * @param threshold the inclusive threshold which the similarity measure is
     * compared to
     */
    protected void setThreshold(final double threshold) {
        if (threshold < getLowerBound() || threshold > getHigherBound()) {
            throw new IllegalArgumentException("The threshold must be between" +
                    " " + getLowerBound() + " and " + getHigherBound());
        }

        m_threshold = threshold;
    }

    /**
     * Utility method for clustering algorithms which require both the use of
     * the value and the threshold to keep them from having to compute the
     * measure twice.
     *
     * @param value
     * @return true if the value implies that the two elements that yielded it
     * are similar
     */
    public boolean impliesSimilarity(final double value) {
        return value >= m_threshold;
    }

    /**
     * Clamps the value x in the interval min and max.
     *
     * @param x the value to clamp
     * @param min the inclusive lower bound
     * @param max the inclusive upper bound
     * @return x if it was already in [min, max], the nearest bound otherwise
     */
    protected static double clamp(
            final double x, final double min, final double max) {
        if (x < min) return min;

        if (x > max) return max;

        return x;
    }
}

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
     * Inclusive value over which two elements are considered similar.
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
            throw new IllegalArgumentException("The lower bound must be "
                    + "less than or equal to the higher bound");
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
            throw new IllegalArgumentException("The threshold must be between"
                    + " " + getLowerBound() + " and " + getHigherBound());
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
        if (x < min) {
            return min;
        }

        if (x > max) {
            return max;
        }

        return x;
    }
}

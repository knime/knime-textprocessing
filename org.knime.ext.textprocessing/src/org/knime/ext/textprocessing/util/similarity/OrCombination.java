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
 *   Jul 22, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.util.similarity;

import java.util.NoSuchElementException;
import java.util.Set;

import org.knime.ext.textprocessing.util.clustering.Cluster;

/**
 * Considers two elements as similar if at least one of the submeasures does.
 *
 * This similarity measure differs from the other ones in that, for an element
 * to be considered similar to a cluster, it must be similar to all of its
 * elements.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of elements to compare
 */
public class OrCombination<T> extends SimilarityMeasure<T> {

    private Set<SimilarityMeasure<T>> m_measures;

    private double m_lowerbound;
    private double m_higherbound;

    /**
     * @param measures the measures to use
     */
    public OrCombination(final Set<SimilarityMeasure<T>> measures) {
        super();

        if (measures.isEmpty()) {
            throw new IllegalArgumentException("At least one measure must be " +
            		"provided.");
        }

        // Infer the bounds from the measures'
        m_lowerbound = m_higherbound = 0.0;
        for (SimilarityMeasure<T> m : measures) {
            m_lowerbound += m.getLowerBound();
            m_higherbound += m.getHigherBound();
        }

        m_measures = measures;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getValue(final T e1, final T e2) {
        double s = 0.0;

        for (SimilarityMeasure<T> m : m_measures) {
            s += m.getValue(e1, e2);
        }

        return s;
    }

    /**
     * The concept of threshold here is somewhat fuzzy: the similarity value
     * may be over the global threshold (impliesSimilarity() = true) while none
     * of the individual thresholds have been exceeded. For this reason,
     * getMostSimilar should be used in this case.
     *
     * {@inheritDoc}
     */
    @Override
    public double getSimilarityValue(final T e, final Cluster<T> c) {
        throw new RuntimeException("This operation is not well defined for " +
        		"this specific similarity measure. Use getMostSimilar.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areSimilar(final T e1, final T e2) {
        for (SimilarityMeasure<T> m : m_measures) {
            if (m.areSimilar(e1, e2)) {
                return true;
            }
        }

        return false;
    }

    /**
     * For this similarity measure, a cluster will only be considered if _all_
     * the elements in it are considered similar by at least one submeasure. The
     * highest total similarity will then be used to determine which one to pick
     *
     * {@inheritDoc}
     */
    @Override
    public Cluster<T> getMostSimilar(
            final T e, final Set<Cluster<T>> clusters)
            throws NoSuchElementException {
        double maxvalue = m_lowerbound;
        Cluster<T> maxcluster = null;

        for (Cluster<T> c : clusters) {
            boolean allsimilar = true;
            double totalvalue = 0.0;

            for (T e2 : c) {
                boolean similar = false;

                // Considered similar to the element if one of the
                // measures is positive
                for (SimilarityMeasure<T> m : m_measures) {
                    double val = m.getValue(e, e2);
                    totalvalue += val;

                    if (m.impliesSimilarity(val)) {
                        similar = true;
                    }
                }

                if (!similar) {
                    allsimilar = false;
                    break;
                }
            }

            if (allsimilar) {
                if (totalvalue > maxvalue) {
                    maxvalue = totalvalue;
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
     * {@inheritDoc}
     */
    @Override
    public double getHigherBound() {
        return m_higherbound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLowerBound() {
        return m_lowerbound;
    }

}

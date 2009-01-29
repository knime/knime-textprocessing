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

import java.util.Set;

import org.knime.ext.textprocessing.util.similarity.SimilarityMeasure;

/**
 * Interface for clustering algorithms.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 * @param <T> the type of element to cluster
 */
public interface ClusteringAlgorithm<T> {

    /**
     * @param elements the elements to cluster
     * @param measure the measure to use to judge the distance between elements
     * @return a partition of 'elements'
     */
    public abstract Set<Cluster<T>> cluster(
            Set<T> elements, SimilarityMeasure<T> measure);
}

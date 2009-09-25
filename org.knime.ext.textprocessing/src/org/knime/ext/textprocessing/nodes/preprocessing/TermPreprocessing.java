/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
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
 *   19.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.ext.textprocessing.data.Term;

/**
 * This interface has to be implemented by all term preprocessing nodes
 * no matter if filter or modification nodes. 
 * The method {@link TermPreprocessing#preprocessTerm(Term)} has to be implemented by 
 * all underlying classes and provide a certain term preprocessing functionality. 
 * A stemmer node for instance has to stem the given term and return the 
 * stemmed one. If a term preprocessing class filters out a given term, 
 * <code>null</code> has to be returned.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public interface TermPreprocessing extends Preprocessing {

    /**
     * Preprocesses the given term in a certain manner. Modification nodes, 
     * such as stemmer or case converter return the modified term. Filter nodes
     * such as stop word filter return <code>null</code> if the given term
     * was filtered out, otherwise the term is returned unmodified.
     * 
     * @param term The term to preprocess
     * @return The preprocessed term
     */
    public Term preprocessTerm(final Term term);
}

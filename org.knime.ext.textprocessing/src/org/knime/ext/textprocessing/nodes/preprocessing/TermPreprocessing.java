/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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

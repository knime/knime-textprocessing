/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   30.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.nefilter;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 * The filter class of the named entity filter node. Provides methods to filter
 * terms which are modifiable or unmodifiable, respectively.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class NamedEntityFilter implements TermPreprocessing {

    private boolean m_filterModifiable;

    /**
     * Creates a new instance of <code>NamedEntityFilter</code> with given
     * flag specifying if modifiable or unmodifiable terms are filtered.
     *
     * @param filterModifiable If <code>true</code> modifiable terms are
     * filtered, otherwise unmodifiable.
     */
    public NamedEntityFilter(final boolean filterModifiable) {
        m_filterModifiable = filterModifiable;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term preprocessTerm(final Term term) {
        if (m_filterModifiable && term.isUnmodifiable()) {
            return term;
        } else if (!m_filterModifiable && !term.isUnmodifiable()) {
            return term;
        }
        return null;
    }

}

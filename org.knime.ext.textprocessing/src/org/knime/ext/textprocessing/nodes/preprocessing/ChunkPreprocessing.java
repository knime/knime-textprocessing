/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 * -------------------------------------------------------------------
 *
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import java.util.Hashtable;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.DocumentChunk;



/**
 * This interface has to be implemented by all chnk preprocessing nodes
 * no matter if filter or modification nodes.
 * The method {@link ChunkPreprocessing#preprocessChunk(DocumentChunk)} has to be
 * implemented by all underlying classes and provide a certain chunk
 * preprocessing functionality.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public interface ChunkPreprocessing extends Preprocessing {


    /**
     * This method provides a term-term mapping. The given term, contained in
     * the specified chunk will be mapped to output terms. The mapping of
     * these terms is returned as hash table. This provided mapping is used
     * to change / preprocess terms and to enable deep preprocessing.
     *
     * @param chunk The chunk, consisting of a set of terms and a corresponding
     * document.
     * @return A term-term mapping, which maps the input terms, contained in the
     * given chunk, onto output or preprocessed terms.
     */
    public abstract Hashtable<Term, Term> preprocessChunk(final DocumentChunk chunk);
}

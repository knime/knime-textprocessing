/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2006
 * Universitaet Konstanz, Germany.
 * Lehrstuhl fuer Angewandte Informatik
 * Prof. Dr. Michael R. Berthold
 * 
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner.
 * -------------------------------------------------------------------
 * 
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.util.DocumentChunk;

import java.util.Hashtable;



/**
 * This interface has to be implemented by all chnk preprocessing nodes
 * no matter if filter or modification nodes. 
 * The method {@link ChunkPreprocessing#preprocessChunk()} has to be 
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
    public abstract Hashtable<Term, Term> preprocessChunk(
            final DocumentChunk chunk);
}

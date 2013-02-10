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
 *   14.10.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.misc.ngram;

/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.8
 */
public final class NGramConfigKeys {

    private NGramConfigKeys() { }

    /**
     * The configuration key for the N value.
     */
    static final String N = "N";

    /**
     * The configuration key for the n gram type.
     */
    static final String NGRAM_TYPE = "NGramType";

    /**
     * The configuration key for the output type (bow or frequency table).
     */
    static final String NGRAM_OUTPUT = "DNGramOutput";

    /**
     * The configuration key for the input column containing the documents.
     */
    static final String DOCUMENT_INPUT_COL = "DocumentInputColumn";

    /**
     * The configuration key for the number of maximal parallel threads.
     */
    static final String NUMBER_OF_THREADS = "MaximalParallelThreads";

    /**
     * The configuration key for the chunk size (number of documents) per
     * thread.
     */
    static final String CHUNK_SIZE = "ChunkSize";
}

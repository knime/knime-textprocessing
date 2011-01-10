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
 *   19.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

/**
 * Contains the default configuration keys. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class PreprocessingConfigKeys {

    private PreprocessingConfigKeys() { }
    
    /**
     * The configuration key for the deep preprcessing setting.
     */
    public static final String CFG_KEY_DEEP_PREPRO = "DeepPreprocessing";
    
    /**
     * The configuration key for appending the incoming document.
     */
    public static final String CFG_KEY_APPEND_INCOMING = "AppendIncomingDoc";
    
    /**
     * The configuration key for the column containing the documents to process.
     */
    public static final String CFG_KEY_DOCUMENT_COL = "DocCol";
    
    /**
     * The configuration key for the column containing the original documents
     * to append unchanged (if specified).
     */
    public static final String CFG_KEY_ORIGDOCUMENT_COL = "OriginalDocCol";
    
    /**
     * The configuration key for the chunk size to use.
     */
    public static final String CFG_KEY_CHUNK_SIZE = "ChunkSize";
    
    /**
     * The configuration key for the "preprocess unmodifiable terms" flag.
     */
    public static final String CFG_KEY_PREPRO_UNMODIFIABLE = 
        "Preprocess Unmodifiable";
}

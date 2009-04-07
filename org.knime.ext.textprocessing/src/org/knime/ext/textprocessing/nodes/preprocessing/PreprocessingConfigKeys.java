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
 *   19.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing;

/**
 * Contains the default configuration keys. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class PreprocessingConfigKeys {

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

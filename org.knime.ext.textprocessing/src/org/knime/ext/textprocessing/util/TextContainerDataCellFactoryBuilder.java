/* -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 * 
 * Copyright, 2003 - 2009
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
 *   16.02.2009 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * @author Kilian Thiel, University of Konstanz
 */
public final class TextContainerDataCellFactoryBuilder {
    
    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(TextContainerDataCellFactoryBuilder.class);
    
    private TextContainerDataCellFactoryBuilder() { }
    
    /**
     * @return The <code>TextContainerDataCellFactory</code> creating 
     * document cells.
     */
    public static TextContainerDataCellFactory createDocumentCellFactory() {
        if (!TextprocessingPreferenceInitializer.useBlobCell()) {
            LOGGER.info("Creating document cell factory!");
            return new DocumentDataCellFactory();
        }
        LOGGER.info("Creating document blob cell factory!");
        return new DocumentBlobDataCellFactory();
    }
    
    /**
     * @return The <code>TextContainerDataCellFactory</code> creating 
     * term cells.
     */
    public static TextContainerDataCellFactory createTermCellFactory() {
        return new TermDataCellFactory();
    }
}

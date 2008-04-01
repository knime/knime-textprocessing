/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   18.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.List;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;

/**
 * Provides convenient methods that create 
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * {@link org.knime.ext.textprocessing.data.Document}s.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public abstract class DocumentDataTableBuilder implements DataTableBuilder {

    /**
     * Empty constructor of <code>DocumentDataTableBuilder</code>.
     */
    public DocumentDataTableBuilder() { }
    
    /**
     * Creates and returns a {@link org.knime.core.node.BufferedDataTable} 
     * containing a single column with the given documents as 
     * {@link org.knime.ext.textprocessing.data.DocumentCell}s, one for each 
     * row.
     * 
     * @param exec The <code>ExecutionContext</code> to create the 
     * <code>BufferedDataTable</code> with.
     * @param docs The list of 
     * {@link org.knime.ext.textprocessing.data.Document}s to keep in the table.
     * @return The <code>BufferedDataTable</code> containing the given 
     * documents. 
     * @throws CanceledExecutionException If execution was canceled.
     */
    public abstract BufferedDataTable createDataTable(
            final ExecutionContext exec, final List<Document> docs) 
    throws CanceledExecutionException;
}

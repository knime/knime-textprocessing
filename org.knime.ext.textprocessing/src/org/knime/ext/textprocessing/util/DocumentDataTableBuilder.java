/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
 *   18.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.List;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentDataTableBuilder {

    private DocumentDataTableBuilder() { }
    
    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing just one column of type <code>DocumentCell</code> to
     * store text documents.
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with just one column of type <code>DocumentListCell</code>.
     */
    public static DataTableSpec createDocumentDataTableSpec() {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator dcscDocs =
                new DataColumnSpecCreator("Document", DocumentCell.TYPE);
        return new DataTableSpec(dcscDocs.createSpec());
    }
    
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
     * @throws CanceledExecutionException If the execution has been canceled.
     */
    public static BufferedDataTable createDocumentDataTable(
            final ExecutionContext exec, final List<Document> docs) 
    throws CanceledExecutionException {
        DataContainer dc =
                exec.createDataContainer(DocumentDataTableBuilder
                        .createDocumentDataTableSpec());

        int i = 1;
        for (Document d : docs) {
            RowKey rowKey = new RowKey(new IntCell(i));
            DocumentCell docCell = new DocumentCell(d);
            DataRow row = new DefaultRow(rowKey, docCell);
            dc.addRowToTable(row);
        }
        dc.close();

        return exec.createBufferedDataTable(dc.getTable(), exec);
    }
}

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
 *   11.09.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * A utility class providing static methods to transform and change data 
 * structures containing terms, documents or similar.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DataStructureUtil {

    /**
     * Builds a set of documents out of the given data table and returns it.
     * The index of the cells containing the documents has to be specified.
     * Furthermore an execution context has to be given, to enable to cancel the
     * process as well as display its progress.
     * 
     * @param data The data table containing the documents to store in a set.
     * @param documentCellIndex The index of the cells containing the documents.
     * @param exec An execution context to enable the user to cancel the process
     * as well as display its progress.
     * @return A set containing all the documents in the given data table.
     * @throws CanceledExecutionException If the user cancels the process.
     */
    public static final Set<Document> buildDocumentSet(
            final BufferedDataTable data, final int documentCellIndex, 
            final ExecutionContext exec) throws CanceledExecutionException {
        Set<Document> documents = new HashSet<Document>();
        
        int rowCount = 1;
        int rows = data.getRowCount();
        
        RowIterator it = data.iterator();        
        while(it.hasNext()) {
            DataRow row = it.next();
            Document doc = ((DocumentValue)row.getCell(documentCellIndex))
                            .getDocument();
            documents.add(doc);

            if (exec != null) {
                exec.checkCanceled();
                double prog = (double)rows / (double)rowCount;
                exec.setProgress(prog, "Caching row " + rowCount + " of "
                        + rows);
                rowCount++;
            }
        }
        
        return documents;
    }
}

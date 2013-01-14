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
 *   11.09.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
public final class DataStructureUtil {

    private DataStructureUtil() { }
    
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
        while (it.hasNext()) {
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
    
    /**
     * Builds a list of documents out of the given data table and returns it.
     * The index of the cells containing the documents has to be specified.
     * Furthermore an execution context has to be given, to enable to cancel the
     * process as well as display its progress.
     * 
     * @param data The data table containing the documents to store in a set.
     * @param documentCellIndex The index of the cells containing the documents.
     * @param exec An execution context to enable the user to cancel the process
     * as well as display its progress.
     * @return A list containing all the documents in the given data table.
     * @throws CanceledExecutionException If the user cancels the process.
     */    
    public static final List<Document> buildDocumentList(
            final BufferedDataTable data, final int documentCellIndex, 
            final ExecutionContext exec) throws CanceledExecutionException {
        int rowCount = 1;
        int rows = data.getRowCount();
        List<Document> documents = new ArrayList<Document>(rows);
        
        RowIterator it = data.iterator();        
        while (it.hasNext()) {
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

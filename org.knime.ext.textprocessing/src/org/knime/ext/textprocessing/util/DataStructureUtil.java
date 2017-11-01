/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
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
            if (!row.getCell(documentCellIndex).isMissing()) {
                Document doc = ((DocumentValue)row.getCell(documentCellIndex)).getDocument();
                documents.add(doc);
            }

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

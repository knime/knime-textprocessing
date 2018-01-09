/*
 * ------------------------------------------------------------------------
 *
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   May 26, 2016 (hermann): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.df;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class DfCellFactory extends FrequencyCellFactory {

    /**
     * The name of the column containing the df value
     */

    public static final String COLNAME = "DF";

    /**
     * The flag specifying that the column containing the df values is a int column.
     */
    public static final boolean INT_COL = true;

    private Map<Term, Set<UUID>> m_termUUID;

    /**
     * @param documentCellIndex The column index containing the documents.
     * @param termCellindex The column index containing the terms.
     * @param docData The data table containing the complete bag of words.
     * @param exec he execution context to monitor the progress and check it
     * user canceled the process.
     * @throws CanceledExecutionException If user canceled the process.
     */
    public DfCellFactory(final int documentCellIndex, final int termCellindex, final BufferedDataTable docData,
        final ExecutionContext exec) throws CanceledExecutionException {
        super(documentCellIndex, termCellindex, COLNAME, INT_COL);

        m_termUUID = new HashMap<Term, Set<UUID>>();

        long maxRows = docData.size();
        long currRow = 1;

        RowIterator it = docData.iterator();
        while (it.hasNext()) {
            double prog = (double)currRow / (double)maxRows;
            exec.setProgress(prog, "Computing idf value of term " + currRow + " of " + maxRows);
            exec.checkCanceled();
            currRow++;
            DataRow row = it.next();

            // check for missing values in term or document column!
            if (row.getCell(getTermColIndex()).isMissing() || row.getCell(getDocumentColIndex()).isMissing()) {
                continue;
            }

            Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
            Document d = ((DocumentValue)row.getCell(getDocumentColIndex())).getDocument();

            if (!m_termUUID.containsKey(t)) {
                Set<UUID> uuids = new HashSet<UUID>();
                uuids.add(d.getUUID());
                m_termUUID.put(t, uuids);
            } else {
                Set<UUID> uuids = m_termUUID.get(t);
                uuids.add(d.getUUID());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        // check for missing values in term column
        if (row.getCell(getTermColIndex()).isMissing() || row.getCell(getDocumentColIndex()).isMissing()) {
            return new DataCell[]{DataType.getMissingCell()};
        }

        Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
        int df = 0;

        // check if set m_termUUID.get(t) exists
        if (m_termUUID.containsKey(t)) {
            df = m_termUUID.get(t).size();

        }
        return new DataCell[]{new IntCell(df)};
    }

}

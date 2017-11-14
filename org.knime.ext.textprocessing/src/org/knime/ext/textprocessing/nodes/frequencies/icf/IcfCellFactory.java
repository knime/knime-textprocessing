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
 *   18.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.icf;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.frequencies.Frequencies;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory;

/**
 * The icf cell factory computes the inverse category frequency value of each term and adds the value as a new double
 * cell. The categories are taken from the documents containing the term.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class IcfCellFactory extends FrequencyCellFactory {

    /**
     * The name of the column containing the icf value.
     */
    public static final String COLNAME = "ICF";

    /**
     * The flag specifying that the column containing the icf values is a double column.
     */
    public static final boolean INT_COL = false;

    private Hashtable<Term, Set<DocumentCategory>> m_termCatData;

    private Set<DocumentCategory> m_cats;


    /**
     * Creates new instance of <code>IcfCellFactory</code> which computes
     * the idf value for each row and adds new column containing the values.
     *
     * @param documentCellIndex The column index containing the documents.
     * @param termCellindex The column index containing the terms.
     * @param docData The data table containing the complete bag of words.
     * @param exec The execution context to monitor the progress and check it
     * user canceled the process.
     * @throws CanceledExecutionException If user canceled the process.
     */
    public IcfCellFactory(final int documentCellIndex,
            final int termCellindex, final BufferedDataTable docData,
            final ExecutionContext exec) throws CanceledExecutionException {
        super(documentCellIndex, termCellindex, COLNAME, INT_COL);

        m_termCatData = new Hashtable<Term, Set<DocumentCategory>>();
        m_cats = new HashSet<DocumentCategory>();

        long maxRows = docData.size();
        int currRow = 1;

        RowIterator it = docData.iterator();
        while (it.hasNext()) {
            double prog = (double)currRow / (double)maxRows;
            exec.setProgress(prog, "Computing icf value of term " + currRow
                    + " of " + maxRows);
            exec.checkCanceled();
            currRow++;

            DataRow row = it.next();
            if (!row.getCell(getTermColIndex()).isMissing() && !row.getCell(getDocumentColIndex()).isMissing()) {
                Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
                Set<DocumentCategory> cats =
                    ((DocumentValue)row.getCell(getDocumentColIndex()))
                    .getDocument().getCategories();

                m_cats.addAll(cats);

                // save term and doc
                Set<DocumentCategory> termCats;
                if (!m_termCatData.containsKey(t)) {
                    termCats = new HashSet<DocumentCategory>();
                } else {
                    termCats = m_termCatData.get(t);
                }
                termCats.addAll(cats);
                m_termCatData.put(t, termCats);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        if (!row.getCell(getTermColIndex()).isMissing() && !row.getCell(getDocumentColIndex()).isMissing()) {
            Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
            double idf = 0;
            if (m_termCatData.containsKey(t)) {
                idf = Frequencies.inverseDocumentFrequency(m_cats.size(), m_termCatData.get(t).size());
            }
            return new DoubleCell[]{new DoubleCell(idf)};
        } else {
            return new DataCell[]{DataType.getMissingCell()};
        }

    }

}

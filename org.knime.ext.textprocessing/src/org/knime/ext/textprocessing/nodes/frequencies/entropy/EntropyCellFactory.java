/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *   Jun 15, 2016 (hermann): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.entropy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.util.Pair;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.frequencies.Frequencies;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory;

/**
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
class EntropyCellFactory extends FrequencyCellFactory {

    /**
     * The name of the column containing the entropy value
     */

    public static final String COLNAME = "Entropy";

    /**
     * The flag specifying that the column containing the entropy values is a double column.
     */
    public static final boolean INT_COL = false;

    /**
     * a term and his total frequency throughout the entire collection
     */
    private final Map<Term, Double> m_termFrequencyGlobal = new HashMap<Term, Double>();

    /**
     * a term and his frequency in one document
     */
    private final Map<Pair<Term, UUID>, Double> m_termFrequencyLocal = new HashMap<Pair<Term, UUID>, Double>();

    /**
     * the entropy
     */
    private final Map<Pair<Term, UUID>, Double> m_entropy = new HashMap<Pair<Term, UUID>, Double>();

    /**
     * a Set of documents UUID
     */
    final Set<UUID> m_docsUUID = new HashSet<UUID>();

    /**
     * @param documentCellIndex
     * @param termCellindex
     * @param docData
     * @param exec
     * @throws CanceledExecutionException
     */
    public EntropyCellFactory(final int documentCellIndex, final int termCellindex, final BufferedDataTable docData,
        final ExecutionContext exec) throws CanceledExecutionException {
        super(documentCellIndex, termCellindex, COLNAME, INT_COL);

        exec.setMessage("Computing frequencies.");

        final long noRows = docData.size();
        RowIterator it = docData.iterator();
        double tf = 0;
        while (it.hasNext()) {
            exec.checkCanceled();

            final DataRow row = it.next();

            // check for missing values in term or document column!
            if (row.getCell(getTermColIndex()).isMissing() || row.getCell(getDocumentColIndex()).isMissing()) {
                continue;
            }

            final Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
            final Document d = ((DocumentValue)row.getCell(getDocumentColIndex())).getDocument();

            m_docsUUID.add(d.getUUID());

            tf = Frequencies.absoluteTermFrequency(t, d);

            final Pair<Term, UUID> key = new Pair<Term, UUID>(t, d.getUUID());

            // if tf is 0 this row in the bow is invalid => return missing as value
            if (tf == 0) {
                m_entropy.put(key, Double.NaN);
                continue;
            } else {
                m_termFrequencyLocal.put(key, tf);
            }

            if (!m_termFrequencyGlobal.containsKey(t)) {
                m_termFrequencyGlobal.put(t, tf);
            } else {
                double totalFrequency = m_termFrequencyGlobal.get(t);
                m_termFrequencyGlobal.put(t, tf + totalFrequency);
            }
        }

        exec.setProgress((double)noRows / (double)(noRows + noRows * m_docsUUID.size()), "Computing entropies.");

        // Compute the sum
        long rowCount = 1;
        it = docData.iterator();
        while (it.hasNext()) {
            exec.checkCanceled();
            exec.setProgress(
                (double)(noRows + rowCount * m_docsUUID.size()) / (double)(noRows + noRows * m_docsUUID.size()),
                "Computing entropy " + rowCount + " of " + noRows + ".");
            rowCount++;

            final DataRow row = it.next();

            // check for missing values in term or document column!
            if (row.getCell(getTermColIndex()).isMissing() || row.getCell(getDocumentColIndex()).isMissing()) {
                continue;
            }

            final Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
            final Document d = ((DocumentValue)row.getCell(getDocumentColIndex())).getDocument();

            final double entropy = computeEntropy(t, m_docsUUID);

            final Pair<Term, UUID> key = new Pair<Term, UUID>(t, d.getUUID());

            if (!m_entropy.containsKey(key)) {
                m_entropy.put(key, entropy);
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

        final Term t = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
        final Document d = ((DocumentValue)row.getCell(getDocumentColIndex())).getDocument();
        final Pair<Term, UUID> key = new Pair<Term, UUID>(t, d.getUUID());

        final double entropy = m_entropy.get(key);
        if (Double.isNaN(entropy)) {
            return new DataCell[]{DataType.getMissingCell()};
        } else {
            return new DataCell[]{new DoubleCell(entropy)};
        }
    }

    private double computeEntropy(final Term term, final Set<UUID> documents) {
        double result = 0;
        final long N = documents.size();
        /* log(N) == 0 */
        if (N == 1) {
            return Double.NaN;
        }

        for (final UUID uuid : documents) {
            final Pair<Term, UUID> key = new Pair<Term, UUID>(term, uuid);
            final double f;
            /* term is not in doc -> f = 0, continue */
            if (!m_termFrequencyLocal.containsKey(key)) {
                continue;
            } else {
                f = m_termFrequencyLocal.get(key);
            }

            final double F = m_termFrequencyGlobal.get(term);
            if (F == 0) {
                return Double.NaN;
            } else {
                result += (f / F) * Math.log10(f / F) / Math.log10(N);
            }
        }

        return result + 1;
    }

}

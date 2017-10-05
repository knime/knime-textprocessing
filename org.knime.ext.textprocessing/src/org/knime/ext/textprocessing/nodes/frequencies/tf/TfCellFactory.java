/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.nodes.frequencies.Frequencies;
import org.knime.ext.textprocessing.nodes.frequencies.FrequencyCellFactory;

/**
 * The tf cell factory computes the relative term frequency value of each
 * term document tuple and adds the value as a new double cell.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TfCellFactory extends FrequencyCellFactory {

    /**
     * The name of the column containing the tf value.
     */
    public static final String COLNAME_REL = "TF rel";

    /**
     * The name of the column containing the absolute tf value.
     */
    public static final String COLNAME_ABS = "TF abs";


    private boolean m_relative = TfNodeModel.DEF_RELATIVE;


    /**
     * Creates new instance of <code>TfCellFactory</code> which computes
     * the tf value for each row and adds new column containing the values.
     * If parameter <code>relative</code> is set <code>true</code> the relative
     * term frequency is computed, otherwise the absolute.
     *
     * @param documentCellIndex The column index containing the documents.
     * @param termCellindex The column index containing the terms.
     * @param relative if set <code>true</code> the relative
     * term frequency is computed, otherwise the absolute.
     */
    public TfCellFactory(final int documentCellIndex,
            final int termCellindex, final boolean relative) {
        super(documentCellIndex, termCellindex, getColName(relative),
                getIntCol(relative));
        m_relative = relative;
    }

    private static boolean getIntCol(final boolean relative) {
        if (relative) {
            return false;
        }
        return true;
    }

    private static String getColName(final boolean relative) {
        if (relative) {
            return COLNAME_REL;
        }
        return COLNAME_ABS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DataCell[] getCells(final DataRow row) {

        Term term = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
        Document doc = ((DocumentValue)row.getCell(getDocumentColIndex()))
                        .getDocument();
        DataCell freq;
        if (m_relative) {
            freq = new DoubleCell(Frequencies.relativeTermFrequency(term, doc));
        } else {
            freq = new IntCell(Frequencies.absoluteTermFrequency(term, doc));
        }
        return new DataCell[]{freq};
    }
}

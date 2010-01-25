/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;

import java.util.Hashtable;
import java.util.Set;

/**
 * Provides convenient methods that create
 * {@link org.knime.core.node.BufferedDataTable}s containing
 * a bag of words which consists one column containing
 * {@link org.knime.ext.textprocessing.data.Document}s and one column containing
 * {@link org.knime.ext.textprocessing.data.Term}s.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class BagOfWordsDataTableBuilder implements DataTableBuilder {

    /**
     * The default document column name in bow data tables.
     */
    public static final String DEF_DOCUMENT_COLNAME = "Document";

    /**
     * The default original document column name in bow data tables.
     */
    public static final String DEF_ORIG_DOCUMENT_COLNAME = "Orig Document";

    /**
     * The default term column name in bow data tables.
     */
    public static final String DEF_TERM_COLNAME = "Term";
    
    private final TextContainerDataCellFactory m_documentCellFac;

    /**
     * Empty constructor of <code>BagOfWordsDataTableBuilder</code>.
     */
    public BagOfWordsDataTableBuilder() {
        m_documentCellFac = 
            TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
    }


    /**
     * Creates and returns a new {@link org.knime.core.node.BufferedDataTable}
     * containing a column with the given data cells and one with the terms.
     * The data cells have to be compatible with <code>DocumentValue</code>,
     * otherwise an <code>IllegalArgumentException</code> will be thrown.
     * The given data cells are reused when creating the new data table,
     * no new data cells are created to save memory and take full advantage
     * of the benefit of <code>BlobDataCell</code>s.
     *
     * @param exec The context to create a new <code>BufferedDataTable</code>
     * and monitor the progress.
     * @param docTerms A hash table containing the <code>DataCell</code>s
     * with the documents and the terms to create a data table out of.
     * @param useTermCache If true the term cells will be cached and
     * only created once for each term. Equal term are represented by the
     * same <code>TermCell</code>.
     * @return The created <code>BufferedDataTable</code> containing the given
     * data cells with the documents and terms represented by
     * <code>TermCell</code>s.
     * @throws CanceledExecutionException If execution was canceled.
     * @throws IllegalArgumentException If a data cell is not compatible with
     * <code>DocumentValue</code>.
     */
    public BufferedDataTable createDataTable(final ExecutionContext exec,
            final Hashtable<DataCell, Set<Term>> docTerms,
            final boolean useTermCache) throws CanceledExecutionException,
            IllegalArgumentException {
        // create cache
        FullDataCellCache termCache =
                new FullDataCellCache(new TermDataCellFactory());
        BufferedDataContainer dc =
                exec.createDataContainer(this.createDataTableSpec());

        int i = 1;
        Set<DataCell> keys = docTerms.keySet();
        int rowCount = keys.size();
        int currRow = 1;

        for (DataCell d : keys) {
            if (d == null) {
                continue;
            }

            if (!d.getType().isCompatible(DocumentValue.class)) {
                throw new IllegalArgumentException("DataCell is not "
                        + "compatible with DocumentValue!");
            }

            Set<Term> terms = docTerms.get(d);
            for (Term t : terms) {
                if (t == null) {
                    continue;
                }
                exec.checkCanceled();
                RowKey rowKey = new RowKey(new Integer(i).toString());
                i++;

                TermCell termCell;
                if (!useTermCache) {
                    termCell = new TermCell(t);
                } else {
                    termCell = (TermCell)termCache.getInstance(t);
                }

                DataRow row = new DefaultRow(rowKey, termCell, d);
                dc.addRowToTable(row);
            }

            double progress = (double)currRow / (double)rowCount;
            exec.setProgress(progress, "Creating Bow of document " + currRow
                    + " of " + rowCount);
            exec.checkCanceled();
            currRow++;
        }
        dc.close();

        docTerms.clear();

        return dc.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final DataTableSpec createDataTableSpec() {
        return createDataTableSpec(false);
    }
    
 
    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing minimum one column of type <code>DocumentCell</code> to
     * store text documents and one column of type <code>TermCell</code>
     * representing the terms contained by a certain document.
     * @param appendExtraDocCol if set <code>true</code> an additional column
     * containing <code>DocumentCell</code> is appended.
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with one column of type <code>DocumentListCell</code> and one
     *         column of type <code>TermCell</code>.
     */
    public final DataTableSpec createDataTableSpec(
            final boolean appendExtraDocCol) {        
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator docs = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME, 
                m_documentCellFac.getDataType());
        DataColumnSpecCreator docs2 = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME, 
                m_documentCellFac.getDataType());        
        DataColumnSpecCreator terms = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_TERM_COLNAME, 
                TermCell.TYPE);
        
        if (!appendExtraDocCol) {
            return new DataTableSpec(terms.createSpec(), docs.createSpec());
        }
        return new DataTableSpec(terms.createSpec(), docs.createSpec(), 
                docs2.createSpec());
    }
}

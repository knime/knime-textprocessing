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
 *   17.04.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.frequencies.tf;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.DoubleCell;
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
    public static final String COLNAME = "TF";
    
    /**
     * The flag specifying that the column containing the tf values is a double 
     * column.
     */
    public static final boolean INT_COL = false;
    
    
    /**
     * Creates new instance of <code>TfCellFactory</code> which computes
     * the tf value for each row and adds new column containing the values.
     * 
     * @param documentCellIndex The column index containing the documents. 
     * @param termCellindex The column index containing the terms.
     */
    public TfCellFactory(final int documentCellIndex,
            final int termCellindex) {
        super(documentCellIndex, termCellindex, COLNAME, INT_COL);
    }
    
    /**
     * {@inheritDoc}
     */
    public final DataCell[] getCells(final DataRow row) {
        
        Term term = ((TermValue)row.getCell(getTermColIndex())).getTermValue();
        Document doc = ((DocumentValue)row.getCell(getDocumentColIndex()))
                        .getDocument(); 
        
        DoubleCell freq = new DoubleCell(
                Frequencies.relativeTermFrequency(term, doc));
        return new DataCell[]{freq};
    }
}

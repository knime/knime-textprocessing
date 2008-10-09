/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
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
 *   03.03.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.TermCell;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class BagOfWordsBlobCellDataTableBuilder extends
        BagOfWordsDataTableBuilder {

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
    @Override
    public DataTableSpec createDataTableSpec(final boolean appendExtraDocCol) {
        // create DataTableSpec for output DataTable
        DataColumnSpecCreator docs = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_DOCUMENT_COLNAME, 
                DocumentBlobCell.TYPE);
        DataColumnSpecCreator docs2 = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_ORIG_DOCUMENT_COLNAME, 
                DocumentBlobCell.TYPE);        
        DataColumnSpecCreator terms = new DataColumnSpecCreator(
                BagOfWordsDataTableBuilder.DEF_TERM_COLNAME, 
                TermCell.TYPE);
        
        if (!appendExtraDocCol) {
            return new DataTableSpec(terms.createSpec(), docs.createSpec());
        }
        return new DataTableSpec(terms.createSpec(), docs.createSpec(), 
                docs2.createSpec());
    }
    
    /**
     * Creates a new <code>DataTableSpec</code> for <code>DataTable</code>s
     * containing one column of type <code>DocumentCell</code> to
     * store text documents and one column of type <code>TermCell</code>
     * representing the terms contained by a certain document. 
     * 
     * @return The <code>DataTableSpec</code> for <code>DataTable</code>s
     *         with one column of type <code>DocumentListCell</code> and one
     *         column of type <code>TermCell</code>.
     */
    public DataTableSpec createDataTableSpec() {
        return createDataTableSpec(false);
    }    
    
    /**
     * {@inheritDoc}
     * 
     * This method creates a factory for <code>DocumentBlobCell</code>s.
     */
    @Override
    protected TextContainerDataCellFactory getDocumentCellDataFactory() {
        return new DocumentBlobDataCellFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean validateDocumentCellType(final DataCell documentCell) {
        if (documentCell instanceof DocumentBlobCell) {
            return true;
        }
        return false;
    }
}

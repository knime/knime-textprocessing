/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
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
 *   25.06.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.misc.categorytoclass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentClassCellFactory implements CellFactory {

    private static final String UNDEFINED = "undefined";
    
    private int m_documentCellIndex = -1;
    
    /**
     * Creates a new instance of <code>DocumentClassCellFactory</code> with
     * given index of the column containing <code>DocumentCell</code>s.
     * @param documentCellIndex the index of the column containing
     * <code>DocumentCell</code>s. 
     */
    public DocumentClassCellFactory(final int documentCellIndex) {
        m_documentCellIndex = documentCellIndex;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        StringCell classCell;
        Document doc = ((DocumentValue)row.getCell(m_documentCellIndex))
                        .getDocument();
        
        Set<DocumentCategory> cats = doc.getCategories();
        List<DocumentCategory> catsList = new ArrayList<DocumentCategory>(cats);
        Collections.sort(catsList, new Comparator <DocumentCategory>() {
            public int compare(DocumentCategory o1, DocumentCategory o2) {
                if (o1 != null && o2 != null) {
                    return o1.getCategoryName().compareTo(o2.getCategoryName());
                }
                return 0;
            }
        });
        
        String cat = UNDEFINED;
        if (catsList.size() > 0) {
            cat = catsList.get(0).getCategoryName();
        }
        classCell = new StringCell(cat);
        return new DataCell[]{classCell};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        DataColumnSpec classCell = new DataColumnSpecCreator("Document class", 
                    StringCell.TYPE).createSpec();
        return new DataColumnSpec[]{classCell};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(int curRowNr, int rowCount, RowKey lastKey,
            ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Addig class of row: " + curRowNr 
                + " of " + rowCount + " rows");
    }
}

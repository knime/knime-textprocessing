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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.node.ExecutionMonitor;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.util.DocumentBlobDataCellFactory;
import org.knime.ext.textprocessing.util.FullDataCellCache;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentCellFactory implements CellFactory {

    private StringsToDocumentConfig m_config;
    
    private FullDataCellCache m_cache;
    
    public StringsToDocumentCellFactory(final StringsToDocumentConfig config) 
    throws IllegalArgumentException {
        if (config == null) {
            throw new IllegalArgumentException(
                    "Configuration object may not be null!");
        }
        m_config = config;
        m_cache = new FullDataCellCache(new DocumentBlobDataCellFactory());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        DocumentBuilder docBuilder = new DocumentBuilder();
        
        // Set title
        if (m_config.getTitleStringIndex() >= 0) {
            DataCell titleCell = row.getCell(m_config.getTitleStringIndex());
            if (!titleCell.isMissing() && 
                    titleCell.getType().isCompatible(StringValue.class)) {
                String title = ((StringValue)titleCell).getStringValue();
                docBuilder.addTitle(title);
            }
        }
        // Set authors
        if (m_config.getAuthorsStringIndex() >= 0) {
            DataCell auhorsCell = row.getCell(m_config.getAuthorsStringIndex());
            if (!auhorsCell.isMissing() &&
                    auhorsCell.getType().isCompatible(StringValue.class)) {
                String authors = ((StringValue)auhorsCell).getStringValue();
                String[]authorsArr = authors.split(
                        m_config.getAuthorsSplitChar());
                for (String author : authorsArr) {
                    String firstName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;
                    String lastName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;
                    
                    String names[] = author.split(" ");
                    if (names.length > 1) {
                        firstName = "";
                        for (int i = 0; i < names.length - 1; i++) {
                            firstName += names[i] + " ";
                        }
                        lastName = names[names.length - 1];
                    } else if (names.length == 1) {
                        lastName = names[0];
                    }
                    
                    Author docAuthor = new Author(firstName.trim(), 
                            lastName.trim());
                    docBuilder.addAuthor(docAuthor);
                }
            }
        }
        
        Document doc = docBuilder.createDocument();
        return new DataCell[]{m_cache.getInstance(doc)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        DataColumnSpec docCol = new DataColumnSpecCreator("Document", 
                DocumentCell.TYPE).createSpec();
        return new DataColumnSpec[]{docCol};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(final int curRowNr, final int rowCount, 
            final RowKey lastKey, final ExecutionMonitor exec) {
        double prog = (double)curRowNr / (double)rowCount;
        exec.setProgress(prog, "Processing row: " + curRowNr 
                + " of " + rowCount + " rows");
    }

}

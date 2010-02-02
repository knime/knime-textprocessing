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
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.util.DocumentBlobDataCellFactory;
import org.knime.ext.textprocessing.util.FullDataCellCache;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A <code>CellFactory</code> to build a document for each data row. The
 * given <code>StringsToDocumentConfig</code> instance specifies which
 * columns of the row to use as title, text authors, etc. 
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentCellFactory implements CellFactory {

    private static final NodeLogger LOGGER =
            NodeLogger.getLogger(StringsToDocumentCellFactory.class);
    
    private StringsToDocumentConfig m_config;
    
    private FullDataCellCache m_cache;
    
    /**
     * Creates new instance of <code>StringsToDocumentCellFactory</code> with
     * given configuration.
     * 
     * @param config The configuration how to build a document.
     * @throws IllegalArgumentException If given configuration is 
     * <code>null</code>.
     */
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
            String title = "";
            if (!titleCell.isMissing() 
                    && titleCell.getType().isCompatible(StringValue.class)) {
                title = ((StringValue)titleCell).getStringValue();
            }
            
            docBuilder.addTitle(title);
        }
        
        //Set fulltext
        if (m_config.getFulltextStringIndex() >= 0) {
            DataCell textCell = row.getCell(m_config.getFulltextStringIndex());
            String fulltext = "";
            if (!textCell.isMissing() 
                    && textCell.getType().isCompatible(StringValue.class)) {
                fulltext = ((StringValue)textCell).getStringValue();
            }
            docBuilder.addSection(fulltext, SectionAnnotation.UNKNOWN);
        }
        
        // Set authors
        if (m_config.getAuthorsStringIndex() >= 0) {
            DataCell auhorsCell = row.getCell(m_config.getAuthorsStringIndex());
            if (!auhorsCell.isMissing() 
                    && auhorsCell.getType().isCompatible(StringValue.class)) {
                String authors = ((StringValue)auhorsCell).getStringValue();
                String[]authorsArr = authors.split(
                        m_config.getAuthorsSplitChar());
                for (String author : authorsArr) {
                    String firstName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;
                    String lastName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;
                    
                    String[] names = author.split(" ");
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
        
        // set document source
        String docSource = m_config.getDocSource();
        if (docSource.length() > 0) {
            DocumentSource ds = new DocumentSource(m_config.getDocSource());
            docBuilder.addDocumentSource(ds);
        }

        // set document category
        String docCat = m_config.getDocCat();
        if (docCat.length() > 0) {
            DocumentCategory dc = new DocumentCategory(docCat);
            docBuilder.addDocumentCategory(dc);
        }
        
        // set document type
        String docType = m_config.getDocType();
        docBuilder.setDocumentType(DocumentType.stringToDocumentType(docType));
        
        // set publication date
        String pubDate = m_config.getPublicationDate();
        Pattern p = Pattern.compile("([\\d]{2})-([\\d]{2})-([\\d]{4})");
        Matcher m = p.matcher(pubDate);
        if (m.matches()) {
            int day = Integer.parseInt(m.group(1));
            int month = Integer.parseInt(m.group(2));
            int year = Integer.parseInt(m.group(3));
            
            try {
                PublicationDate date = new PublicationDate(year, month, day);
                docBuilder.setPublicationDate(date);
            } catch (ParseException e) {
                LOGGER.info("Publication date culd not be set!");
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

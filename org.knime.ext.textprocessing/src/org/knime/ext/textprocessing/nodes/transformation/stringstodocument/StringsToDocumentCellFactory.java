/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * A <code>CellFactory</code> to build a document for each data row. The
 * given <code>StringsToDocumentConfig</code> instance specifies which
 * columns of the row to use as title, text authors, etc.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentCellFactory extends AbstractCellFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StringsToDocumentCellFactory.class);

    private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})-([\\d]{4})");

    private final StringsToDocumentConfig m_config;

    private final DataCellCache m_cache;

    /**
     * Creates new instance of <code>StringsToDocumentCellFactory</code> with
     * given configuration.
     *
     * @param config The configuration how to build a document.
     * @param exec the execution context to prepare text container cell factory.
     * @param newColSpecs The specs of the new columns that are created.
     * @param numberOfThreads The number of parallel threads to use.
     * @throws IllegalArgumentException If given configuration is
     * <code>null</code>.
     * @since 2.9
     */
    public StringsToDocumentCellFactory(final StringsToDocumentConfig config, final ExecutionContext exec,
        final DataColumnSpec[] newColSpecs, final int numberOfThreads) throws IllegalArgumentException {
        super(newColSpecs);

        this.setParallelProcessing(true, numberOfThreads, 10 * numberOfThreads);

        if (config == null) {
            throw new IllegalArgumentException("Configuration object may not be null!");
        }
        m_config = config;
        final TextContainerDataCellFactory docCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        docCellFac.prepare(exec);
        m_cache = new LRUDataCellCache(docCellFac);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DocumentBuilder docBuilder = new DocumentBuilder();

        // Set title
        if (m_config.getTitleStringIndex() >= 0) {
            DataCell titleCell = row.getCell(m_config.getTitleStringIndex());
            String title = "";
            if (!titleCell.isMissing() && titleCell.getType().isCompatible(StringValue.class)) {
                title = ((StringValue)titleCell).getStringValue();
            }

            docBuilder.addTitle(title);
        }

        //Set fulltext
        if (m_config.getFulltextStringIndex() >= 0) {
            final DataCell textCell = row.getCell(m_config.getFulltextStringIndex());
            String fulltext = "";
            if (!textCell.isMissing() && textCell.getType().isCompatible(StringValue.class)) {
                fulltext = ((StringValue)textCell).getStringValue();
            }
            docBuilder.addSection(fulltext, SectionAnnotation.UNKNOWN);
        }

        // Set authors
        if (m_config.getAuthorsStringIndex() >= 0) {
            final DataCell auhorsCell = row.getCell(m_config.getAuthorsStringIndex());
            if (!auhorsCell.isMissing() && auhorsCell.getType().isCompatible(StringValue.class)) {
                final String authors = ((StringValue)auhorsCell).getStringValue();
                final String[]authorsArr = authors.split(m_config.getAuthorsSplitChar());
                for (String author : authorsArr) {
                    String firstName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;
                    String lastName = StringsToDocumentConfig.DEF_AUTHOR_NAMES;

                    final String[] names = author.split(" ");
                    if (names.length > 1) {
                        final StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < names.length - 1; i++) {
                            sb.append(names[i]);
                            sb.append(" ");
                        }
                        firstName = sb.toString();
                        lastName = names[names.length - 1];
                    } else if (names.length == 1) {
                        lastName = names[0];
                    }

                    docBuilder.addAuthor(new Author(firstName.trim(), lastName.trim()));
                }
            }
        }

        // set document source
        String docSource = m_config.getDocSource();
        if (m_config.getUseSourceColumn()) {
            if (m_config.getSourceStringIndex() >= 0) {
                final DataCell sourceCell = row.getCell(m_config.getSourceStringIndex());
                if (!sourceCell.isMissing() && sourceCell.getType().isCompatible(StringValue.class)) {
                    docSource = ((StringValue)sourceCell).getStringValue();
                } else {
                    docSource = "";
                }
            }
        }
        if (docSource.length() > 0) {
            docBuilder.addDocumentSource(new DocumentSource(docSource));
        }

        // set document category
        String docCat = m_config.getDocCat();
        if (m_config.getUseCatColumn()) {
            if (m_config.getCategoryStringIndex() >= 0) {
                final DataCell catCell = row.getCell(m_config.getCategoryStringIndex());
                if (!catCell.isMissing() && catCell.getType().isCompatible(StringValue.class)) {
                    docCat = ((StringValue)catCell).getStringValue();
                } else {
                    docCat = "";
                }
            }
        }
        if (docCat.length() > 0) {
            docBuilder.addDocumentCategory(new DocumentCategory(docCat));
        }

        // set document type
        docBuilder.setDocumentType(DocumentType.stringToDocumentType(m_config.getDocType()));

        // set publication date
        final Matcher m = DATE_PATTERN.matcher(m_config.getPublicationDate());
        if (m.matches()) {
            final int day = Integer.parseInt(m.group(1));
            final int month = Integer.parseInt(m.group(2));
            final int year = Integer.parseInt(m.group(3));

            try {
                docBuilder.setPublicationDate(new PublicationDate(year, month, day));
            } catch (ParseException e) {
                LOGGER.info("Publication date could not be set!");
            }
        }

        return new DataCell[]{m_cache.getInstance(docBuilder.createDocument())};
    }

    /**
     * Closes data cell cache.
     * @since 2.8
     */
    public void closeCache() {
        m_cache.close();
    }
}

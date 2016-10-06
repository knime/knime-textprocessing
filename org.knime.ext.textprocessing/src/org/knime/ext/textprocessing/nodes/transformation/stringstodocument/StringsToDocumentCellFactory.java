/*
 * ------------------------------------------------------------------------
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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
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
 * A <code>CellFactory</code> to build a document for each data row. The given <code>StringsToDocumentConfig</code>
 * instance specifies which columns of the row to use as title, text authors, etc.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentCellFactory extends AbstractCellFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StringsToDocumentCellFactory.class);

    private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})-([\\d]{4})");

    private final StringsToDocumentConfig m_config;

    private final LazyInitializer<DataCellCache> m_cacheInitializer;

    /**
     * Creates new instance of <code>StringsToDocumentCellFactory</code> with given configuration.
     *
     * @param config The configuration how to build a document.
     * @param newColSpecs The specs of the new columns that are created.
     * @param numberOfThreads The number of parallel threads to use.
     * @throws IllegalArgumentException If given configuration is <code>null</code>.
     * @since 3.1
     */
    public StringsToDocumentCellFactory(final StringsToDocumentConfig config, final DataColumnSpec[] newColSpecs,
        final int numberOfThreads) throws IllegalArgumentException {
        super(newColSpecs);

        this.setParallelProcessing(true, numberOfThreads, 10 * numberOfThreads);

        if (config == null) {
            throw new IllegalArgumentException("Configuration object may not be null!");
        }
        m_cacheInitializer = new LazyInitializer<DataCellCache>() {
            @Override
            protected DataCellCache initialize() throws ConcurrentException {
                return initializeDataCellCache();
            }
        };
        m_config = config;
    }

    /** Callback from initializer - only be called when executing. */
    private DataCellCache initializeDataCellCache() {
        final TextContainerDataCellFactory docCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        final FileStoreFactory fileStoreFactory = getFileStoreFactory();
        CheckUtils.checkState(fileStoreFactory != null, "File store factory not expected to be null at this point");
        docCellFac.prepare(fileStoreFactory);
        return new LRUDataCellCache(docCellFac);
    }

    /** @return the cache from the initializer, not null. Throws RuntimeException if needed. */
    private DataCellCache getDataCellCache() {
        DataCellCache dataCellCache;
        try {
            dataCellCache = m_cacheInitializer.get();
        } catch (ConcurrentException e) {
            throw new RuntimeException("Couldn't retrieve data cell cache", e);
        }
        return dataCellCache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DocumentBuilder docBuilder = new DocumentBuilder();

        // Set title
        String title = m_config.getDocTitle();
        if (m_config.getUseTitleColumn()) {
            if (m_config.getTitleStringIndex() >= 0) {
                // looping over the data cells will helps checking for each doc title for availability
                final DataCell titleCell = row.getCell(m_config.getTitleStringIndex());
                if (!titleCell.isMissing() && titleCell.getType().isCompatible(StringValue.class)) {
                    title = ((StringValue)titleCell).getStringValue();
                }
                docBuilder.addTitle(title);
            }
        } else {
            title = row.getKey().toString();
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
        if (m_config.getUseAuthorsColumn()) {
            if (m_config.getAuthorsStringIndex() >= 0) {
                final DataCell auhorsCell = row.getCell(m_config.getAuthorsStringIndex());
                if (!auhorsCell.isMissing() && auhorsCell.getType().isCompatible(StringValue.class)) {
                    final String authors = ((StringValue)auhorsCell).getStringValue();
                    final String[] authorsArr = authors.split(m_config.getAuthorsSplitChar());
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
        } else {

            docBuilder.addAuthor(new Author("-", "-"));
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

        DataCellCache dataCellCache = getDataCellCache();
        return new DataCell[]{dataCellCache.getInstance(docBuilder.createDocument())};
    }

    /**
     * Closes data cell cache.
     *
     * @since 2.8
     */
    @Override
    public void afterProcessing() {
        super.afterProcessing();
        getDataCellCache().close();
    }
}

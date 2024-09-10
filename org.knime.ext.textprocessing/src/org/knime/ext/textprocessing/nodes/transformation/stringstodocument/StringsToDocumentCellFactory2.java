/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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

import java.io.DataOutput;
import java.text.ParseException;
import java.time.LocalDate;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.time.localdate.LocalDateValue;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

import com.google.common.base.Utf8;

/**
 * A {@link CellFactory} implementation to build a document for each data row. The given
 * {@code StringsToDocumentConfig2} instance specifies which columns of the row to use as title, text authors, etc.
 *
 * @author Hermann Azong & Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
final class StringsToDocumentCellFactory2 extends AbstractCellFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StringsToDocumentCellFactory2.class);

    private final StringsToDocumentConfig2 m_config;

    private final LazyInitializer<DataCellCache> m_cacheInitializer;

    private boolean m_cacheCreated = false;

    private String m_tokenizerName = TextprocessingPreferenceInitializer.tokenizerName();

    /**
     *  Strings that require more than 65535 bytes to be represented in UTF cannot be written via writeUTF in the method
     *  {@link org.knime.textprocessing.util.TermDocumentDeSerializationUtil#fastSerializeDocument}.
     *  See {@link DataOutput#writeUTF(String)}
     */
    private static final int MAX_ENCODED_STRING_SIZE =  65535;

    /**
     * Creates new instance of {@code StringsToDocumentCellFactory2} with given configuration.
     *
     * @param config The configuration how to build a document.
     * @param newColSpecs The specs of the new columns that are created.
     * @param numberOfThreads The number of parallel threads to use.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @throws IllegalArgumentException If given configuration is {@code null}.
     */
    public StringsToDocumentCellFactory2(final StringsToDocumentConfig2 config, final DataColumnSpec[] newColSpecs,
        final int numberOfThreads, final String tokenizerName) throws IllegalArgumentException {
        super(newColSpecs);

        this.setParallelProcessing(true, numberOfThreads, 10 * numberOfThreads);

        if (config == null) {
            throw new IllegalArgumentException("Configuration object may not be null!");
        }
        m_cacheInitializer = new LazyInitializer<DataCellCache>() {
            @Override
            protected DataCellCache initialize() throws ConcurrentException {
                DataCellCache dataCellCache = initializeDataCellCache();
                m_cacheCreated = true;
                return dataCellCache;
            }
        };
        m_config = config;
        m_tokenizerName = tokenizerName;
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
        final DocumentBuilder docBuilder = new DocumentBuilder(m_tokenizerName);

        // Set title
        if (m_config.getUseTitleColumn()) {
            final DataCell titleCell = row.getCell(m_config.getTitleColumnIndex());
            if (!titleCell.isMissing()) {
                var title = ((StringValue)titleCell).getStringValue();
                checkLength(title, "title", row.getKey());
                docBuilder.addTitle(title);
            }
        } else if (m_config.getTitleMode().contentEquals(StringsToDocumentConfig2.TITLEMODE_ROWID)) {
            docBuilder.addTitle(row.getKey().toString());
        }

        //Set fulltext
        final DataCell textCell = row.getCell(m_config.getFulltextColumnIndex());
        if (!textCell.isMissing()) {
            docBuilder.addSection(((StringValue)textCell).getStringValue(), SectionAnnotation.UNKNOWN);
        }

        // Set authors
        setAuthors(docBuilder, row);


        // set document source
        if (m_config.getUseSourceColumn()) {
            final DataCell sourceCell = row.getCell(m_config.getSourceColumnIndex());
            if (!sourceCell.isMissing()) {
                var source = ((StringValue)sourceCell).getStringValue();
                checkLength(source, "document source", row.getKey());
                docBuilder.addDocumentSource(new DocumentSource(source));
            }
        } else if (m_config.getDocSource().length() > 0) {
            docBuilder.addDocumentSource(new DocumentSource(m_config.getDocSource()));
        }

        // set document category
        if (m_config.getUseCatColumn()) {
            final DataCell catCell = row.getCell(m_config.getCategoryColumnIndex());
            if (!catCell.isMissing()) {
                var category = ((StringValue)catCell).getStringValue();
                checkLength(category, "document category", row.getKey());
                docBuilder.addDocumentCategory(new DocumentCategory(category));
            }
        } else if (m_config.getDocCat().length() > 0) {
            docBuilder.addDocumentCategory(new DocumentCategory(m_config.getDocCat()));
        }

        // set document type
        docBuilder.setDocumentType(DocumentType.stringToDocumentType(m_config.getDocType()));

        // set publication date
        if (m_config.getUsePubDateColumn()) {
            final DataCell pubDateCell = row.getCell(m_config.getPubDateColumnIndex());
            if (!pubDateCell.isMissing()) {
                LocalDate date = ((LocalDateValue)pubDateCell).getLocalDate();
                setPublicationDate(docBuilder, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
            }
        } else {
            LocalDate date = m_config.getPublicationDate();
            setPublicationDate(docBuilder, date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        }

        // return datacells cells
        return new DataCell[]{getDataCellCache().getInstance(docBuilder.createDocument())};
    }

    private void setAuthors(final DocumentBuilder docBuilder, final DataRow row) {

        if (m_config.getUseAuthorsColumn()) {

            final DataCell authorsCell = row.getCell(m_config.getAuthorsColumnIndex());

            if (!authorsCell.isMissing()) {
                final String authors = ((StringValue)authorsCell).getStringValue();
                final String[] authorsArr = authors.split(m_config.getAuthorsSplitChar());
                setAuthors(docBuilder, row.getKey(), authorsArr);
            }
        } else if (!m_config.getAuthorFirstName().isEmpty() || !m_config.getAuthorLastName().isEmpty()) {
            docBuilder.addAuthor(new Author(m_config.getAuthorFirstName(), m_config.getAuthorLastName()));
        }
    }

    private static void setAuthors(final DocumentBuilder docBuilder, final RowKey rowKey, final String[] authorsArr) {
        for (String author : authorsArr) {
            String firstName = "";
            String lastName = "";

            final String[] names = author.split(" ");
            if (names.length > 1) {
                final StringBuilder sb = new StringBuilder();
                for (int i = 0; i < names.length - 1; i++) {
                    sb.append(names[i]);
                    sb.append(" ");
                }
                firstName = sb.toString();
                checkLength(firstName, "first name of an author", rowKey);
                lastName = names[names.length - 1];
                checkLength(lastName, "last name of an author", rowKey);
            } else if (names.length == 1) {
                lastName = names[0];
            }

            docBuilder.addAuthor(new Author(firstName.trim(), lastName.trim()));
        }
    }

    private static void checkLength(final String stringToCheck, final String stringName, final RowKey rowKey) {
        final int encodedLength = Utf8.encodedLength(stringToCheck);
        CheckUtils.checkArgument(encodedLength < MAX_ENCODED_STRING_SIZE, "The %s in row '%s' is too long.", stringName,
            rowKey.getString());
    }

    /**
     * Closes data cell cache.
     */
    @Override
    public void afterProcessing() {
        super.afterProcessing();
        if (m_cacheCreated) {
            getDataCellCache().close();
        }
    }

    // sets the publication date to the document builder
    private static void setPublicationDate(final DocumentBuilder docBuilder, final int year, final int month, final int day) {
        try {
            docBuilder.setPublicationDate(new PublicationDate(year, month, day));
        } catch (ParseException e) {
            LOGGER.info("Publication date could not be set!");
        }
    }
}

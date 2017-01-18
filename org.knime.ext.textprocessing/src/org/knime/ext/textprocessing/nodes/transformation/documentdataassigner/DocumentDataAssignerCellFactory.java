/*
 * ------------------------------------------------------------------------
 *
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
 *   17.01.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentdataassigner;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.filestore.DocumentBufferedFileStoreCell;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class DocumentDataAssignerCellFactory extends AbstractCellFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentDataAssignerCellFactory.class);

    private static final Pattern DATE_PATTERN = Pattern.compile("([\\d]{2})-([\\d]{2})-([\\d]{4})");

    private final LazyInitializer<DataCellCache> m_cacheInitializer;

    private DocumentDataAssignerConfig m_conf;

    /**
     * @param conf
     * @param dataColumnSpec
     */
    public DocumentDataAssignerCellFactory(final DocumentDataAssignerConfig conf,
        final DataColumnSpec[] dataColumnSpec) {
        super(dataColumnSpec);

        m_conf = conf;
        this.setParallelProcessing(true, m_conf.getNumberOfThreads(), 10 * m_conf.getNumberOfThreads());

        m_cacheInitializer = new LazyInitializer<DataCellCache>() {

            @Override
            protected DataCellCache initialize() throws ConcurrentException {
                return initializeDataCellCache();
            }
        };
    }

    /**
     * @return
     */
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
        if (!row.getCell(m_conf.getDocumentColumnIndex()).isMissing()) {
            DocumentBufferedFileStoreCell docCell =
                    (DocumentBufferedFileStoreCell)row.getCell(m_conf.getDocumentColumnIndex());
                Document doc = docCell.getDocument();
                DocumentBuilder docBuilder = new DocumentBuilder(doc, true);

                //set authors
                if (m_conf.getAuthorsColumnIndex() < 0) {
                    if ((m_conf.getAuthorsFirstName() != DocumentDataAssignerConfig.DEF_AUTHOR_FIRST_NAME
                        && !m_conf.getAuthorsFirstName().isEmpty())
                        || (m_conf.getAuthorsLastName() != DocumentDataAssignerConfig.DEF_AUTHOR_LAST_NAME
                            && !m_conf.getAuthorsLastName().isEmpty())) {
                        docBuilder.addAuthor(new Author(m_conf.getAuthorsFirstName(), m_conf.getAuthorsLastName()));
                    }
                } else {
                    DataCell authorCell = row.getCell(m_conf.getAuthorsColumnIndex());
                    if (!authorCell.isMissing() && authorCell.getType().isCompatible(StringValue.class)) {
                        String authors[] = ((StringCell)authorCell).getStringValue().split(m_conf.getAuthorsSplitStr());
                        for (String author : authors) {
                            final String[] names = author.split(" ");
                            if (names.length > 1) {
                                docBuilder.addAuthor(new Author(names[0], names[names.length - 1]));
                            } else {
                                docBuilder.addAuthor(new Author("-", names[0]));
                            }
                        }
                    }
                }

                //set document source
                if (m_conf.getSourceColumnIndex() < 0) {
                    if (!m_conf.getDocSource().isEmpty()) {
                        docBuilder.addDocumentSource(new DocumentSource(m_conf.getDocSource()));
                    }
                } else {
                    DataCell sourceCell = row.getCell(m_conf.getSourceColumnIndex());
                    if (!sourceCell.isMissing() && sourceCell.getType().isCompatible(StringValue.class)) {
                        docBuilder.addDocumentSource(new DocumentSource(((StringCell)sourceCell).getStringValue()));
                    }
                }

                //set document category
                if (m_conf.getCategoryColumnIndex() < 0) {
                    if (!m_conf.getDocCategory().isEmpty()) {
                        docBuilder.addDocumentCategory(new DocumentCategory(m_conf.getDocCategory()));
                    }
                } else {
                    DataCell categoryCell = row.getCell(m_conf.getCategoryColumnIndex());
                    if (!categoryCell.isMissing() && categoryCell.getType().isCompatible(StringValue.class)) {
                        docBuilder.addDocumentCategory(new DocumentCategory(((StringCell)categoryCell).getStringValue()));
                    }
                }

                //set document type
                docBuilder.setDocumentType(DocumentType.stringToDocumentType(m_conf.getDocType()));

                //set publication date
                if (m_conf.getPubDateColumnIndex() < 0) {
                    setPublicationDate(m_conf.getDocPubDate(), docBuilder);
                } else {
                    DataCell pubDateCell = row.getCell(m_conf.getPubDateColumnIndex());
                    if (!pubDateCell.isMissing() && pubDateCell.getType().isCompatible(StringValue.class)) {
                        setPublicationDate(((StringCell)pubDateCell).getStringValue(), docBuilder);
                    }
                }

                DataCellCache dataCellCache = getDataCellCache();
                return new DataCell[]{dataCellCache.getInstance(docBuilder.createDocument())};
        } else {
            return new DataCell[]{DataType.getMissingCell()};
        }

    }

    private void setPublicationDate(final String str, final DocumentBuilder docBuilder) {
        if (!str.isEmpty()) {
            Matcher m = DATE_PATTERN.matcher(str);
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
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterProcessing() {
        super.afterProcessing();
        try {
            getDataCellCache().close();
        } catch (IllegalStateException e) {
            // catch exception if file store wasn't created due to empty table
        }
    }

}

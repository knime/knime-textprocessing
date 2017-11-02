/*
========================================================================
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
 * -------------------------------------------------------------------
 *
 * History
 *    09.12.2008 (Tobias Koetter): created
 */

package org.knime.ext.textprocessing.nodes.transformation.documenttostring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.collection.CollectionCellFactory;
import org.knime.core.data.collection.ListCell;
import org.knime.core.data.collection.SetCell;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;


/**
 *
 * @author Tobias Koetter, University of Konstanz
 * @deprecated Use {@link DocumentDataExtractor2} instead.
 */
@Deprecated
public enum DocumentDataExtractor {

    /**Returns the title of a document.*/
    TITLE("Title", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final String title = doc.getTitle();
            if (title == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(title);
        }
    }),
    /**Returns the abstract of a document.*/
    ABSTRACT("Abstract", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final String text = doc.getAbstract();
            if (text == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(text);
        }
    }),
    /**Returns the text of a document.*/
    TEXT("Text", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final String text = doc.getText();
            if (text == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(text);
        }
    }),
    /**Returns the text of the body of a document without title, journal or meta info text.
     * @since 2.8
     * */
    DOCUMENTBODYTEXT("Document body text", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final String text = doc.getDocumentBodyText();
            if (text == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(text);
        }
    }),
    /**Returns the authors of a document as string.*/
    AUTHOR("Author", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<Author> authors = doc.getAuthors();
            if (authors == null || authors.size() == 0) {
                return DataType.getMissingCell();
            }
            final StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (final Author author : authors) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(author.getFirstName());
                buf.append(' ');
                buf.append(author.getLastName());
            }
            return new StringCell(buf.toString());
        }
    }),
    /**Returns the authors of a document as set.*/
    AUTHOR_SET("Author set", new Extractor() {
        @Override
        public DataType getDataType() {
            return SetCell.getCollectionType(StringCell.TYPE);
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<Author> authors = doc.getAuthors();
            if (authors == null || authors.size() == 0) {
                return DataType.getMissingCell();
            }
            final List<DataCell> names =
                new ArrayList<DataCell>(authors.size());
            for (final Author author : authors) {
                final String name =
                    author.getFirstName() + " " + author.getLastName();
                names.add(new StringCell(name));
            }
            return CollectionCellFactory.createSetCell(names);
        }
    }),
    /**Returns the categories of a document.*/
    CATEGORY("Category", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<DocumentCategory> categories = doc.getCategories();
            if (categories == null || categories.size() == 0) {
                return DataType.getMissingCell();
            }
            final StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (final DocumentCategory category : categories) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(category.getCategoryName());
            }
            return new StringCell(buf.toString());
        }
    }),
    /**Returns the categories of a document as set.*/
    CATEGORY_SET("Category set", new Extractor() {
        @Override
        public DataType getDataType() {
            return SetCell.getCollectionType(StringCell.TYPE);
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<DocumentCategory> categories = doc.getCategories();
            if (categories == null || categories.size() == 0) {
                return DataType.getMissingCell();
            }
            final List<DataCell> names =
                new ArrayList<DataCell>(categories.size());
            for (final DocumentCategory category : categories) {
                names.add(new StringCell(category.getCategoryName()));
            }
            return CollectionCellFactory.createSetCell(names);
        }
    }),
    /**Returns the source of a document.*/
    SOURCE("Source", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<DocumentSource> sources = doc.getSources();
            if (sources == null || sources.size() == 0) {
                return DataType.getMissingCell();
            }
            final StringBuilder buf = new StringBuilder();
            boolean first = true;
            for (final DocumentSource source : sources) {
                if (first) {
                    first = false;
                } else {
                    buf.append(", ");
                }
                buf.append(source.getSourceName());
            }
            return new StringCell(buf.toString());
        }
    }),
    /**Returns the sources of a document as set.*/
    SOURCE_SET("Source set", new Extractor() {
        @Override
        public DataType getDataType() {
            return SetCell.getCollectionType(StringCell.TYPE);
        }
        @Override
        public DataCell getValue(final Document doc) {
            final Set<DocumentSource> sources = doc.getSources();
            if (sources == null || sources.size() == 0) {
                return DataType.getMissingCell();
            }
            final List<DataCell> names =
                new ArrayList<DataCell>(sources.size());
            for (final DocumentSource source : sources) {
                names.add(new StringCell(source.getSourceName()));
            }
            return CollectionCellFactory.createSetCell(names);
        }
    }),
    /**Returns the type of a document.*/
    Type("Type", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final DocumentType type = doc.getType();
            if (type == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(type.name());
        }
    }),
    /**Returns the publication date of a document.*/
    PUB_DATE("Publication date", new Extractor() {
        @Override
        public DataType getDataType() {
            return DateAndTimeCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final PublicationDate date = doc.getPubDate();
            if (date == null || (date.getYear() == 0 && date.getMonth() == 0
                    && date.getDay() == 0)) {
                return DataType.getMissingCell();
            }
            return new DateAndTimeCell(date.getYear(), date.getMonth() - 1,
                    date.getDay());
        }
    }),
    /**Returns the file path of a document.*/
    DOC_FILE("File path", new Extractor() {
        @Override
        public DataType getDataType() {
            return StringCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            final File file = doc.getDocFile();
            if (file == null) {
                return DataType.getMissingCell();
            }
            return new StringCell(file.getAbsolutePath());
        }
    }),
    /**Returns the length of a document.*/
    LENGTH("Number of terms", new Extractor() {
        @Override
        public DataType getDataType() {
            return IntCell.TYPE;
        }
        @Override
        public DataCell getValue(final Document doc) {
            return new IntCell(doc.getLength());
        }
    }),
    /**
     * Returns the meta info of a document.
     * @since 2.8
     */
    METAINFO("Meta info", new Extractor() {
        @Override
        public DataType getDataType() {
            return ListCell.getCollectionType(StringCell.TYPE);
        }
        @Override
        public DataCell getValue(final Document doc) {
            List<DataCell> strCells = new ArrayList<DataCell>();
            DocumentMetaInfo metaInfo = doc.getMetaInformation();
            if (metaInfo != null) {
                for (String key : metaInfo.getMetaInfoKeys()) {
                    String value = metaInfo.getMetaInfoValue(key);
                    if (key != null && value != null) {
                        strCells.add(new StringCell(key + ":" + value));
                    }
                }
            }
            return CollectionCellFactory.createListCell(strCells);
        }
    });

    private interface Extractor {
        /**
         * @param doc the {@link Document} to extract the data from
         * @return the extracted data as {@link DataCell}
         */
        public DataCell getValue(final Document doc);

        /**
         * @return the {@link DataType}
         */
        public DataType getDataType();
    }

    private final String m_name;
    private final Extractor m_extractor;

    /**Constructor for class DocumentExtractor.
     *
     */
    private DocumentDataExtractor(final String name,
            final Extractor extractor) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("name must not be empty");
        }
        if (extractor == null) {
            throw new NullPointerException("extractor must not be null");
        }
        m_name = name;
        m_extractor = extractor;

    }

    /**
     * @return the name of the extractor
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the {@link DataType} the extractor returns as result
     */
    public DataType getDataType() {
        return m_extractor.getDataType();
    }

    /**
     * @param doc the {@link Document} to extract the data from
         * @return the extracted data as {@link DataCell}
     */
    public DataCell getValue(final Document doc) {
        return m_extractor.getValue(doc);
    }

    /**
     * @return the name of all extractors
     */
    public static String[] getExtractorNames() {
        final DocumentDataExtractor[] values = values();
        final String[] names = new  String[values.length];
        for (int i = 0, length = values.length; i < length; i++) {
            names[i] = values[i].getName();
        }
        return names;
    }

    /**
     * @param names the name of the extractors to get
     * @return the extractors with the given name in the same order
     */
    public static DocumentDataExtractor[] getExctractor(final String...names) {
        if (names == null) {
            return null;
        }
        final DocumentDataExtractor[] extractors =
            new DocumentDataExtractor[names.length];
        for (int i = 0, length = names.length; i < length; i++) {
            final String name = names[i];
            for (final DocumentDataExtractor extractor : values()) {
                if (extractor.getName().equals(name)) {
                    extractors[i] = extractor;
                    break;
                }
            }
            if (extractors[i] == null) {
                throw new IllegalArgumentException(
                        "Invalid extractor name: " + name);
            }
        }
        return extractors;
    }
}

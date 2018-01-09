/*
 * ------------------------------------------------------------------------
 *
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
 *   24.05.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.misc.markuptagfilter;

import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.jsoup.Jsoup;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.util.CheckUtils;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.util.DataCellCache;
import org.knime.ext.textprocessing.util.LRUDataCellCache;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */

class MarkupTagFilterCellFactory extends AbstractCellFactory {

    private final int[] m_colIndexToFilter;

    private final LazyInitializer<DataCellCache> m_cacheInitializer;

    private final String m_tokenizerName;

    /**
     * Creates instance of {@code MarkupTagFilterCellFactory}
     *
     * @param colIndextoFilter The indices of the columns with the Strings to filter.
     * @param newColSpecs The specs of the new columns (replaced or appended).
     * @param tokenizerName The name of the tokenizer used to retokenize filtered documents.
     */
    MarkupTagFilterCellFactory(final int[] colIndexToFilter, final DataColumnSpec[] newColSpecs,
        final String tokenizerName) {
        super(newColSpecs);
        m_tokenizerName = tokenizerName;

        m_colIndexToFilter = colIndexToFilter;

        m_cacheInitializer = new LazyInitializer<DataCellCache>() {
            @Override
            protected DataCellCache initialize() throws ConcurrentException {
                return initializeDataCellCache();
            }
        };

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
        DataCell[] newCells = new DataCell[m_colIndexToFilter.length];
        int noCols = row.getNumCells();
        int nextIndexToFilter = 0;
        int currIndexToFilter = -1;

        // walk through all columns
        for (int i = 0; i < noCols; i++) {

            // get next index of column to filter (if still columns to filter are available).
            if (nextIndexToFilter < m_colIndexToFilter.length) {
                currIndexToFilter = m_colIndexToFilter[nextIndexToFilter];
            }

            // if string needs to be filtered
            if (i == currIndexToFilter) {
                final DataCell outCell;
                if (row.getCell(i).isMissing()) {
                    outCell = DataType.getMissingCell();
                } else {
                    if (row.getCell(i).getType().isCompatible(DocumentValue.class)) {
                        Document doc = ((DocumentValue)row.getCell(i)).getDocument();

                        // create new instance of DocumentBuilder and copy the meta-information from input document
                        // excluding the text information
                        final DocumentBuilder docBuilder = new DocumentBuilder(doc, m_tokenizerName);

                        // get section annotation names and section texts, filter the texts for tags
                        // and add them to the new DocumentBuilder
                        int numberofsections = doc.getSections().size();
                        SectionAnnotation[] docAnnos = new SectionAnnotation[numberofsections];
                        String[] docSecTexts = new String[numberofsections];

                        int j = 0;
                        for (Section sec : doc.getSections()) {
                            docAnnos[j] = sec.getAnnotation();
                            docSecTexts[j] = Jsoup.parse(sec.getTextWithWsSuffix()).text();
                            docBuilder.addSection(docSecTexts[j], docAnnos[j]);
                            j++;
                        }

                        // create new document and attach it to the output cell

                        DataCellCache dataCellCache = getDataCellCache();

                        outCell = dataCellCache.getInstance(docBuilder.createDocument());

                    } else if (row.getCell(i).getType().isCompatible(StringValue.class)) {
                        String value = ((StringValue)row.getCell(i)).getStringValue();
                        value = Jsoup.parse(value).text();

                        outCell = StringCellFactory.create(value);
                    } else {
                        outCell = row.getCell(i);
                    }
                }
                newCells[nextIndexToFilter++] = outCell;
            }
        }
        return newCells;

    }

}

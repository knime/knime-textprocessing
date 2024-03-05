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
 *   05.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnProperties;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.proxy.URLConnectionFactory;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;
import org.xml.sax.InputSource;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;

/**
 * The {@code RSSFeedReaderDataTableCreator2} creates the DataCells from the URLs. It is also used as joiner class to
 * collect all {@code FeedReaderResult2}s.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.4
 */
class RSSFeedReaderDataTableCreator2 {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(RSSFeedReaderDataTableCreator2.class);

    private BufferedDataContainer m_dataContainer;

    private int m_timeOut;

    private long m_rowCount;

    private boolean m_createDocCol;

    private boolean m_createXMLCol;

    private boolean m_createHttpColumn;

    private String m_docColName;

    private String m_xmlColName;

    private String m_httpColName;

    private String m_tokenizerName;

    private final List<FeedReaderResult2> m_feedReaderResults = new LinkedList<FeedReaderResult2>();

    private AtomicLong m_missingRowCount = new AtomicLong();

    private int m_httpCode = -2;

    /**
     * Creates a new instance of the RSSFeedReaderDataTableCreator2.
     *
     * @param createDocumentColumn Set true, if an additional Document column should be created.
     * @param createXMLColumn Set true, if an additional XML column should be created.
     */
    RSSFeedReaderDataTableCreator2(final boolean createDocumentColumn, final boolean createXMLColumn,
        final boolean createHttpColumn, final int timeOut, final String docColName, final String xmlColName,
        final String httpColName, final String tokenizerName) {
        m_createDocCol = createDocumentColumn;
        m_createXMLCol = createXMLColumn;
        m_timeOut = timeOut;
        m_createHttpColumn = createHttpColumn;
        m_docColName = docColName;
        m_xmlColName = xmlColName;
        m_httpColName = httpColName;
        m_tokenizerName = tokenizerName;
    }

    /**
     * @param urlAsString The url.
     * @param timeOut The time in ms until the connection times out.
     */
    void createDataCellsFromUrl(final DataCell inputCell, final FileStoreFactory fileStoreFactory) {
        if (!inputCell.isMissing()) {
            String urlAsString = ((StringValue)inputCell).getStringValue();
            FeedReaderResult2 result = new FeedReaderResult2(urlAsString, m_createDocCol, m_createXMLCol,
                m_createHttpColumn, m_tokenizerName, fileStoreFactory);
            SyndFeed feed = null;

            try {
                SyndFeedInput feedInput = new SyndFeedInput();
                URL url = new URL(urlAsString);
                LOGGER.debug("Connect to " + urlAsString + " or load file.");
                URLConnection conn = URLConnectionFactory.getConnection(url);
                conn.setConnectTimeout(m_timeOut);
                conn.setReadTimeout(m_timeOut);

                try (InputStream is = conn.getInputStream()) {
                    feed = feedInput.build(new InputSource(is));
                } catch (FeedException e) {
                    LOGGER.warn(
                        "Unknown feed type for URL " + urlAsString + " feed could not be parsed: " + e.getMessage(), e);
                }
                if (conn instanceof HttpURLConnection) {
                    m_httpCode = ((HttpURLConnection)conn).getResponseCode();
                    result.setHttpCode(m_httpCode);
                }
            } catch (MalformedURLException e) {
                LOGGER.warn(urlAsString + " is not a valid URL: " + e.getMessage());
            } catch (IOException e) {
                LOGGER.warn("Could not read from URL '" + urlAsString + "': " + e.getMessage(), e);
            }

            result.setResults(feed);
            result.createListOfDataCellsFromResults();
            m_feedReaderResults.add(result);
        } else {
            m_missingRowCount.incrementAndGet();
            FeedReaderResult2 result = new FeedReaderResult2(null, m_createDocCol, m_createXMLCol, m_createHttpColumn,
                m_tokenizerName, fileStoreFactory);
            result.setResults(null);
            result.createListOfDataCellsFromResults();
            m_feedReaderResults.add(result);
        }
    }

    /**
     * @return Returns the map containing the urls and the DataCells belonging to the urls.
     */
    private List<FeedReaderResult2> getResults() {
        return m_feedReaderResults;
    }

    long getMissingRowCount() {
        return m_missingRowCount.longValue();
    }

    /**
     * @param exec The {@code ExecutionContext}.
     */
    private synchronized void openDataContainer(final ExecutionContext exec) {
        if (m_dataContainer == null) {
            m_dataContainer = exec.createDataContainer(createDataTableSpec());
        }
    }

    /**
     * This method is used to join the results which have been created while processing multiple chunks.
     *
     * @param rssFeedReaderCreator The {@code RSSFeedReaderDataTableCreator2} that stores the results from processing
     *            one chunk.
     * @param exec The {@code ExecutionContext}.
     */
    synchronized void joinResults(final RSSFeedReaderDataTableCreator2 rssFeedReaderCreator,
        final ExecutionContext exec) {
        openDataContainer(exec);
        addResultsToDataContainer(rssFeedReaderCreator.getResults());
        m_missingRowCount.addAndGet(rssFeedReaderCreator.getMissingRowCount());
    }

    /**
     * This method adds the results created in an instance of {@code RSSFeedReaderDataTableCreator2} to the
     * DataContainer.
     *
     * @param url The feed url.
     * @param cells The array of DataCells belonging to the url.
     */
    private synchronized void addResultsToDataContainer(final List<FeedReaderResult2> results) {
        for (FeedReaderResult2 result : results) {
            List<DataCell[]> resultCells = result.createListOfDataCellsFromResults();
            for (DataCell[] dataCells : resultCells) {
                final RowKey key = RowKey.createRowKey(m_rowCount);
                final DataRow newRow = new DefaultRow(key, dataCells);
                m_dataContainer.addRowToTable(newRow);
                m_rowCount++;
            }
        }
    }

    /**
     * @param exec The {@code ExecutionContext}
     * @return Returns the {@code BufferedDataTable} containing the results from the {@code DataContainer}.
     */
    synchronized BufferedDataTable createDataTable(final ExecutionContext exec) {
        openDataContainer(exec);
        m_dataContainer.close();
        return m_dataContainer.getTable();
    }

    /**
     * @return Returns the {@code DataTableSpec} for the output table.
     */
    DataTableSpec createDataTableSpec() {
        List<DataColumnSpec> outputColSpecs = new ArrayList<>();

        Map<String, String> props = new LinkedHashMap<String, String>();
        props.put(DocumentDataTableBuilder.WORD_TOKENIZER_KEY, m_tokenizerName);
        DataColumnSpecCreator docSpecCreator = new DataColumnSpecCreator(m_docColName, DocumentCell.TYPE);
        docSpecCreator.setProperties(new DataColumnProperties(props));

        outputColSpecs.add(new DataColumnSpecCreator("Feed Url", StringCell.TYPE).createSpec());
        outputColSpecs.add(new DataColumnSpecCreator("Title", StringCell.TYPE).createSpec());
        outputColSpecs.add(new DataColumnSpecCreator("Description", StringCell.TYPE).createSpec());
        outputColSpecs.add(new DataColumnSpecCreator("Published", LocalDateTimeCellFactory.TYPE).createSpec());
        outputColSpecs.add(new DataColumnSpecCreator("Item Url", StringCell.TYPE).createSpec());

        if (m_createDocCol) {
            outputColSpecs.add(docSpecCreator.createSpec());
        }
        if (m_createXMLCol) {
            outputColSpecs.add(new DataColumnSpecCreator(m_xmlColName, XMLCell.TYPE).createSpec());
        }
        if (m_createHttpColumn) {
            outputColSpecs.add(new DataColumnSpecCreator(m_httpColName, IntCell.TYPE).createSpec());
        }

        return new DataTableSpec(outputColSpecs.toArray(new DataColumnSpec[outputColSpecs.size()]));
    }
}

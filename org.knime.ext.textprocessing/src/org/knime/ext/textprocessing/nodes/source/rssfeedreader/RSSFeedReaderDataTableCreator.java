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
 *   05.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.data.DocumentCell;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class RSSFeedReaderDataTableCreator {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(RSSFeedReaderDataTableCreator.class);

    private BufferedDataContainer m_dataContainer;

    private int m_timeOut;

    private long m_rowCount;

    private boolean m_createDocCol;

    private boolean m_createXMLCol;

    private boolean m_createHttpColumn;

    private final List<FeedReaderResult> m_feedReaderResults = new LinkedList<FeedReaderResult>();

    private AtomicInteger m_missingRowCount = new AtomicInteger(0);

    private int m_httpCode = -2;

    /**
     * Creates a new instance of the RSSFeedReaderDataTableCreator.
     *
     * @param createDocumentColumn Set true, if an additional Document column should be created.
     * @param createXMLColumn Set true, if an additional XML column should be created.
     */
    RSSFeedReaderDataTableCreator(final boolean createDocumentColumn, final boolean createXMLColumn,
        final boolean createHttpColumn, final int timeOut) {
        m_createDocCol = createDocumentColumn;
        m_createXMLCol = createXMLColumn;
        m_timeOut = timeOut;
        m_createHttpColumn = createHttpColumn;
    }

    /**
     * @param urlAsString The url.
     * @param timeOut The time in ms until the connection times out.
     */
    void createDataCellsFromUrl(final DataCell inputCell) {
        if (!inputCell.isMissing()) {
            String urlAsString = ((StringValue)inputCell).getStringValue();
            FeedReaderResult result =
                new FeedReaderResult(urlAsString, m_createDocCol, m_createXMLCol, m_createHttpColumn);
            URL url = null;
            SyndFeedInput feedInput = new SyndFeedInput();
            SyndFeed feed = null;
            boolean isLocal = true;
            InputStreamReader isr = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                LOGGER.warn("Could not create URL from given string: " + urlAsString);
            }
            LOGGER.debug("Connect to " + urlAsString + " or load file.");
            //Reading the feeds
            try {
                // try to read local URL which could also include knime:// protocol. This will fail for remote
                // http:// URLs.
                isr = new InputStreamReader(new FileInputStream(FileUtil.getFileFromURL(url)), "UTF-8");
                feed = feedInput.build(isr);
            } catch (IllegalArgumentException e) {
                // URL is not a local URL. URL will be red again.
                isLocal = false;
            } catch (IOException e) {
                LOGGER.warn("Could not read from URL: " + urlAsString);
            } catch (FeedException e) {
                LOGGER.warn("Unknown feed type for URL, feed could not be parsed: " + urlAsString);
            } finally {
                if (isr != null) {
                    try {
                        isr.close();
                    } catch (IOException e) {
                        LOGGER.debug("Input stream reader could not be closed.");
                    }
                }
            }
            if (!isLocal && url != null) {
                try {
                    // try to read remote URL
                    HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
                    httpCon.setConnectTimeout(m_timeOut);
                    httpCon.setReadTimeout(m_timeOut);
                    m_httpCode = httpCon.getResponseCode();
                    result.setHttpCode(m_httpCode);
                    feed = feedInput.build(new XmlReader(httpCon));
                } catch (IOException e) {
                    LOGGER.warn("Could not open connection to " + urlAsString);
                } catch (IllegalArgumentException | FeedException e) {
                    LOGGER.warn("Unknown feed type for URL: " + urlAsString);
                }
            }
            result.setResults(feed);
            result.createListOfDataCellsFromResults();
            m_feedReaderResults.add(result);
        } else {
            m_missingRowCount.addAndGet(1);
            FeedReaderResult result = new FeedReaderResult(null, m_createDocCol, m_createXMLCol, m_createHttpColumn);
            SyndFeed feed = null;
            result.setResults(feed);
            result.createListOfDataCellsFromResults();
            m_feedReaderResults.add(result);
        }
    }

    /**
     * @return Returns the map containing the urls and the DataCells belonging to the urls.
     */
    private List<FeedReaderResult> getResults() {
        return m_feedReaderResults;
    }

    int getMissingRowCount() {
        return m_missingRowCount.intValue();
    }

    /**
     * @param exec The {@code ExecutionContext}.
     */
    private synchronized void openDataContainer(final ExecutionContext exec) {
        if (exec != null) {
            if (m_dataContainer == null) {
                m_dataContainer = exec.createDataContainer(createDataTableSpec());
            }
        }
    }

    /**
     * This method is used to join the results which have been created while processing multiple chunks.
     *
     * @param rssFeedReaderCreator The {@code RSSFeedReaderDataTableCreator} that stores the results from processing one
     *            chunk.
     * @param exec The {@code ExecutionContext}.
     */
    synchronized void joinResults(final RSSFeedReaderDataTableCreator rssFeedReaderCreator,
        final ExecutionContext exec) {
        if (rssFeedReaderCreator != null && exec != null) {
            openDataContainer(exec);
            addResultsToDataContainer(rssFeedReaderCreator.getResults());
            m_missingRowCount.addAndGet(rssFeedReaderCreator.getMissingRowCount());
        }
    }

    /**
     * This method adds the results created in an instance of {@code RSSFeedReaderDataTableCreator} to the
     * DataContainer.
     *
     * @param url The feed url.
     * @param cells The array of DataCells belonging to the url.
     */
    private synchronized void addResultsToDataContainer(final List<FeedReaderResult> results) {
        if (m_dataContainer != null && m_dataContainer.isOpen()) {
            for (FeedReaderResult result : results) {
                List<DataCell[]> resultCells = result.createListOfDataCellsFromResults();
                for (DataCell[] dataCells : resultCells) {
                    final RowKey key = RowKey.createRowKey(m_rowCount);
                    final DataRow newRow = new DefaultRow(key, dataCells);
                    m_dataContainer.addRowToTable(newRow);
                    m_rowCount++;
                }
            }
        }
    }

    /**
     * @param exec
     * @return Returns the {@code BufferedDataTable} containing the results from the {@code DataContainer}.
     */
    synchronized BufferedDataTable createDataTable(final ExecutionContext exec) {
        openDataContainer(exec);

        if (m_dataContainer != null && m_dataContainer.isOpen()) {
            m_dataContainer.close();
            return m_dataContainer.getTable();
        }
        return null;
    }

    /**
     * @return Returns the {@code DataTableSpec} for the output table.
     */
    DataTableSpec createDataTableSpec() {

        DataColumnSpec[] outputColSpecs;

        if (!m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
            outputColSpecs = new DataColumnSpec[5];
        } else if (m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
            outputColSpecs = new DataColumnSpec[6];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
        } else if (!m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
            outputColSpecs = new DataColumnSpec[6];
            outputColSpecs[5] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
        } else if (!m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
            outputColSpecs = new DataColumnSpec[6];
            outputColSpecs[5] = new DataColumnSpecCreator("HTTP Response Code", IntCell.TYPE).createSpec();
        } else if ((m_createDocCol && m_createXMLCol && !m_createHttpColumn)) {
            outputColSpecs = new DataColumnSpec[7];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
            outputColSpecs[6] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
        } else if ((m_createDocCol && !m_createXMLCol && m_createHttpColumn)) {
            outputColSpecs = new DataColumnSpec[7];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
            outputColSpecs[6] = new DataColumnSpecCreator("HTTP Response Code", IntCell.TYPE).createSpec();
        } else if ((!m_createDocCol && m_createXMLCol && m_createHttpColumn)) {
            outputColSpecs = new DataColumnSpec[7];
            outputColSpecs[5] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
            outputColSpecs[6] = new DataColumnSpecCreator("HTTP Response Code", IntCell.TYPE).createSpec();
        } else {
            outputColSpecs = new DataColumnSpec[8];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
            outputColSpecs[6] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
            outputColSpecs[7] = new DataColumnSpecCreator("HTTP Response Code", IntCell.TYPE).createSpec();
        }

        outputColSpecs[0] = new DataColumnSpecCreator("Feed Url", StringCell.TYPE).createSpec();
        outputColSpecs[1] = new DataColumnSpecCreator("Title", StringCell.TYPE).createSpec();
        outputColSpecs[2] = new DataColumnSpecCreator("Description", StringCell.TYPE).createSpec();
        outputColSpecs[3] = new DataColumnSpecCreator("Published", DateAndTimeCell.TYPE).createSpec();
        outputColSpecs[4] = new DataColumnSpecCreator("Item Url", StringCell.TYPE).createSpec();

        return new DataTableSpec(outputColSpecs);
    }
}

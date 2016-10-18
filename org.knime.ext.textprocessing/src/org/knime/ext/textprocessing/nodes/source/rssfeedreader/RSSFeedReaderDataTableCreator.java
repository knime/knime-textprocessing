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
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.xml.sax.SAXException;

import com.beust.jcommander.internal.Lists;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
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
        FeedReaderResult result = new FeedReaderResult();
        if (!inputCell.isMissing()) {
            String urlAsString = ((StringValue)inputCell).getStringValue();
            result.setURL(urlAsString);
            URL url = null;
            try {
                url = new URL(urlAsString);
            } catch (MalformedURLException e) {
                LOGGER.warn("MalformedURLException: Could not create URL from given string: " + urlAsString);
                result.addDataCells(createMissingValueCells());
                m_feedReaderResults.add(result);
            }
            LOGGER.debug("Connect to " + urlAsString + " or load file.");
            //Reading the feeds
            SyndFeedInput feedInput = new SyndFeedInput();
            SyndFeed feed = null;
            boolean isLocal = true;
            try {
                // try to read local URL which could also include knime:// protocol. This will fail for remote
                // http:// URLs.
                InputStreamReader isr =
                    new InputStreamReader(new FileInputStream(FileUtil.getFileFromURL(url)), "UTF-8");
                feed = feedInput.build(isr);
                isr.close();
            } catch (IllegalArgumentException e) {
                isLocal = false;
            } catch (IOException e) {
                LOGGER.warn("IOException: Could not connect to URL / Could not find file: " + urlAsString);
                result.addDataCells(createMissingValueCells());
                m_feedReaderResults.add(result);
            } catch (FeedException e) {
                LOGGER.warn("FeedException: Unknown feed type for URL: " + urlAsString);
                result.addDataCells(createMissingValueCells());
                m_feedReaderResults.add(result);
            }
            if (!isLocal && url != null) {
                try {
                    // try to read remote URL
                    HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
                    httpCon.setConnectTimeout(m_timeOut);
                    httpCon.setReadTimeout(m_timeOut);
                    m_httpCode = httpCon.getResponseCode();
                    feed = feedInput.build(new XmlReader(httpCon));
                } catch (IOException e) {
                    LOGGER.warn("IOException: Could not open connection to " + urlAsString);
                    result.addDataCells(createMissingValueCells());
                    m_feedReaderResults.add(result);
                } catch (IllegalArgumentException | FeedException e) {
                    LOGGER.warn("IllegalArgumentException/FeedException: Unknown feed type for URL: " + urlAsString);
                    result.addDataCells(createMissingValueCells());
                    m_feedReaderResults.add(result);
                }
            }
            if (feed != null) {
                @SuppressWarnings("unchecked")
                List<SyndEntry> entries = feed.getEntries();
                Iterator<SyndEntry> itEntries = entries.iterator();

                // read entries and fill cells with information
                while (itEntries.hasNext()) {
                    SyndEntry entry = itEntries.next();
                    DataCell[] cells;
                    // check if xml or doc columns should be created and generate cell content if true
                    if (!m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
                        cells = new DataCell[5];
                        Arrays.fill(cells, DataType.getMissingCell());
                    } else if (m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
                        cells = new DataCell[6];
                        Arrays.fill(cells, DataType.getMissingCell());
                        Document doc = createDocumentFromEntry(entry);
                        if (doc != null) {
                            cells[5] = new DocumentCell(doc);
                        } else {
                            cells[5] = DataType.getMissingCell();
                        }
                    } else if (!m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
                        cells = new DataCell[6];
                        Arrays.fill(cells, DataType.getMissingCell());
                        cells[5] = createXMLContentFromEntry(entry, feed);
                    } else if (!m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
                        cells = new DataCell[6];
                        Arrays.fill(cells, DataType.getMissingCell());
                        if (m_httpCode != -2) {
                            cells[5] = new IntCell(m_httpCode);
                        }
                    } else if (m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
                        cells = new DataCell[7];
                        Arrays.fill(cells, DataType.getMissingCell());
                        Document doc = createDocumentFromEntry(entry);
                        if (doc != null) {
                            cells[5] = new DocumentCell(doc);
                        } else {
                            cells[5] = DataType.getMissingCell();
                        }
                        cells[6] = createXMLContentFromEntry(entry, feed);
                    } else if (m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
                        cells = new DataCell[7];
                        Arrays.fill(cells, DataType.getMissingCell());
                        Document doc = createDocumentFromEntry(entry);
                        if (doc != null) {
                            cells[5] = new DocumentCell(doc);
                        } else {
                            cells[5] = DataType.getMissingCell();
                        }
                        if (m_httpCode != -2) {
                            cells[6] = new IntCell(m_httpCode);
                        }
                    } else if (!m_createDocCol && m_createXMLCol && m_createHttpColumn) {
                        cells = new DataCell[7];
                        Arrays.fill(cells, DataType.getMissingCell());
                        cells[5] = createXMLContentFromEntry(entry, feed);
                        if (m_httpCode != -2) {
                            cells[6] = new IntCell(m_httpCode);
                        }
                    } else {
                        cells = new DataCell[8];
                        Arrays.fill(cells, DataType.getMissingCell());
                        Document doc = createDocumentFromEntry(entry);
                        if (doc != null) {
                            cells[5] = new DocumentCell(doc);
                        } else {
                            cells[5] = DataType.getMissingCell();
                        }
                        cells[6] = createXMLContentFromEntry(entry, feed);
                        if (m_httpCode != -2) {
                            cells[7] = new IntCell(m_httpCode);
                        }
                    }

                    // generate basic cell content
                    cells[0] = new StringCell(urlAsString);

                    if (entry.getTitle() != null) {
                        cells[1] = new StringCell(entry.getTitle());

                    }
                    if (entry.getDescription() != null) {
                        cells[2] = new StringCell(entry.getDescription().getValue());
                    }
                    if (entry.getPublishedDate() != null) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(entry.getPublishedDate());
                        cells[3] = new DateAndTimeCell(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                            cal.get(Calendar.SECOND));
                    }
                    if (entry.getLink() != null) {
                        cells[4] = new StringCell(entry.getLink());
                    }
                    result.addDataCells(cells);
                }
                m_feedReaderResults.add(result);
            }
        } else {
            m_missingRowCount.addAndGet(1);
            result.addDataCells(createMissingValueCells());
            m_feedReaderResults.add(result);
        }

    }

    // builds a document based on the feed entry
    private Document createDocumentFromEntry(final SyndEntry entry) {
        DocumentBuilder docBuilder = new DocumentBuilder();

        // initialize empty document
        docBuilder.addTitle(null);
        docBuilder.setPublicationDate(null);
        docBuilder.addDocumentSource(null);

        // add information to document builder
        if (entry.getTitle() != null) {
            docBuilder.addTitle(entry.getTitle());
        }
        if (entry.getDescription() != null) {
            docBuilder.addSection(entry.getDescription().getValue(), SectionAnnotation.ABSTRACT);
        }
        if (entry.getPublishedDate() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(entry.getPublishedDate());
            PublicationDate pubDate;
            try {
                pubDate = PublicationDate.createPublicationDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH));
                docBuilder.setPublicationDate(pubDate);
            } catch (ParseException e) {
                LOGGER.warn("Parse Exception: Could not parse date from feed entry " + entry.getLink());
            }
        }
        if (entry.getLink() != null) {
            docBuilder.addDocumentSource(new DocumentSource(entry.getLink()));
        }
        return docBuilder.createDocument();

    }

    // builds xmlcontent based on the feed and feed entry information
    private DataCell createXMLContentFromEntry(final SyndEntry entry, final SyndFeed feed) {

        SyndFeedOutput feedoutput = new SyndFeedOutput();
        List<SyndEntry> entries = Lists.newArrayList();
        entries.add(entry);
        feed.setEntries(entries);
        StringWriter xmlWriter = new StringWriter();
        try {
            feedoutput.output(feed, xmlWriter);
        } catch (IOException e) {
            LOGGER.warn("IOException: Could not write feed entry (" + entry.getLink() + ") to StringWriter");
        } catch (FeedException e) {
            LOGGER.warn("FeedException: Could not create XML representation for feed entry: " + entry.getLink(), e);
        }
        String xmlWithFeedInfo = xmlWriter.toString();
        try {
            return XMLCellFactory.create(xmlWithFeedInfo);
        } catch (IOException e) {
            LOGGER.warn("IOException: Could not read XML String for feed entry " + entry.getLink());
            return DataType.getMissingCell();
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.warn("ParserConfigurationException/SAXException: Could not parse XML to create content for XMLCell ("
                + entry.getLink() + ")");
            return DataType.getMissingCell();
        } catch (XMLStreamException e) {
            return DataType.getMissingCell();
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
                List<DataCell[]> resultCells = result.getDataCells();
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
     * @param urlAsString The feed url.
     * @return Returns a map containing missing value cells.
     */
    DataCell[] createMissingValueCells() {
        DataCell[] cells;
        if (!m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[5];
            Arrays.fill(cells, DataType.getMissingCell());
        } else if ((m_createDocCol && !m_createXMLCol && !m_createHttpColumn)
            || (!m_createDocCol && m_createXMLCol && !m_createHttpColumn)) {
            cells = new DataCell[6];
            Arrays.fill(cells, DataType.getMissingCell());
        } else if (!m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
            cells = new DataCell[6];
            Arrays.fill(cells, DataType.getMissingCell());
            if (m_httpCode != -2) {
                cells[5] = new IntCell(m_httpCode);
            }
        } else if (m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[7];
            Arrays.fill(cells, DataType.getMissingCell());
        } else if ((m_createDocCol && !m_createXMLCol && m_createHttpColumn)
            || (!m_createDocCol && m_createXMLCol && m_createHttpColumn)) {
            cells = new DataCell[7];
            Arrays.fill(cells, DataType.getMissingCell());
            if (m_httpCode != -2) {
                cells[6] = new IntCell(m_httpCode);
            }
        } else {
            cells = new DataCell[8];
            Arrays.fill(cells, DataType.getMissingCell());
            if (m_httpCode != -2) {
                cells[7] = new IntCell(m_httpCode);
            }
        }
        return cells;
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

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
 *   04.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
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
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
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
public class RSSFeedReaderNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(RSSFeedReaderNodeModel.class);

    static final Boolean DEF_CREATE_DOC_COLUMN = false;

    static final Boolean DEF_CREATE_XML_COLUMN = false;

    private SettingsModelString m_feedUrlColumn = RSSFeedReaderNodeDialog.createFeedUrlColumnModel();

    private SettingsModelBoolean m_createDocColumn = RSSFeedReaderNodeDialog.createDocumentColumnModel();

    private SettingsModelBoolean m_createXMLColumn = RSSFeedReaderNodeDialog.createXMLColumnModel();

    private String m_feedUrlColumnName;

    /**
     * @param nrInDataPorts
     * @param nrOutDataPorts
     */
    protected RSSFeedReaderNodeModel() {
        super(1, 1);
    }

    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        BufferedDataTable inputTable = inData[0];
        DataTableSpec inputTableSpec = inputTable.getSpec();

        DataTableSpec outputSpec = createOutputSpec();
        BufferedDataContainer buf = exec.createDataContainer(outputSpec);

        long rowCount = inputTable.size();
        long currentOutputRow = 0;

        // iterate through columns
        for (int i = 0; i < inputTableSpec.getNumColumns(); i++) {
            // iterate through rows if column with correct name has been found
            if (inputTableSpec.getColumnSpec(i).getName().equals(m_feedUrlColumnName)) {
                for (DataRow row : inputTable) {
                    String urlAsString = row.getCell(i).toString();
                    URL url = new URL(urlAsString);
                    HttpURLConnection httpCon = (HttpURLConnection)url.openConnection();
                    // Reading the feed
                    SyndFeedInput feedInput = new SyndFeedInput();
                    SyndFeed feed = feedInput.build(new XmlReader(httpCon));
                    List<SyndEntry> entries = feed.getEntries();
                    Iterator<SyndEntry> itEntries = entries.iterator();

                    while (itEntries.hasNext()) {
                        SyndEntry entry = itEntries.next();
                        RowKey key = RowKey.createRowKey(currentOutputRow++);
                        DataCell[] cells = new DataCell[5];
                        Arrays.fill(cells, DataType.getMissingCell());

                        if (m_createDocColumn.getBooleanValue() && !m_createXMLColumn.getBooleanValue()) {
                            cells = new DataCell[6];
                            cells[5] = new DocumentCell(createDocumentFromEntry(entry));
                        }
                        if (!m_createDocColumn.getBooleanValue() && m_createXMLColumn.getBooleanValue()) {
                            cells = new DataCell[6];
                            cells[5] = createXMLContentFromEntry(entry, feed);
                        }
                        if (m_createDocColumn.getBooleanValue() && m_createXMLColumn.getBooleanValue()) {
                            cells = new DataCell[7];
                            cells[5] = new DocumentCell(createDocumentFromEntry(entry));
                            cells[6] = createXMLContentFromEntry(entry, feed);
                            //COMBINATION FROM ABOVE
                        }

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

                        DataRow outputRow = new DefaultRow(key, cells);
                        buf.addRowToTable(outputRow);
                    }

                    exec.checkCanceled();
                    exec.setProgress((double)i / rowCount, "Processing URL: " + i);
                }
            }
        }

        buf.close();
        return new BufferedDataTable[]{buf.getTable()};
    }

    private Document createDocumentFromEntry(final SyndEntry entry) throws ParseException {
        DocumentBuilder docBuilder = new DocumentBuilder();

        docBuilder.addTitle(entry.getTitle());
        docBuilder.addSection(entry.getDescription().getValue(), SectionAnnotation.ABSTRACT);
        Calendar cal = Calendar.getInstance();
        cal.setTime(entry.getPublishedDate());
        PublicationDate pubDate = PublicationDate.createPublicationDate(cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        docBuilder.setPublicationDate(pubDate);
        docBuilder.addDocumentSource(new DocumentSource(entry.getLink()));

        return docBuilder.createDocument();
    }

    private DataCell createXMLContentFromEntry(final SyndEntry entry, final SyndFeed feed)
        throws IOException, ParserConfigurationException, SAXException, XMLStreamException, FeedException {

        SyndFeedOutput feedoutput = new SyndFeedOutput();
        List<SyndEntry> entries = Lists.newArrayList();
        entries.add(entry);
        feed.setEntries(entries);
        StringWriter xmlWriter = new StringWriter();
        feedoutput.output(feed, xmlWriter);
        String xmlWithFeedInfo = xmlWriter.toString();
        StringBuilder xmlWithoutFeedInfoBuilder = new StringBuilder();
        if (feed.getFeedType().contains("atom")) {
            xmlWithoutFeedInfoBuilder.append("<entry>");
            xmlWithoutFeedInfoBuilder.append(StringUtils.substringBetween(xmlWithFeedInfo, "<entry>", "</entry>"));
            xmlWithoutFeedInfoBuilder.append("</entry>");
        } else if (feed.getFeedType().contains("rss")) {
            xmlWithoutFeedInfoBuilder.append("<channel>");
            xmlWithoutFeedInfoBuilder.append(StringUtils.substringBetween(xmlWithFeedInfo, "<channel>", "</channel>"));
            xmlWithoutFeedInfoBuilder.append("</channel>");
        }
        return XMLCellFactory.create(xmlWithFeedInfo);
    }

    private DataTableSpec createOutputSpec() {

        DataColumnSpec[] outputColSpecs = new DataColumnSpec[5];

        if (!m_createDocColumn.getBooleanValue() && m_createXMLColumn.getBooleanValue()) {
            outputColSpecs = new DataColumnSpec[6];
            outputColSpecs[5] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
        }
        if (m_createDocColumn.getBooleanValue() && !m_createXMLColumn.getBooleanValue()) {
            outputColSpecs = new DataColumnSpec[6];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
        }
        if (m_createDocColumn.getBooleanValue() && m_createXMLColumn.getBooleanValue()) {
            outputColSpecs = new DataColumnSpec[7];
            outputColSpecs[5] = new DataColumnSpecCreator("Document", DocumentCell.TYPE).createSpec();
            outputColSpecs[6] = new DataColumnSpecCreator("XML", XMLCell.TYPE).createSpec();
        }

        outputColSpecs[0] = new DataColumnSpecCreator("feedUrl", StringCell.TYPE).createSpec();
        outputColSpecs[1] = new DataColumnSpecCreator("title", StringCell.TYPE).createSpec();
        outputColSpecs[2] = new DataColumnSpecCreator("description", StringCell.TYPE).createSpec();
        outputColSpecs[3] = new DataColumnSpecCreator("published", DateAndTimeCell.TYPE).createSpec();
        outputColSpecs[4] = new DataColumnSpecCreator("itemUrl", StringCell.TYPE).createSpec();

        return new DataTableSpec(outputColSpecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        // checking specified string column and guessing first available string column if no column is set.
        DataTableSpec inSpec = inSpecs[0];
        int colIndex = inSpec.findColumnIndex(m_feedUrlColumn.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < inSpec.getNumColumns(); i++) {
                if (inSpec.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    m_feedUrlColumnName = inSpec.getColumnSpec(i).getName();
                    break;
                }
            }
        } else if (colIndex >= 0) {
            m_feedUrlColumnName = m_feedUrlColumn.getStringValue();
        }

        return new DataTableSpec[]{createOutputSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_feedUrlColumn.saveSettingsTo(settings);
        m_createDocColumn.saveSettingsTo(settings);
        m_createXMLColumn.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_feedUrlColumn.validateSettings(settings);
        m_createDocColumn.validateSettings(settings);
        m_createXMLColumn.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_feedUrlColumn.loadSettingsFrom(settings);
        m_createDocColumn.loadSettingsFrom(settings);
        m_createXMLColumn.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }

}

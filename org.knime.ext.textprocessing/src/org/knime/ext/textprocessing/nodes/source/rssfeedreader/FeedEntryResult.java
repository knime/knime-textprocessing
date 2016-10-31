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
 *   27.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataType;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.xml.XMLCellFactory;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.xml.sax.SAXException;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class FeedEntryResult {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(FeedEntryResult.class);

    private String m_feedURL = null;

    private String m_title = null;

    private String m_description = null;

    private Date m_publicationDate = null;

    private String m_itemUrl = null;

    private Document m_document = null;

    private String m_xml = null;

    private int m_responseCode = -2;

    private boolean m_createDocCol = RSSFeedReaderNodeModel.DEF_CREATE_DOC_COLUMN;

    private boolean m_createXMLCol = RSSFeedReaderNodeModel.DEF_CREATE_XML_COLUMN;

    private boolean m_createHttpColumn = RSSFeedReaderNodeModel.DEF_GET_HTTP_RESPONSE_CODE_COLUMN;

    /**
     * Creates a new instance of {@code FeedEntryResult}.
     */
    FeedEntryResult(final String feedUrl, final int responseCode, final boolean docCol, final boolean xmlCol,
        final boolean httpResponseCol) {
        m_feedURL = feedUrl;
        m_responseCode = responseCode;
        m_createDocCol = docCol;
        m_createHttpColumn = httpResponseCol;
        m_createXMLCol = xmlCol;
    }

    /**
     * @param entry The SyndEntry containing the entry information.
     * @param feed The SyndFeed containing the feed information.
     */
    void setEntry(final SyndEntry entry, final SyndFeed feed) {
        // basic entry content
        if (entry.getTitle() != null) {
            m_title = entry.getTitle();
        }
        if (entry.getDescription() != null) {
            m_description = entry.getDescription().getValue();
        }
        if (entry.getPublishedDate() != null) {
            m_publicationDate = entry.getPublishedDate();
        }
        if (entry.getLink() != null) {
            m_itemUrl = entry.getLink();
        }
        // complex entry content
        if (m_createDocCol) {
            m_document = createDocumentFromEntry(entry);
        }

        if (m_createXMLCol) {
            m_xml = createXMLContentFromEntry(entry, feed);
        }
    }

    /**
     * @return Returns a DataCell array containing the feed entry information.
     */
    DataCell[] createEntryResultasDataCell() {
        DataCell[] cells;

        if (!m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[5];
        } else if (m_createDocCol && !m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[6];
            cells[5] = createDocumentCell();
        } else if (!m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[6];
            cells[5] = createXmlCell();
        } else if (!m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
            cells = new DataCell[6];
            cells[5] = createIntCell();
        } else if (m_createDocCol && m_createXMLCol && !m_createHttpColumn) {
            cells = new DataCell[7];
            cells[5] = createDocumentCell();
            cells[6] = createXmlCell();
        } else if (m_createDocCol && !m_createXMLCol && m_createHttpColumn) {
            cells = new DataCell[7];
            cells[5] = createDocumentCell();
            cells[6] = createIntCell();
        } else if (!m_createDocCol && m_createXMLCol && m_createHttpColumn) {
            cells = new DataCell[7];
            cells[5] = createXmlCell();
            cells[6] = createIntCell();
        } else {
            cells = new DataCell[8];
            cells[5] = createDocumentCell();
            cells[6] = createXmlCell();
            cells[7] = createIntCell();
        }

        // basic cells;
        cells[0] = createStringCell(m_feedURL);
        cells[1] = createStringCell(m_title);
        cells[2] = createStringCell(m_description);
        cells[3] = createDateAndTimeCell();
        cells[4] = createStringCell(m_itemUrl);

        return cells;
    }

    private DataCell createStringCell(final String str) {
        DataCell cell = DataType.getMissingCell();
        if (str != null) {
            cell = new StringCell(str);
        }
        return cell;
    }

    private DataCell createDocumentCell() {
        DataCell cell = DataType.getMissingCell();
        if (m_document != null) {
            cell = new DocumentCell(m_document);
        }
        return cell;
    }

    private DataCell createXmlCell() {
        DataCell cell = DataType.getMissingCell();

        try {
            if (m_xml != null) {
                cell = XMLCellFactory.create(m_xml);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read XML String for feed entry " + m_itemUrl);
        } catch (ParserConfigurationException | SAXException e) {
            LOGGER.warn("Could not parse XML to create content for XMLCell (" + m_itemUrl + ")");
        } catch (XMLStreamException e) {
            LOGGER.warn("Could not parse XML to create content for XMLCell (" + m_itemUrl + ")");
        }

        return cell;
    }

    private DataCell createIntCell() {
        DataCell cell = DataType.getMissingCell();
        if (m_responseCode != -2) {
            cell = new IntCell(m_responseCode);
        }
        return cell;
    }

    private DataCell createDateAndTimeCell() {
        DataCell cell = DataType.getMissingCell();
        if (m_publicationDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(m_publicationDate);
            cell = new DateAndTimeCell(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
        }
        return cell;
    }

    // builds xmlcontent based on the feed and feed entry information
    private String createXMLContentFromEntry(final SyndEntry entry, final SyndFeed feed) {
        SyndFeedOutput feedoutput = new SyndFeedOutput();
        List<SyndEntry> entries = new LinkedList<>();
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
        return xmlWriter.toString();
    }

    // builds document based on the feed entry information
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
            docBuilder.addSection(entry.getDescription().getValue(), SectionAnnotation.CHAPTER);
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
}

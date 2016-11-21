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
 *   18.10.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.source.rssfeedreader;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.core.data.DataCell;
import org.knime.core.data.filestore.FileStoreFactory;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

/**
 * The {@code FeedReaderResult} collects a list of {@code FeedEntryResult}s containing all the results that could have been
 * parsed from the specific feed.
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class FeedReaderResult {

    private List<FeedEntryResult> m_entryResults = new LinkedList<>();

    private String m_url;

    private boolean m_createDocCol = RSSFeedReaderNodeModel.DEF_CREATE_DOC_COLUMN;

    private boolean m_createXMLCol = RSSFeedReaderNodeModel.DEF_CREATE_XML_COLUMN;

    private boolean m_createHttpColumn = RSSFeedReaderNodeModel.DEF_GET_HTTP_RESPONSE_CODE_COLUMN;

    private int m_responseCode = -2;

    private final FileStoreFactory m_fileStoreFactory;

    /**
     * Creates a new instance of {@code FeedReaderResult}.
     */
    FeedReaderResult(final String url, final boolean docCol, final boolean xmlCol, final boolean httpResponseCol,
        final FileStoreFactory fileStoreFactory) {
        m_url = url;
        m_createDocCol = docCol;
        m_createXMLCol = xmlCol;
        m_createHttpColumn = httpResponseCol;
        m_fileStoreFactory = fileStoreFactory;
    }

    /**
     * Set the Result based on SyndFeed.
     *
     * @param feedResults The feed containing the entries.
     */
    void setResults(final SyndFeed feedResults) {
        if ((feedResults != null)  && !feedResults.getEntries().isEmpty()) {
            // read entries and fill cells with information
            for (SyndEntry entry : feedResults.getEntries()) {
                FeedEntryResult entryResult =
                    new FeedEntryResult(m_url, m_responseCode, m_createDocCol, m_createXMLCol, m_createHttpColumn,
                        m_fileStoreFactory);
                entryResult.setEntry(entry, feedResults);
                entryResult.createEntryResultasDataCell();
                m_entryResults.add(entryResult);
            }
        } else {
            createEmptyFeedResults();
        }
    }

    /**
     * Creates empty result set that will result in one row for this feed, containing missing values.
     */
    private void createEmptyFeedResults() {
        FeedEntryResult entryResult =
            new FeedEntryResult(m_url, m_responseCode, m_createDocCol, m_createXMLCol, m_createHttpColumn,
                m_fileStoreFactory);
        entryResult.createEntryResultasDataCell();
        m_entryResults.add(entryResult);
    }

    /**
     * @return Returns a list of DataCell arrays containing the information for every feed entry.
     */
    List<DataCell[]> createListOfDataCellsFromResults() {
        return m_entryResults.stream().map(fer -> fer.createEntryResultasDataCell()).collect(Collectors.toList());
    }

    /**
     * Set the HTTP response code for the FeedReaderResult.
     * @param responseCode The HTTP response code.
     */
    void setHttpCode(final int responseCode) {
        m_responseCode = responseCode;
    }
}

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

import org.knime.core.data.DataCell;
import org.knime.core.data.def.StringCell;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class FeedReaderResult {

    private List<DataCell[]> m_dataCells = new LinkedList<DataCell[]>();

    private String m_url;

    /**
     * Creates a new instance of {@code FeedReaderResult}.
     */
    FeedReaderResult() {
        // empty constructor
    }

    /**
     * Sets the URL for the current instance of FeedReaderResult.
     * @param urlAsString The url to set.
     */
    void setURL(final String urlAsString) {
        m_url = urlAsString;
    }

    /**
     * @return URL from the current instance of FeedReaderResult.
     */
    String getURL() {
        return m_url;
    }

    /**
     * Adds a DataCell array to the current instance of FeedReaderResult.
     * Since this method changes the first entry of the incoming DataCell array to
     * a StringCell containing the URL that has been set to this instance of FeedReaderResult. So, the URL should be
     * set before using this method (unless you want to provide a row with missing cells even in the URL column).
     * @param dataCells The DataCells containing the information generated from feed entries.
     */
    void addDataCells(final DataCell[] dataCells) {
        if (m_url != null) {
            dataCells[0] = new StringCell(m_url);
        }
        m_dataCells.add(dataCells);
    }

    /**
     * @return Returns the list of DataCell arrays containing the row information for feed entries of the current
     * instance of FeedReaderResult.
     */
    List<DataCell[]> getDataCells() {
        return m_dataCells;
    }
}

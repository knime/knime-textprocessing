/*******************************************************************************
 * Copyright by KNIME AG, Zurich, Switzerland
 * Website: http://www.knime.com; Email: contact@knime.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, Version 3, as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7:
 *
 * KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 * Hence, KNIME and ECLIPSE are both independent programs and are not
 * derived from each other. Should, however, the interpretation of the
 * GNU GPL Version 3 ("License") under any applicable laws result in
 * KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 * you the additional permission to use and propagate KNIME together with
 * ECLIPSE with only the license terms in place for ECLIPSE applying to
 * ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 * license terms of ECLIPSE themselves allow for the respective use and
 * propagation of ECLIPSE together with KNIME.
 *
 * Additional permission relating to nodes for KNIME that extend the Node
 * Extension (and in particular that are based on subclasses of NodeModel,
 * NodeDialog, and NodeView) and that only interoperate with KNIME through
 * standard APIs ("Nodes"):
 * Nodes are deemed to be separate and independent programs and to not be
 * covered works.  Notwithstanding anything to the contrary in the
 * License, the License does not apply to Nodes, you are not required to
 * license Nodes under the License, and you are granted a license to
 * prepare and propagate Nodes, in each case even if such Nodes are
 * propagated with or for interoperation with KNIME.  The owner of a Node
 * may freely choose the license terms applicable to such Node, including
 * when such Node is propagated with or for interoperation with KNIME.
 *******************************************************************************/
package org.knime.ext.textprocessing.dl4j.data;

import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.sentenceiterator.SentencePreProcessor;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.node.BufferedDataTable;
import org.knime.ext.dl4j.base.exception.DataCellConversionException;
import org.knime.ext.dl4j.base.util.ConverterUtils;
import org.knime.ext.dl4j.base.util.TableUtils;

/**
 * {@link SentenceIterator} for a {@link BufferedDataTable}. Expects a column contained in the data table holding one
 * sentence per row.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class BufferedDataTableSentenceIterator implements SentenceIterator {

    private BufferedDataTable m_table;

    private CloseableRowIterator m_tableIterator;

    private final int m_documentColumnIndex;

    private final boolean m_skipMissing;

    private long m_currentRow = 0;

    private long m_lastIndexWithoutMissing;


    /**
     * Convenience constructor for class BufferedDataTableSentenceIterator. Equal to calling
     * <code>this(table, documentColumnName, false)</code>
     *
     * @param table the table to iterate
     * @param documentColumnName the name of the document column
     */
    public BufferedDataTableSentenceIterator(final BufferedDataTable table, final String documentColumnName) {
        this(table, documentColumnName, false);
    }

    /**
     * Constructor for class BufferedDataTableSentenceIterator
     *
     * @param table the table to iterate
     * @param documentColumnName the name of the document column
     * @param skipMissing whether rows containing missing cells should be skipped
     */
    public BufferedDataTableSentenceIterator(final BufferedDataTable table, final String documentColumnName, final boolean skipMissing) {
        m_skipMissing = skipMissing;
        m_table = table;
        m_documentColumnIndex = table.getSpec().findColumnIndex(documentColumnName);
        m_tableIterator = table.iterator();
        if(skipMissing){
            m_lastIndexWithoutMissing = searchLastIndexWithoutMissing();
        }
        reset();
    }

    /**
     * Convenience helper because we always check for the same index.
     *
     * @param row
     * @return
     */
    private boolean containsMissing(final DataRow row) {
        return TableUtils.hasMissing(row, new int[]{m_documentColumnIndex});
    }

    /**
     * Returns the index of the last row in the table that contains no missing values. Missing in the sense of
     * <code>containsMissing(final DataRow row)</code> implementation.
     *
     * @return
     */
    private long searchLastIndexWithoutMissing() {
        long lastIndex = 0;
        long i = 0;
        while (m_tableIterator.hasNext()) {
            final DataRow row = m_tableIterator.next();
            if (!containsMissing(row)) {
                lastIndex = i;
            }
            i++;
        }
        reset();
        return lastIndex;
    }

    /**
     * Returns the next String contained in the document column of the table.
     */
    @Override
    public String nextSentence() {
        final DataRow row = m_tableIterator.next();
        final DataCell cell = row.getCell(m_documentColumnIndex);
        String documentContent = null;

        if (m_skipMissing && containsMissing(row)) {
            m_currentRow++;
            return nextSentence();
        }

        try {
            documentContent = ConverterUtils.convertDataCellToJava(cell, String.class);
        } catch (DataCellConversionException e) {
            throw new RuntimeException("Error in row " + row.getKey() + " : " + e.getMessage(), e);
        }

        m_currentRow++;
        return documentContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        if (m_skipMissing) {
            //if no follow up rows are valid we are done
            return m_currentRow <= m_lastIndexWithoutMissing;
        } else {
            return m_tableIterator.hasNext();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        m_tableIterator.close();
        m_tableIterator = m_table.iterator();
        m_currentRow = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void finish() {
        // nothing to do
    }

    /**
     * Method not supported. Will throw exception if called.
     */
    @Override
    public SentencePreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Method getPreProcessor not available in " + this.getClass().getName());
    }

    /**
     * Method not supported. Will throw exception if called.
     */
    @Override
    public void setPreProcessor(final SentencePreProcessor preProcessor) {
        throw new UnsupportedOperationException("Method setPreProcessor not available in " + this.getClass().getName());
    }

    /**
     * Close this iterator.
     */
    public void close() {
        m_tableIterator.close();
        m_tableIterator = null;
        m_table = null;
    }
}

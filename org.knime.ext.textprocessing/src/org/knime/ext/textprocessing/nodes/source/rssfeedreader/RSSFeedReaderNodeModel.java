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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.ThreadPool;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class RSSFeedReaderNodeModel extends NodeModel {

    static final NodeLogger LOGGER = NodeLogger.getLogger(RSSFeedReaderNodeModel.class);

    static final Boolean DEF_CREATE_DOC_COLUMN = false;

    static final Boolean DEF_CREATE_XML_COLUMN = false;

    static final Boolean DEF_GET_HTTP_RESPONSE_CODE_COLUMN = false;

    static final String DEF_DOC_COL_NAME = "Document";

    static final String DEF_XML_COL_NAME = "XML";

    static final String DEF_HTTP_COL_NAME = "HTTP Response Code";

    static final int DEF_THREADS = 2;

    static final int MIN_THREADS = 1;

    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    static final int DEF_TIMEOUT = 3000;

    static final int MIN_TIMEOUT = 0;

    static final int MAX_TIMEOUT = Integer.MAX_VALUE;

    private SettingsModelString m_feedUrlColumn = RSSFeedReaderNodeDialog.createFeedUrlColumnModel();

    private SettingsModelBoolean m_createDocColumn = RSSFeedReaderNodeDialog.createDocumentColumnModel();

    private SettingsModelBoolean m_createXMLColumn = RSSFeedReaderNodeDialog.createXMLColumnModel();

    private SettingsModelIntegerBounded m_numberOfThreadsModel = RSSFeedReaderNodeDialog.createNumberOfThreadsModel();

    private SettingsModelIntegerBounded m_timeOutModel = RSSFeedReaderNodeDialog.createTimeOutModel();

    private SettingsModelBoolean m_getHttpResponseCodeColumn = RSSFeedReaderNodeDialog.getHttpResponseCodeModel();

    private SettingsModelString m_docColName = RSSFeedReaderNodeDialog.createDocColumnNameModel();

    private SettingsModelString m_xmlColName = RSSFeedReaderNodeDialog.createXmlColumnNameModel();

    private SettingsModelString m_httpColName = RSSFeedReaderNodeDialog.createHttpColumnNameModel();

    private int m_urlColIndex = -1;

    /**
     * Creates a new instance {@code RSSFeedReaderNodeModel} with one {@code BufferedDataTable} input and one
     * {@code BufferedDataTable} output.
     */
    protected RSSFeedReaderNodeModel() {
        super(1, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        BufferedDataTable inputTable = inData[0];
        final long rowCount = inputTable.size();

        // creating thread pool, semaphore and chunk size
        final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
        final Semaphore semaphore = new Semaphore(m_numberOfThreadsModel.getIntValue());
        final int chunkSize = (int)rowCount / m_numberOfThreadsModel.getIntValue();

        RSSFeedReaderDataTableCreator joiner =
            new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(), m_createXMLColumn.getBooleanValue(),
                m_getHttpResponseCodeColumn.getBooleanValue(), m_timeOutModel.getIntValue(),
                m_docColName.getStringValue(), m_xmlColName.getStringValue(), m_httpColName.getStringValue());
        List<DataCell> dataCellChunk = null;

        AtomicInteger urlCount = new AtomicInteger(0);
        int itCount = 0;
        RowIterator iterator = inputTable.iterator();
        List<Future<?>> futures = new ArrayList<>();

        final FileStoreFactory fsFactory = FileStoreFactory.createWorkflowFileStoreFactory(exec);

        // iterating through urls
        while (iterator.hasNext()) {
            exec.checkCanceled();
            DataRow row = iterator.next();
            itCount++;

            //create empty chunk based on chunkSize
            if (dataCellChunk == null) {
                dataCellChunk = new ArrayList<DataCell>(chunkSize);
            }

            //add urls to chunk
            if (itCount < chunkSize) {
                dataCellChunk.add(row.getCell(m_urlColIndex));
                // dataCellChunk.add(((StringValue)row.getCell(m_urlColIndex)).getStringValue());
                //chunk is full, process and clear
            } else {
                dataCellChunk.add(row.getCell(m_urlColIndex));
                futures.add(
                    pool.enqueue(processChunk(dataCellChunk, joiner, exec, semaphore, urlCount, rowCount, fsFactory)));
                dataCellChunk = null;
                itCount = 0;
            }
        }

        // enqueue the last chunk and wait
        if (dataCellChunk != null && dataCellChunk.size() > 0) {
            futures
                .add(pool.enqueue(processChunk(dataCellChunk, joiner, exec, semaphore, urlCount, rowCount, fsFactory)));
        }

        for (Future<?> f : futures) {
            f.get();
        }

        exec.setMessage("Creating output table.");
        if (joiner.getMissingRowCount() > 0) {
            this.setWarningMessage(
                "Could not load/connect to " + joiner.getMissingRowCount() + " of " + rowCount + " URLs.");
        }
        return new BufferedDataTable[]{joiner.createDataTable(exec)};
    }

    private Runnable processChunk(final List<DataCell> dataCellsWithUrls, final RSSFeedReaderDataTableCreator joiner,
        final ExecutionContext exec, final Semaphore semaphore, final AtomicInteger urlCount, final long inputTableSize,
        final FileStoreFactory fsFactory) throws CanceledExecutionException {
        exec.checkCanceled();
        return new Runnable() {
            @Override
            public void run() {
                RSSFeedReaderDataTableCreator rssFeedReaderTC = null;
                try {
                    semaphore.acquire();
                    rssFeedReaderTC = new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(),
                        m_createXMLColumn.getBooleanValue(), m_getHttpResponseCodeColumn.getBooleanValue(),
                        m_timeOutModel.getIntValue(), m_docColName.getStringValue(), m_xmlColName.getStringValue(),
                        m_httpColName.getStringValue());
                    for (DataCell dataCell : dataCellsWithUrls) {
                        exec.checkCanceled();
                        rssFeedReaderTC.createDataCellsFromUrl(dataCell, fsFactory);
                        int processedUrls = urlCount.addAndGet(1);
                        double progress = (double)processedUrls / (double)inputTableSize;
                        exec.setProgress(progress,
                            "Parsed feed entries from " + processedUrls + "/" + inputTableSize + " urls.");
                    }
                    exec.checkCanceled();
                    joiner.joinResults(rssFeedReaderTC, exec);
                } catch (final CanceledExecutionException e) {
                    // handled in main executer thread
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                } finally {
                    semaphore.release();
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        // checking specified string column and guessing first available string column if no column is set.
        DataTableSpec inSpec = inSpecs[0];

        checkColumnNames(m_docColName.getStringValue());
        checkColumnNames(m_xmlColName.getStringValue());
        checkColumnNames(m_httpColName.getStringValue());

        int colIndex = inSpec.findColumnIndex(m_feedUrlColumn.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < inSpec.getNumColumns(); i++) {
                if (inSpec.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    this.setWarningMessage("Guessing url string column \"" + inSpec.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
        }

        if (colIndex < 0) {
            LOGGER.error("Input table contains no string column!");
            throw new InvalidSettingsException("Input table contains no string column!");
        }

        m_urlColIndex = colIndex;

        RSSFeedReaderDataTableCreator rssFeedReaderDTC =
            new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(), m_createXMLColumn.getBooleanValue(),
                m_getHttpResponseCodeColumn.getBooleanValue(), m_timeOutModel.getIntValue(),
                m_docColName.getStringValue(), m_xmlColName.getStringValue(), m_httpColName.getStringValue());
        return new DataTableSpec[]{rssFeedReaderDTC.createDataTableSpec()};
    }

    private void checkColumnNames(final String str) throws InvalidSettingsException {
        final List<String> columnNamesList = Arrays.asList("Feed Url", "Title", "Description", "Item Url", "Published");
        if (columnNamesList.contains(str)) {
            LOGGER.error("Can't create new column " + str + " as output spec already contains such column");
            throw new InvalidSettingsException(
                "Can't create new column " + str + " as output spec already contains such column");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_feedUrlColumn.saveSettingsTo(settings);
        m_createDocColumn.saveSettingsTo(settings);
        m_createXMLColumn.saveSettingsTo(settings);
        m_numberOfThreadsModel.saveSettingsTo(settings);
        m_timeOutModel.saveSettingsTo(settings);
        m_getHttpResponseCodeColumn.saveSettingsTo(settings);
        m_docColName.saveSettingsTo(settings);
        m_xmlColName.saveSettingsTo(settings);
        m_httpColName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_feedUrlColumn.validateSettings(settings);
        m_createDocColumn.validateSettings(settings);
        m_createXMLColumn.validateSettings(settings);
        m_numberOfThreadsModel.validateSettings(settings);
        m_timeOutModel.validateSettings(settings);
        m_getHttpResponseCodeColumn.validateSettings(settings);
        m_docColName.validateSettings(settings);
        m_xmlColName.validateSettings(settings);
        m_httpColName.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_feedUrlColumn.loadSettingsFrom(settings);
        m_createDocColumn.loadSettingsFrom(settings);
        m_createXMLColumn.loadSettingsFrom(settings);
        m_numberOfThreadsModel.loadSettingsFrom(settings);
        m_timeOutModel.loadSettingsFrom(settings);
        m_getHttpResponseCodeColumn.loadSettingsFrom(settings);
        m_docColName.loadSettingsFrom(settings);
        m_xmlColName.loadSettingsFrom(settings);
        m_httpColName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do.
    }
}

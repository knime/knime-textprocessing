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
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
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

    static final Boolean DEF_CREATE_DOC_COLUMN = false;

    static final Boolean DEF_CREATE_XML_COLUMN = false;

    static final int DEF_THREADS = 3;

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

    private int m_urlColIndex = -1;

    private RSSFeedReaderDataTableCreator m_rssFeedReaderDataTableCreator;

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
        final long rowCount = inputTable.size();

        m_rssFeedReaderDataTableCreator =
            new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(), m_createXMLColumn.getBooleanValue());

        final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
        final Semaphore semaphore = new Semaphore(m_numberOfThreadsModel.getIntValue());
        final int chunkSize = (int) rowCount / m_numberOfThreadsModel.getIntValue();
        int count = 0;

        RSSFeedReaderDataTableCreator joiner =
            new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(), m_createXMLColumn.getBooleanValue());
        List<String> urlChunk = null;

        AtomicInteger urlCount = new AtomicInteger(0);
        RowIterator it = inputTable.iterator();
        List<Future<?>> futures = new ArrayList<>();
        while (it.hasNext()) {
            exec.checkCanceled();
            DataRow row = it.next();
//            if (row.getCell(m_urlColIndex).isMissing()) {
//                continue;
//            }
            count++;

            //create empty chunk based on chunkSize
            if (urlChunk == null) {
                urlChunk = new ArrayList<String>(chunkSize);
            }

            //add to chunk
            if (count < chunkSize) {
                if (row.getCell(m_urlColIndex).isMissing()) {
                    urlChunk.add("MISSING_VALUE");
                } else {
                    urlChunk.add(row.getCell(m_urlColIndex).toString());
                }
                //chunk is full, process and clear
            } else {
                if (row.getCell(m_urlColIndex).isMissing()) {
                    urlChunk.add("MISSING_VALUE");
                } else {
                    urlChunk.add(row.getCell(m_urlColIndex).toString());
                }
                futures.add(pool.enqueue(processChunk(urlChunk, joiner, exec, semaphore, urlCount, rowCount)));
                urlChunk = null;
                count = 0;
            }
        }

        // enqueue the last chunk and wait
        if (urlChunk != null && urlChunk.size() > 0) {
            futures.add(pool.enqueue(processChunk(urlChunk, joiner, exec, semaphore, urlCount, rowCount)));
        }

        for (Future<?> f : futures) {
            f.get();
        }

        exec.setMessage("Creating output table.");
        return new BufferedDataTable[]{joiner.createDataTable(exec)};
    }

    private Runnable processChunk(final List<String> urls, final RSSFeedReaderDataTableCreator joiner,
        final ExecutionContext exec, final Semaphore semaphore, final AtomicInteger urlCount, final long inputTableSize)
            throws CanceledExecutionException {
        exec.checkCanceled();
        return new Runnable() {
            @Override
            public void run() {
                RSSFeedReaderDataTableCreator rssFeedReaderTC = null;
                try {
                    semaphore.acquire();
                    rssFeedReaderTC = new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(),
                        m_createXMLColumn.getBooleanValue());
                    for (String urlAsString : urls) {
                        exec.checkCanceled();
                        rssFeedReaderTC.createDataCellsFromUrl(urlAsString, m_timeOutModel.getIntValue());
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
        int colIndex = inSpec.findColumnIndex(m_feedUrlColumn.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < inSpec.getNumColumns(); i++) {
                if (inSpec.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    this.setWarningMessage("Guessing document column \"" + inSpec.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
        }

        if (colIndex < 0) {
            throw new InvalidSettingsException("Input table contains no string column!");
        }

        m_urlColIndex = colIndex;

        m_rssFeedReaderDataTableCreator =
            new RSSFeedReaderDataTableCreator(m_createDocColumn.getBooleanValue(), m_createXMLColumn.getBooleanValue());
        return new DataTableSpec[]{m_rssFeedReaderDataTableCreator.createDataTableSpec()};
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
        m_numberOfThreadsModel.saveSettingsTo(settings);
        m_timeOutModel.saveSettingsTo(settings);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }

}

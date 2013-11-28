/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 2, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ---------------------------------------------------------------------
 *
 * History
 *   15.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;
import org.knime.core.data.DataTableSpec;
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
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.ThreadPool;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

/**
 * The model for all {@link org.knime.ext.textprocessing.data.Document} parser nodes, no matter what format they parse.
 * The factory provides them with the right {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}
 * instance to use for parsing the files with the specified file extensions in the specified directory. <br/>
 * All files partitioned into several chunks of files. The chunks of files are then parsed concurrently the worker
 * threads of the KNIME thread pool. For each thread a new concrete {@link DocumentParser} instance is created using the
 * specified {@link DocumentParserFactory}.
 *
 * @author Kilian Thiel, University of Konstanz
 */
/**
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 */
public class DocumentParserNodeModel extends NodeModel {

    /**
     * The default path of the directory containing the files to parse.
     */
    public static final String DEFAULT_PATH = System.getProperty("user.home");

    /**
     * The default value of the recursive flag (if set <code>true</code> the specified directory is search recursively).
     */
    public static final boolean DEFAULT_RECURSIVE = false;

    /**
     * The default value of the ignore hidden files flag (if set <code>true</code> the hidden files will be not
     * considered for parsing.
     */
    public static final boolean DEFAULT_IGNORE_HIDDENFILES = true;

    /**
     * The default category of the documents.
     */
    public static final String DEFAULT_CATEGORY = "Default category";

    /**
     * The default source of the documents.
     */
    public static final String DEFAULT_SOURCE = "";

    /**
     * The default charset.
     */
    public static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

    /**
     * The default document type.
     */
    public static final DocumentType DEFAULT_DOCTYPE = DocumentType.UNKNOWN;

    private static final NodeLogger LOGGER = NodeLogger.getLogger(DocumentParserNodeModel.class);

    private SettingsModelString m_pathModel = DocumentParserNodeDialog.getPathModel();

    private SettingsModelBoolean m_recursiveModel = DocumentParserNodeDialog.getRecursiveModel();

    private SettingsModelString m_categoryModel = DocumentParserNodeDialog.getCategoryModel();

    private SettingsModelString m_sourceModel = DocumentParserNodeDialog.getSourceModel();

    private SettingsModelString m_typeModel = DocumentParserNodeDialog.getTypeModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel = DocumentParserNodeDialog.getIgnoreHiddenFilesModel();

    private SettingsModelString m_charsetModel = CharsetDocumentParserNodeDialog.getCharsetModel();

    private boolean m_withCharset = false;

    private final DocumentParserFactory m_parserFactory;

    private final List<String> m_validExtensions;

    private final DocumentDataTableBuilder m_dtBuilder;

    /**
     * Creates a new instance of <code>DocumentParserNodeModel</code> with the specified parser factory to create the
     * parser to use, and the valid extensions of files to parse.
     *
     * @param parserFac The factory creating the parser instances to use.
     * @param withCharset if <code>true</code> the character set of the character set model is handed to the parser in
     *            order to properly decode the text to parse, otherwise not. Be aware that if <code>true</code> is set
     *            the {@link org.knime.ext.textprocessing.nodes.source.parser.CharsetDocumentParserNodeDialog} needs to
     *            be used in order to enable the user to specify a certain encoding via the dialog.
     * @param validFileExtensions The valid extensions of files to parse.
     * @since 2.9
     */
    public DocumentParserNodeModel(final DocumentParserFactory parserFac, final boolean withCharset,
        final String... validFileExtensions) {
        super(0, 1);
        m_parserFactory = parserFac;
        m_validExtensions = Arrays.asList(validFileExtensions);
        m_dtBuilder = new DocumentDataTableBuilder();
        m_withCharset = withCharset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        // check selected directory
        final String dir = m_pathModel.getStringValue();
        final File f = new File(dir);
        if (!f.isDirectory() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected directory: " + dir + " is not valid!");
        }

        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    /**
     * Creates and returns a new instance of {@link DocumentParser} using the {@link DocumentParserFactory}. Category,
     * source, and type of the document are set to the parser.
     *
     * @return The new parser instance.
     * @throws Exception If parser could not be created.
     */
    private final DocumentParser createParser() throws InstantiationException {
        final DocumentParser parser = m_parserFactory.createParser();

        final String category = m_categoryModel.getStringValue();
        if (category != null && category.length() > 0) {
            parser.setDocumentCategory(new DocumentCategory(category));
        }
        final String source = m_sourceModel.getStringValue();
        if (source != null && source.length() > 0) {
            parser.setDocumentSource(new DocumentSource(source));
        }
        final DocumentType type = DocumentType.valueOf(m_typeModel.getStringValue());
        if (type != null) {
            parser.setDocumentType(type);
        }
        if (m_withCharset) {
            parser.setCharset(Charset.forName(m_charsetModel.getStringValue()));
        }

        return parser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final File dir = new File(m_pathModel.getStringValue());
        final boolean recursive = m_recursiveModel.getBooleanValue();
        final boolean ignoreHiddenFiles = m_ignoreHiddenFilesModel.getBooleanValue();

        final FileCollector fc = new FileCollector(dir, m_validExtensions, recursive, ignoreHiddenFiles);
        final List<File> files = fc.getFiles();
        final int numberOfFiles = files.size();

        final int numberOfThreads = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();
        final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
        final Semaphore semaphore = new Semaphore(numberOfThreads);
        final int chunkSize = numberOfFiles / numberOfThreads;
        final AtomicInteger fileCount = new AtomicInteger(0);

        try {
            m_dtBuilder.openDataTable(exec);

            List<File> chunk = new ArrayList<File>(chunkSize);
            for (final File f : files) {
                chunk.add(f);
                if (chunk.size() >= chunkSize) {
                    pool.enqueue(parseFiles(chunk, exec, semaphore, fileCount, numberOfFiles));
                    chunk = new ArrayList<File>(chunkSize);
                }
            }
            // process last chunk
            if (chunk.size() > 0) {
                pool.enqueue(parseFiles(chunk, exec, semaphore, fileCount, numberOfFiles));
            }

            pool.waitForTermination();

            return new BufferedDataTable[]{m_dtBuilder.getAndCloseDataTable()};
        } finally {
            m_dtBuilder.closeCache();
        }
    }

    /**
     * Creates and returns new anonymous {@link Runnable} instance that parses the given chunk of files.
     * @param files The files to parse.
     * @param exec The exection context.
     * @param semaphore Semaphore to accquire.
     * @param fileCount Count of parsed files.
     * @param noFiles Number of files to parse in total, over all threads.
     * @return new anonymous {@link Runnable} instance that parses given files.
     * @throws CanceledExecutionException If execution was canceled.
     */
    private Runnable parseFiles(final List<File> files, final ExecutionContext exec, final Semaphore semaphore,
        final AtomicInteger fileCount, final int noFiles) throws CanceledExecutionException {
        exec.checkCanceled();
        return new Runnable() {

            @Override
            public void run() {
                try {
                    semaphore.acquire();

                    final DocumentParser parser = createParser();

                    for (final File f : files) {
                        exec.checkCanceled();
                        final int count = fileCount.incrementAndGet();
                        final double progress = count / (double)noFiles;
                        exec.setProgress(progress, "Parsing file " + count + " of " + noFiles + " ...");

                        LOGGER.info("Parsing file: " + f.getAbsolutePath());

                        InputStream is = null;
                        try {
                            if (f.getName().toLowerCase().endsWith(".gz")) {
                                is = new BufferedInputStream(new GZIPInputStream(new FileInputStream(f)));
                            } else {
                                is = new BufferedInputStream(new FileInputStream(f));
                            }
                            parser.setDocumentFilepath(f.getAbsolutePath());

                            // first remove all listeners in order to avoid that two or more listeners are registered,
                            // adding the same document twice or more times.
                            parser.removeAllDocumentParsedListener();
                            parser.addDocumentParsedListener(new InternalDocumentParsedEventListener());

                            parser.parseDocument(is);
                        } catch (Exception e) {
                            LOGGER.error("Could not parse file: " + f.getAbsolutePath().toString());
                            setWarningMessage("Could not parse all files properly!");
                        } finally {
                            if (is != null) {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    LOGGER.debug("Could not close input stream of file:"
                                        + f.getAbsolutePath().toString());
                                }
                            }
                        }
                    }
                    parser.clean();
                } catch (InstantiationException e) {
                    LOGGER.error("Parser instance could not be created.");
                    setWarningMessage("Could not parse files!");
                } catch (InterruptedException e) {
                    LOGGER.warn("Parser thread was interrupted, could not parse all files.");
                    setWarningMessage("Could not parse all files properly!");
                } catch (CanceledExecutionException e) {
                    // handled by main executor thread
                } finally {
                    semaphore.release();
                }
            }
        };
    }

    private class InternalDocumentParsedEventListener implements DocumentParsedEventListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public synchronized void documentParsed(final DocumentParsedEvent event) {
            if (m_dtBuilder != null) {
                final Document d = event.getDocument();
                if (d != null) {
                    m_dtBuilder.addDocument(d);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec) throws IOException,
        CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        try {
            m_dtBuilder.getAndCloseDataTable();
        } catch (Exception e) { /* Do noting just try */
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
        m_categoryModel.loadSettingsFrom(settings);
        m_sourceModel.loadSettingsFrom(settings);
        m_typeModel.loadSettingsFrom(settings);
        m_ignoreHiddenFilesModel.loadSettingsFrom(settings);

        if (m_withCharset) {
            m_charsetModel.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
        m_categoryModel.saveSettingsTo(settings);
        m_sourceModel.saveSettingsTo(settings);
        m_typeModel.saveSettingsTo(settings);
        m_ignoreHiddenFilesModel.saveSettingsTo(settings);

        if (m_withCharset) {
            m_charsetModel.saveSettingsTo(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_categoryModel.validateSettings(settings);
        m_sourceModel.validateSettings(settings);
        m_typeModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);

        if (m_withCharset) {
            m_charsetModel.validateSettings(settings);
        }
    }
}

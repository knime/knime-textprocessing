/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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

import org.knime.core.data.DataTableSpec;
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
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.util.DocumentDataTableBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;


/**
 * The model for all {@link org.knime.ext.textprocessing.data.Document} parser
 * nodes, no matter what format they parse. The factory provides them with the
 * right {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParser}
 * instance they use to parse the specified files.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentParserNodeModel extends NodeModel {

    /**
     * The default path of the directory containing the files to parse.
     */
    public static final String DEFAULT_PATH = System.getProperty("user.home");

    /**
     * The default value of the recursive flag (if set <code>true</code> the
     * specified directory is search recursively).
     */
    public static final boolean DEFAULT_RECURSIVE = false;

    /**
     * The default value of the ignore hidden files flag
     * (if set <code>true</code> the hidden files will be not considered for
     * parsing.
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
    public static final String DEFAULT_CHARSET =
        Charset.defaultCharset().name();

    /**
     * The default document type.
     */
    public static final DocumentType DEFAULT_DOCTYPE = DocumentType.UNKNOWN;

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(DocumentParserNodeModel.class);

    private SettingsModelString m_pathModel =
        DocumentParserNodeDialog.getPathModel();

    private SettingsModelBoolean m_recursiveModel =
        DocumentParserNodeDialog.getRecursiveModel();

    private SettingsModelString m_categoryModel =
        DocumentParserNodeDialog.getCategoryModel();

    private SettingsModelString m_sourceModel =
        DocumentParserNodeDialog.getSourceModel();

    private SettingsModelString m_typeModel =
        DocumentParserNodeDialog.getTypeModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel =
        DocumentParserNodeDialog.getIgnoreHiddenFilesModel();

    private SettingsModelString m_charsetModel =
        CharsetDocumentParserNodeDialog.getCharsetModel();

    private boolean m_withCharset = false;

    private DocumentParser m_parser;

    private List<String> m_validExtensions;

    private DocumentDataTableBuilder m_dtBuilder;

    /**
     * Creates a new instance of <code>DocumentParserNodeModel</code> with the
     * specified parser to use and the valid extensions of files to parse.
     *
     * @param parser The parser to use.
     * @param validFileExtensions The valid extensions of files to parse.
     */
    public DocumentParserNodeModel(final DocumentParser parser,
            final boolean withCharset, final String... validFileExtensions) {
        super(0, 1);
        m_parser = parser;
        m_validExtensions = Arrays.asList(validFileExtensions);
        m_dtBuilder = new DocumentDataTableBuilder();
        m_withCharset = withCharset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        return new DataTableSpec[]{m_dtBuilder.createDataTableSpec()};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
        List<Document> docs = new ArrayList<Document>();

        File dir = new File(m_pathModel.getStringValue());
        boolean recursive = m_recursiveModel.getBooleanValue();
        boolean ignoreHiddenFiles = m_ignoreHiddenFilesModel.getBooleanValue();
        String category = m_categoryModel.getStringValue();
        if (category != null && category.length() > 0) {
            m_parser.setDocumentCategory(new DocumentCategory(category));
        }
        String source = m_sourceModel.getStringValue();
        if (source != null && source.length() > 0) {
            m_parser.setDocumentSource(new DocumentSource(source));
        }
        DocumentType type = DocumentType.valueOf(m_typeModel.getStringValue());
        if (type != null) {
            m_parser.setDocumentType(type);
        }
        if (m_withCharset) {
            Charset charset = Charset.forName(m_charsetModel.getStringValue());
            m_parser.setCharset(charset);
        }

        FileCollector fc = new FileCollector(dir, m_validExtensions, recursive,
                ignoreHiddenFiles);
        List<File> files = fc.getFiles();
        int fileCount = files.size();
        int currFile = 1;
        for (File f : files) {

            double progress = (double)currFile / (double)fileCount;
            exec.setProgress(progress, "Parsing file " + currFile + " of "
                    + fileCount);
            exec.checkCanceled();
            currFile++;
            LOGGER.info("Parsing file: " + f.getAbsolutePath());

            InputStream is;
            if (f.getName().toLowerCase().endsWith(".gz")
                    || f.getName().toLowerCase().endsWith(".zip")) {
                is = new GZIPInputStream(new FileInputStream(f));
            } else {
                is = new FileInputStream(f);
            }
            m_parser.setDocumentFilepath(f.getAbsolutePath());

            try {
                docs.addAll(m_parser.parse(is));
            } catch (Exception e) {
                LOGGER.error("Could not parse file: "
                        + f.getAbsolutePath().toString());
                setWarningMessage("Could not parse all files properly!");
                throw e;
            }
        }

        return new BufferedDataTable[]{m_dtBuilder.createDataTable(exec, docs)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }




    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_categoryModel.validateSettings(settings);
        m_sourceModel.validateSettings(settings);
        m_typeModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);

        if (m_withCharset) {
            m_charsetModel.validateSettings(settings);
        }

        // check selected directory
        String dir = ((SettingsModelString)m_pathModel.
                createCloneWithValidatedValue(settings)).getStringValue();
        File f = new File(dir);
        if (!f.isDirectory() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected directory: "
                    + dir + " is not valid!");
        }
    }

}

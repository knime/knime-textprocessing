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
 *   08.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaMetadataKeys;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.streamable.BufferedDataTableRowOutput;
import org.knime.core.node.streamable.PartitionInfo;
import org.knime.core.node.streamable.PortInput;
import org.knime.core.node.streamable.PortOutput;
import org.knime.core.node.streamable.RowOutput;
import org.knime.core.node.streamable.StreamableOperator;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.nodes.source.parser.FileCollector;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaParserNodeModel extends NodeModel {

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
     * The action command value for choosing file extension in the dialog selection.
     */
    public static final String EXT_TYPE = "Extension";

    /**
     * The action command value for choosing MIME-Type in the dialog selection.
     */
    public static final String MIME_TYPE = "MIME";

    /**
     * The default value of the action command for choosing file extension in the dialog selection.
     */
    public static final String DEFAULT_TYPE = EXT_TYPE;

    private static final Set<MediaType> VALID_TYPES = new AutoDetectParser().getSupportedTypes(new ParseContext());

    /**
     * The list of all MIME-Types that will be shown in the dialog.
     */
    public static final String[] MIMETYPE_LIST;

    static {
        MIMETYPE_LIST = getMimeTypes();
    }

    /**
     * The list of all file extensions that will be shown in the dialog.
     */
    public static final String[] EXTENSION_LIST;

    static {
        EXTENSION_LIST = getExtensions();
    }

    /**
     * The default list that will be shown in the dialog. The default is the list of file extensions.
     */
    public static final String[] DEFAULT_TYPE_LIST = EXTENSION_LIST;

    /**
     * The default list of meta data information to be parsed.
     */
    public static final String[] DEFAULT_COLUMNS_LIST =
        TikaColumnKeys.COLUMN_PROPERTY_MAP.keySet().toArray(new String[TikaColumnKeys.COLUMN_PROPERTY_MAP.size()]);

    /**
     * The default value of the "extract attachments" flag.
     */
    public static final boolean DEFAULT_EXTRACT = false;

    /**
     * The default path of the directory containing the extracted attachment files.
     */
    public static final String DEFAULT_EXTRACT_PATH = System.getProperty("user.home");

    private static final String[] OUTPUT_TWO_COL_NAMES = {"Files", "Attachments"};

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParserNodeModel.class);

    private SettingsModelString m_pathModel = TikaParserNodeDialog.getPathModel();

    private SettingsModelBoolean m_recursiveModel = TikaParserNodeDialog.getRecursiveModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel = TikaParserNodeDialog.getIgnoreHiddenFilesModel();

    private SettingsModelString m_typesModel = TikaParserNodeDialog.getTypeModel();

    private SettingsModelStringArray m_typeListModel = TikaParserNodeDialog.getTypeListModel();

    private SettingsModelStringArray m_columnModel = TikaParserNodeDialog.getColumnModel();

    private SettingsModelBoolean m_extractBooleanModel = TikaParserNodeDialog.getExtractBooleanModel();

    private SettingsModelString m_extractPathModel = TikaParserNodeDialog.getExtractPathModel();

    /**
     * Creates a new instance of {@code TikaParserNodeModel}
     */
    public TikaParserNodeModel() {

        super(0, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {

        return new DataTableSpec[]{null, null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] data, final ExecutionContext exec)
        throws Exception {
        // create output spec and its container
        BufferedDataContainer container1 =
            exec.createDataContainer(createOutputTableSpec(Arrays.asList(m_columnModel.getStringArrayValue())));
        BufferedDataContainer container2 =
            exec.createDataContainer(createOutputTableSpec(Arrays.asList(OUTPUT_TWO_COL_NAMES)));

        BufferedDataTableRowOutput output1 = new BufferedDataTableRowOutput(container1);
        BufferedDataTableRowOutput output2 = new BufferedDataTableRowOutput(container2);

        createStreamableOperator(null, null).runFinal(new PortInput[0], new PortOutput[]{output1, output2}, exec);
        output1.close();
        output2.close();
        return new BufferedDataTable[]{output1.getDataTable(), output2.getDataTable()};
    }

    @Override
    public StreamableOperator createStreamableOperator(final PartitionInfo partitionInfo,
        final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new StreamableOperator() {

            @Override
            public void runFinal(final PortInput[] inputs, final PortOutput[] outputs, final ExecutionContext exec)
                throws Exception {
                assert inputs.length == 0;

                RowOutput rowOutput1 = (RowOutput)outputs[0]; // data output port 1
                RowOutput rowOutput2 = (RowOutput)outputs[1]; // data output port 2

                boolean ext, error = false;
                if (m_typesModel.getStringValue().equals(EXT_TYPE)) {
                    ext = true;
                } else {
                    ext = false;
                }

                final File dir = getFile(m_pathModel.getStringValue());
                final File outputDir = getFile(m_extractPathModel.getStringValue());
                final boolean recursive = m_recursiveModel.getBooleanValue();
                final boolean ignoreHiddenFiles = m_ignoreHiddenFilesModel.getBooleanValue();

                final FileCollector fc;
                if (ext) {
                    fc = new FileCollector(dir, Arrays.asList(m_typeListModel.getStringArrayValue()), recursive,
                        ignoreHiddenFiles);
                } else {
                    fc = new FileCollector(dir, new ArrayList<String>(), recursive, ignoreHiddenFiles);
                }

                final List<File> files = fc.getFiles();
                final int numberOfFiles = files.size();

                if (numberOfFiles == 0) {
                    LOGGER.warn("Directory is empty: " + dir.getPath());
                }

                List<String> outputColumnsOne = Arrays.asList(m_columnModel.getStringArrayValue());
                int rowKeyOne = 0;
                int rowKeyTwo = 0;
                for (int i = 0; i < numberOfFiles; i++) {
                    File file = files.get(i);
                    if (!file.isFile()) {
                        continue;
                    }
                    try {
                        String mime_type = "-";
                        ContentHandler handler = new BodyContentHandler(-1);
                        AutoDetectParser parser = new AutoDetectParser();
                        Metadata metadata = new Metadata();
                        metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, file.getName());

                        mime_type = parser.getDetector()
                            .detect(new BufferedInputStream(new FileInputStream(file.getPath())), metadata).toString();

                        if (mime_type.equals(MediaType.OCTET_STREAM.toString())) {
                            LOGGER.warn("Could not detect/parse file: " + file.getAbsolutePath());
                            error = true;
                            continue;
                        }
                        if (!ext) {
                            List<String> validTypes = Arrays.asList(m_typeListModel.getStringArrayValue());
                            if (!validTypes.contains(mime_type)) {
                                continue;
                            }

                        }
                        LOGGER.info("Parsing file: " + file.getAbsolutePath());

                        TikaInputStream stream = TikaInputStream.get(file.toPath());

                        try {
                            if (m_extractBooleanModel.getBooleanValue()) {
                                EmbeddedFilesExtractor ex = new EmbeddedFilesExtractor();
                                ex.extract(stream, outputDir.toPath(), file.getName());
                                if (ex.hasError()) {
                                    LOGGER.error("Can't write extracted files to the output directory: "
                                        + file.getAbsolutePath());
                                }
                                metadata = ex.getMetadata();
                                handler = ex.getHandler();

                                DataCell[] cellsTwo;
                                DataRow rowTwo;
                                for (String entry : ex.getOutputFiles()) {
                                    cellsTwo = new DataCell[OUTPUT_TWO_COL_NAMES.length];
                                    cellsTwo[0] = new StringCell(file.getName());
                                    cellsTwo[1] = new StringCell(entry);
                                    rowTwo = new DefaultRow(RowKey.createRowKey((long)rowKeyTwo), cellsTwo);
                                    rowOutput2.push(rowTwo);
                                    rowKeyTwo++;
                                }
                            } else {
                                parser.parse(stream, handler, metadata, new ParseContext());
                            }
                        } finally {
                            stream.close();
                        }

                        DataCell[] cellsOne = new DataCell[outputColumnsOne.size()];
                        for (int j = 0; j < outputColumnsOne.size(); j++) {
                            String colName = outputColumnsOne.get(j);
                            Property prop = TikaColumnKeys.COLUMN_PROPERTY_MAP.get(colName);
                            if (prop == null && colName.equals(TikaColumnKeys.COL_FILENAME)) {
                                cellsOne[j] = new StringCell(file.getName());
                            } else if (prop == null && colName.equals(TikaColumnKeys.COL_MIME_TYPE)) {
                                if (mime_type.equals("-")) {
                                    cellsOne[j] = DataType.getMissingCell();
                                } else {
                                    cellsOne[j] = new StringCell(mime_type);
                                }
                            } else if (prop == null && colName.equals(TikaColumnKeys.COL_CONTENT)) {
                                cellsOne[j] = new StringCell(handler.toString());
                            } else {
                                String val = metadata.get(prop);
                                if (val == null) {
                                    cellsOne[j] = DataType.getMissingCell();
                                } else {
                                    cellsOne[j] = new StringCell(val);
                                }
                            }
                        }

                        DataRow rowOne = new DefaultRow(RowKey.createRowKey((long)rowKeyOne), cellsOne);
                        rowOutput1.push(rowOne);
                        rowKeyOne++;

                        exec.checkCanceled();
                        exec.setProgress(i / (double)numberOfFiles, "Parsing file " + i + " of " + numberOfFiles);

                    } catch (FileNotFoundException e) {
                        LOGGER.warn("Could not find file: " + file.getAbsolutePath(), e);
                        error = true;
                    } catch (IOException | SAXException | TikaException e) {
                        LOGGER.warn("Could not detect/parse file: " + file.getAbsolutePath(), e);
                        error = true;
                    }
                } // end for loop

                if (error) {
                    setWarningMessage("Could not parse all files properly!");
                }

            }
        };
    }

    private DataTableSpec createOutputTableSpec(final List<String> selectedColumns) {
        DataColumnSpec[] cspecs = new DataColumnSpec[selectedColumns.size()];
        int i = 0;
        for (String col : selectedColumns) {
            cspecs[i] = new DataColumnSpecCreator(col, StringCell.TYPE).createSpec();
            i++;
        }

        return new DataTableSpec(cspecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
        m_ignoreHiddenFilesModel.saveSettingsTo(settings);
        m_typesModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_typeListModel.saveSettingsTo(settings);
        m_extractBooleanModel.saveSettingsTo(settings);
        m_extractPathModel.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);
        m_typesModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_typeListModel.validateSettings(settings);
        m_extractBooleanModel.validateSettings(settings);
        m_extractPathModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
        m_ignoreHiddenFilesModel.loadSettingsFrom(settings);
        m_typesModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_typeListModel.loadSettingsFrom(settings);
        m_extractBooleanModel.loadSettingsFrom(settings);
        m_extractPathModel.loadSettingsFrom(settings);
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
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    private static String[] getMimeTypes() {
        Iterator<MediaType> it = VALID_TYPES.iterator();
        List<String> list = new ArrayList<String>();
        while (it.hasNext()) {
            list.add(it.next().toString());
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list.toArray(new String[list.size()]);
    }

    private static String[] getExtensions() {
        List<String> result = new ArrayList<String>();
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        Iterator<MediaType> mimeTypes = VALID_TYPES.iterator();
        while (mimeTypes.hasNext()) {
            String mime = mimeTypes.next().toString();

            try {
                List<String> extList = allTypes.forName(mime).getExtensions();
                if (!extList.isEmpty()) {
                    for (String s : extList) {
                        String withoutDot = s.substring(1, s.length());
                        if (!result.contains(withoutDot)) {
                            result.add(withoutDot);
                        }
                    }
                }
            } catch (MimeTypeException e) {
                LOGGER.error("Could not fetch MIME type: " + mime, new MimeTypeException("Fetching MIME type failed!"));
            }
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result.toArray(new String[result.size()]);
    }

    private static File getFile(final String file) throws InvalidSettingsException {
        File f = null;
        try {
            // first try if file string is an URL (files in drop dir come as URLs)
            final URL url = new URL(file);
            f = FileUtil.getFileFromURL(url);
        } catch (MalformedURLException e) {
            // if no URL try string as path to file
            f = new File(file);
        }

        if (!f.isDirectory() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected dir: " + file + " cannot be accessed!");
        }

        return f;
    }

}

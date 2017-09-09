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
 *   07.11.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.exception.EncryptedDocumentException;
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
import org.apache.tika.parser.PasswordProvider;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.eclipse.core.runtime.CoreException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.NodeLogger;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaParserConfig.TikaColumnKeys;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The class to parse any files based on Tika
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaParser {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParser.class);

    private ContentHandler m_handler;

    private final AutoDetectParser m_parser;

    private Metadata m_metadata;

    private final ParseContext m_context;

    private List<String> m_outputColumnsOne = null;

    private List<String> m_validTypes = null; // MIME types

    private String m_errorColName = "";

    private boolean m_authBoolean = false;

    private boolean m_extBoolean = false;

    private final boolean m_sourceNode;

    private String m_password = "";

    private String m_errorMsg = "";

    private Map<String, Integer> m_duplicates = null;

    private boolean m_extractInlineImages = false;

    /**
     * @param sourceNode set to true for TikaParser, else false
     */
    public TikaParser(final boolean sourceNode) {
        m_handler = new BodyContentHandler(-1);
        m_parser = new AutoDetectParser();
        m_metadata = new Metadata();
        m_context = new ParseContext();
        m_sourceNode = sourceNode;
    }

    /**
     * This method parses a file and creates a list of DataCell arrays containing the parsed information and its
     * attachments.
     *
     * @param url the file to be parsed
     * @param attachmentDir the directory where any attachments should be stored
     * @return a list of data cells (index 0 should contain cells for the first output port, the rest for the second
     *         output port
     * @throws URISyntaxException
     * @throws IOException
     */
    public List<DataCell[]> parse(final URL url, final File attachmentDir) throws IOException, URISyntaxException {
        String mime_type = "-";
        List<DataCell[]> result = new ArrayList<DataCell[]>();

        // sorts PDF sentences from left to right and up to down.
        PDFParserConfig pdfConfig = new PDFParserConfig();
        pdfConfig.setSortByPosition(true);
        m_context.set(PDFParserConfig.class, pdfConfig);

        File localFile;
        try {
            localFile = FileUtil.getFileFromURL(url);
        } catch (Exception e) {
            localFile = null;
        }

        if (localFile != null) {
            boolean isDir = !localFile.isFile();
            boolean canRead = localFile.canRead();
            if (!m_sourceNode && isDir && canRead) {
                m_errorMsg = "File might be a directory";
                if (!m_sourceNode) {
                    result.add(createMissingRow(url, m_errorMsg));
                    return result;
                } else {
                    return null;
                }
            }
        }

        if (!m_sourceNode && m_extBoolean) {
            if (!m_validTypes.contains(FilenameUtils.getExtension(url.getFile()).toLowerCase())) { //getName
                m_errorMsg = "File doesn't match any selected extension(s)";
                result.add(createMissingRow(url, m_errorMsg));
                return result;
            }
        }

        if (localFile != null) {
            if (!localFile.canRead()) { // can read
                m_errorMsg = "Unreadable file";
                if (!m_sourceNode || m_extBoolean) {
                    result.add(createMissingRow(url, m_errorMsg));
                } // else, it's a source node and MIME type, the file should be ignored but give a warning on console --> return empty list
                return result;
            }
        }

        if (m_authBoolean) {
            setPasswordToContext();
        }

        m_metadata.set(TikaMetadataKeys.RESOURCE_NAME_KEY, url.getFile()); //getName

        try (BufferedInputStream str = new BufferedInputStream(FileUtil.openStreamWithTimeout(url))) {
            mime_type = m_parser.getDetector().detect(str, m_metadata).toString();
        } catch (FileNotFoundException e) {
            m_errorMsg = "Could not find file";
            if (!m_sourceNode || m_extBoolean) {
                result.add(createMissingRow(url, m_errorMsg));
            } // else, it's a source node and MIME type, the file should be ignored but give a warning on console --> return empty list
            return result;
        } catch (IOException e) {
            if (isMountpointRelative(url) && e.getCause() instanceof CoreException) {
                m_errorMsg = e.getCause().getMessage();
                result.add(createMissingRow(url, m_errorMsg));
            } else {
                m_errorMsg = "Unable to determine the MIME-type";
                if (!m_sourceNode || m_extBoolean) {
                    result.add(createMissingRow(url, m_errorMsg));
                } // else, it's a source node and MIME type, the file should be ignored but give a warning on console --> return empty list
            }
            return result;
        } catch (UnsupportedOperationException e) {
            if (isMountpointRelative(url)) {
                m_errorMsg =
                    "Unable to access files on server " + url.getHost() + ". Please make sure you are logged in.";
            } else {
                m_errorMsg = e.getMessage();
            }
            result.add(createMissingRow(url, m_errorMsg));
            return result;
        }

        if (mime_type.equals(MediaType.OCTET_STREAM.toString())) {
            if (m_extBoolean) {
                m_errorMsg = "Could not detect/parse file";
                result.add(createMissingRow(url, m_errorMsg));
                return result;
            }
        }
        if (!m_extBoolean) {
            if (!m_validTypes.contains(mime_type)) {
                if (!m_sourceNode) {
                    m_errorMsg = "File doesn't match any selected MIME-type(s)";
                    result.add(createMissingRow(url, m_errorMsg));
                } // for source node, skip if mime type is not in the list of input mime types --> return empty list
                return result;
            }
        }

        try {
            if (attachmentDir != null) {
                try (TikaInputStream stream = TikaInputStream.get(FileUtil.openStreamWithTimeout(url));) {
                    EmbeddedFilesExtractor ex = new EmbeddedFilesExtractor();
                    ex.setContext(m_context);
                    ex.setDuplicateFilesList(m_duplicates);
                    ex.setExtractInlineImages(m_extractInlineImages);
                    ex.extract(stream, attachmentDir.toPath(), FilenameUtils.getName(url.getPath())); //getName
                    if (ex.hasError()) {
                        m_errorMsg = "Could not write embedded files to the output directory";
                        LOGGER.error(m_errorMsg + ": " + url.getPath());
                    }
                    m_metadata = ex.getMetadata();
                    m_handler = ex.getHandler();

                    DataCell[] cellsTwo = {};
                    for (Entry<String, String> entry : ex.getOutputFiles().entrySet()) {
                        cellsTwo = new DataCell[TikaParserConfig.OUTPUT_TWO_COL_NAMES.length];
                        cellsTwo[0] = new StringCell(getPath(url));
                        cellsTwo[1] = new StringCell(entry.getKey());
                        cellsTwo[2] = new StringCell(entry.getValue());
                        result.add(cellsTwo);
                    }
                }
            } else {
                try (TikaInputStream stream = TikaInputStream.get(url);) {
                    m_parser.parse(stream, m_handler, m_metadata, m_context);
                }
            }
        } catch (EncryptedDocumentException e) {
            m_errorMsg = "Could not parse encrypted file, invalid password";
            result.add(createMissingRow(url, m_errorMsg));
            return result;
        } catch (IOException | SAXException | TikaException e) {
            m_errorMsg = "Could not parse file, it might be broken";
            result.add(createMissingRow(url, m_errorMsg));
            return result;
        }

        DataCell[] cellsOne = new DataCell[m_outputColumnsOne.size()];
        for (int j = 0; j < m_outputColumnsOne.size(); j++) {
            String colName = m_outputColumnsOne.get(j);
            Property prop = TikaColumnKeys.COLUMN_PROPERTY_MAP.get(colName);
            if (prop == null && colName.equals(TikaColumnKeys.COL_FILEPATH)) {
                cellsOne[j] = new StringCell(getPath(url));
            } else if (prop == null && colName.equals(TikaColumnKeys.COL_MIME_TYPE)) {
                if (mime_type.equals("-")) {
                    cellsOne[j] = DataType.getMissingCell();
                } else {
                    cellsOne[j] = new StringCell(mime_type);
                }
            } else if (prop == null && colName.equals(TikaColumnKeys.COL_CONTENT)) {
                cellsOne[j] = new StringCell(m_handler.toString());
            } else if (prop == null && colName.equals(m_errorColName)) {
                cellsOne[j] = m_errorMsg.isEmpty() ? DataType.getMissingCell() : new StringCell(m_errorMsg);
            } else {
                String val = m_metadata.get(prop);
                if (val == null) {
                    cellsOne[j] = DataType.getMissingCell();
                } else {
                    cellsOne[j] = new StringCell(val);
                }
            }
        }
        result.add(0, cellsOne);
        return result;
    }

    private boolean isMountpointRelative(final URL url) {
        final String host = url.getHost();
        return url.getProtocol().equals("knime") && !host.equals("knime.workflow") && !host.equals("knime.mountpoint")
            && !host.equals("knime.node");
    }

    private void setPasswordToContext() {
        m_context.set(PasswordProvider.class, new PasswordProvider() {
            @Override
            public String getPassword(final Metadata md) {
                return m_password;
            }
        });
    }

    private DataCell[] createMissingRow(final URL url, final String errorMsg) throws IOException, URISyntaxException {
        String file = getPath(url);
        int outputSize = m_outputColumnsOne.size();
        DataCell[] cells = new DataCell[outputSize];
        for (int j = 0; j < outputSize; j++) {
            String colName = m_outputColumnsOne.get(j);
            if (colName.equals(TikaColumnKeys.COL_FILEPATH)) {
                cells[j] = new StringCell(file);
            } else if (colName.equals(m_errorColName)) {
                cells[j] = new StringCell(errorMsg);
            } else {
                cells[j] = DataType.getMissingCell();
            }
        }
        return cells;
    }

    /**
     * @param url the URL
     * @return the complete path of the URL
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String getPath(final URL url) throws IOException, URISyntaxException {
        String res = URLDecoder.decode(url.getPath(), "UTF-8");
        if (url.getProtocol().startsWith("http")) {
            return url.getProtocol() + "://" + url.getHost() + res;
        } else {
            return FileUtil.resolveToPath(url).toString();
        }
    }

    /**
     * @param outputCols names of output columns
     * @param file the file path
     * @param rowKey row key for the output row
     * @param errorMsg error message that should be contained in the row
     * @param errorColName the name of the error column
     * @return DataRow a data row containing missing cells and an error message
     */
    public static DataRow setMissingRow(final List<String> outputCols, final String file, final RowKey rowKey,
        final String errorMsg, final String errorColName) {
        int outputSize = outputCols.size();
        DataCell[] cellsOne = new DataCell[outputSize];
        for (int j = 0; j < outputSize; j++) {
            String colName = outputCols.get(j);
            if (colName.equals(TikaColumnKeys.COL_FILEPATH)) {
                cellsOne[j] = file.isEmpty() ? DataType.getMissingCell() : new StringCell(file);
            } else if (colName.equals(errorColName)) {
                cellsOne[j] = new StringCell(errorMsg);
            } else {
                cellsOne[j] = DataType.getMissingCell();
            }
        }
        return new DefaultRow(rowKey, cellsOne);
    }

    /**
     * @param outputCols names of output columns
     * @param file the file path
     * @param rowKey row key for the output row
     * @param errorMsg error message that should be contained in the row
     * @param errorColName the name of the error column
     * @return DataRow a data row containing missing cells and an error message
     */
    public static DataRow setMissingRow(final List<String> outputCols, final String file, final int rowKey,
        final String errorMsg, final String errorColName) {
        return setMissingRow(outputCols, file, RowKey.createRowKey((long)rowKey), errorMsg, errorColName);
    }

    /**
     * @return the list of all supported MIME types in Tika.
     */
    public static String[] getMimeTypes() {
        Iterator<MediaType> it = TikaParserConfig.VALID_TYPES.iterator();
        List<String> list = new ArrayList<String>();
        while (it.hasNext()) {
            list.add(it.next().toString());
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list.toArray(new String[list.size()]);
    }

    /**
     * @return the list of all supported extensions in Tika.
     */
    public static String[] getExtensions() {
        List<String> result = new ArrayList<String>();
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        Iterator<MediaType> mimeTypes = TikaParserConfig.VALID_TYPES.iterator();
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

    /////// getters and setters ///////

    /**
     * @return the m_authBoolean
     */
    public boolean getAuthBoolean() {
        return m_authBoolean;
    }

    /**
     * @param authBoolean the authBoolean to set
     */
    public void setAuthBoolean(final boolean authBoolean) {
        this.m_authBoolean = authBoolean;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * @param auth the password to set
     */
    public void setPassword(final String auth) {
        this.m_password = auth;
    }

    /**
     * @return the m_outputColumnsOne
     */
    public List<String> getOutputColumnsOne() {
        return m_outputColumnsOne;
    }

    /**
     * @param outputColumnsOne the outputColumnsOne to set
     */
    public void setOutputColumnsOne(final List<String> outputColumnsOne) {
        this.m_outputColumnsOne = outputColumnsOne;
    }

    /**
     * @return the m_errorColName
     */
    public String getErrorColName() {
        return m_errorColName;
    }

    /**
     * @param errorColName the errorColName to set
     */
    public void setErrorColName(final String errorColName) {
        this.m_errorColName = errorColName;
    }

    /**
     * @return the m_extBoolean
     */
    public boolean getExtBoolean() {
        return m_extBoolean;
    }

    /**
     * @param extBoolean the extBoolean to set
     */
    public void setExtBoolean(final boolean extBoolean) {
        this.m_extBoolean = extBoolean;
    }

    /**
     * @return the m_validTypes
     */
    public List<String> getValidTypes() {
        return m_validTypes;
    }

    /**
     * @param validTypes the validTypes to set
     */
    public void setValidTypes(final List<String> validTypes) {
        this.m_validTypes = validTypes;
    }

    /**
     * @return the m_duplicates
     */
    public Map<String, Integer> getDuplicates() {
        return m_duplicates;
    }

    /**
     * @param duplicates the duplicates to set
     */
    public void setDuplicates(final Map<String, Integer> duplicates) {
        this.m_duplicates = duplicates;
    }

    /**
     * @return the m_errorMsg
     */
    public String getErrorMsg() {
        return m_errorMsg;
    }

    /**
     * @return the m_extractInlineImages
     */
    public boolean getExtractInlineImages() {
        return m_extractInlineImages;
    }

    /**
     * @param extractInlineImages the boolean value to set
     */
    public void setExtractInlineImages(final boolean extractInlineImages) {
        this.m_extractInlineImages = extractInlineImages;
    }

}

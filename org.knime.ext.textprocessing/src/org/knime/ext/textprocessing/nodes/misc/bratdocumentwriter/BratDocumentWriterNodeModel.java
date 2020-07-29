/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
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
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
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
 *   Oct 18, 2018 (dewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.bratdocumentwriter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.SystemUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.CloseableRowIterator;
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
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.IndexedTerm;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;
import org.knime.ext.textprocessing.util.DocumentUtil;

/**
 * The {@link NodeModel} for the Brat Document Writer. This node writes document tags and terms in an .ann file and the
 * document text in a .txt file.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
final class BratDocumentWriterNodeModel extends NodeModel {

    /**
     * The delimiter splitting the document title from its body.
     */
    private static final String TITLE_DELIMITER = "\n";

    /**
     * Boolean to check whether the OS is windows.
     */
    private static final boolean IS_WINDOWS = SystemUtils.IS_OS_WINDOWS;

    /**
     * The Logger for BratDocumentWriterNodeModel.
     */
    private static final NodeLogger LOGGER = NodeLogger.getLogger(BratDocumentWriterNodeModel.class);

    /**
     * The SettingsModelString for the document column.
     */
    private final SettingsModelString m_docColModel = BratDocumentWriterNodeDialog.getDocColModel();

    /**
     * The SettingsModelString for the directory path.
     */
    private final SettingsModelString m_directoryModel = BratDocumentWriterNodeDialog.getDirectoryModel();

    /**
     * The SettingsModelBoolean for the overwrite flag.
     */
    private final SettingsModelBoolean m_overwriteModel = BratDocumentWriterNodeDialog.getOverwriteModel();

    /**
     * The SettingsModelString for the file name prefix.
     */
    private final SettingsModelString m_prefixModel = BratDocumentWriterNodeDialog.getPrefixModel();

    /**
     * The SettingsModelString for the file name suffix.
     */
    private final SettingsModelString m_suffixModel = BratDocumentWriterNodeDialog.getSuffixModel();

    /**
     * The constructor of the Brat Document Writer node. The node has one input and no output port.
     */
    BratDocumentWriterNodeModel() {
        super(1, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        checkDataTableSpec(inSpecs[0]);

        // check target directory
        CheckUtils.checkDestinationDirectory(m_directoryModel.getStringValue());

        // check suffix and prefix for invalid chars
        checkForInvalidChars(m_prefixModel.getStringValue());
        checkForInvalidChars(m_suffixModel.getStringValue());

        return new DataTableSpec[]{};
    }

    /**
     * Check the input data table spec.
     *
     * @param spec the data table spec to be checked
     * @throws InvalidSettingsException
     */
    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // check that input spec has at least 1 document column
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final DataTableSpec inputSpec = inData[0].getDataTableSpec();
        checkDataTableSpec(inputSpec);
        final int docColIndex = inputSpec.findColumnIndex(m_docColModel.getStringValue());

        final double rowCount = inData[0].size();
        long currRow = 0;
        try (final CloseableRowIterator it = inData[0].iterator()) {
            int countMissing = 0;

            while (it.hasNext()) {
                final DataRow row = it.next();
                // get document cell from original data table
                final DataCell docCell = row.getCell(docColIndex);
                // if cell is not missing, try to read the doc and write to files
                if (!docCell.isMissing()) {
                    final Document doc = ((DocumentValue)docCell).getDocument();
                    // add prefix and suffix to filename if available
                    // and verify that the filename does not contain forbidden symbols
                    // if it is okay, try to write to files
                    writeDocumentToFiles(doc, buildFilename(row.getKey().getString()));
                } else { // otherwise count as missing
                    countMissing++;
                    LOGGER.debug("Skipping row " + row.getKey().getString() + " since the cell is missing.");
                }
                // report status
                final long fCurrRow = ++currRow;
                exec.setProgress(currRow / rowCount, () -> "Processing document " + fCurrRow + " of " + rowCount);
                exec.checkCanceled();
            }
            if (countMissing > 0) {
                setWarningMessage("Skipped " + countMissing + " rows due to missing values.");
            }
            if (rowCount == 0) {
                setWarningMessage("Input table is empty.");
            }
        }
        return new BufferedDataTable[]{};
    }

    /**
     * Write the document text and its tags and terms to .txt and .ann files. If there is an error while writing, it
     * will first try to delete both of the files, and then throw an exception.
     *
     * @param doc the document to be stored
     * @param filename the supposed name of the file to be written
     * @throws InvalidSettingsException if the file path is problematic
     * @throws InvalidPathException if the file path looks like a file system path but is invalid
     * @throws URISyntaxException if the passed URL does not conform with RFC2396 for URIs
     * @throws IOException if an I/O error occurs
     */
    private void writeDocumentToFiles(final Document doc, final String filename)
        throws InvalidSettingsException, InvalidPathException, URISyntaxException, IOException {
        final String dirPath = m_directoryModel.getStringValue();
        // check the directory path
        final String dirWarning = CheckUtils.checkDestinationDirectory(dirPath);
        // set a warning message if there is one
        if (dirWarning != null) {
            setWarningMessage(dirWarning);
        }
        // add extensions to the filename
        final String txtFilename = dirPath + "/" + filename + ".txt";
        final String annFilename = dirPath + "/" + filename + ".ann";

        try {
            // write document text to txt file
            writeToFile(createOutputStream(txtFilename), doc.getTitle().isEmpty() ? doc.getText()
                : String.join(TITLE_DELIMITER, doc.getTitle(), doc.getDocumentBodyText()));

            // fetch and write the tags and terms
            writeToFile(createOutputStream(annFilename),
                convertToString(DocumentUtil.getIndexedTerms(doc, true, TITLE_DELIMITER)));
        } catch (final IOException e) {
            // if something is wrong mid writing, try to delete both files
            try {
                deleteFile(txtFilename);
                deleteFile(annFilename);
            } catch (final IOException ex) {
                // if an error occurs while deleting the files then
                // nothing we can do
            }
            throw e;
            // for remote files nothing will be done
        }
    }

    /**
     * Add prefix and suffix to filename if exist and then check the filename if it contains any forbidden symbol.
     *
     * @param filename the file name
     * @return verified file name with its suffix and prefix
     * @throws InvalidSettingsException if the file name contains forbidden symbol
     */
    private String buildFilename(final String filename) throws InvalidSettingsException {
        String result = filename;
        if (!m_prefixModel.getStringValue().isEmpty()) {
            result = m_prefixModel.getStringValue() + filename;
        }
        if (!m_suffixModel.getStringValue().isEmpty()) {
            result += m_suffixModel.getStringValue();
        }
        checkForInvalidChars(result);
        checkReservedNamesInWindows(result);

        return result;
    }

    /**
     * Verify the file name to make sure it does not contain any forbidden symbol.
     *
     * @param filename the file name
     * @throws InvalidSettingsException if the file name contains forbidden symbol
     */
    static void checkForInvalidChars(final String filename) throws InvalidSettingsException {
        // forbid /:?<>*"|\
        Pattern pattern = Pattern.compile("[/:?<>*\"|\\\\]");
        Matcher matcher = pattern.matcher(filename);
        if (matcher.find()) {
            final int invalidIdx = matcher.start();
            throw new InvalidSettingsException(
                "Invalid file name: contains invalid char " + filename.charAt(invalidIdx));
        }
    }

    /**
     * Check if the filename is the same as any filenames that are reserved in Windows.
     *
     * @param filename the filename to be checked
     * @throws InvalidSettingsException if the filename is a reserved name in Windows
     */
    private static void checkReservedNamesInWindows(final String filename) throws InvalidSettingsException {
        if (IS_WINDOWS) {
            Pattern forbiddenWindowsNames = Pattern.compile("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$");
            Matcher matcher = forbiddenWindowsNames.matcher(filename);
            if (matcher.find()) {
                throw new InvalidSettingsException(
                    "Invalid file name: the file name " + filename + " is forbidden on Windows.");
            }
        }
    }

    /**
     * Delete local file.
     *
     * @param filepath the to be deleted file path
     * @throws IOException if an I/O error occurs
     */
    private static void deleteFile(final String filepath) throws IOException {
        final Path path = Paths.get(filepath);
        // check if file exists, if yes then delete
        if (Files.exists(path)) {
            Files.delete(path);
        }
        // if file does not exist anyway, do nothing
    }

    /**
     * Open an output stream based on a given file path.
     *
     * @param filepath the input file path
     * @throws InvalidSettingsException if the file path is problematic
     * @throws InvalidPathException if the file path looks like a file system path but is invalid
     * @throws URISyntaxException if the passed URL does not conform with RFC2396 for URIs
     * @throws IOException if an I/O error occurs
     */
    private OutputStream createOutputStream(final String filepath)
        throws InvalidSettingsException, InvalidPathException, IOException, URISyntaxException {
        // check the validity of file path
        final String warning = CheckUtils.checkDestinationFile(filepath, m_overwriteModel.getBooleanValue());
        // set a warning message if there is one
        if (warning != null) {
            setWarningMessage(warning);
        }
        final URL url = FileUtil.toURL(filepath);
        final Path localPath = FileUtil.resolveToPath(url);
        if (localPath != null) {
            return new BufferedOutputStream(Files.newOutputStream(localPath));
        } else {
            return new BufferedOutputStream(FileUtil.openOutputStream(url, "PUT"));
        }
    }

    /**
     * Write strings to the output stream.
     *
     * @param out the output stream
     * @param content the string to be written
     * @throws IOException if an I/O error occurs
     */
    private static void writeToFile(final OutputStream out, final String content) throws IOException {
        out.write(content.getBytes());
        out.flush();
        out.close();
    }

    /**
     * Concatenate all the terms and tags into one string. Each line contains one term with one particular tag. So if a
     * term has multiple tags, each one will be written in one line.
     *
     * An example of a line is like this: T1<tab>Location 61 69<tab>Germany
     *
     * Where T1 is the term index (Brat-style), Location is the tag, both 61 and 69 are start and stop index of the term
     * respectively, while Germany is the term.
     *
     * @param list the list of the terms
     * @return the string containing all the terms
     */
    private static String convertToString(final List<IndexedTerm> list) {
        StringBuilder out = new StringBuilder();
        int idx = 1;
        for (IndexedTerm obj : list) {
            List<String> tags = obj.getTagValues();
            for (String tag : tags) {
                out.append("T" + idx++);
                out.append("\t");
                out.append(tag);
                out.append(" ");
                out.append(obj.getStartIndex());
                out.append(" ");
                out.append(obj.getStopIndex());
                out.append("\t");
                out.append(obj.getTermValue());
                out.append("\n");
            }
        }
        return out.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docColModel.saveSettingsTo(settings);
        m_directoryModel.saveSettingsTo(settings);
        m_overwriteModel.saveSettingsTo(settings);
        m_prefixModel.saveSettingsTo(settings);
        m_suffixModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_directoryModel.validateSettings(settings);
        m_overwriteModel.validateSettings(settings);
        m_prefixModel.validateSettings(settings);
        m_suffixModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_directoryModel.loadSettingsFrom(settings);
        m_overwriteModel.loadSettingsFrom(settings);
        m_prefixModel.loadSettingsFrom(settings);
        m_suffixModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do
    }

}

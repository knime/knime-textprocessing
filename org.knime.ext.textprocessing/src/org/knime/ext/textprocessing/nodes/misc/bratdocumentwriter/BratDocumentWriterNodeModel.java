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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.json.JSONObject;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowIterator;
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
import org.knime.core.util.PathUtils;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.nodes.view.bratdocumentviewer.BratDocumentViewerNodeModel;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

/**
 * The {@link NodeModel} for the Brat Document Writer. This node writes document tags and terms in an .ann file and the
 * document text in a .txt file.
 *
 * @author Andisa Dewi, KNIME AG, Berlin, Germany
 */
public class BratDocumentWriterNodeModel extends NodeModel {

    /**
     * The default target directory.
     */
    public static final String DEF_DIR = System.getProperty("user.home");

    private static final NodeLogger LOGGER = NodeLogger.getLogger(BratDocumentWriterNodeModel.class);

    private SettingsModelString m_docColModel = BratDocumentWriterNodeDialog.getDocColModel();

    private SettingsModelString m_directoryModel = BratDocumentWriterNodeDialog.getDirectoryModel();

    private SettingsModelBoolean m_overwriteModel = BratDocumentWriterNodeDialog.getOverwriteModel();

    private int m_docColIndex = -1;

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
        return new DataTableSpec[]{};
    }

    /**
     * Check the input data table spec
     *
     * @param spec the data table spec
     * @throws InvalidSettingsException
     */
    private final void checkDataTableSpec(final DataTableSpec spec) throws InvalidSettingsException {
        // check input spec
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        verifier.verifyMinimumDocumentCells(1, true);

        ColumnSelectionVerifier.verifyColumn(m_docColModel, spec, DocumentValue.class, null)
            .ifPresent(msg -> setWarningMessage(msg));

        // check target directory
        CheckUtils.checkDestinationDirectory(m_directoryModel.getStringValue());

        m_docColIndex = spec.findColumnIndex(m_docColModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {

        DataTableSpec inputSpec = inData[0].getDataTableSpec();
        checkDataTableSpec(inputSpec);

        final long rowCount = inData[0].size();
        long currRow = 1;
        final RowIterator it = inData[0].iterator();
        int countMissing = 0;
        while (it.hasNext()) {
            DataRow row = it.next();
            // get cell from original data table
            DataCell docCell = row.getCell(m_docColIndex);
            if (!docCell.isMissing()) {
                Document doc = ((DocumentValue)docCell).getDocument();
                writeDocumentToFiles(doc, row.getKey().getString());
            } else {
                countMissing++;
                LOGGER.debug("Skipping row " + row.getKey().getString() + " since the cell is missing.");
            }
            // report status
            double progress = (double)currRow / (double)rowCount;
            exec.setProgress(progress, "Processing document " + currRow + " of " + rowCount);
            exec.checkCanceled();
            currRow++;
        }
        if (countMissing > 0) {
            setWarningMessage("Skipped " + countMissing + " rows due to missing values.");
        }
        if (rowCount == 0) {
            setWarningMessage("Input table is empty.");
        }

        return new BufferedDataTable[]{};
    }

    /**
     * Write the document text and its tags and terms to .txt and .ann files
     *
     * @param doc the document to be stored
     * @param filename the supposed name of the file
     * @throws InvalidSettingsException
     * @throws IOException
     */
    private void writeDocumentToFiles(final Document doc, final String filename) throws Exception {
        synchronized (this) {
            // try to resolve the dir path
            URL remoteBaseUrl = FileUtil.toURL(m_directoryModel.getStringValue());
            Path localDir = FileUtil.resolveToPath(remoteBaseUrl);

            Path file = null;
            URL url = null;

            try {
                // build the filename for the .txt file
                String txtFilename = filename + ".txt";

                // check if path is local or remote
                if (localDir != null) {
                    file = PathUtils.resolvePath(localDir, txtFilename);
                    checkOverwriteOption(file);
                } else {
                    url = new URL(remoteBaseUrl.toString() + "/" + txtFilename);
                }

                // write document text to txt file
                writeToFile(openOutputStream(url, file), doc.getText());

                // build the filename for the .ann file
                String annFilename = filename + ".ann";

                // check if path is local or remote
                if (localDir != null) {
                    file = PathUtils.resolvePath(localDir, annFilename);
                    checkOverwriteOption(file);
                } else {
                    url = new URL(remoteBaseUrl.toString() + "/" + annFilename);
                }

                // fetch and write the tags and terms
                List<JSONObject> list = BratDocumentViewerNodeModel.processTagsAndTerms(doc);
                // write tags and terms to ann file
                writeToFile(openOutputStream(url, file), packInString(list));
            } catch (Exception e) {
                // if an error happens mid writing, try to delete the file
                if (Files.exists(file)) {
                    Files.delete(file);
                }
                // nothing can be done with remote url?
                // throw the error with the error message
                throw new Exception(e.getMessage());
            }

        }

    }

    /**
     * Write strings to the output stream
     *
     * @param out the output stream
     * @param content the string to be written
     * @throws IOException
     */
    private static void writeToFile(final OutputStream out, final String content) throws IOException {
        if (out != null) {
            out.write(content.getBytes());
            out.flush();
            out.close();
        }
    }

    /**
     * Check the overwrite option. If the overwrite flag is false and the file exists, then throw an error
     *
     * @param path the file
     * @throws IOException
     */
    private void checkOverwriteOption(final Path path) throws IOException {
        if (!m_overwriteModel.getBooleanValue()) {
            if (Files.exists(path)) {
                throw new IOException(
                    "Output file '" + path + "' exists and must not be overwritten due to user settings");
            }
        }
    }

    /**
     * Put all the JSON objects into one string. Each line contains one JSON object.
     *
     * @param list the list of the JSON objects
     * @return the string containing the JSONs
     */
    private static String packInString(final List<JSONObject> list) {
        String out = "";
        for (JSONObject obj : list) {
            out += obj.getString(BratDocumentViewerNodeModel.ID_KEY) + "\t"
                + obj.getString(BratDocumentViewerNodeModel.TAG_KEY) + " "
                + obj.getInt(BratDocumentViewerNodeModel.FIRSTPOS_KEY) + " "
                + obj.getInt(BratDocumentViewerNodeModel.LASTPOS_KEY) + "\t"
                + obj.getString(BratDocumentViewerNodeModel.TERM_KEY);
            out += "\n";
        }
        return out;
    }

    /**
     * Open an output stream based on the input URL (remote) or path (local)
     *
     * @param url the url
     * @param file the path
     * @return the output streams
     * @throws IOException
     */
    private static OutputStream openOutputStream(final URL url, final Path file) throws IOException {
        if (file != null) {
            return new BufferedOutputStream(Files.newOutputStream(file));
        } else {
            return new BufferedOutputStream(FileUtil.openOutputConnection(url, "PUT").getOutputStream());
        }
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

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.validateSettings(settings);
        m_directoryModel.validateSettings(settings);
        m_overwriteModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColModel.loadSettingsFrom(settings);
        m_directoryModel.loadSettingsFrom(settings);
        m_overwriteModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do

    }

}

/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   23.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class DictionaryReplacerNodeModel extends PreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger
    .getLogger(DictionaryReplacerNodeModel.class);

    /**
     * The default dictionary file path.
     */
    public static final String DEF_DICTFILE = System.getProperty("user.home");

    /**
     * The default valid dictionary file extensions (txt).
     */
    public static final String[] VALID_DICTFILE_EXTENIONS = new String[]{"txt"};

    /**
     * The default separator.
     */
    public static final String DEFAULT_SEPARATOR = ",";

    private SettingsModelString m_fileModel =
        DictionaryReplacerNodeDialog.getDictionaryFileModel();

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        String file = m_fileModel.getStringValue();

        File f = new File(file);
        if (!f.isFile() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected dictionary file: "
                    + file + " is not valid!");
        }
        return super.configure(inSpecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        Hashtable<String, String> dictionary = new Hashtable<String, String>();
        File f = new File(m_fileModel.getStringValue());
        if (f.exists() && f.canRead() && f.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] keyVal = line.trim().split(DEFAULT_SEPARATOR);
                    if (keyVal.length == 2) {
                        dictionary.put(keyVal[0], keyVal[1]);
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                LOGGER.warn("Not such file !");
            } catch (IOException e) {
                LOGGER.warn("Cant read from file");
            }
        }
        m_preprocessing = new DictionaryReplacer(dictionary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_fileModel.validateSettings(settings);

        ((SettingsModelString)m_fileModel
                .createCloneWithValidatedValue(settings)).getStringValue();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

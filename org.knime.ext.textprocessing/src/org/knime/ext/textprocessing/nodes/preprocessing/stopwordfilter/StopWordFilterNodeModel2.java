/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   02.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.stopwordfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.nodes.preprocessing.StreamableFunctionPreprocessingNodeModel;
import org.knime.ext.textprocessing.nodes.preprocessing.TermPreprocessing;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class StopWordFilterNodeModel2 extends StreamableFunctionPreprocessingNodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StopWordFilterNodeModel2.class);

    /** The default setting for the use of case sensitivity. */
    public static final boolean DEF_CASE_SENSITIVE = false;

    /** The default setting for the usage of build in lists. */
    public static final boolean DEF_USE_BUILIN_LIST = true;

    private SettingsModelString m_fileModel = StopWordFilterNodeDialog2.getFileModel();

    private SettingsModelBoolean m_caseModel = StopWordFilterNodeDialog2.getCaseSensitiveModel();

    private SettingsModelBoolean m_useBuildinListModel = StopWordFilterNodeDialog2.getUseBuildInListModel();

    private SettingsModelString m_buildinListModel = StopWordFilterNodeDialog2.getBuildInListModel();

    private Set<String> m_stopWords;

    /**
     * Constructor of {@link StopWordFilterNodeModel2}.
     */
    public StopWordFilterNodeModel2() {
        super();
        m_useBuildinListModel.addChangeListener(new StopwordChangeListener());
        updateModels();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TermPreprocessing createPreprocessing() throws Exception {
        Set<String> stopWords;
        if (m_useBuildinListModel.getBooleanValue()) {
            Set<String> stopWordList = BuildInStopwordListFactory.getInstance()
                    .getStopwordListByName(m_buildinListModel.getStringValue());
            stopWords = new HashSet<String>(stopWordList);
        } else {
            if (m_stopWords == null) {
                stopWords = new HashSet<String>();
            } else {
                stopWords = m_stopWords;
            }
        }

        return new StopWordFilter(stopWords, m_caseModel.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalConfigure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        if (!m_useBuildinListModel.getBooleanValue()) {
            getFile();
        }
    }

    private File getFile() throws InvalidSettingsException {
        final String file = m_fileModel.getStringValue();
        File f = null;
        try {
            // first try if file string is an URL (files in drop dir come as URLs)
            final URL url = new URL(file);
            f = FileUtil.getFileFromURL(url);
        } catch (MalformedURLException e) {
            // if no URL try string as path to file
            f = new File(file);
        }

        if (!f.isFile() || !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected stop word file: " + file + " cannot be accessed!");
        }

        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preparePreprocessing(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        if (!m_useBuildinListModel.getBooleanValue()) {
            File f = getFile();
            m_stopWords = new HashSet<String>();
            try {
                BufferedReader reader = new BufferedReader(new FileReader(f));
                String line;
                while ((line = reader.readLine()) != null) {
                    m_stopWords.add(line.trim());
                }
                reader.close();
            } catch (FileNotFoundException e) {
                LOGGER.warn("Not such stop word file.");
                throw new InvalidSettingsException(
                    "Selected stop word file: " + m_fileModel.getStringValue() + " cannot be found!");
            } catch (IOException e) {
                LOGGER.warn("Cant read from stop word file.");
                throw new InvalidSettingsException(
                    "Selected stop word file: " + m_fileModel.getStringValue() + " cannot be accessed!");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        m_stopWords = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_fileModel.loadSettingsFrom(settings);
        m_caseModel.loadSettingsFrom(settings);
        m_useBuildinListModel.loadSettingsFrom(settings);
        m_buildinListModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_fileModel.saveSettingsTo(settings);
        m_caseModel.saveSettingsTo(settings);
        m_useBuildinListModel.saveSettingsTo(settings);
        m_buildinListModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_fileModel.validateSettings(settings);
        m_caseModel.validateSettings(settings);
        m_useBuildinListModel.validateSettings(settings);
        m_buildinListModel.validateSettings(settings);
    }

    private class StopwordChangeListener implements ChangeListener {
        @Override
        public void stateChanged(final ChangeEvent arg0) {
            updateModels();
        }
    }

    private void updateModels() {
        if (m_useBuildinListModel.getBooleanValue()) {
            m_buildinListModel.setEnabled(true);
            m_fileModel.setEnabled(false);
        } else {
            m_buildinListModel.setEnabled(false);
            m_fileModel.setEnabled(true);
        }
    }
}

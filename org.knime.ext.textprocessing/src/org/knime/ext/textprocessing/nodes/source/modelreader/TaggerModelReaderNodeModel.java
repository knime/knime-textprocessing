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
 *   Oct 11, 2018 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.source.modelreader;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;

/**
 * Abstract {@code NodeModel} for tagger model reader nodes.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public abstract class TaggerModelReaderNodeModel extends NodeModel {

    /** The configuration key for storing the path to the model file. */
    private static final String CFG_KEY_PATH = "model_path";

    /** The default path. */
    private static final String DEF_PATH = System.getProperty("user.home");

    /** A {@code SettingsModelString} storing the path to the model file. */
    static final SettingsModelString createPathModel() {
        return new SettingsModelString(CFG_KEY_PATH, DEF_PATH);
    }

    /** The settings model used to store the path to the model file. */
    private final SettingsModelString m_pathModel = createPathModel();

    /**
     * Creates a new instance of {@code TaggerModelReaderNodeModel} given a specific {@link PortType}.
     *
     * @param outputPortType The {@code PortType} defining the model output port.
     */
    protected TaggerModelReaderNodeModel(final PortType outputPortType) {
        super(null, new PortType[]{outputPortType});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        CheckUtils.checkSourceFile(m_pathModel.getStringValue());
        return additionalConfiguration();
    }

    /**
     * Used to do additional configuration and return an array of {@link PortObjectSpec PortObjectSpecs} (default is an
     * array containing {@code null}. Specific implementations should override this method if needed.
     *
     * @return Returns an array of {@code PortObjectSpecs}.
     */
    protected PortObjectSpec[] additionalConfiguration() {
        return new PortObjectSpec[]{null};
    }

    /**
     * Returns the path to the model file.
     *
     * @return Returns the path to the model file.
     */
    protected final String getFilePath() {
        return m_pathModel.getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here ...

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here ...

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_pathModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_pathModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }
}

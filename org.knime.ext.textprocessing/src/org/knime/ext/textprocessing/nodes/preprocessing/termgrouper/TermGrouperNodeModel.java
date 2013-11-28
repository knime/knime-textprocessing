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
 * -------------------------------------------------------------------
 *
 * History
 *   24.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.termgrouper;

import java.io.File;
import java.io.IOException;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.AbstractPreprocessor;
import org.knime.ext.textprocessing.nodes.preprocessing.ChunkPreprocessor;
import org.knime.ext.textprocessing.nodes.preprocessing.DirectChunkPreprocessor;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeModel;


/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class TermGrouperNodeModel extends PreprocessingNodeModel {

    /**
     * The default tag grouping policy.
     */
    public static final String DEFAULT_POLICY = TermGrouper.DELETE_ALL;

    private SettingsModelString m_tagGroupingPolicyModel =
        TermGrouperNodeDialog.getTagGroupingPolicyModel();

    /**
     * Creates new instance of <code>TermGrouperNodeModel</code> with a
     * <code>ChunkPreprocessor</code>.
     */
    public TermGrouperNodeModel() {
        super();
    }

    /**
     * Creates and returns a new instance of {@link ChunkPreprocessor}.
     * @return a new instance of {@link ChunkPreprocessor}.
     * @since 2.9
     */
    @Override
    protected AbstractPreprocessor getPreprocessorForBowPP() {
        return new ChunkPreprocessor();
    }

    /**
     * Creates and returns a new instance of {@link DirectChunkPreprocessor}.
     * @return a new instance of {@link DirectChunkPreprocessor}.
     * @since 2.9
     */
    @Override
    protected AbstractPreprocessor getPreprocessorForDirectPP() {
        return new DirectChunkPreprocessor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initPreprocessing() {
        m_preprocessing = new TermGrouper(m_tagGroupingPolicyModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_tagGroupingPolicyModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_tagGroupingPolicyModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_tagGroupingPolicyModel.validateSettings(settings);
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
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
    throws IOException, CanceledExecutionException {
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
}

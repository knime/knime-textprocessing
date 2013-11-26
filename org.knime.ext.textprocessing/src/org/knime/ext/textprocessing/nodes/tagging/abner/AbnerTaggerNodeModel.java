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
 *   28.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.abner;

import java.io.File;
import java.io.IOException;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeModel;

/**
 * The node model of the ABNER (A Biomedical Named Entity Recognizer) tagger.
 * Extends {@link org.knime.core.node.NodeModel} and provides methods to
 * configure and execute the node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTaggerNodeModel extends TaggerNodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default value of the ABNER tagging model.
     */
    public static final String DEF_ABNERMODEL =
        AbnerDocumentTagger.MODEL_BIOCREATIVE;

    private SettingsModelBoolean m_setUnmodifiableModel =
        AbnerTaggerNodeDialog.createSetUnmodifiableModel();

    private SettingsModelString m_abnerTaggingModel =
        AbnerTaggerNodeDialog.createAbnerModelModel();

    /**
     * Creates a new instance of <code>AbnerTaggerNodeModel</code> with one table in and one out port.
     */
    public AbnerTaggerNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     * @since 2.9
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        return new AbnerDocumentTagger(m_setUnmodifiableModel.getBooleanValue(), m_abnerTaggingModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
        m_abnerTaggingModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.validateSettings(settings);
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
    protected void reset() { }
}

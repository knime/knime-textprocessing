/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   22.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanford;

import java.io.File;
import java.io.IOException;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeModel;

/**
 * The node model of the POS (part of speech) tagger. Extends
 * {@link org.knime.core.node.NodeModel} and provides methods to configure and
 * execute the node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StanfordTaggerNodeModel extends TaggerNodeModel {

    /**
     * Default tagger model.
     */
    public static final String DEF_MODEL = "English left 3 words";

    private SettingsModelString m_taggerModelModel =
        StanfordTaggerNodeDialog.createTaggerModelModel();

    /**
     * Creates new instance of <code>StanfordTaggerNodeModel</code> which adds
     * part of speech tags to terms of documents.
     */
    public StanfordTaggerNodeModel() {
        super();
    }

    /**
     * {@inheritDoc}
     * @since 2.9
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        return new StanfordDocumentTagger(false, m_taggerModelModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() { }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_taggerModelModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_taggerModelModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_taggerModelModel.validateSettings(settings);
    }
}

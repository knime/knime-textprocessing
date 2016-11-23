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
import org.knime.ext.textprocessing.nodes.tagging.StreamableFunctionTaggerNodeModel;

/**
 * The node model of the ABNER (A Biomedical Named Entity Recognizer) tagger. Extends
 * {@link org.knime.core.node.NodeModel} and provides methods to configure and execute the node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class AbnerTaggerNodeModel extends StreamableFunctionTaggerNodeModel {

    /**
     * The default value of the terms unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default value of the ABNER tagging model.
     */
    public static final String DEF_ABNERMODEL = AbnerDocumentTagger.MODEL_BIOCREATIVE;

    private SettingsModelBoolean m_setUnmodifiableModel = AbnerTaggerNodeDialog.createSetUnmodifiableModel();

    private SettingsModelString m_abnerTaggingModel = AbnerTaggerNodeDialog.createAbnerModelModel();

    /**
     * Creates a new instance of <code>AbnerTaggerNodeModel</code> with one table in and one out port.
     */
    public AbnerTaggerNodeModel() {
        super();
        m_tokenizer = AbnerTaggerNodeDialog.getTokenizerModel();
    }

    /**
     * {@inheritDoc}
     *
     * @since 2.9
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        return new AbnerDocumentTagger(m_setUnmodifiableModel.getBooleanValue(), m_abnerTaggingModel.getStringValue(),
            m_tokenizer.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getMaxNumberOfParallelThreads() {
        // Abner model can not be parallelized. Maximal number of parallel threads is limited here to 1.
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.loadSettingsFrom(settings);
        // only load if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizer.getKey())) {
            m_tokenizer.loadSettingsFrom(settings);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_setUnmodifiableModel.saveSettingsTo(settings);
        m_abnerTaggingModel.saveSettingsTo(settings);
        m_tokenizer.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_setUnmodifiableModel.validateSettings(settings);
        m_abnerTaggingModel.validateSettings(settings);
        // only validate if key is contained in settings (for backwards compatibility)
        if (settings.containsKey(m_tokenizer.getKey())) {
            m_tokenizer.validateSettings(settings);
        }
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }
}

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
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.io.File;
import java.io.IOException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpennlpNerTaggerNodeModel extends TaggerNodeModel {

    /**
     * The default value for the unmodifiable flag.
     */
    public static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default model.
     */
    public static final String DEF_OPENNLPMODEL =
        OpenNlpModelFactory.getInstance().getDefaultName();

    /**
     * The default value for the dictionary flag.
     */
    public static final boolean DEFAULT_USE_DICT = false;

    /**
     * The default dictionary file location.
     */
    public static final String DEFAULT_DICT_FILENAME =
        System.getProperty("user.home");

    /**
     * The default model file location.
     * @since 2.7
     */
    public static final String DEFAULT_MODEL_FILENAME =
        System.getProperty("user.home");

    private SettingsModelBoolean m_unmodifiableModel =
        OpenNlpNerNodeDialog.createSetUnmodifiableModel();

    private SettingsModelString m_modelNameModel =
        OpenNlpNerNodeDialog.createOpenNlpModelModel();

    private SettingsModelBoolean m_useDictFileModel =
        OpenNlpNerNodeDialog.createUseDictModel();

    private SettingsModelString m_modelFileModel =
            OpenNlpNerNodeDialog.createModelFileModel();

    /**
     * Creates new instance of <code>OpennlpNerTaggerNodeModel</code>.
     */
    public OpennlpNerTaggerNodeModel() {
        super();
        m_useDictFileModel.addChangeListener(new SettingsChangeListener());
        checkSettings();
    }

    /**
     * {@inheritDoc}
     * @since 2.9
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        final DocumentTagger tagger;

        final String modelFileName;
        if (m_useDictFileModel.getBooleanValue()) {
            modelFileName = m_modelFileModel.getStringValue();
            tagger =
                new OpennlpNerDocumentTagger(m_unmodifiableModel.getBooleanValue(), m_modelNameModel.getStringValue(),
                    modelFileName);

        } else {
            tagger =
                new OpennlpNerDocumentTagger(m_unmodifiableModel.getBooleanValue(), OpenNlpModelFactory.getInstance()
                    .getModelByName(m_modelNameModel.getStringValue()));
        }

        return tagger;
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
        m_unmodifiableModel.loadSettingsFrom(settings);
        m_modelNameModel.loadSettingsFrom(settings);
        m_useDictFileModel.loadSettingsFrom(settings);
        m_modelFileModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_modelNameModel.saveSettingsTo(settings);
        m_unmodifiableModel.saveSettingsTo(settings);
        m_useDictFileModel.saveSettingsTo(settings);
        m_modelFileModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        super.validateSettings(settings);
        m_modelNameModel.validateSettings(settings);
        m_unmodifiableModel.validateSettings(settings);
        m_useDictFileModel.validateSettings(settings);
        m_modelFileModel.validateSettings(settings);

        boolean useDictFile = ((SettingsModelBoolean)m_useDictFileModel
                .createCloneWithValidatedValue(settings)).getBooleanValue();
        if (useDictFile) {
            String file = ((SettingsModelString)m_modelFileModel
                    .createCloneWithValidatedValue(settings)).getStringValue();
            File f = new File(file);
            if (!f.isFile() || !f.exists() || !f.canRead()) {
                throw new InvalidSettingsException("Selected model file: "
                        + file + " is not valid!");
            }
        }
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

    private class SettingsChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent arg0) {
            checkSettings();
        }

    }

    private void checkSettings() {
        if (m_useDictFileModel.getBooleanValue()) {
            m_modelFileModel.setEnabled(true);
        } else {
            m_modelFileModel.setEnabled(false);
        }
    }
}

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
 *   25.08.2016 (David Kolb): created
 */
package org.knime.ext.textprocessing.dl4j.nodes.embeddings.learn.w2v.dialog;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.dl4j.base.nodes.dialog.AbstractGridBagDialogComponentGroup;
import org.knime.ext.dl4j.base.util.EnumUtils;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.ElementsLearningAlgorithm;
import org.knime.ext.textprocessing.dl4j.settings.enumerate.WordVectorLearnerParameter;
import org.knime.ext.textprocessing.dl4j.settings.impl.WordVectorParameterSettingsModels2;

/**
 * Implementation of a AbstractGridBagDialogComponentGroup containing learning parameter.
 *
 * @author David Kolb, KNIME.com GmbH
 */
public class Word2VecLearningParametersComponentGroup extends AbstractGridBagDialogComponentGroup {

    private SettingsModelIntegerBounded m_layerSizeSettings;
    private SettingsModelIntegerBounded m_batchSizeSettings;
    private SettingsModelIntegerBounded m_trainingIterationsSettings;
    private SettingsModelIntegerBounded m_epochsSettings;
    private SettingsModelIntegerBounded m_seedSettings;
    private SettingsModelIntegerBounded m_windowSizeSettings;
    private SettingsModelIntegerBounded m_minWordFreqSizeSettings;

    private SettingsModelDoubleBounded m_samplingSettings;
    private SettingsModelDoubleBounded m_negativeSamplingSettings;
    private SettingsModelDoubleBounded m_learningRateSettings;
    private SettingsModelDoubleBounded m_minLearningRateSettings;

    private SettingsModelString m_elementsAlgoSettings;

    private SettingsModelBoolean m_useHsSettings;
    private SettingsModelBoolean m_skipMissingSettings;

    /**
     * @param settings
     */
    public Word2VecLearningParametersComponentGroup(final WordVectorParameterSettingsModels2 settings) {
        m_layerSizeSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.LAYER_SIZE);
        m_batchSizeSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.BATCH_SIZE);
        m_trainingIterationsSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.TRAINING_ITERATIONS);
        m_epochsSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.EPOCHS);
        m_seedSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.SEED);
        m_windowSizeSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.WINDOW_SIZE);
        m_minWordFreqSizeSettings = (SettingsModelIntegerBounded)settings.createParameter(WordVectorLearnerParameter.MIN_WORD_FREQUENCY);

        m_samplingSettings = (SettingsModelDoubleBounded)settings.createParameter(WordVectorLearnerParameter.SAMPLING);
        m_negativeSamplingSettings = (SettingsModelDoubleBounded)settings.createParameter(WordVectorLearnerParameter.NEGATIVE_SAMPLING);
        m_learningRateSettings = (SettingsModelDoubleBounded)settings.createParameter(WordVectorLearnerParameter.LEARNING_RATE);
        m_minLearningRateSettings = (SettingsModelDoubleBounded)settings.createParameter(WordVectorLearnerParameter.MIN_LEARNING_RATE);

        m_elementsAlgoSettings = (SettingsModelString)settings.createParameter(WordVectorLearnerParameter.ELEMENTS_LEARNING_ALGO);

        m_useHsSettings = (SettingsModelBoolean)settings.createParameter(WordVectorLearnerParameter.USE_HIERARCHICAL_SOFTMAX);
        m_skipMissingSettings = (SettingsModelBoolean)settings.createParameter(WordVectorLearnerParameter.SKIP_MISSING_CELLS);

        addWhitespaceRow(2);
        addNumberEditRowComponent(m_learningRateSettings, "Learning Rate");
        addNumberEditRowComponent(m_minLearningRateSettings, "Minimum Learning Rate");
        addNumberEditRowComponent(m_layerSizeSettings, "Layer Size");
        addNumberEditRowComponent(m_batchSizeSettings, "Batch Size");
        addNumberEditRowComponent(m_seedSettings, "Seed");
        addNumberEditRowComponent(m_epochsSettings, "Number of Epochs");
        addNumberEditRowComponent(m_trainingIterationsSettings, "Number of Training Iterations");
        addNumberEditRowComponent(m_windowSizeSettings, "Context Window Size");
        addNumberEditRowComponent(m_minWordFreqSizeSettings, "Minimum Word Frequency");
        addNumberEditRowComponent(m_samplingSettings, "Sampling Rate");

        addCheckboxRow(m_useHsSettings, "Use Hierarchical Softmax?", true);
        addNumberEditRowComponent(m_negativeSamplingSettings, "Negative Sampling Rate");

        m_useHsSettings.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                updateEnableStatus();
            }
        });

        addComboBoxRow(m_elementsAlgoSettings, "Elements Learning Algorithm",
            EnumUtils.getStringCollectionFromToString(ElementsLearningAlgorithm.values()));

        addHorizontalSeparator();
        addCheckboxRow(m_skipMissingSettings, "Skip Missing Cells?", true);
    }

    private void updateEnableStatus() {
        m_negativeSamplingSettings.setEnabled(!m_useHsSettings.getBooleanValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        super.loadSettingsFrom(settings, specs);
        updateEnableStatus();
    }
}

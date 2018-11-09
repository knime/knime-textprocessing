/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.data.OpenNlpNerTaggerModelPortObject;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.MissingTaggerModelException;
import org.knime.ext.textprocessing.nodes.tagging.StreamableFunctionTaggerNodeModel2;
import org.knime.ext.textprocessing.nodes.tagging.StreamableTaggerNodeModel2;
import org.knime.ext.textprocessing.nodes.tagging.dict.CommonDictionaryTaggerSettingModels;

/**
 * The node model of the OpenNLP NER tagger node. This node extends {@link StreamableFunctionTaggerNodeModel2} which
 * provides streaming functionality.
 *
 * @author Kilian Thiel, KNIME.com GmbH, Berlin, Germany
 * @since 3.5
 */
final class OpennlpNerTaggerNodeModel2 extends StreamableTaggerNodeModel2 {

    /** The configuration key for the OpenNLP tagging model. */
    private static final String CFGKEY_MODEL = "OPENNLP Model";

    /** The configuration key for the model from inport option. */
    private static final String CFGKEY_USE_MODEL_FROM_INPORT = "Use Model From Inport";

    /** The default model. */
    private static final String DEF_OPENNLPMODEL = OpenNlpModelFactory.getInstance().getDefaultName();

    /** The default value for the "use input model" option. */
    private static final boolean DEF_USE_INPORT_MODEL = false;

    /** The index of the optional model input port. */
    private static final int MODEL_PORT_IDX = 1;

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the name of
     * the OpenNLP tagging model to use.
     *
     * @return A {@code SettingsModelString} containing the the name of the OpenNLP tagging model to use.
     */
    static final SettingsModelString createOpenNlpModelModel() {
        return new SettingsModelString(CFGKEY_MODEL, DEF_OPENNLPMODEL);
    }

    /**
     * Creates and returns a {@link SettingsModelBoolean} for the inport model flag.
     *
     * @return Returns a {@code SettingsModelBoolean} for the inport model flag.
     */
    static final SettingsModelBoolean createUseInportModelModel() {
        return new SettingsModelBoolean(CFGKEY_USE_MODEL_FROM_INPORT, DEF_USE_INPORT_MODEL);
    }

    /**
     * A {@code SettingsModelBoolean} for the "set unmodifiable" option.
     */
    private final SettingsModelBoolean m_unmodifiableModel =
        CommonDictionaryTaggerSettingModels.createSetUnmodifiableModel();

    /**
     * A {@code SettingsModelString} that keeps the name of the selected OpenNLP model.
     */
    private final SettingsModelString m_modelNameModel = createOpenNlpModelModel();

    /**
     * A {@code SettingsModelBoolean} for the "use input model" option.
     */
    private final SettingsModelBoolean m_useInportModel = createUseInportModelModel();

    /**
     * A {@code SettingsModelString} that keeps the name of the selected named-entity tag value.
     */
    private final SettingsModelString m_tagValueModel = CommonDictionaryTaggerSettingModels.createTagModel();

    /**
     * A String to save the path of the connected model.
     */
    private OpenNlpModel m_inputModel;

    /**
     * Creates new instance of {@code OpennlpNerTaggerNodeModel2}.
     */
    OpennlpNerTaggerNodeModel2() {
        super(
            new PortType[]{BufferedDataTable.TYPE,
                PortTypeRegistry.getInstance().getPortType(OpenNlpNerTaggerModelPortObject.class, true)},
            new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInputPortSpecs(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        // check for optional model input and enable/disable "own model" flag.
        final boolean hasModelInput = inSpecs[MODEL_PORT_IDX] != null;
        if (useInputPortModel() && !hasModelInput) {
            throw new InvalidSettingsException(
                "No input model port connected. Can not use model from input model port!");
        }

        // check if tagger model exists in current installation
        if ((!hasModelInput || !useInputPortModel())
            && (!OpenNlpModelFactory.getInstance().getModelNames().contains(m_modelNameModel.getStringValue()))) {
            throw new MissingTaggerModelException(m_modelNameModel.getStringValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareTagger(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        m_inputModel = useInputPortModel()
            ? new OpenNlpModel(((OpenNlpNerTaggerModelPortObject)inObjects[MODEL_PORT_IDX]).getModel(),
                m_tagValueModel.getStringValue())
            : OpenNlpModelFactory.getInstance().getModelByName(m_modelNameModel.getStringValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        return new OpennlpNerDocumentTagger(m_unmodifiableModel.getBooleanValue(), m_inputModel, getTokenizerName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_useInportModel.saveSettingsTo(settings);
        m_modelNameModel.saveSettingsTo(settings);
        m_unmodifiableModel.saveSettingsTo(settings);
        m_tagValueModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        if (settings.containsKey(m_useInportModel.getConfigName())) {
            m_useInportModel.loadSettingsFrom(settings);
        }
        if (settings.containsKey(m_tagValueModel.getKey())) {
            m_tagValueModel.loadSettingsFrom(settings);
        }
        m_modelNameModel.loadSettingsFrom(settings);
        m_unmodifiableModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        if (settings.containsKey(m_useInportModel.getConfigName())) {
            m_useInportModel.validateSettings(settings);
        }
        if (settings.containsKey(m_tagValueModel.getKey())) {
            m_tagValueModel.validateSettings(settings);
        }
        m_modelNameModel.validateSettings(settings);
        m_unmodifiableModel.validateSettings(settings);
    }

    /**
     * Checks if the input model should be used.
     *
     * @return True, if the input model should be used.
     */
    private final boolean useInputPortModel() {
        return m_useInportModel.isEnabled() && m_useInportModel.getBooleanValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do ...
    }
}

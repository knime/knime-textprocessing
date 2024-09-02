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
 *   30.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger;

import java.io.File;
import java.io.IOException;

import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.core.node.streamable.InputPortRole;
import org.knime.ext.textprocessing.data.NERModelPortObjectSpec;
import org.knime.ext.textprocessing.data.StanfordNERModelPortObject;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.nodes.tagging.DocumentTagger;
import org.knime.ext.textprocessing.nodes.tagging.MissingTaggerModelException;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModelRegistry;
import org.knime.ext.textprocessing.nodes.tagging.StreamableTaggerNodeModel2;
import org.knime.ext.textprocessing.preferences.PreferenceUtil;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

/**
 * The {@link NodeModel} for the StanfordNLP NE tagger node. This node extends the {@link StreamableTaggerNodeModel2}.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class StanfordNlpNeTaggerNodeModel2 extends StreamableTaggerNodeModel2 {

    /**
     * The default key for the unmodifiable flag.
     */
    static final boolean DEFAULT_UNMODIFIABLE = true;

    /**
     * The default StanfordNlpModel.
     */
    static final String DEF_STANFORDNLPMODEL = "English 3 classes distsim";

    /**
     * The default key for the own model flag.
     */
    static final boolean DEF_USE_INPORT_MODEL = false;

    /**
     * The default key for combining successive terms with same tag.
     */
    static final boolean DEFAULT_COMBINE_MULTIWORDS = true;

    // the settings models

    private final SettingsModelString m_classifierModel = StanfordNlpNeTaggerNodeDialog2.createStanfordNeModelModel();

    private final SettingsModelBoolean m_unmodifiableModel =
        StanfordNlpNeTaggerNodeDialog2.createSetUnmodifiableModel();

    private final SettingsModelBoolean m_useInportModel = StanfordNlpNeTaggerNodeDialog2.createUseInportModelModel();

    private final SettingsModelBoolean m_combineMultiWords =
        StanfordNlpNeTaggerNodeDialog2.createCombineMultiWordsModel();

    // initialize member variables for port object and its information

    private boolean m_hasModelInput = false;

    private CRFClassifier<CoreLabel> m_inputModel;

    private Tag m_tag;

    /**
     * The constructor of the {@code StanfordNlpNeTaggerNodeModel2}, which creates a node with two inports (
     * {@code DataTable} and {@code NLPNeModelPortObject}) and two outports ({@code DataTable}s: One for the tagged
     * documents and one for scores).
     */
    StanfordNlpNeTaggerNodeModel2() {
        super(
            new PortType[]{BufferedDataTable.TYPE,
                PortTypeRegistry.getInstance().getPortType(StanfordNERModelPortObject.class, true)},
            new InputPortRole[]{InputPortRole.NONDISTRIBUTED_NONSTREAMABLE});

        m_useInportModel.addChangeListener(e -> checkSettings());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInputPortSpecs(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        // check for optional model input and enable/disable "own model" flag.
        if (inSpecs[1] != null) {
            m_hasModelInput = true;
            NERModelPortObjectSpec spec = (NERModelPortObjectSpec)inSpecs[1];
            String tokenizer = spec.getTokenizerName();
            // compare tokenizer provided by model to tokenizer selected in node dialog
            if (tokenizer != null) {
                if (!tokenizer.equals(getTokenizerName())) {
                    setWarningMessage("Tokenization of input model (" + tokenizer
                        + ") differs to selected tokenization (" + getTokenizerName() + ").");
                }
            } else if (!getTokenizerName().equals(PreferenceUtil.getDefaultTokenizer())) {
                setWarningMessage(
                    "Tokenization of input model (" + PreferenceUtil.getDefaultTokenizer()
                        + ") differs to selected tokenization (" + getTokenizerName() + ").");
            }
        } else {
            m_hasModelInput = false;
        }
        checkInputModel();

        // check if tagger model exists in current installation
        if ((!m_hasModelInput || !m_useInportModel.getBooleanValue()) && (!StanfordTaggerModelRegistry.getInstance()
            .getNerTaggerModelMap().containsKey(m_classifierModel.getStringValue()))) {
            throw new MissingTaggerModelException(m_classifierModel.getStringValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void prepareTagger(final PortObject[] inPortObjects, final ExecutionContext exec) throws Exception {
        if (inPortObjects[1] != null) {
            m_useInportModel.setEnabled(true);

        } else {
            m_useInportModel.setBooleanValue(false);
            m_useInportModel.setEnabled(false);
        }
        // get the port object, model, the dictionary and the tag to build the model, if needed
        if (m_useInportModel.getBooleanValue()) {
            StanfordNERModelPortObject inputModelPortObject = (StanfordNERModelPortObject)inPortObjects[1];
            m_inputModel = inputModelPortObject.getNERModel();
            m_tag = inputModelPortObject.getTag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DocumentTagger createTagger() throws Exception {
        // create the tagger
        final DocumentTagger tagger;
        final String modelName;
        if (m_useInportModel.isEnabled() && m_useInportModel.getBooleanValue()) {
            tagger = new StanfordNlpNeDocumentTagger(m_unmodifiableModel.getBooleanValue(),
                m_combineMultiWords.getBooleanValue(), m_inputModel, m_tag, getTokenizerName());
        } else {
            modelName = m_classifierModel.getStringValue();
            tagger = new StanfordNlpNeDocumentTagger(m_unmodifiableModel.getBooleanValue(),
                m_combineMultiWords.getBooleanValue(), modelName, getTokenizerName());
        }
        return tagger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // empty ...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        m_classifierModel.saveSettingsTo(settings);
        m_unmodifiableModel.saveSettingsTo(settings);
        m_useInportModel.saveSettingsTo(settings);
        m_combineMultiWords.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadValidatedSettingsFrom(settings);
        m_classifierModel.loadSettingsFrom(settings);
        m_unmodifiableModel.loadSettingsFrom(settings);
        m_useInportModel.loadSettingsFrom(settings);
        m_combineMultiWords.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.validateSettings(settings);
        m_classifierModel.validateSettings(settings);
        m_unmodifiableModel.validateSettings(settings);
        m_useInportModel.validateSettings(settings);
        m_combineMultiWords.validateSettings(settings);
        checkInputModel();
    }

    private void checkInputModel() throws InvalidSettingsException {
        // sanity check if copied node uses input model but the input model is not connected.
        if (m_useInportModel.getBooleanValue() && !m_hasModelInput) {
            m_useInportModel.setBooleanValue(false);
            throw new InvalidSettingsException(
                "No input model port connected. Can not use model from input model port!");
        }

        if (!m_hasModelInput) {
            m_useInportModel.setBooleanValue(false);
            m_useInportModel.setEnabled(false);
        } else {
            m_useInportModel.setEnabled(true);
        }
    }

    private void checkSettings() {
        if (m_useInportModel.isEnabled() && m_useInportModel.getBooleanValue()) {
            m_classifierModel.setEnabled(false);
        } else if (!m_useInportModel.isEnabled() || !m_useInportModel.getBooleanValue()) {
            m_classifierModel.setEnabled(true);
        }
    }
}

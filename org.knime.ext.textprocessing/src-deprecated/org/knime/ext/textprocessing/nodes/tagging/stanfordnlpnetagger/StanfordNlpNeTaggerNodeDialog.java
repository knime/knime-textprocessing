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
 *   30.06.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnetagger;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.ext.textprocessing.nodes.tagging.StanfordTaggerModelRegistry;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 * @deprecated Use custom node dialog instead.
 */
@Deprecated
public class StanfordNlpNeTaggerNodeDialog extends TaggerNodeSettingsPane {

    /**
     * @return Returns a SettingsModelBoolean for the unmodifiable flag.
     */
    public static SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(StanfordNlpNeTaggerConfigKeys.CFGKEY_UNMODIFIABLE,
            StanfordNlpNeTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }

    /**
     * @return Returns a SettingsModelBoolean for the own model flag.
     */
    public static SettingsModelBoolean createUseInportModelModel() {
        return new SettingsModelBoolean(StanfordNlpNeTaggerConfigKeys.CFGKEY_USE_INPORT_MODEL,
            StanfordNlpNeTaggerNodeModel.DEF_USE_INPORT_MODEL);
    }

    /**
     * @return Returns a SettingsModelString for the available StanfordNLP NE models
     */
    public static SettingsModelString createStanfordNeModelModel() {
        return new SettingsModelString(StanfordNlpNeTaggerConfigKeys.CFGKEY_STANFORDNLPMODEL,
            StanfordNlpNeTaggerNodeModel.DEF_STANFORDNLPMODEL);
    }

    /**
     * @return Returns a SettingModelBoolean for combining multi-words.
     */
    public static SettingsModelBoolean createCombineMultiWordsModel() {
        return new SettingsModelBoolean(StanfordNlpNeTaggerConfigKeys.CFGKEY_COMBINE_MULTIWORDS,
            StanfordNlpNeTaggerNodeModel.DEFAULT_COMBINE_MULTIWORDS);
    }

    // create member variables for the settings models

    private SettingsModelBoolean m_useInportModel;

    private SettingsModelString m_classifierModel;

    private SettingsModelBoolean m_unmodifiableModel;

    private boolean m_hasModelInput;

    /**
     * The constructor of the {@code StanfordNlpNeTaggerNodeDialog}.
     */
    public StanfordNlpNeTaggerNodeDialog() {
        super();
        createNewTab("Tagger Options");
        setSelected("Tagger Options");

        m_unmodifiableModel = createSetUnmodifiableModel();
        m_useInportModel = createUseInportModelModel();
        m_useInportModel.addChangeListener(new InternalChangeListener());
        m_classifierModel = createStanfordNeModelModel();

        addDialogComponent(new DialogComponentBoolean(m_unmodifiableModel, "Set named entities unmodifiable"));

        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(m_useInportModel, "Use model from input port"));

        setHorizontalPlacement(false);

        addDialogComponent(new DialogComponentBoolean(createCombineMultiWordsModel(), "Combine multi-words"));

        SortedSet<String> models =
            new TreeSet<String>(StanfordTaggerModelRegistry.getInstance().getNerTaggerModelMap().keySet());
        addDialogComponent(new DialogComponentStringSelection(m_classifierModel, "Built-in tagger model", models));
        checkSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        super.loadAdditionalSettingsFrom(settings, specs);

        if (specs[1] != null) {
            m_hasModelInput = true;
        } else if (specs[1] == null) {
            m_hasModelInput = false;
        }

        checkInputModel();
    }

    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {

            // disable string selection if own model flag is checked
            // disable validation check box if own model flag is unchecked
            if (m_useInportModel.isEnabled() && m_useInportModel.getBooleanValue()) {
                m_classifierModel.setEnabled(false);
            } else if (!m_useInportModel.getBooleanValue()) {
                m_classifierModel.setEnabled(true);
            }
            checkSettings();
        }
    }

    private void checkSettings() {
        if (m_useInportModel.isEnabled() && m_useInportModel.getBooleanValue()) {
            m_classifierModel.setEnabled(false);
        } else if (!m_useInportModel.isEnabled() || !m_useInportModel.getBooleanValue()) {
            m_classifierModel.setEnabled(true);
        }
    }

    private void checkInputModel() {
        if (!m_hasModelInput) {
            m_useInportModel.setBooleanValue(false);
            m_useInportModel.setEnabled(false);
        } else {
            m_useInportModel.setEnabled(true);
        }
    }

}

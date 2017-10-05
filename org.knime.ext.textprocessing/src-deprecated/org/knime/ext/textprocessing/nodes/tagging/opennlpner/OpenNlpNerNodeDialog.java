/*
 * ------------------------------------------------------------------------
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
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane;

/**
 * @author Kilian Thiel, University of Konstanz
 * @deprecated Use custom node dialog instead.
 */
@Deprecated
public class OpenNlpNerNodeDialog extends TaggerNodeSettingsPane {

    /**
     * Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the user settings whether terms representing named entities
     * have to be set unmodifiable or not.
     *
     * @return A <code>SettingsModelBoolean</code> containing the terms
     * unmodifiable flag.
     */
    public static SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(
                OpenNlpTaggerConfigKeys.CFGKEY_UNMODIFIABLE,
                OpennlpNerTaggerNodeModel.DEFAULT_UNMODIFIABLE);
    }

    /**
     * Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelString}
     * containing the name of the OPNENLP tagging model to use.
     *
     * @return A <code>SettingsModelString</code> containing the the name of
     * the ABNER tagging model to use.
     */
    public static SettingsModelString createOpenNlpModelModel() {
        return new SettingsModelString(
                OpenNlpTaggerConfigKeys.CFGKEY_MODEL,
                OpennlpNerTaggerNodeModel.DEF_OPENNLPMODEL);
    }

    /**
     * Creates and returns a
     * {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean}
     * containing the user settings whether a dictionary is used or not.
     *
     * @return A <code>SettingsModelBoolean</code> containing the dictionary
     * flag.
     */
    public static SettingsModelBoolean createUseDictModel() {
        return new SettingsModelBoolean(
                OpenNlpTaggerConfigKeys.CFGKEY_USE_DICT,
                OpennlpNerTaggerNodeModel.DEFAULT_USE_DICT);
    }

    /**
     * @return Creates and returns a <code>SettingsModelString</code> containing
     * the file name of the dictionary file.
     */
    public static SettingsModelString createDictFileModel() {
        return new SettingsModelString(
                OpenNlpTaggerConfigKeys.CFGKEY_DICTFILE,
                OpennlpNerTaggerNodeModel.DEFAULT_DICT_FILENAME);
    }

    /**
     * @return Creates and returns a <code>SettingsModelString</code> containing
     * the file name of the model file.
     * @since 2.7
     */
    public static SettingsModelString createModelFileModel() {
        return new SettingsModelString(
                OpenNlpTaggerConfigKeys.CFGKEY_MODELFILE,
                OpennlpNerTaggerNodeModel.DEFAULT_MODEL_FILENAME);
    }

    private SettingsModelBoolean m_useDictFileModel;

    private SettingsModelString m_modelFileModel;

    /**
     * Creates a new instance of <code>OpenNlpNerNodeDialog</code> providing
     * a checkbox enabling the user to specify whether terms representing named
     * entities have to be set unmodifiable or not.
     */
    public OpenNlpNerNodeDialog() {
        super();
        createNewTab("Tagger options");
        setSelected("Tagger options");

        addDialogComponent(new DialogComponentBoolean(
                createSetUnmodifiableModel(),
                "Set named entities unmodifiable"));

        List<String> modelNames = new ArrayList<String>();
        for (String name : OpenNlpModelFactory.getInstance().getModelNames()) {
            modelNames.add(name);
        }
        addDialogComponent(new DialogComponentStringSelection(
                createOpenNlpModelModel(), "OpenNlp model", modelNames));


        m_useDictFileModel = createUseDictModel();
        m_useDictFileModel.addChangeListener(new SettingsChangeListener());
        addDialogComponent(new DialogComponentBoolean(
                m_useDictFileModel, "Use external OpenNLP model file"));

        m_modelFileModel = createModelFileModel();
        addDialogComponent(new DialogComponentFileChooser(
                m_modelFileModel, OpenNlpNerNodeDialog.class.toString(),
                "bin"));

        checkSettings();
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

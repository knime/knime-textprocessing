/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @author Kilian Thiel, University of Konstanz
 *
 */
public class OpenNlpNerNodeDialog extends DefaultNodeSettingsPane {

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

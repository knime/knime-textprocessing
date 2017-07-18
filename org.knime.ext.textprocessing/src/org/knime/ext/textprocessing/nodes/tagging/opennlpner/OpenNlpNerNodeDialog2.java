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
 * -------------------------------------------------------------------
 *
 * History
 *   28.09.2009 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.opennlpner;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tagging.TaggerNodeSettingsPane2;

/**
 * The node dialog class for the OpenNLP NER tagger node.
 *
 * @author Kilian Thiel, KNIME.com GmbH, Berlin, Germany
 * @since 3.5
 */
class OpenNlpNerNodeDialog2 extends TaggerNodeSettingsPane2 {

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelBoolean} containing the user
     * settings whether terms representing named entities have to be set unmodifiable or not.
     *
     * @return A {@code SettingsModelBoolean} containing the terms unmodifiable flag.
     */
    static SettingsModelBoolean createSetUnmodifiableModel() {
        return new SettingsModelBoolean(OpenNlpTaggerConfigKeys2.CFGKEY_UNMODIFIABLE,
            OpennlpNerTaggerNodeModel2.DEFAULT_UNMODIFIABLE);
    }

    /**
     * Creates and returns a {@link org.knime.core.node.defaultnodesettings.SettingsModelString} containing the name of
     * the OPNENLP tagging model to use.
     *
     * @return A {@code SettingsModelString} containing the the name of the ABNER tagging model to use.
     */
    static SettingsModelString createOpenNlpModelModel() {
        return new SettingsModelString(OpenNlpTaggerConfigKeys2.CFGKEY_MODEL,
            OpennlpNerTaggerNodeModel2.DEF_OPENNLPMODEL);
    }

    /**
     * Creates a new instance of {@code OpenNlpNerNodeDialog2} providing a checkbox enabling the user to specify whether
     * terms representing named entities have to be set unmodifiable or not.
     */
    OpenNlpNerNodeDialog2() {
        super();
        createNewTab("Tagger options");
        setSelected("Tagger options");

        addDialogComponent(new DialogComponentBoolean(createSetUnmodifiableModel(), "Set named entities unmodifiable"));

        List<String> modelNames = new ArrayList<String>();
        for (String name : OpenNlpModelFactory.getInstance().getModelNames()) {
            modelNames.add(name);
        }
        addDialogComponent(new DialogComponentStringSelection(createOpenNlpModelModel(), "OpenNlp model", modelNames));
    }
}

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
 *   29.10.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.hyphenator;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class HyphenatorNodeDialog2 extends PreprocessingNodeSettingsPane2 {

    /**
     * @return Creates and returns a new instance of {@code SettingsModelString} containing the language.
     */
    public static final SettingsModelString getLanguageModel() {
        return new SettingsModelString(HyphenatorConfigKeys.CFGKEY_LANG, HyphenatorNodeModel2.DEF_LANGUAGE);
    }

    /**
     * @return Creates and returns a new instance of {@code SettingsModelString} containing the separator character.
     */
    public static final SettingsModelString getSeparatorModel() {
        return new SettingsModelString(HyphenatorConfigKeys.CFGKEY_SEPARATOR, HyphenatorNodeModel2.DEF_SEPARATOR);
    }

    /**
     * Creates new instance of {@code HyphenatorNodeDialog2}.
     */
    public HyphenatorNodeDialog2() {
        super();
        createNewTab("Hyphenation options");
        setSelected("Hyphenation options");

        List<String> languages = new ArrayList<String>(HyphenationPatternFactory.getInstance().getLanguages());
        addDialogComponent(new DialogComponentStringSelection(getLanguageModel(), "Language", languages));

        addDialogComponent(new DialogComponentString(getSeparatorModel(), "Separator"));
    }
}

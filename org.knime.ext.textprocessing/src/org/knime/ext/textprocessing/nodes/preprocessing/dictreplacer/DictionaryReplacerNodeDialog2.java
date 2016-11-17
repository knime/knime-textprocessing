/*
 * ------------------------------------------------------------------------
 *
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
 *   12.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer;

import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactory;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

import com.google.common.collect.ImmutableMap;

/**
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class DictionaryReplacerNodeDialog2 extends PreprocessingNodeSettingsPane2 {

    /**
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the path to the dictionary
     *         file.
     */
    public static final SettingsModelString getDictionaryFileModel() {
        return new SettingsModelString(DictionaryReplacerConfigKeys2.CFGKEY_DICTFILE,
            DictionaryReplacerNodeModel2.DEF_DICTFILE);
    }

    /**
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the name of the tokenizer
     *         used for word tokenization.
     * @since 3.3
     */
    public static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(DictionaryReplacerConfigKeys2.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates new instance of {@link DictionaryReplacerNodeDialog2}.
     */
    public DictionaryReplacerNodeDialog2() {
        super();

        createNewTab("Dictionary options");
        setSelected("Dictionary options");

        addDialogComponent(
            new DialogComponentFileChooser(getDictionaryFileModel(), DictionaryReplacerNodeDialog2.class.toString(),
                JFileChooser.FILES_ONLY, DictionaryReplacerNodeModel2.VALID_DICTFILE_EXTENIONS));

        Set<String> tokenizerList = new TreeSet<String>();
        for (ImmutableMap.Entry<String, TokenizerFactory> entry : TokenizerFactoryRegistry.getTokenizerFactoryMap()
            .entrySet()) {
            tokenizerList.add(entry.getKey());
        }
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));
    }
}

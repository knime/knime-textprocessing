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
 *   12.11.2015 (Kilian): created
 */
package org.knime.ext.textprocessing.nodes.preprocessing.dictreplacer.twoinports;

import java.util.Collection;
import java.util.stream.Collectors;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentOptionalString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelOptionalString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.preprocessing.PreprocessingNodeSettingsPane2;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * The {@code NodeDialog} for the Dictionary Replacer node.
 *
 * @author Kilian Thiel, KNIME.com, Berlin, Germany
 * @since 3.1
 */
public final class DictionaryReplacer2InPortsNodeDialog2 extends PreprocessingNodeSettingsPane2 {

    /**
     * Creates and returns a new instance of {@link SettingsModelString} containing the name of the column with the
     * strings to replace.
     *
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the name of the column with
     *         the strings to replace.
     */
    public static final SettingsModelString getReplaceColumnModel() {
        return new SettingsModelString(DictionaryReplacer2InPortsConfigKeys2.CFGKEY_KEYCOLUMN, "");
    }

    /**
     * Creates and returns a new instance of {@link SettingsModelString} containing the name of the column with the
     * strings to use as replacement.
     *
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the name of the column with
     *         the strings to use as replacement.
     */
    public static final SettingsModelString getReplacementColumnModel() {
        return new SettingsModelString(DictionaryReplacer2InPortsConfigKeys2.CFGKEY_VALUECOLUMN, "");
    }

    /**
     * Creates and returns a new instance of {@link SettingsModelString} containing the name of the tokenizer used for
     * word tokenization.
     *
     * @return Creates and returns a new instance of {@link SettingsModelString} containing the name of the tokenizer
     *         used for word tokenization.
     * @since 3.3
     */
    public static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(DictionaryReplacer2InPortsConfigKeys2.CFGKEY_TOKENIZER,
            TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates and returns a new instance of {@link SettingsModelOptionalString} containing a replacement string that is
     * used to replace terms that are not available in the dictionary.
     *
     * @return Creates and returns a new instance of {@link SettingsModelOptionalString} containing a replacement string
     *         that is used to replace terms that are not available in the dictionary.
     */
    static final SettingsModelOptionalString getReplaceUnknownWordsModel() {
        return new SettingsModelOptionalString(DictionaryReplacer2InPortsConfigKeys2.CFGKEY_REPLACE_UNKNOWN_WORDS, "0",
            false);
    }

    /**
     * {@link SettingsModelOptionalString} containing a replacement string that is used to replace terms that are not
     * available in the dictionary.
     */
    private final SettingsModelOptionalString m_replaceUnknownWordsModel = getReplaceUnknownWordsModel();

    /**
     * Constructor of {@link DictionaryReplacer2InPortsNodeDialog2}.
     */
    @SuppressWarnings("unchecked")
    public DictionaryReplacer2InPortsNodeDialog2() {
        super();

        createNewTab("Dictionary options");
        setSelected("Dictionary options");

        addDialogComponent(new DialogComponentColumnNameSelection(getReplaceColumnModel(),
            "Column containing the strings to replace", 1, true, StringValue.class));

        addDialogComponent(new DialogComponentColumnNameSelection(getReplacementColumnModel(),
            "Column containing the replacement strings", 1, true, StringValue.class));

        addDialogComponent(
            new DialogComponentOptionalString(m_replaceUnknownWordsModel, "Replace words not in dictionary by"));

        final Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().entrySet().stream()
                .map(e -> e.getKey()).collect(Collectors.toList());
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));
    }
}

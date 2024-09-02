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
 * ---------------------------------------------------------------------
 *
 * Created on 25.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.nodes.tagging;

import java.util.Collection;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.PreferenceUtil;

/**
 * A {@link org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane} which provides additionally a tab
 * containing a number input to specify the number of threads to use for parallel tagging.
 *
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @since 2.9
 * @deprecated Use {@link TaggerNodeSettingsPane2} instead.
 */
@Deprecated
public class TaggerNodeSettingsPane extends DefaultNodeSettingsPane {

    /**
     * Creates and returns the settings model, storing the number of maximal parallel threads for tagging.
     * @return The settings model with number of maximal parallel threads.
     */
    static final SettingsModelIntegerBounded getNumberOfThreadsModel() {
        return new SettingsModelIntegerBounded(
            TaggerConfigKeys.CFGKEY_NUMBER_OF_THREADS, TaggerNodeModel.DEFAULT_NUMBER_OF_THREADS, 1,
            TaggerNodeModel.MAX_NUMBER_OF_THREADS);
    }

    /**
     * Creates and returns the settings model, storing the name of the word tokenizer.
     * @return The settings model with the name of the word tokenizer.
     * @since 3.3
     */
    public static final SettingsModelString getTokenizerModel() {
        return new SettingsModelString(TaggerConfigKeys.CFGKEY_TOKENIZER,
            PreferenceUtil.tokenizerName());
    }

    /**
     * Creates new instance of {@code TaggerNodeSettingsPane}.
     */
    public TaggerNodeSettingsPane() {
        removeTab("Options");
        createNewTabAt("General options", 1);

        addDialogComponent(new DialogComponentNumber(getNumberOfThreadsModel(),
            "Number of maximal parallel tagging processes", 1));

        Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().keySet();
        addDialogComponent(new DialogComponentStringSelection(getTokenizerModel(), "Word tokenizer", tokenizerList));
    }
}

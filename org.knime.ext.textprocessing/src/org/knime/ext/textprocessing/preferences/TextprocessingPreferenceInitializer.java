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
 * History
 *   03.03.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class TextprocessingPreferenceInitializer extends AbstractPreferenceInitializer {

    /** The default serialization setting. */
    public static final boolean DEFAULT_DML_DESERIALIZATION = true;

    /** The default row preprocessing setting. */
    public static final boolean DEFAULT_ROW_PREPROCESSING = true;

    /*** The default size of the tokenizer pool. */
    public static final int DEFAULT_TOKENIZER_POOLSIZE = 10;

    /*** The default tokenizer. */
    public static final String DEFAULT_TOKENIZER = "OpenNLP English WordTokenizer";

    /** The default setting whether tokenizer pool is initialized on startup. */
    public static final boolean DEFAULT_TOKENIZER_INIT_ONSTARTUP = true;

    /*** The maximum size of the tokenizer pool. */
    public static final int MAX_TOKENIZER_POOLSIZE = 1000;

    /** Preference key for the usage of backwards compatibility. */
    public static final String PREF_DML_DESERIALIZATION = "knime.textprocessing.dmldeserialization";

    /** Preference key for the usage of row preprocessing.
     * @deprecated setting is not used anymore.
     * */
    @Deprecated
    public static final String PREF_ROW_PREPROCESSING = "knime.textprocessing.rowpreprocessing";

    /** Preference key for the tokenizer pool size. */
    public static final String PREF_TOKENIZER_POOLSIZE = "knime.textprocessing.tokenizer.poolsize";

    /** Preference key setting whether tokenizer pool is initialized on startup. */
    public static final String PREF_TOKENIZER_INIT_ONSTARTUP = "knime.textprocessing.tokenizer.initonstartup";

    /**
     * Preference key for the tokenizer.
     */
    public static final String PREF_TOKENIZER = "knime.textprocessing.tokenizer.tokenizer";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        var plugin = TextprocessingCorePlugin.getDefault();
        if (plugin != null) {
            IPreferenceStore store = plugin.getPreferenceStore();
            //set default values
            store.setDefault(PREF_DML_DESERIALIZATION, DEFAULT_DML_DESERIALIZATION);
            store.setDefault(PREF_ROW_PREPROCESSING, DEFAULT_ROW_PREPROCESSING);
            store.setDefault(PREF_TOKENIZER_POOLSIZE, DEFAULT_TOKENIZER_POOLSIZE);
            store.setDefault(PREF_TOKENIZER, DEFAULT_TOKENIZER);
        }
    }

    /**
     * Returns true if dml deserialization has to be applied.
     *
     * @return the dml deserialization setting
     */
    public static boolean useDmlDeserialization() {
        var plugin = TextprocessingCorePlugin.getDefault();
        if (plugin == null) {
            return DEFAULT_DML_DESERIALIZATION;
        }
        final IPreferenceStore pStore = plugin.getPreferenceStore();
        if (!pStore.contains(PREF_DML_DESERIALIZATION)) {
            return DEFAULT_DML_DESERIALIZATION;
        }
        return pStore.getBoolean(PREF_DML_DESERIALIZATION);
    }

    /**
     * Returns true if row preprocessing has to be applied.
     *
     * @return the preprocessing policy
     * @deprecated setting is not used anymore.
     */
    @Deprecated
    public static boolean useRowPreprocessing() {
        var plugin = TextprocessingCorePlugin.getDefault();
        if (plugin == null) {
            return DEFAULT_ROW_PREPROCESSING;
        }
        final IPreferenceStore pStore = plugin.getPreferenceStore();
        if (!pStore.contains(PREF_ROW_PREPROCESSING)) {
            return DEFAULT_ROW_PREPROCESSING;
        }
        return pStore.getBoolean(PREF_ROW_PREPROCESSING);
    }

    /**
     * Returns the size of the tokenizer pool.
     *
     * @return the size of the tokenizer pool.
     */
    public static int tokenizerPoolSize() {
        var plugin = TextprocessingCorePlugin.getDefault();
        if (plugin == null) {
            return DEFAULT_TOKENIZER_POOLSIZE;
        }
        final IPreferenceStore pStore = plugin.getPreferenceStore();
        if (!pStore.contains(PREF_TOKENIZER_POOLSIZE)) {
            return DEFAULT_TOKENIZER_POOLSIZE;
        }
        return pStore.getInt(PREF_TOKENIZER_POOLSIZE);
    }

    /**
     * Returns the select tokenizer name. If not tokenizer is selected, the default tokenizer will be returned.
     *
     * @return a tokenizer name
     */
    public static String tokenizerName() {
        var plugin = TextprocessingCorePlugin.getDefault();
        if (plugin == null) {
            return DEFAULT_TOKENIZER;
        }
        final IPreferenceStore pStore = plugin.getPreferenceStore();
        if(!pStore.contains(PREF_TOKENIZER)) {
            return DEFAULT_TOKENIZER;
        }
        return pStore.getString(PREF_TOKENIZER);
    }
}

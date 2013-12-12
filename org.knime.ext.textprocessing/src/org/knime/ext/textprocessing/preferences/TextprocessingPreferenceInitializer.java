/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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

    /*** The maximum size of the tokenizer pool. */
    public static final int MAX_TOKENIZER_POOLSIZE = 1000;

    /** The default setting whether tokenizer pool is initialized on startup. */
    public static final boolean DEFAULT_TOKENIZER_INIT_ONSTARTUP = true;

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
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TextprocessingCorePlugin.getDefault().getPreferenceStore();

        //set default values
        store.setDefault(PREF_DML_DESERIALIZATION, DEFAULT_DML_DESERIALIZATION);
        store.setDefault(PREF_ROW_PREPROCESSING, DEFAULT_ROW_PREPROCESSING);
        store.setDefault(PREF_TOKENIZER_POOLSIZE, DEFAULT_TOKENIZER_POOLSIZE);
        store.setDefault(PREF_TOKENIZER_INIT_ONSTARTUP, DEFAULT_TOKENIZER_INIT_ONSTARTUP);
    }

    /**
     * Returns true if dml deserialization has to be applied.
     *
     * @return the dml deserialization setting
     */
    public static boolean useDmlDeserialization() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
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
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
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
    public static final int tokenizerPoolSize() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_TOKENIZER_POOLSIZE)) {
            return DEFAULT_TOKENIZER_POOLSIZE;
        }
        return pStore.getInt(PREF_TOKENIZER_POOLSIZE);
    }

    /**
     * Returns the init on startup flag of the tokenizer pool.
     *
     * @return the setting whether the tokenizer pool is initialized on startup or lazy.
     */
    public static final boolean initTokenizerPoolOnStartup() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_TOKENIZER_INIT_ONSTARTUP)) {
            return DEFAULT_TOKENIZER_INIT_ONSTARTUP;
        }
        return pStore.getBoolean(PREF_TOKENIZER_INIT_ONSTARTUP);
    }
}

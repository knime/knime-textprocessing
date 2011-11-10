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
public class TextprocessingPreferenceInitializer extends
        AbstractPreferenceInitializer {

    public static final boolean DEFAULT_USE_BLOB = true;
    
    public static final boolean DEFAULT_DML_DESERIALIZATION = true;
    
    public static final boolean DEFAULT_ROW_PREPROCESSING = true;
    
    /** Preference key for the usage of blob cells setting. */
    public static final String PREF_USE_BLOB = "knime.textprocessing.blobcell";
    
    /** Preference key for the usage of backwards compatibility. */
    public static final String PREF_DML_DESERIALIZATION = 
        "knime.textprocessing.dmldeserialization";
    
    /** Preference key for the usage of row preprocessing. */
    public static final String PREF_ROW_PREPROCESSING = 
        "knime.textprocessing.rowpreprocessing";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TextprocessingCorePlugin.getDefault()
            .getPreferenceStore();

        //set default values
        store.setDefault(PREF_USE_BLOB, DEFAULT_USE_BLOB);
        store.setDefault(PREF_DML_DESERIALIZATION, DEFAULT_DML_DESERIALIZATION);
        store.setDefault(PREF_ROW_PREPROCESSING, DEFAULT_ROW_PREPROCESSING);
    }

    /**
     * Returns true if Blob cells have to be used.
     * 
     * @return the Blob cell setting
     */
    public static boolean useBlobCell() {
        final IPreferenceStore pStore = 
            TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_USE_BLOB)) {
            return DEFAULT_USE_BLOB;
        }
        return pStore.getBoolean(PREF_USE_BLOB);
    }
    
    /**
     * Returns true if dml deserialization has to be applied.
     * 
     * @return the dml deserialization setting
     */
    public static boolean useDmlDeserialization() {
        final IPreferenceStore pStore = 
            TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_DML_DESERIALIZATION)) {
            return DEFAULT_DML_DESERIALIZATION;
        }
        return pStore.getBoolean(PREF_DML_DESERIALIZATION);
    }
    
    /**
     * Returns true if row preprocessing has to be applied.
     * 
     * @return the preprocessing policy
     */
    public static boolean useRowPreprocessing() {
        final IPreferenceStore pStore = 
            TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_ROW_PREPROCESSING)) {
            return DEFAULT_ROW_PREPROCESSING;
        }
        return pStore.getBoolean(PREF_ROW_PREPROCESSING);
    }      
}

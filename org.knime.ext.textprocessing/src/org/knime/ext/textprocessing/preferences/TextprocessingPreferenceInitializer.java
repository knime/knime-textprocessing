/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
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

    private static boolean DEFAULT_USE_BLOB = true;
    
    /** Preference key for the usage of blob cells setting. */
    public static final String PREF_USE_BLOB = "knime.textprocessing.blobcell";    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TextprocessingCorePlugin.getDefault()
            .getPreferenceStore();

        //set default values
        store.setDefault(PREF_USE_BLOB, DEFAULT_USE_BLOB);
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
}

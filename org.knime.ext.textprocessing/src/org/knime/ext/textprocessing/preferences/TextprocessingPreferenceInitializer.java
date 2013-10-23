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
public class TextprocessingPreferenceInitializer extends
        AbstractPreferenceInitializer {

    /**
     * The default "use blob" setting.
     * @deprecated use {@link TextprocessingPreferenceInitializer#BLOB_CELLTYPE} instead.
     */
    @Deprecated
    public static final boolean DEFAULT_USE_BLOB = true;

    /** The blob cell type setting. */
    public static final String BLOB_CELLTYPE = "blobCell";

    /** The regular cell type setting. */
    public static final String REGULAR_CELLTYPE = "regularCell";

    /** The file store cell type setting. */
    public static final String FILESTORE_CELLTYPE = "fileStoreCell";

    /**
     * The default cell type setting.
     */
    public static final String DEFAULT_CELLTYPE = FILESTORE_CELLTYPE;

    /**
     * The default number of documents to store in a single file store file.
     */
    public static final int DEFAULT_FILESTORE_CHUNKSIZE = 1000;

    /**
     * The default serialization setting.
     */
    public static final boolean DEFAULT_DML_DESERIALIZATION = false;

    /**
     * The default row preprocessing setting.
     */
    public static final boolean DEFAULT_ROW_PREPROCESSING = true;

    /** Preference key for the usage of blob cells setting.
     * @deprecated use {@link TextprocessingPreferenceInitializer#PREF_CELL_TYPE} instead.
     * */
    @Deprecated
    public static final String PREF_USE_BLOB = "knime.textprocessing.blobcell";

    /** Preference key for the document cell type. */
    public static final String PREF_CELL_TYPE = "knime.textprocessing.celltype";

    /**
     * Preference key for the chunk size of the file store, specifying how many documents are stored in a single
     * file store file.
     */
    public static final String PREF_FILESTORE_CHUNKSIZE = "knime.textprocessing.filestore.chunksize";

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
        store.setDefault(PREF_CELL_TYPE, DEFAULT_CELLTYPE);
        store.setDefault(PREF_DML_DESERIALIZATION, DEFAULT_DML_DESERIALIZATION);
        store.setDefault(PREF_ROW_PREPROCESSING, DEFAULT_ROW_PREPROCESSING);
        store.setDefault(PREF_FILESTORE_CHUNKSIZE, DEFAULT_FILESTORE_CHUNKSIZE);
    }

    /**
     * Returns true if Blob cells have to be used.
     * @deprecated Use {@link TextprocessingPreferenceInitializer#cellType()} instead.
     * @return the Blob cell setting
     */
    @Deprecated
    public static boolean useBlobCell() {
        final IPreferenceStore pStore =
            TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_USE_BLOB)) {
            return DEFAULT_USE_BLOB;
        }
        return pStore.getBoolean(PREF_USE_BLOB);
    }

    /**
     * @return The specified number of documents to store in a single file store file.
     */
    public static final int fileStoreChunkSize() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_FILESTORE_CHUNKSIZE)) {
            return DEFAULT_FILESTORE_CHUNKSIZE;
        }
        if (pStore.getInt(PREF_FILESTORE_CHUNKSIZE) <= 0) {
            return 1;
        }
        return pStore.getInt(PREF_FILESTORE_CHUNKSIZE);
    }

    /**
     * @return The specified cell type to use.
     */
    public static String cellType() {
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        if (!pStore.contains(PREF_CELL_TYPE)) {
            return DEFAULT_CELLTYPE;
        }
        return pStore.getString(PREF_CELL_TYPE);
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

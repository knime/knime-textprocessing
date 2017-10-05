/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * Created on 12.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;

/**
 * The initializer for the document cell storage prefrences.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public class StoragePreferenceInitializer extends AbstractPreferenceInitializer {

    /** The blob cell type setting. */
    public static final String BLOB_CELLTYPE = "blobCell";

    /** The regular cell type setting. */
    public static final String REGULAR_CELLTYPE = "regularCell";

    /** The file store cell type setting. */
    public static final String FILESTORE_CELLTYPE = "fileStoreCell";

    /** The default cell type setting.*/
    public static final String DEFAULT_CELLTYPE = FILESTORE_CELLTYPE;

    /** The default number of documents to store in a single file store file.*/
    public static final int DEFAULT_FILESTORE_CHUNKSIZE = 10000;


    /** Preference key for the document cell type. */
    public static final String PREF_CELL_TYPE = "knime.textprocessing.celltype";

    /**
     * Preference key for the chunk size of the file store, specifying how many documents are stored in a single
     * file store file.
     */
    public static final String PREF_FILESTORE_CHUNKSIZE = "knime.textprocessing.filestore.chunksize";

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = TextprocessingCorePlugin.getDefault().getPreferenceStore();

        //set default values
        store.setDefault(PREF_CELL_TYPE, DEFAULT_CELLTYPE);
        store.setDefault(PREF_FILESTORE_CHUNKSIZE, DEFAULT_FILESTORE_CHUNKSIZE);
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
}

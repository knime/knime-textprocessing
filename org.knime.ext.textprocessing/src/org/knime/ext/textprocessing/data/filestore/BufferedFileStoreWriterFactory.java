/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
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
 * Created on 19.11.2013 by Kilian Thiel
 */

package org.knime.ext.textprocessing.data.filestore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.knime.core.data.filestore.FileStore;
import org.knime.ext.textprocessing.preferences.StoragePreferenceInitializer;

/**
 * Singleton factory providing buffered file store writer for file stores.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
final class BufferedFileStoreWriterFactory {

    private static BufferedFileStoreWriterFactory instance = null;

    private static final int DEFALT_BUFFER_SIZE = 100;

    private final Map<String, BufferedFileStoreWriter> m_bufferedWriter =
            new ConcurrentHashMap<String, BufferedFileStoreWriter>();

    private BufferedFileStoreWriterFactory() { }

    /**
     * @return the singleton instance of the factory.
     */
    static synchronized BufferedFileStoreWriterFactory instance() {
        if (instance == null) {
            instance = new BufferedFileStoreWriterFactory();
        }
        return instance;
    }

    /**
     * Returns the related buffered writer for the given file store.
     * @param fileStore The file store to get the related buffered writer for,
     * @return The buffered writer for the given file store.
     */
    BufferedFileStoreWriter getBufferedFileStoreWriter(final FileStore fileStore) {
        BufferedFileStoreWriter fileStoreWriter = m_bufferedWriter.get(fileStore.toString());
        if (fileStoreWriter == null) {
            final int bufferSize = Math.min(DEFALT_BUFFER_SIZE, StoragePreferenceInitializer.fileStoreChunkSize());
            fileStoreWriter = new BufferedFileStoreWriter(fileStore, bufferSize);
            m_bufferedWriter.put(fileStore.toString(), fileStoreWriter);
        }
        return fileStoreWriter;
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.knime.core.data.filestore.FileStore;
import org.knime.ext.textprocessing.data.Document;

/**
 * Serializes document data into file store files in a buffered manner.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
final class BufferedFileStoreWriter {

    private FileStore m_fileStore;

    private final int m_bufferSize;

    private long m_offset = 0;

    private final List<byte[]> m_buffer;

    private AtomicBoolean m_dirty = new AtomicBoolean(false);

    /**
     * Constructor for class {@link BufferedFileStoreWriter}.
     * @param fileStore The file store to serialize documents to.
     * @param bufferSize The maximal number of documents in the buffer.
     */
    BufferedFileStoreWriter(final FileStore fileStore, final int bufferSize) {
        m_fileStore = fileStore;
        m_bufferSize = bufferSize;
        m_buffer = new ArrayList<byte[]>(m_bufferSize);
    }

    /**
     * Puts given document into buffer and writes buffer into file store file if maximum buffer size is reached.
     * @param document The document to write.
     * @return The address of the document in the file store file.
     * @throws IOException If document data could not be written to file store file.
     */
    public synchronized DocumentAddress write(final Document document) throws IOException {
        final byte[] serializeDoc = AbstractDocumentFileStoreCell.serializeDocument(document);
        final DocumentAddress address = new DocumentAddress(document.getUUID(), m_offset, serializeDoc.length);
        m_offset += serializeDoc.length;
        m_buffer.add(serializeDoc);
        m_dirty.set(true);

        if (m_buffer.size() >= m_bufferSize) {
            writeBuffer();
        }

        return address;
    }

    /**
     * Writes bufferd document data into file store file.
     * @throws IOException If data could not be written into file store file.
     */
    private synchronized void writeBuffer() throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(m_fileStore.getFile(), true));
            for (byte[] d : m_buffer) {
                os.write(d);
            }
        } finally {
            if (os != null) {
                os.close();
            }
        }
        m_buffer.clear();
    }

    /**
     * Flushes buffer, writes all data in buffer into file store file. A file store instance is required in order to
     * validate the file to write to. The file store file may have been changed or moved, thus the related file store
     * instance have to be provided in order to be able to update the file location.
     * @param fileStore The related file store instance.
     * @throws IOException If buffer could not be flushed into file store file.
     */
    public void flush(final FileStore fileStore) throws IOException {
        if (m_dirty.compareAndSet(true, false)) {
            updateFileStore(fileStore);
            writeBuffer();
        }
    }

    /**
     * Updates related file store instance if file store file has been moved.
     * @param fileStore The file store to update.
     */
    private void updateFileStore(final FileStore fileStore) {
        if (m_fileStore.toString().equals(fileStore.toString())
                && !m_fileStore.getFile().getAbsolutePath().equals(fileStore.getFile().getAbsolutePath())) {
            m_fileStore = fileStore;
        }
    }
}

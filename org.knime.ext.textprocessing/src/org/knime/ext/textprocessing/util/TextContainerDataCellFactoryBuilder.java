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
 * -------------------------------------------------------------------
 *
 * History
 *   16.02.2009 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.data.filestore.DocumentBufferedFileStoreDataCellFactory;
import org.knime.ext.textprocessing.data.filestore.DocumentFileStoreDataCellFactory;
import org.knime.ext.textprocessing.preferences.StoragePreferenceInitializer;

/**
 * @author Kilian Thiel, University of Konstanz
 */
public final class TextContainerDataCellFactoryBuilder {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(TextContainerDataCellFactoryBuilder.class);

    private TextContainerDataCellFactoryBuilder() { }

    /**
     * @return The <code>TextContainerDataCellFactory</code> creating
     * document cells.
     */
    public static TextContainerDataCellFactory createDocumentCellFactory() {
        if (StoragePreferenceInitializer.cellType().equals(
            StoragePreferenceInitializer.REGULAR_CELLTYPE)) {
            LOGGER.debug("Creating document cell factory!");
            return new DocumentDataCellFactory();
        } else if (StoragePreferenceInitializer.cellType().equals(
            StoragePreferenceInitializer.BLOB_CELLTYPE)) {
          LOGGER.debug("Creating document blob cell factory!");
          return new DocumentBlobDataCellFactory();
        }

        // create file store cells:
        // if more than 1 documents are stored in cell use buffered cell, else use regular file store cell.
        if (StoragePreferenceInitializer.fileStoreChunkSize() >= 2) {
            LOGGER.debug("Creating buffered document file store cell factory!");
            return new DocumentBufferedFileStoreDataCellFactory();
        } else {
            LOGGER.debug("Creating document file store cell factory!");
            return new DocumentFileStoreDataCellFactory();
        }
    }

    /**
     * @return The <code>TextContainerDataCellFactory</code> creating
     * term cells.
     */
    public static TextContainerDataCellFactory createTermCellFactory() {
        return new TermDataCell2Factory();
    }
}

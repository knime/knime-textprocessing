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
 * -------------------------------------------------------------------
 *
 * History
 *   16.02.2009 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import org.knime.core.node.NodeLogger;
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

        LOGGER.debug("Creating document file store cell factory!");
        return new DocumentFileStoreDataCellFactory();
    }

    /**
     * @return The <code>TextContainerDataCellFactory</code> creating
     * term cells.
     */
    public static TextContainerDataCellFactory createTermCellFactory() {
        return new TermDataCell2Factory();
    }
}

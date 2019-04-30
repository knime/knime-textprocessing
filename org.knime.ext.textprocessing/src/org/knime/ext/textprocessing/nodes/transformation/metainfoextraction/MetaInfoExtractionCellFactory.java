/*
 * ------------------------------------------------------------------------
 *
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
 *   Apr 26, 2019 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoextraction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataType;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * Cell factory to extract meta information from documents and create data cells.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MetaInfoExtractionCellFactory extends AbstractCellFactory {

    /**
     * The {@link BufferedDataTable} containing the data.
     */
    private final BufferedDataTable m_data;

    /**
     * The index of the document column.
     */
    private final int m_docColIdx;

    /**
     * List of keys that will be extracted.
     */
    private final List<String> m_keys;

    /**
     * Creates a new instance of {@code MetaInfoExtractionCellFactory}.
     *
     * @param dataTable The {@link BufferedDataTable} containing the data.
     * @param docColIdx The index of the document column.
     */
    MetaInfoExtractionCellFactory(final BufferedDataTable dataTable, final int docColIdx) {
        super(true);
        m_data = dataTable;
        m_docColIdx = docColIdx;
        m_keys = getKeysFromColumn(m_data, m_docColIdx);
    }

    /**
     * Get all meta information keys from documents in a column.
     *
     * @param data The {@link BufferedDataTable} containing the data.
     * @param docColIdx The index of the document column.
     * @return Returns a list of all meta information keys from documents in one column.
     */
    private static final List<String> getKeysFromColumn(final BufferedDataTable data, final int docColIdx) {
        final Set<String> keys = new LinkedHashSet<>();
        for (final DataRow row : data) {
            final DataCell cell = row.getCell(docColIdx);
            if (!cell.isMissing()) {
                keys.addAll(((DocumentValue)cell).getDocument().getMetaInformation().getMetaInfoKeys());
            }
        }
        return new ArrayList<>(keys);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell[] getCells(final DataRow row) {
        final DataCell docCell = row.getCell(m_docColIdx);
        final DataCell[] newCells = new DataCell[m_keys.size()];
        Arrays.fill(newCells, DataType.getMissingCell());
        if (!docCell.isMissing() || !docCell.getType().isCompatible(DocumentValue.class)) {
            final Document doc = ((DocumentValue)docCell).getDocument();
            final DocumentMetaInfo metaInfo = doc.getMetaInformation();
            final List<String> keys = new ArrayList<>(metaInfo.getMetaInfoKeys());
            // look up position for each meta info key from the document in the total key set and
            // add a new StringCell with the corresponding value
            for (final String key : keys) {
                newCells[m_keys.indexOf(key)] = new StringCell(metaInfo.getMetaInfoValue(key));
            }
        }
        return newCells;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataColumnSpec[] getColumnSpecs() {
        return m_keys.stream()//
            .map(key -> new DataColumnSpecCreator(key, StringCell.TYPE).createSpec())//
            .toArray(DataColumnSpec[]::new);
    }

    /**
     * True, if documents contain meta information.
     *
     * @return True, if documents contain meta information.
     */
    final boolean metaInfoAvailable() {
        return !m_keys.isEmpty();
    }

}

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
 *   Apr 25, 2019 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.ExecutionContext;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;

/**
 * Cell factory to add meta information from multiple columns to a single {@link Document}.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
final class MetaInfoCellFactory2 extends SingleCellFactory {

    /** The {@link DataTableSpec}. */
    private final DataTableSpec m_spec;

    /** The document column index. */
    private final int m_docColIdx;

    /** A map with column names and indices of meta info columns. */
    private final Map<String, Integer> m_colNamesAndIdx;

    /** The document cell factory. */
    private final TextContainerDataCellFactory m_documentCellFac;

    /**
     * Creates a new instance of {@link MetaInfoCellFactory2}.
     *
     * @param spec The {@link DataTableSpec}.
     * @param docColIdx The document column index.
     * @param metaInfoColNames An array of columns names containing meta information.
     * @param exec The {@link ExecutionContext}.
     */
    MetaInfoCellFactory2(final DataTableSpec spec, final int docColIdx, final String[] metaInfoColNames,
        final ExecutionContext exec) {
        super(true, spec.getColumnSpec(docColIdx));
        m_spec = spec;
        m_docColIdx = docColIdx;
        m_colNamesAndIdx = Stream.of(metaInfoColNames)//
            .collect(Collectors.toMap(colName -> colName, colName -> m_spec.findColumnIndex(colName)));
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        if (exec != null) {
            m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DataCell getCell(final DataRow row) {
        final DataCell cell = row.getCell(m_docColIdx);
        if (!cell.isMissing()) {
            final Document d = ((DocumentValue)cell).getDocument();
            final DocumentBuilder db = new DocumentBuilder(d);
            db.setSections(d.getSections());
            m_colNamesAndIdx.entrySet().stream()//
                .forEach(e -> addMetaInformation(row, db, e.getKey(), e.getValue()));
            final Document newDoc = db.createDocument();
            return m_documentCellFac.createDataCell(newDoc);
        }
        return cell;
    }

    /**
     * Adds meta information to a {@link DocumentBuilder} in case there is no missing value.
     *
     * @param row The {@link DataRow}.
     * @param db The {@link DocumentBuilder} to add the meta information to.
     * @param key The key.
     * @param metaInfColIdx The column index of the column containing the value.
     */
    private static final void addMetaInformation(final DataRow row, final DocumentBuilder db, final String key,
        final int metaInfColIdx) {
        final DataCell cell = row.getCell(metaInfColIdx);
        if (!cell.isMissing()) {
            db.addMetaInformation(key, ((StringValue)cell).getStringValue());
        }
    }
}

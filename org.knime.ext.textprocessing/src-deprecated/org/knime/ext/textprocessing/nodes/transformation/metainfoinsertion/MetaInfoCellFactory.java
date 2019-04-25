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
 * ------------------------------------------------------------------------
 */
package org.knime.ext.textprocessing.nodes.transformation.metainfoinsertion;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
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
 * @author Kilian Thiel, KNIME AG, Zurich, Switzerland
 * @deprecated Use custom cell factory instead.
 */
@Deprecated
public class MetaInfoCellFactory extends SingleCellFactory {

    private int m_docColIndx;
    private int m_keyColIndx;
    private int m_valueColIndx;
    private TextContainerDataCellFactory m_documentCellFac;

    /**
     * Constructor of {@link MetaInfoCellFactory} with given indices of document, key and value columns to set. The
     * given execution context is used to prepare the cell factory. To create the data table spec only, without
     * creating new data cells, the execution context may be {@code null}.
     * @param docColSpec The data column spec of the document column.
     * @param docColIndx The index of the column containing the documents.
     * @param keyColIndx The index of the column containing the keys.
     * @param valueColIndx The index of the column containing the values.
     * @param exec The execution context to prepare cell factory internally.
     */
    public MetaInfoCellFactory(final DataColumnSpec docColSpec, final int docColIndx, final int keyColIndx,
                               final int valueColIndx, final ExecutionContext exec) {
        super(true, docColSpec);

        m_docColIndx = docColIndx;
        m_keyColIndx = keyColIndx;
        m_valueColIndx = valueColIndx;
        m_documentCellFac = TextContainerDataCellFactoryBuilder.createDocumentCellFactory();
        if (exec != null) {
            m_documentCellFac.prepare(FileStoreFactory.createWorkflowFileStoreFactory(exec));
        }
    }

    /* (non-Javadoc)
     * @see org.knime.core.data.container.SingleCellFactory#getCell(org.knime.core.data.DataRow)
     */
    @Override
    public DataCell getCell(final DataRow row) {
        // value of new cell is value of old cell until new document has been created successfully (Bug: 4996)
        DataCell newCell = row.getCell(m_docColIndx);
        if (!row.getCell(m_docColIndx).isMissing() && !row.getCell(m_keyColIndx).isMissing()
                && !row.getCell(m_valueColIndx).isMissing()) {
            final Document d = ((DocumentValue)row.getCell(m_docColIndx)).getDocument();
            final String key = ((StringValue)row.getCell(m_keyColIndx)).getStringValue();
            final String value = ((StringValue)row.getCell(m_valueColIndx)).getStringValue();

            final DocumentBuilder db = new DocumentBuilder(d);
            db.setSections(d.getSections());
            db.addMetaInformation(key, value);
            final Document newDoc = db.createDocument();

            newCell = m_documentCellFac.createDataCell(newDoc);
        }
        return newCell;
    }
}

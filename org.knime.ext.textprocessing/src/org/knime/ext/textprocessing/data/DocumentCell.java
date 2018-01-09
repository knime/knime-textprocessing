/*
========================================================================
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
 *   19.12.2006 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;
import java.io.OutputStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 * A {@link org.knime.core.data.DataCell} implementation holding a
 * {@link org.knime.ext.textprocessing.data.Document} value by storing this
 * value in a private <code>Document</code> member. It provides a document value
 * as well as a string value.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentCell extends DataCell implements DocumentValue, StringValue {

    /**
     * Serial Version ID.
     */
    private static final long serialVersionUID = -3321138724982107144L;

    /**
     * Convenience access member for
     * <code>DataType.getType(DocumentCell.class)</code>.
     *
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(DocumentCell.class);

    private Document m_document;

    /**
     * Creates new instance of <code>DocumentCell</code> will given document.
     *
     * @param document The document to set.
     */
    public DocumentCell(final Document document) {
        m_document = document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        if (dc == null) {
            return false;
        }
        DocumentCell d = (DocumentCell)dc;

        if (!d.getDocument().equals(m_document)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
        return DocumentValue.equalContent(this, (DocumentValue)otherValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return DocumentValue.hashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue() {
        StringBuffer buf = new StringBuffer();
        buf.append("\"");
        buf.append(m_document.getTitle());
        buf.append("\"");

        return buf.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getDocument() {
        return m_document;
    }

    /**
     * Factory for (de-)serializing a DocumentCell.
     *
     * @noreference This class is not intended to be referenced by clients.
     */
    public static final class DocumentSerializer implements DataCellSerializer<DocumentCell> {
        /**
         * {@inheritDoc}
         */
        @Override
        public DocumentCell deserialize(final DataCellDataInput input)
                throws IOException {
            return new DocumentCell(TermDocumentDeSerializationUtil
                    .fastDeserializeDocument((input)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final DocumentCell cell,
                final DataCellDataOutput output) throws IOException {
            cell.serializeDocument((OutputStream)output);
        }
    }

    private void serializeDocument(final OutputStream out) throws IOException {
        TermDocumentDeSerializationUtil.serializeDocument(m_document, out);
    }
}

/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * Created on 21.10.2013 by Kilian Thiel
 */
package org.knime.ext.textprocessing.data;

import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 * A data cell implementation holding a {@link org.knime.ext.textprocessing.data.Term} value by storing this value in a
 * private <code>Term</code> member. It provides a term value as well as a string value. For de-serialization the
 * methods {@link TermDocumentDeSerializationUtil#fastSerializeTerm(Term, java.io.DataOutput)} and
 * {@link TermDocumentDeSerializationUtil#fastDeserializeTerm(java.io.DataInput)} are used, which is faster than the
 * de-serialization used in {@link TermCell}.
 *
 * @author Kilian Thiel, KNIME.com, Zurich, Switzerland
 * @since 2.9
 */
public class TermCell2 extends DataCell implements TermValue, StringValue {
    /**
     * SerialVersionID.
     */
    private static final long serialVersionUID = -2034469067638819611L;

    /**
     * Convenience access member for <code>DataType.getType(TermCell2.class)</code>.
     *
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(TermCell2.class);

    private Term m_term;

    /**
     * Creates a new instance of <code>TermCell</code> with given {@link org.knime.ext.textprocessing.data.Term}.
     *
     * @param term The <code>Term</code> to set.
     */
    public TermCell2(final Term term) {
        m_term = term;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalsDataCell(final DataCell dc) {
        if (dc == null) {
            return false;
        }
        TermCell2 t = (TermCell2)dc;

        return t.getTermValue().equals(m_term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean equalContent(final DataValue otherValue) {
        return TermValue.equalContent(this, (TermValue)otherValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return TermValue.hashCode(this);
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
        return m_term.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Term getTermValue() {
        return m_term;
    }

    /**
     * Factory for (de-)serializing a TermCell.
     *
     *  @noreference This class is not intended to be referenced by clients.
     */
    public static final class TermSerializer implements DataCellSerializer<TermCell2> {
        /**
         * {@inheritDoc}
         */
        @Override
        public TermCell2 deserialize(final DataCellDataInput input) throws IOException {
            return new TermCell2(TermDocumentDeSerializationUtil.fastDeserializeTerm(input));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final TermCell2 cell, final DataCellDataOutput output) throws IOException {
            TermDocumentDeSerializationUtil.fastSerializeTerm(cell.getTermValue(), output);
        }
    }
}

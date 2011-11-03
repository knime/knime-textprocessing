/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 * ---------------------------------------------------------------------
 * 
 * History
 *   04.01.2007 (thiel): created
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
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.util.TermDocumentDeSerializationUtil;

/**
 * A data cell implementation holding a 
 * {@link org.knime.ext.textprocessing.data.Term} value by storing this value in
 * a private <code>Term</code> member. It provides a term value as well
 * as a string value.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermCell extends DataCell implements StringValue, TermValue {
    
    private static final NodeLogger LOGGER = 
        NodeLogger.getLogger(TermCell.class);
    
    /**
     * Convenience access member for
     * <code>DataType.getType(TermCell.class)</code>.
     * 
     * @see DataType#getType(Class)
     */
    public static final DataType TYPE = DataType.getType(TermCell.class);

    /**
     * Returns the preferred value class of this cell implementation. This
     * method is called per reflection to determine which is the preferred
     * renderer, comparator, etc.
     * 
     * @return TermValue.class;
     */
    public static final Class<? extends DataValue> getPreferredValueClass() {
        return TermValue.class;
    }

    private static final TermSerializer SERIALIZER = new TermSerializer();

    /**
     * Returns the factory to read/write DataCells of this class from/to a
     * DataInput/DataOutput. This method is called via reflection.
     * 
     * @return A serializer for reading/writing cells of this kind.
     * @see DataCell
     */
    public static final TermSerializer getCellSerializer() {
        return SERIALIZER;
    }

    private Term m_term;

    /**
     * Creates a new instance of <code>TermCell</code> with given 
     * {@link org.knime.ext.textprocessing.data.Term}.
     * @param term The <code>Term</code> to set. 
     */
    public TermCell(final Term term) {
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
        TermCell t = (TermCell)dc;
        
        return t.getTermValue().equals(m_term);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_term.hashCode();
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
    public String getStringValue() {
        return m_term.toString();
    }

    /**
     * {@inheritDoc}
     */
    public Term getTermValue() {
        return m_term;
    }
    
    
    
    /** Factory for (de-)serializing a TermCell. */
    private static class TermSerializer implements 
        DataCellSerializer<TermCell> {

        /**
         * {@inheritDoc}
         */
        @Override
        public TermCell deserialize(final DataCellDataInput input) 
        throws IOException {
            return TermDocumentDeSerializationUtil.deserializeTermCell(input);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final TermCell cell, 
                final DataCellDataOutput output) throws IOException {
            cell.serializeTerm((OutputStream) output);
        }
    }
    
    private void serializeTerm(final OutputStream out) throws IOException {
        TermDocumentDeSerializationUtil.serializeTerm(m_term, out);
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2010
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
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.DataCellDataOutput;
import org.knime.core.data.DataCellSerializer;
import org.knime.core.data.DataType;
import org.knime.core.data.DataValue;
import org.knime.core.data.StringValue;

/**
 * A data cell implementation holding a 
 * {@link org.knime.ext.textprocessing.data.Term} value by storing this value in
 * a private <code>Term</code> member. It provides a term value as well
 * as a string value.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class TermCell extends DataCell implements StringValue, TermValue {

    private static final String SEPARATOR = "\n";
    
    private static final String TAG_SECTION = "<<TAGS>>";
    
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
            String s = input.readUTF();
            return TermCell.createTermCell(s);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void serialize(final TermCell cell, 
                final DataCellDataOutput output) throws IOException {
            output.writeUTF(cell.getSerializationString());
        }
    }
    
    
    private String getSerializationString() {        
        return TermCell.getSerializationString(this.getTermValue());
    }
    
    private static TermCell createTermCell(final String s) {
        return new TermCell(TermCell.createTerm(s));
    }
    
    /**
     * Returns the instance of <code>Term</code> related to the given string.
     * @param s The string to get the related <code>Term</code> instance for.
     * @return The instance of <code>Term</code> related to the given string.
     */
    static Term createTerm(final String s) {
        List<Word> words = new ArrayList<Word>();
        List<Tag> tags = new ArrayList<Tag>();

        String[] str = s.split(TermCell.TAG_SECTION);
        
        // words
        if (str.length > 0) {
            String wordStr = str[0]; 
            String[] wordsArr = wordStr.split(TermCell.SEPARATOR);
            for (String w : wordsArr) {
                if (w != null && w.length() > 0) {
                    words.add(new Word(w));
                }
            }
        }
        
        // tags
        if (str.length > 1) {
            String tagStr = str[1];
            String[] tagsArr = tagStr.split(TermCell.SEPARATOR);
            for (int i = 0; i < tagsArr.length; i++) {
                String type = tagsArr[i];
                i++;
                
                // if no tags are assigned, continue
                if (i >= tagsArr.length) {
                    continue;
                }
                
                String value = tagsArr[i];
                tags.add(TagFactory.getInstance().createTag(type, value));
            }
        }
        
        // modifiability
        boolean unmodifiable = false;
        if (str.length > 2) {
            String modifiabilityStr = str[2];
            unmodifiable = new Boolean(modifiabilityStr);
        }
        
        return new Term(words, tags, unmodifiable);
    }    
    
    /**
     * Returns the serialization string of the given term.
     * @param term The term to return its serialization string.
     * @return The serialization string of the given term.
     */
    static String getSerializationString(final Term term) {
        StringBuffer buf = new StringBuffer();
        
        for (Word w : term.getWords()) {
            buf.append(w.getWord());
            buf.append(TermCell.SEPARATOR);
        }
        buf.append(TermCell.TAG_SECTION);
        for (Tag t : term.getTags()) {
            buf.append(t.getTagType());
            buf.append(TermCell.SEPARATOR);
            buf.append(t.getTagValue());
            buf.append(TermCell.SEPARATOR);
        }
        buf.append(TermCell.TAG_SECTION);
        buf.append(term.isUnmodifiable());
        
        return buf.toString();
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *  propagated with or for interoperation with KNIME. The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 * 
 * History
 *   28.10.2011 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.container.LongUTFDataInputStream;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.source.parser.DocumentParser;
import org.knime.ext.textprocessing.nodes.source.parser.dml.DmlDocumentParser;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public final class TermDocumentDeSerializationUtil {

    private static final NodeLogger LOGGER =
        NodeLogger.getLogger(TermDocumentDeSerializationUtil.class);    
    
    private static final String SEPARATOR = "\n";
    
    private static final String TAG_SECTION = "<<TAGS>>";  
    
    /**
     * Singelton instance of deserialization until.
     */
    private static TermDocumentDeSerializationUtil DESERIALIZATION_UTIL =
        new TermDocumentDeSerializationUtil();
    
    private boolean m_dmlDeserialization;
    
    private TermDocumentDeSerializationUtil() {
        m_dmlDeserialization = 
            TextprocessingPreferenceInitializer.DEFAULT_DML_DESERIALIZATION;
        final IPreferenceStore pStore = 
            TextprocessingCorePlugin.getDefault().getPreferenceStore();
        pStore.addPropertyChangeListener(
                new TextprocessingPropertyChangeListener());
    }
    
    /**
     * @return The dml deserialization flag.
     */
    public boolean dmlDeserialization() {
        return m_dmlDeserialization;
    }
    
    /**
     * Returns the instance of <code>Document</code> related to the given 
     * string.
     * @param str The string to get the related <code>Document</code> 
     * instance for.
     * @return The instance of <code>Document</code> related to the given 
     * string.
     * @throws Exception If document could not be parsed
     */
    public static Document createDocumentFromDML(final String str)
    throws Exception {
        DocumentParser parser = new DmlDocumentParser();
        List<Document> docs = parser.parse(new ByteArrayInputStream(
                str.getBytes("UTF-8")));
        Document doc = null;
        if (docs.size() > 0) {
            doc = docs.get(0);
        }
        return doc;
    }
    
    /**
     * Deserializes document from DML string and creates a instance of a 
     * <code>DocumentBlobCell</code>. 
     * 
     * @param str The string to deserialize
     * @return The <code>DocumentBlobCell</code> wrapping the deserialized 
     * document.
     */
    public static DocumentBlobCell createDocumentBlobCellFromDML(
            final String str) {
        Document d;
        try {
            d = TermDocumentDeSerializationUtil.createDocumentFromDML(str);
        } catch (Exception e) {
            LOGGER.warn("Parse error: DocumentBlobCell could not be created!");
            return null;
        }
        return new DocumentBlobCell(d);
    }

    /**
     * Deserializes document from DML string and creates a instance of a 
     * <code>DocumentCell</code>. 
     * 
     * @param str The string to deserialize
     * @return The <code>DocumentCell</code> wrapping the deserialized 
     * document.
     */
    public static DocumentCell createDocumentCellFromDML(final String str) {
        Document d;
        try {
            d = TermDocumentDeSerializationUtil.createDocumentFromDML(str);
        } catch (Exception e) {
            LOGGER.warn("Parse error: DocumentCell could not be created!");
            return null;
        }
        return new DocumentCell(d);
    }
    
    /**
     * Returns the instance of <code>Term</code> related to the given string.
     * @param s The string to get the related <code>Term</code> instance for.
     * @return The instance of <code>Term</code> related to the given string.
     */
    public static Term createTermFromUTF(final String s) {
        List<Word> words = new ArrayList<Word>();
        List<Tag> tags = new ArrayList<Tag>();

        String[] str = s.split(TermDocumentDeSerializationUtil.TAG_SECTION);
        
        // words
        if (str.length > 0) {
            String wordStr = str[0]; 
            String[] wordsArr = wordStr.split(
                    TermDocumentDeSerializationUtil.SEPARATOR);
            for (String w : wordsArr) {
                if (w != null && w.length() > 0) {
                    words.add(new Word(w));
                }
            }
        }
        
        // tags
        if (str.length > 1) {
            String tagStr = str[1];
            String[] tagsArr = tagStr.split(
                    TermDocumentDeSerializationUtil.SEPARATOR);
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
     * Deserializes term from serialization string and creates a instance of a 
     * <code>TermCell</code>. 
     * 
     * @param s The string to deserialize
     * @return The <code>TermCell</code> wrapping the deserialized term.
     */
    public static TermCell createTermCellFromUTF(final String s) {
        return new TermCell(
                TermDocumentDeSerializationUtil.createTermFromUTF(s));
    }    
    
    /**
     * Returns the serialization string of the given term.
     * @param term The term to return its serialization string.
     * @return The serialization string of the given term.
     */
    @Deprecated
    public static String getUTFSerializationString(final Term term) {
        StringBuffer buf = new StringBuffer();
        
        for (Word w : term.getWords()) {
            buf.append(w.getWord());
            buf.append(TermDocumentDeSerializationUtil.SEPARATOR);
        }
        buf.append(TermDocumentDeSerializationUtil.TAG_SECTION);
        for (Tag t : term.getTags()) {
            buf.append(t.getTagType());
            buf.append(TermDocumentDeSerializationUtil.SEPARATOR);
            buf.append(t.getTagValue());
            buf.append(TermDocumentDeSerializationUtil.SEPARATOR);
        }
        buf.append(TermDocumentDeSerializationUtil.TAG_SECTION);
        buf.append(term.isUnmodifiable());
        
        return buf.toString();
    }    
    
    /**
     * Checks if term is serialized as UTF string or binary.
     * @param input The input stream, streaming the serialized term.
     * @return <code>true</code> if term is serialized as deprecated UTF 
     * string, <code>false</code> otherwise.
     * @throws IOException If stream could not be red.
     */
    public static boolean termIsDeprecSerialized(
            final BufferedInputStream input) throws IOException {
        boolean isDeprecated = false;
        int bufferSize = 1024;
        if (input.markSupported()) {
            input.mark(bufferSize);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input));
            char[] buffer = new char[bufferSize];
            reader.read(buffer);
            String start = new String(buffer);
            if (start.contains(TAG_SECTION)) {
                isDeprecated = true;
            }
            input.reset();
        }
        return isDeprecated;
    }    
    
    /**
     * Checks if document is serialized as DML string or binary.
     * @param input The input stream, streaming the serialized document.
     * @return <code>true</code> if document is serialized as deprecated DML 
     * string, <code>false</code> otherwise.
     * @throws IOException If stream could not be red.
     */
    public static boolean docIsDeprecSerialized(
            final BufferedInputStream input) throws IOException {
        boolean isXml = false;
        int bufferSize = 1024;
        if (input.markSupported()) {
            input.mark(bufferSize);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(input));
            char[] buffer = new char[bufferSize];
            reader.read(buffer);
            String start = new String(buffer);
            // <?xml version="1.0" encoding="UTF-8"?>
            if (start.contains("<?xml version=")) {
                isXml = true;
            }
            input.reset();
        }
        return isXml;
    }
    
    
    /**
     * Serializes (binary) the given document on given stream.
     * @param doc The document to serialize.
     * @param out The stream to serialize to.
     * @throws IOException If document could not be written to stream. 
     */
    public static void serializeDocument(final Document doc, 
            final OutputStream out) throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(doc);
            return;
        } catch (IOException e) {
            LOGGER.warn(
                    "Serialization error: Document could not be serialized!");
            throw(e);
        }        
    }
    
    /**
     * Deserializes a document from given stream. The document need to be 
     * serialized in binary form.
     * 
     * @param in Stream to read serialized document from.
     * @return Deserialized document.
     * @throws IOException If document could not be red from stream.
     */
    public static Document deserializeDocument(final InputStream in)
    throws IOException {
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            Document doc = (Document)ois.readObject();
            return doc;
        } catch (IOException e) {
            LOGGER.warn(
                "Deserialization error: Document could not be deserialized!");
            throw(e);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Class <Document> not found, document could not " 
                    + "be deserialized");
            return null;
        }
    }
    
    /**
     * Deserializes a document from given input and wraps it into a 
     * <code>DocumentBlobCell</code>.
     * 
     * @param input Input to read serialized document from.
     * @return Deserialized document wrapped into <code>DocumentBlobCell</code>.
     * @throws IOException If document could not be red from input.
     */
    public static DocumentBlobCell deserializeDocumentBlobCell(
            final DataCellDataInput input) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(
                (InputStream)input);
        if (DESERIALIZATION_UTIL.m_dmlDeserialization) {
            if (TermDocumentDeSerializationUtil.docIsDeprecSerialized(bis)) {
                LongUTFDataInputStream dis = 
                    new LongUTFDataInputStream(new DataInputStream(bis));
                return TermDocumentDeSerializationUtil.
                createDocumentBlobCellFromDML(dis.readUTF());
            }
        }
        return new DocumentBlobCell(
                TermDocumentDeSerializationUtil.deserializeDocument(bis));
    }
    
    /**
     * Deserializes a document from given input and wraps it into a 
     * <code>DocumentCell</code>.
     * 
     * @param input Input to read serialized document from.
     * @return Deserialized document wrapped into <code>DocumentCell</code>.
     * @throws IOException If document could not be red from input.
     */    
    public static DocumentCell deserializeDocumentCell(
            final DataCellDataInput input) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(
                (InputStream)input);
        if (DESERIALIZATION_UTIL.m_dmlDeserialization) {
            if (TermDocumentDeSerializationUtil.docIsDeprecSerialized(bis)) {
                LongUTFDataInputStream dis = 
                    new LongUTFDataInputStream(new DataInputStream(bis));
                return TermDocumentDeSerializationUtil.
                createDocumentCellFromDML(dis.readUTF());
            }
        }
        return new DocumentCell(
                TermDocumentDeSerializationUtil.deserializeDocument(bis));
    }
    
    /**
     * Serializes (binary) the given term on given stream.
     * @param term The term to serialize.
     * @param out The stream to serialize to.
     * @throws IOException If document could not be written to stream. 
     */    
    public static void serializeTerm(final Term term, final OutputStream out)
    throws IOException {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(term);
            return;
        } catch (IOException e) {
            LOGGER.warn("Serialization error: Term could not be serialized!");
            throw(e);
        }
    }
    
    /**
     * Deserializes a term from given stream. The term need to be serialized 
     * in binary form.
     * 
     * @param in Stream to read serialized term from.
     * @return Deserialized term.
     * @throws IOException If term could not be red from stream.
     */    
    public static Term deserializeTerm(final InputStream in) throws IOException 
    {
        try {
            ObjectInputStream ois = new ObjectInputStream(in);
            Term term = (Term)ois.readObject();
            return term;
        } catch (IOException e) {
            LOGGER.warn(
                "Deserialization error: Term could not be deserialized!");
            throw(e);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Class <Term> not found, term could not " 
                    + "be deserialized");
            return null;
        }
    }
    
    /**
     * Deserializes a term from given input and wraps it into a 
     * <code>TermCell</code>.
     * 
     * @param input Input to read serialized term from.
     * @return Deserialized term wrapped into <code>TermCell</code>.
     * @throws IOException If term could not be red from input.
     */      
    public static TermCell deserializeTermCell(final DataCellDataInput input)
    throws IOException {
        BufferedInputStream bis = new BufferedInputStream(
                (InputStream)input);            
        if (DESERIALIZATION_UTIL.m_dmlDeserialization) {
            if (TermDocumentDeSerializationUtil.termIsDeprecSerialized(bis)) {
                LongUTFDataInputStream dis = 
                    new LongUTFDataInputStream(new DataInputStream(bis));
                return TermDocumentDeSerializationUtil.createTermCellFromUTF(
                        dis.readUTF());
            }
        }
        return new TermCell(
                TermDocumentDeSerializationUtil.deserializeTerm(bis));
    }
    
    private class TextprocessingPropertyChangeListener implements 
    IPropertyChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            m_dmlDeserialization = 
                TextprocessingPreferenceInitializer.useDmlDeserialization();
        }
    }
}

/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
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
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.knime.core.data.DataCellDataInput;
import org.knime.core.data.container.LongUTFDataInputStream;
import org.knime.core.node.NodeLogger;
import org.knime.ext.textprocessing.TextprocessingCorePlugin;
import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentBlobCell;
import org.knime.ext.textprocessing.data.DocumentBuilder;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentMetaInfo;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermCell;
import org.knime.ext.textprocessing.data.TermCell2;
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
    private static TermDocumentDeSerializationUtil DESERIALIZATION_UTIL = new TermDocumentDeSerializationUtil();

    private boolean m_dmlDeserialization;

    private TermDocumentDeSerializationUtil() {
        m_dmlDeserialization = TextprocessingPreferenceInitializer.DEFAULT_DML_DESERIALIZATION;
        final IPreferenceStore pStore = TextprocessingCorePlugin.getDefault().getPreferenceStore();
        pStore.addPropertyChangeListener(new TextprocessingPropertyChangeListener());
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
     * @deprecated XML serialization is not used anymore since 2.4. Use
     * {@link TermDocumentDeSerializationUtil#fastDeserializeDocument(DataInput)} instead.
     */
    @Deprecated
    public static Document createDocumentFromDML(final String str)
    throws Exception {
        DocumentParser parser = new DmlDocumentParser();
        List<Document> docs = parser.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
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
     * @deprecated XML serialization is not used anymore since 2.4. Use
     * {@link TermDocumentDeSerializationUtil#fastDeserializeDocument(DataInput)} instead.
     */
    @Deprecated
    public static DocumentBlobCell createDocumentBlobCellFromDML(final String str) {
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
     * @deprecated XML serialization is not used anymore since 2.4. Use
     * {@link TermDocumentDeSerializationUtil#fastDeserializeDocument(DataInput)} instead.
     */
    @Deprecated
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
     * @deprecated XML serialization is not used anymore since 2.4. Use
     * {@link TermDocumentDeSerializationUtil#fastDeserializeTerm(DataInput)} instead.
     */
    @Deprecated
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
     * @deprecated XML serialization is not used anymore since 2.4. Use {@link TermCell2} instead.
     */
    @Deprecated
    public static TermCell createTermCellFromUTF(final String s) {
        return new TermCell(TermDocumentDeSerializationUtil.createTermFromUTF(s));
    }

    /**
     * Returns the serialization string of the given term.
     * @param term The term to return its serialization string.
     * @return The serialization string of the given term.
     * @deprecated XML serialization is not used anymore since 2.4. Use
     * {@link TermDocumentDeSerializationUtil#fastSerializeTerm(Term, DataOutput)} instead.
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
     * @deprecated XML serialization is not used anymore since 2.4.
     */
    @Deprecated
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
     * @deprecated XML serialization is not used anymore since 2.4.
     */
    @Deprecated
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
    public static void serializeDocument(final Document doc, final OutputStream out) throws IOException {
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
    public static Document deserializeDocument(final InputStream in) throws IOException {
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
        BufferedInputStream bis = new BufferedInputStream((InputStream)input);
        if (DESERIALIZATION_UTIL.m_dmlDeserialization) {
            if (TermDocumentDeSerializationUtil.docIsDeprecSerialized(bis)) {
                LongUTFDataInputStream dis = new LongUTFDataInputStream(new DataInputStream(bis));
                return TermDocumentDeSerializationUtil.createDocumentBlobCellFromDML(dis.readUTF());
            }
        }
        return new DocumentBlobCell(TermDocumentDeSerializationUtil.deserializeDocument(bis));
    }

    /**
     * Deserializes a document from given input and wraps it into a
     * <code>DocumentCell</code>.
     *
     * @param input Input to read serialized document from.
     * @return Deserialized document wrapped into <code>DocumentCell</code>.
     * @throws IOException If document could not be red from input.
     * @deprecated use {@link TermDocumentDeSerializationUtil#fastDeserializeDocument(DataInput)} instead.
     */
    @Deprecated
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
        return new DocumentCell(TermDocumentDeSerializationUtil.deserializeDocument(bis));
    }

    /**
     * Deserializes a document from given data input by deserializing all fields of the
     * document in a specific order, not using the Java standard object serialization.
     * Terms of the document are deserialized by
     * {@link TermDocumentDeSerializationUtil#fastDeserializeTerm(DataInput)}.
     *
     * @param in The stream to deserialize the document from
     * @return The deserialized document
     * @throws IOException If document could not be written to stream.
     * @since 2.9
     */
    public static Document fastDeserializeDocument(final DataInput in) throws IOException {
        try {
            final UUID uuid = UUID.fromString(in.readUTF());
            final int length = in.readInt();
            final String titleCache = in.readUTF();

            final DocumentType type = DocumentType.stringToDocumentType(in.readUTF());
            final File file = new File(in.readUTF());

            // sections
            final int noSections = in.readInt();
            final List<Section> sections = new ArrayList<Section>(noSections);
            for (int s = 0; s < noSections; s++) {
                final SectionAnnotation anno = SectionAnnotation.stringToAnnotation(in.readUTF());
                // paragraphs
                final int noParagraphs = in.readInt();
                final List<Paragraph> paragraphs = new ArrayList<Paragraph>(noParagraphs);
                for (int p = 0; p < noParagraphs; p++) {
                    // sentences
                    final int noSentences = in.readInt();
                    final List<Sentence> sentences = new ArrayList<Sentence>(noSentences);
                    for (int sen = 0; sen < noSentences; sen++) {
                        // terms
                        final int noTerms = in.readInt();
                        final List<Term> terms = new ArrayList<Term>(noTerms);
                        for (int t = 0; t < noTerms; t++) {
                            terms.add(fastDeserializeTerm(in));
                        }
                        sentences.add(new Sentence(terms));
                    }
                    paragraphs.add(new Paragraph(sentences));
                }
                sections.add(new Section(paragraphs, anno));
            }

            // authors
            final int noAuthors = in.readInt();
            final Set<Author> authors = new LinkedHashSet<Author>(noAuthors);
            for (int i = 0; i < noAuthors; i++) {
                authors.add(new Author(in.readUTF(), in.readUTF()));
            }

            // document sources
            final int noDocSources = in.readInt();
            final Set<DocumentSource> sources = new LinkedHashSet<DocumentSource>(noDocSources);
            for (int i = 0; i < noDocSources; i++) {
                sources.add(new DocumentSource(in.readUTF()));
            }

            // document categories
            final int noDocCategories = in.readInt();
            final Set<DocumentCategory> categories = new LinkedHashSet<DocumentCategory>(noDocCategories);
            for (int i = 0; i < noDocCategories; i++) {
                categories.add(new DocumentCategory(in.readUTF()));
            }

            // publication date
            PublicationDate pubDate =
                PublicationDate.createPublicationDate(in.readInt(), in.readInt(), in.readInt());
            if (pubDate == null) {
                pubDate = new PublicationDate();
            }

            // document meta info
            final int noMetaInfo = in.readInt();
            final Map<String, String> metaInfo = new HashMap<String, String>();
            for (int i = 0; i < noMetaInfo; i++) {
                metaInfo.put(in.readUTF(), in.readUTF());
            }
            final DocumentMetaInfo docMetaInfo = new DocumentMetaInfo(metaInfo);

            // create document
            final Document doc = DocumentBuilder.createDocument(sections, type, authors, sources, categories, pubDate,
                file, docMetaInfo);

            Field field = doc.getClass().getDeclaredField("m_uuid");
            field.setAccessible(true);
            field.set(doc, uuid);
            field = doc.getClass().getDeclaredField("m_length");
            field.setAccessible(true);
            field.setInt(doc, length);
            field = doc.getClass().getDeclaredField("m_titleCache");
            field.setAccessible(true);
            field.set(doc, titleCache);

            return doc;
        } catch (IOException e) {
            LOGGER.warn("Deserialization error: Document could not be deserialized!");
            throw(e);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Deserialization error: Internal field could not be set to document!");
            throw(e);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Deserialization error: Internal field could not be accessed to document!", e);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("Deserialization error: Internal field is not available in document!", e);
        } catch (SecurityException e) {
            LOGGER.warn("Deserialization error: Internal field could not be set to document!");
            throw(e);
        } catch (ParseException e) {
            LOGGER.warn("Deserialization error: Could not parse deserialized publication date!", e);
        }
        return null;
    }

    /**
     * Serializes (binary) the given document to given data output by serializing all fields of
     * the document in a specific order and not using standard Java serialization. Terms of the
     * documents are serialized by
     * {@link TermDocumentDeSerializationUtil#fastSerializeTerm(Term, DataOutput)}
     *
     * @param doc The document to serialize
     * @param out The stream to serialize to.
     * @throws IOException If document could not be written to stream.
     * @since 2.9
     */
    public static void fastSerializeDocument(final Document doc, final DataOutput out) throws IOException {
        try {
            out.writeUTF(doc.getUUID().toString());
            out.writeInt(doc.getLength());
            out.writeUTF(doc.getTitle());
            out.writeUTF(doc.getType().toString());
            out.writeUTF(doc.getDocFile().getAbsolutePath());

            // sections
            out.writeInt(doc.getSections().size());
            for (Section s : doc.getSections()) {
                out.writeUTF(s.getAnnotation().toString());
                // paragraphs
                out.writeInt(s.getParagraphs().size());
                for (Paragraph p : s.getParagraphs()) {
                    // sentences
                    out.writeInt(p.getSentences().size());
                    for (Sentence sen : p.getSentences()) {
                        // terms
                        out.writeInt(sen.getTerms().size());
                        for (Term t : sen.getTerms()) {
                            fastSerializeTerm(t, out);
                        }
                    }
                }
            }

            // authors
            out.writeInt(doc.getAuthors().size());
            for (Author a : doc.getAuthors()) {
                out.writeUTF(a.getFirstName());
                out.writeUTF(a.getLastName());
            }

            // document sources
            out.writeInt(doc.getSources().size());
            for (DocumentSource src : doc.getSources()) {
                out.writeUTF(src.getSourceName());
            }

            // document categories
            out.writeInt(doc.getCategories().size());
            for (DocumentCategory cat : doc.getCategories()) {
                out.writeUTF(cat.getCategoryName());
            }

            // publication date
            out.writeInt(doc.getPubDate().getYear());
            out.writeInt(doc.getPubDate().getMonth());
            out.writeInt(doc.getPubDate().getDay());

            // document meta info
            final DocumentMetaInfo metaInfo = doc.getMetaInformation();
            if (metaInfo == null) {
                out.writeInt(0);
            } else {
                out.writeInt(metaInfo.size());
                for (String key : metaInfo.getMetaInfoKeys()) {
                    out.writeUTF(key);
                    out.writeUTF(metaInfo.getMetaInfoValue(key));
                }
            }

            return;
        } catch (IOException e) {
            LOGGER.warn("Serialization error: Document could not be serialized!");
            throw(e);
        }
    }

    /**
     * Serializes the given term on given data output by serializing its lists of words, whitespace suffices
     * and tags. Terms that consist of one word only and have no tags assigned are serialized without serializing the
     * list of word(s) but only the one word itself.
     *
     * @param term The term to serialize.
     * @param out The stream to serialize to.
     * @throws IOException If document could not be written to stream.
     * @since 2.9
     */
    public static void fastSerializeTerm(final Term term, final DataOutput out) throws IOException {
        try {
            boolean isComplex = term.getWords().size() != 1 && term.getTags().isEmpty();
            out.writeBoolean(isComplex);
            out.writeBoolean(term.isUnmodifiable());
            out.writeInt(term.hashCode());
            if (isComplex) {
                // write words
                out.writeInt(term.getWords().size());
                for (Word w : term.getWords()) {
                    out.writeUTF(w.getWord());
                    out.writeUTF(w.getWhitespaceSuffix());
                }
                // write tags
                out.writeInt(term.getTags().size());
                for (Tag t : term.getTags()) {
                    out.writeUTF(t.getTagValue());
                    out.writeUTF(t.getTagType());
                }
            } else {
                final Word theWord = term.getWords().get(0);
                out.writeUTF(theWord.getWord());
                out.writeUTF(theWord.getWhitespaceSuffix());
            }
        } catch (IOException e) {
            LOGGER.warn("Serialization error: Term could not be serialized!");
            throw(e);
        }
    }

    /**
     * Deserializes a term from given data input by deserializing its lists of words, whitespace suffices and tags.
     * Terms that consist of one word only and have no tags assigned are deserialized without deserializing the
     * list of word(s) but only the one word itself.
     *
     * @param in Stream to read serialized term from.
     * @return Deserialized term.
     * @throws IOException If term could not be red from stream.
     * @since 2.9
     */
    public static Term fastDeserializeTerm(final DataInput in) throws IOException {
        try {
            final boolean isComplex = in.readBoolean();
            final boolean unmodifieable = in.readBoolean();
            final int hashCode = in.readInt();
            final List<Word> words;
            final List<Tag> tags;

            if (isComplex) {
                // read words
                final int noWords = in.readInt();
                words = new ArrayList<Word>(noWords);
                for (int i = 0; i < noWords; i++) {
                    words.add(new Word(in.readUTF(), in.readUTF()));
                }
                // read tags
                final int noTags = in.readInt();
                tags = new ArrayList<Tag>(noTags);
                for (int i = 0; i < noTags; i++) {
                    tags.add(new Tag(in.readUTF(), in.readUTF()));
                }
            } else {
                final Word w = new Word(in.readUTF(), in.readUTF());
                words = new ArrayList<Word>(1);
                words.add(w);
                tags = new ArrayList<Tag>(0);
            }

            final Term t = new Term(words, tags, unmodifieable);
            Field hashField = t.getClass().getDeclaredField("m_hashCode");
            hashField.setAccessible(true);
            hashField.setInt(t, hashCode);

            return t;
        } catch (IOException e) {
            LOGGER.warn("Deserialization error: Term could not be deserialized!");
            throw(e);
        } catch (NoSuchFieldException e) {
            LOGGER.warn("Deserialization error: No hash code field in term!", e);
        } catch (SecurityException e) {
            LOGGER.warn("Deserialization error: Hash code coud not be set to term!");
            throw(e);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Deserialization error: Hash code coud not be set to term!");
            throw(e);
        } catch (IllegalAccessException e) {
            LOGGER.warn("Deserialization error: No access to hash code field in term!", e);
        }
        return null;
    }

    /**
     * Serializes (binary) the given term on given stream.
     * @param term The term to serialize.
     * @param out The stream to serialize to.
     * @throws IOException If document could not be written to stream.
     * @deprecated use {@link TermDocumentDeSerializationUtil#fastSerializeTerm(Term, DataOutput)} instead.
     */
    @Deprecated
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
     * @deprecated use {@link TermDocumentDeSerializationUtil#fastDeserializeTerm(DataInput)} instead.
     */
    @Deprecated
    public static Term deserializeTerm(final InputStream in) throws IOException {
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
     * @deprecated use {@link TermDocumentDeSerializationUtil#fastDeserializeTerm(DataInput)} instead.
     */
    @Deprecated
    public static TermCell deserializeTermCell(final DataCellDataInput input)
    throws IOException {
        BufferedInputStream bis = new BufferedInputStream(
                (InputStream)input);
        if (DESERIALIZATION_UTIL.m_dmlDeserialization) {
            if (TermDocumentDeSerializationUtil.termIsDeprecSerialized(bis)) {
                LongUTFDataInputStream dis =
                    new LongUTFDataInputStream(new DataInputStream(bis));
                return TermDocumentDeSerializationUtil.createTermCellFromUTF(dis.readUTF());
            }
        }
        return new TermCell(TermDocumentDeSerializationUtil.deserializeTerm(bis));
    }

    private class TextprocessingPropertyChangeListener implements IPropertyChangeListener {
        /**
         * {@inheritDoc}
         */
        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            m_dmlDeserialization = TextprocessingPreferenceInitializer.useDmlDeserialization();
        }
    }
}

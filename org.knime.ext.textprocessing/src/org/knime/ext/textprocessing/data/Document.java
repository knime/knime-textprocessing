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
 * ---------------------------------------------------------------------
 *
 * History
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.knime.ext.textprocessing.data.tag.Tagged;
import org.knime.ext.textprocessing.util.TextContainers;

/**
 * Contains the documents text as with all its
 * {@link org.knime.ext.textprocessing.data.Section}s, which are, i.e. title,
 * abstract, chapters, etc. These
 * {@link org.knime.ext.textprocessing.data.Section}s consist of
 * {@link org.knime.ext.textprocessing.data.Paragraph}s which contain
 * {@link org.knime.ext.textprocessing.data.Sentence}s containing
 * {@link org.knime.ext.textprocessing.data.Term}s. A term finally is
 * represented by one or more {@link org.knime.ext.textprocessing.data.Word}s.
 * Additionally {@link org.knime.ext.textprocessing.data.Author}s, a
 * {@link org.knime.ext.textprocessing.data.PublicationDate}, a
 * {@link org.knime.ext.textprocessing.data.DocumentSource}, a
 * {@link org.knime.ext.textprocessing.data.DocumentCategory} and a
 * {@link org.knime.ext.textprocessing.data.DocumentType} can be assigned to a
 * <code>Document</code> in order to specify more details. <br/>
 * <br/>
 * To create instances of <code>Document</code> use the
 * {@link org.knime.ext.textprocessing.data.DocumentBuilder} which provides
 * methods to add text and finally build a new <code>Document</code> instance
 * out of it.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Document implements TextContainer, Serializable, Tagged {

    /**
     * Serial Version ID.
     */
    private static final long serialVersionUID = 8370032424383401173L;

    /**
     * The default document type value (UNKNOWN).
     */
    public static final DocumentType DEFAULT_TYPE = DocumentType.UNKNOWN;

    /**
     * The default document file . (System.getProperty("user.home") +
     * "NoFileSpecified")):
     */
    public static final File DEFAULT_FILE = new File(
            System.getProperty("user.home") + "/NoFileSpecified.txt");

    private final UUID m_uuid = UUID.randomUUID();

    private List<Section> m_sections;

    private DocumentType m_type;

    private Set<Author> m_authors;

    private Set<DocumentSource> m_sources;

    private Set<DocumentCategory> m_categories;

    private PublicationDate m_pubDate;

    private File m_docFile;

    private DocumentMetaInfo m_metaInfo;

    /**
     * Length of the document in terms.
     */
    private int m_length = -1;

    private String m_titleCache = null;

    /**
     * Creates a new instance of <code>Document</code> with the given
     * parameters, like the documents sections, type, authors, sources,
     * categories, publication date, the file of the document and the meta
     * information to set. If any of these parameters is <code>null</code> a
     * <code>NullPointerException</code> is thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     *            title, abstract chapters, etc.
     * @param type The type of the document to set, i.e. book, transaction or
     *            proceeding.
     * @param authors The authors of the document.
     * @param sources The sources of a document to set, specifying where the
     *            documents stems from, i.e. Reuters, PubMed, etc.
     * @param categories The categories of a document to set, specifying roughly
     *            the topic of the document, i.e. breast cancer, presidential
     *            elections.
     * @param date The documents publication date to set.
     * @param documentFile The file containing the document.
     * @param metaInfo The meta information of the document to set.
     * @throws NullPointerException If any of the parameters are set
     *             <code>null</code>.
     * @since 2.8
     */
    Document(final List<Section> sections, final DocumentType type,
            final Set<Author> authors, final Set<DocumentSource> sources,
            final Set<DocumentCategory> categories, final PublicationDate date,
            final File documentFile, final DocumentMetaInfo metaInfo)
                    throws NullPointerException {

        // sections
        if (sections == null) {
            throw new NullPointerException(
                    "The list of sections may not be null!");
        }

        // type
        if (type == null) {
            throw new NullPointerException(
                    "The document type may not be null!");
        }

        // authors
        if (authors == null) {
            throw new NullPointerException(
                    "The set of authors may not be null!");
        }

        // sources
        if (sources == null) {
            throw new NullPointerException(
                    "The set of sources may not be null!");
        }

        // categories
        if (categories == null) {
            throw new NullPointerException(
                    "The set of categories may not be null!");
        }

        // date
        if (date == null) {
            throw new NullPointerException(
                    "The publication date may not be null!");
        }

        // file (if null create empty file instance)
        if (documentFile == null) {
            m_docFile = DEFAULT_FILE;
        } else {
            m_docFile = documentFile;
        }

        // the meta information
        if (metaInfo == null) {
            throw new NullPointerException(
                    "The meta information may not be null!");
        }

        m_pubDate = date;
        m_categories = categories;
        m_sources = sources;
        m_authors = authors;
        m_type = type;
        m_sections = sections;
        m_metaInfo = metaInfo;
    }

    /**
     * Creates a new instance of <code>Document</code> with the given
     * parameters, like the documents sections, type, authors, sources,
     * categories, publication date and the file of the document to set. If any
     * of these parameters is <code>null</code> a
     * <code>NullPointerException</code> is thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     *            title, abstract chapters, etc.
     * @param type The type of the document to set, i.e. book, transaction or
     *            proceeding.
     * @param authors The authors of the document.
     * @param sources The sources of a document to set, specifying where the
     *            documents stems from, i.e. Reuters, PubMed, etc.
     * @param categories The categories of a document to set, specifying roughly
     *            the topic of the document, i.e. breast cancer, presidential
     *            elections.
     * @param date The documents publication date to set.
     * @param documentFile The file containing the document.
     * @throws NullPointerException If any of the parameters are set
     *             <code>null</code>.
     */
    Document(final List<Section> sections, final DocumentType type,
             final Set<Author> authors, final Set<DocumentSource> sources,
             final Set<DocumentCategory> categories, final PublicationDate date,
             final File documentFile) throws NullPointerException {
        this(sections, type, authors, sources, categories, date, documentFile,
             new DocumentMetaInfo());
    }

    /**
     * Creates a new instance of <code>Document</code> with a given list of
     * sections which may not be <code>null</code>, otherwise a
     * <code>NullPointerException</code> will be thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     *            title, abstract chapters, etc.
     * @throws NullPointerException If the given list of sections to set is
     *             <code>null</code>.
     */
    Document(final List<Section> sections) throws NullPointerException {
        this(sections, DEFAULT_TYPE, new LinkedHashSet<Author>(),
                new LinkedHashSet<DocumentSource>(), new LinkedHashSet<DocumentCategory>(),
                new PublicationDate(), null);
    }

    /**
     * Creates a new instance of <code>Document</code> with a given list of
     * sections, the authors of the document, the publication date and a file
     * containing the document's text to set. None of the given parameters may
     * be <code>null</code>, otherwise a <code>NullPointerException</code> will
     * be thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     *            title, abstract chapters, etc.
     * @param authors The authors of the document.
     * @param date The documents publication date to set.
     * @param documentFile The file containing the document.
     * @throws NullPointerException If any of the parameters are set
     *             <code>null</code>.
     */
    Document(final List<Section> sections, final Set<Author> authors,
            final PublicationDate date, final File documentFile) {
        this(sections, DEFAULT_TYPE, authors, new LinkedHashSet<DocumentSource>(),
                new LinkedHashSet<DocumentCategory>(), date, documentFile);
    }

    /**
     * @return the sections of the document.
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(m_sections);
    }

    /**
     * @return the type of the document.
     */
    public DocumentType getType() {
        return m_type;
    }

    /**
     * @return the authors of the document.
     */
    public Set<Author> getAuthors() {
        return Collections.unmodifiableSet(m_authors);
    }

    /**
     * @return the sources of the document.
     */
    public Set<DocumentSource> getSources() {
        return Collections.unmodifiableSet(m_sources);
    }

    /**
     * @return the categories of the document.
     */
    public Set<DocumentCategory> getCategories() {
        return Collections.unmodifiableSet(m_categories);
    }

    /**
     * @return the publication date of the document.
     */
    public PublicationDate getPubDate() {
        return m_pubDate;
    }

    /**
     * @return the file containing the document.
     */
    public File getDocFile() {
        return m_docFile;
    }

    /**
     * @return The meta information of the document.
     * @since 2.8
     */
    public DocumentMetaInfo getMetaInformation() {
        return m_metaInfo;
    }

    /**
     * Returns all {@link org.knime.ext.textprocessing.data.Section}s with the
     * specified {@link org.knime.ext.textprocessing.data.SectionAnnotation} as
     * list. If no sections can be found, an empty list is returned.
     *
     * @param annotation The annotation of the sections to return.
     * @return a list of sections with the given annotation assigned. If no
     *         sections can be found, an empty list is returned.
     */
    public List<Section> getSection(final SectionAnnotation annotation) {
        List<Section> sections = new ArrayList<Section>();
        for (Section s : m_sections) {
            if (s.getAnnotation().equals(annotation)) {
                sections.add(s);
            }
        }
        return sections;
    }

    /**
     * Returns the text of all {@link org.knime.ext.textprocessing.data.Section}
     * s with the specified
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} as string. If
     * no sections can be found an empty string is returned.
     *
     * @param annotation The annotation of the sections to find.
     * @return the text of the sections with the given annotation assigned as
     *         string. If no sections can be found, an empty string is returned.
     */
    public String getSectionText(final SectionAnnotation annotation) {
        StringBuilder sb = new StringBuilder();
        List<Section> secs = getSection(annotation);
        for (Section section : secs) {
            sb.append(section.getText());
        }
        return sb.toString();
    }

    /**
     * @return The title of the document, if a section with "Title" annotation
     *         exists, otherwise an empty string.
     */
    public String getTitle() {
        if (m_titleCache == null) {
            m_titleCache = getSectionText(SectionAnnotation.TITLE);
        }
        return m_titleCache;
    }

    /**
     * @return the length of the document in terms.
     */
    public int getLength() {
        if (m_length == -1) {
            m_length = 0;
            for (Section section : m_sections) {
                for (Paragraph paragraph : section.getParagraphs()) {
                    for (Sentence sentence : paragraph.getSentences()) {
                        m_length += sentence.getTerms().size();
                    }
                }
            }
        }

        return m_length;
    }

    /**
     * @return The abstract of the document, if a section with "Abstract"
     *         annotation exists, otherwise an empty string.
     */
    public String getAbstract() {
        return getSectionText(SectionAnnotation.ABSTRACT);
    }

    /**
     * @return The body text of a document consisting of text from unknown, chapter, and abstract sections. Text from
     * title and meta info sections are nopt included.
     * @since 2.8
     */
    public String getDocumentBodyText() {
        List<Section> sections = new ArrayList<Section>();
        for (Section sec : m_sections) {
            if (sec.getAnnotation().equals(SectionAnnotation.UNKNOWN)
                    || sec.getAnnotation().equals(SectionAnnotation.CHAPTER)
                    || sec.getAnnotation().equals(SectionAnnotation.ABSTRACT)) {
                sections.add(sec);
            }
        }
        return TextContainers.getText(sections);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return TextContainers.getText(m_sections);
    }

    /**
     * {@inheritDoc}
     * @since 2.8
     */
    @Override
    public String getTextWithWsSuffix() {
        return TextContainers.getTextWithWsSuffix(m_sections);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Section section : m_sections) {
            sb.append(section.toString());
        }
        return sb.toString();
    }

    /**
     * Checks the parts of the given document for equality which are not user
     * defined, such as all the <b>sections</b>, the <b>authors</b>, and the
     * <b>publication date</b>. Beside sections, authors, and publication date
     * no other members are compared. If given document is considered as equal
     * <code>true</code> is returned, otherwise <code>false</code>.
     *
     * @param d The document to check for equality based on fixed document
     *            members.
     * @return <code>true</code> if sections, authors, and publication date of
     *         given document are equal, <code>false</code> otherwise.
     */
    public boolean equalsContent(final Document d) {
        if (d == null) {
            return false;
        }

        if ((d.getAuthors() == null && m_authors != null)
                || !d.getAuthors().equals(m_authors)) {
            return false;
        } else if ((d.getSections() == null && m_sections != null)
                || !d.getSections().equals(m_sections)) {
            return false;
        } else if ((d.getPubDate() == null && m_pubDate != null)
                || !d.getPubDate().equals(m_pubDate)) {
            return false;
        } else if ((d.getMetaInformation() == null && m_metaInfo != null)
                || (m_metaInfo == null && d.getMetaInformation() != null)
                || (d.getMetaInformation() != null && m_metaInfo != null
                    && !d.getMetaInformation().equals(m_metaInfo))) {
            return false;
        }

        return true;
    }

    /**
     * Checks the complete document for equality. All members must match to
     * consider the given document as equal.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof Document)) {
            return false;
        }
        Document d = (Document)o;
        if (this == d) {
            return true;
        }

        if (!d.getUUID().equals(m_uuid)) {
            return false;
        }
        if ((d.getSources() == null && m_sources != null)
                || !d.getSources().equals(m_sources)) {
            return false;
        } else if ((d.getCategories() == null && m_categories != null)
                || !d.getCategories().equals(m_categories)) {
            return false;
        } else if ((d.getType() == null && m_type != null)
                || !d.getType().equals(m_type)) {
            return false;
        } else if ((d.getDocFile() == null && m_docFile != null)
                || !d.getDocFile().equals(m_docFile)) {
            return false;
        } else if (!equalsContent(d)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return m_uuid.hashCode();
    }

    /**
     * @return The UUID of the document.
     */
    public UUID getUUID() {
        return m_uuid;
    }

    /**
     * @return a read-only iterator on the sentences of this document.
     */
    public Iterator<Sentence> sentenceIterator() {
        return new SentenceIterator(this);
    }

    /**
     * Read-only iterator over a document's sentences.
     *
     * @author Pierre-Francois Laquerre, University of Konstanz
     */
    private class SentenceIterator implements Iterator<Sentence> {
        private List<Sentence> m_sentences;

        private ListIterator<Sentence> m_iterator;

        /**
         * @param doc the document to iterate over
         */
        public SentenceIterator(final Document doc) {
            m_sentences = new ArrayList<Sentence>();

            for (Section s : doc.getSections()) {
                for (Paragraph p : s.getParagraphs()) {
                    m_sentences.addAll(p.getSentences());
                }
            }

            m_iterator = m_sentences.listIterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return m_iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Sentence next() {
            return m_iterator.next();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * @since 4.6
     */
    @Override
    public Stream<Tag> getTagStream() {
        return Tagged.getTagSetStream(m_sections);
    }
}

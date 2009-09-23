/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2009
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   13.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

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
 * {@link org.knime.ext.textprocessing.data.DocumentType} can be assigned to
 * a <code>Document</code> in order to specify more details.
 * <br/><br/>
 * To create instances of <code>Document</code> use the
 * {@link org.knime.ext.textprocessing.data.DocumentBuilder} which provides
 * methods to add text and finally build a new <code>Document</code> instance
 * out of it.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class Document implements TextContainer, Serializable {

    /**
     * The default document type value (UNKNOWN).
     */
    public static final DocumentType DEFAULT_TYPE = DocumentType.UNKNOWN;

    /**
     * The default document file .
     * (System.getProperty("user.home") + "NoFileSpecified")):
     */
    public static final File DEFAULT_FILE = new File(
            System.getProperty("user.home") + "/NoFileSpecified.txt");

    private List<Section> m_sections;

    private DocumentType m_type;

    private Set<Author> m_authors;

    private Set<DocumentSource> m_sources;

    private Set<DocumentCategory> m_categories;

    private PublicationDate m_pubDate;

    private File m_docFile;

    /**
     * Length of the document in terms.
     */
    private int m_length = -1;

    /**
     * Cache of the hash code.
     */
    private int m_hashCode = -1;

    /**
     * Creates a new instance of <code>Document</code> with the given
     * parameters, like the documents sections, type, authors, sources,
     * categories, publication date and the file of the document to set.
     * If any of these parameters is <code>null</code> a
     * <code>NullPointerException</code> is thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     * title, abstract chapters, etc.
     * @param type The type of the document to set, i.e. book, transaction
     * or proceeding.
     * @param authors The authors of the document.
     * @param sources The sources of a document to set, specifying where the
     * documents stems from, i.e. Reuters, PubMed, etc.
     * @param categories The categories of a document to set, specifying roughly
     * the topic of the document, i.e. breast cancer, presidential elections.
     * @param date The documents publication date to set.
     * @param documentFile The file containing the document.
     * @throws NullPointerException If any of the parameters are set
     * <code>null</code>.
     */
    Document(final List<Section> sections, final DocumentType type,
            final Set<Author> authors, final Set<DocumentSource> sources,
            final Set<DocumentCategory> categories, final PublicationDate date,
            final File documentFile) throws NullPointerException {

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
        
        m_pubDate = date;
        m_categories = categories;
        m_sources = sources;
        m_authors = authors;
        m_type = type;
        m_sections = sections;
    }

    /**
     * Creates a new instance of <code>Document</code> with a given list of
     * sections which may not be <code>null</code>, otherwise a
     * <code>NullPointerException</code> will be thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     * title, abstract chapters, etc.
     * @throws NullPointerException If the given list of sections to set is
     * <code>null</code>.
     */
    Document(final List<Section> sections)
    throws NullPointerException {
        this(sections, DEFAULT_TYPE, new HashSet<Author>(),
                new HashSet<DocumentSource>(), new HashSet<DocumentCategory>(),
                new PublicationDate(), null);
    }


    /**
     * Creates a new instance of <code>Document</code> with a given list of
     * sections, the authors of the document, the publication date and a file
     * containing the document's text to set. None of the given parameters
     * may be <code>null</code>, otherwise a <code>NullPointerException</code>
     * will be thrown.
     *
     * @param sections The sections of the document to set. Sections are i.e.
     * title, abstract chapters, etc.
     * @param authors The authors of the document.
     * @param date The documents publication date to set.
     * @param documentFile The file containing the document.
     * @throws NullPointerException If any of the parameters are set
     * <code>null</code>.
     */
    Document(final List<Section> sections, final Set<Author> authors,
            final PublicationDate date, final File documentFile) {
        this(sections, DEFAULT_TYPE, authors,
                new HashSet<DocumentSource>(), new HashSet<DocumentCategory>(),
                date, documentFile);
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
     * Returns all {@link org.knime.ext.textprocessing.data.Section}s with the
     * specified {@link org.knime.ext.textprocessing.data.SectionAnnotation} as
     * list. If no sections can be found, an empty list is returned.
     *
     * @param annotation The annotation of the sections to return.
     * @return a list of sections with the given annotation assigned.
     * If no sections can be found, an empty list is returned.
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
     * Returns the text of all
     * {@link org.knime.ext.textprocessing.data.Section}s with the specified
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} as string.
     * If no sections can be found an empty string is returned.
     *
     * @param annotation The annotation of the sections to find.
     * @return the text of the sections with the given annotation assigned as
     * string. If no sections can be found, an empty string is returned.
     */
    public String getSectionText(final SectionAnnotation annotation) {
        StringBuilder sb = new StringBuilder();
        List<Section> secs = getSection(SectionAnnotation.TITLE);
        for (int i = 0; i < secs.size(); i++) {
            sb.append(secs.get(i).getText());
            if (i < secs.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }


    /**
     * @return The title of the document, if a section with "Title" annotation
     * exists, otherwise an empty string.
     */
    public String getTitle() {
        return getSectionText(SectionAnnotation.TITLE);
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
     * annotation exists, otherwise an empty string.
     */
    public String getAbstract() {
        return getSectionText(SectionAnnotation.ABSTRACT);
    }


    /**
     * {@inheritDoc}
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_sections.size(); i++) {
            sb.append(m_sections.get(i).getText());
            if (i < m_sections.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m_sections.size(); i++) {
            sb.append(m_sections.get(i).toString());
            if (i < m_sections.size() - 1) {
                sb.append(Term.WORD_SEPARATOR);
            }
        }
        return sb.toString();
    }

    /**
     * Checks the parts of the given document for equality which are not user
     * defined, such as all the <b>sections</b>, the <b>authors</b>, and
     * the <b>publication date</b>. Beside sections, authors, and publication 
     * date no other members are compared. If given document is considered
     * as equal <code>true</code> is returned, otherwise <code>false</code>.
     * 
     * @param d The document to check for equality based on fixed document
     * members.
     * @return <code>true</code> if sections, authors, and publication date 
     * of given document are equal, <code>false</code> otherwise. 
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
        if (m_hashCode == -1) {
            m_hashCode = 0;
            int fac = 119 / 19;

            for (Section s : m_sections) {
                m_hashCode += fac * s.hashCode();
            }
            for (Author a : m_authors) {
                m_hashCode += fac * a.hashCode();
            }
            for (DocumentSource s : m_sources) {
                m_hashCode += fac * s.hashCode();
            }
            for (DocumentCategory c : m_categories) {
                m_hashCode += fac * c.hashCode();
            }
            if (m_pubDate != null) {
                m_hashCode += fac * m_pubDate.hashCode();
            }
            if (m_type != null) {
                m_hashCode += fac * m_type.hashCode();
            }
            if (m_docFile != null) {
                m_hashCode += fac * m_docFile.hashCode();
            }
        }

        return m_hashCode;
    }

    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeObject(DocumentCell.getSerializationString(this));
    }

    
    private void readObject(final ObjectInputStream in) 
    throws IOException, ClassNotFoundException {
        Object o = in.readObject();
        if (!(o instanceof String)) {
            throw new ClassNotFoundException(
                    "Serialized object is not a String!");
        }
        String docStr = (String)o;
        try {            
            Document doc = DocumentCell.createDocument(docStr);
            m_authors = doc.getAuthors();
            m_categories = doc.getCategories();
            m_docFile = doc.getDocFile();
            m_length = doc.getLength();
            m_pubDate = doc.getPubDate();
            m_sections = doc.getSections();
            m_sources = doc.getSources();
            m_type = doc.getType();
        } catch (Exception e) {
            throw new IOException("Could not deserialize document! " 
                    + e.getMessage()); 
        }
    }
    
    
    
    /**
     * @return a read-only iterator on the sentences of this document.
     */
    public Iterator<Sentence> sentenceIterator() {
        return new SentenceIterator(this);
    }

    /**
     * Read-only iterator over a document's sentences.
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
        public boolean hasNext() {
            return m_iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public Sentence next() {
            return m_iterator.next();
        }

        /**
         * {@inheritDoc}
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

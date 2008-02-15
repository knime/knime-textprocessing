/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class Document implements TextContainer {

    /**
     * The default document type value (UNKNOWN).
     */
    public static final DocumentType DEFAULT_TYPE = DocumentType.UNKNOWN;
    
    
    private List<Section> m_sections;
    
    private DocumentType m_type;
    
    private Set<Author> m_authors;
    
    private Set<DocumentSource> m_sources;
    
    private Set<DocumentCategory> m_categories;
    
    private PublicationDate m_pubDate;
    
    private File m_docFile;
    
    
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
    public Document(final List<Section> sections, final DocumentType type,
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
        
        m_docFile = documentFile;
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
    public Document(final List<Section> sections) 
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
    public Document(final List<Section> sections, final Set<Author> authors, 
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
    public String getTitel() {
        return getSectionText(SectionAnnotation.TITLE);
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
        if (!d.getSections().equals(m_sections)) {
            return false;
        } else if (!d.getAuthors().equals(m_authors)) {
            return false;
        } else if (!d.getPubDate().equals(m_pubDate)) {
            return false;
        } else if (!d.getDocFile().equals(m_docFile)) {
            return false;
        } else if (!d.getSources().equals(m_sources)) {
            return false;
        } else if (!d.getCategories().equals(m_categories)) {
            return false;
        } else if (!d.getType().equals(m_type)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int fac = 119;
        int div = 19;
        int hash = 0;
        for (Section s : m_sections) {
            hash += fac * s.hashCode() / div; 
        }
        for (Author a : m_authors) {
            hash += fac / div * a.hashCode();
        }
        for (DocumentSource s : m_sources) {
            hash += fac / div * s.hashCode();
        }
        for (DocumentCategory c : m_categories) {
            hash += fac / div * c.hashCode();
        }
        hash += fac / div * m_pubDate.hashCode();
        hash += fac / div * m_type.hashCode();
        hash += fac / div * m_docFile.hashCode();
        
        return hash;
    }    
}

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
 *   14.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.data.Author;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCategory;
import org.knime.ext.textprocessing.data.DocumentSource;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.PublicationDate;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;

/**
 * A utility class which helps building up a 
 * {@link org.knime.ext.textprocessing.data.Document} by providing methods
 * which allow to add sections, paragraphs and sentences in an easy way,
 * create the a documents word cache and much more.
 * 
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentBuilder {
    
    private List<Sentence> m_sentences = new ArrayList<Sentence>();
    
    private List<Paragraph> m_paragraphs = new ArrayList<Paragraph>();
    
    private List<Section> m_sections = new ArrayList<Section>();
    
    private Hashtable<String, Word> m_words = new Hashtable<String, Word>();
    
    
    private PublicationDate m_date = new PublicationDate();
    
    private File m_docFile = null;
    
    private DocumentType m_type = DocumentType.UNKNOWN;
    
    private Set<Author> m_authors = new HashSet<Author>();
    
    private Set<DocumentSource> m_sources = new HashSet<DocumentSource>();
    
    private Set<DocumentCategory> m_categories = 
        new HashSet<DocumentCategory>();
    
    
    
    public DocumentBuilder() {
        
    }
    
    /**
     * Builds a new {@link org.knime.ext.textprocessing.data.Document} instance
     * with the specified data, like authors, sections, etc.
     * 
     * @return a new {@link org.knime.ext.textprocessing.data.Document} instance
     * with the specified data.
     */
    public Document createDocument() {
        return new Document(m_sections, m_type, m_authors, m_sources, 
                m_categories, m_date, m_docFile);
    }

    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.Author} to the 
     * list of authors.
     * 
     * @param author the author to add to the authors list.
     */
    public void addAuthor(final Author author) {
        m_authors.add(author);
    }    
    
    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.DocumentSource} 
     * to the list of sources.
     * 
     * @param source the source to add to the sources list.
     */
    public void addDocumentSource(final DocumentSource source) {
        m_sources.add(source);
    }    
    
    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.DocumentCategory}
     * to the list of categories.
     * 
     * @param category the category to add to the categories list.
     */
    public void addDocumentCategory(final DocumentCategory category) {
        m_categories.add(category);
    } 
    
    
    /**
     * @param date The date to set as 
     * {@link org.knime.ext.textprocessing.data.PublicationDate}.
     */
    public void setPublicationDate(final PublicationDate date) {
        m_date = date;
    }
    
    /**
     * @param file The file containing the document.
     */
    public void setDocumentFile(final File file) {
        m_docFile = file;
    }    
    
    /**
     * @param type The type to set as 
     * {@link org.knime.ext.textprocessing.data.DocumentType}.
     */
    public void setDocumentType(final DocumentType type) {
        m_type = type;
    }    
    
    
    
    
    
    
    /**
     * Tokenizes the given title and add it as a 
     * {@link org.knime.ext.textprocessing.data.Section} with <code>TITLE</code>
     * annotation to the list of sections.
     * 
     * @param title The title to tokenize and to add as section.
     */
    public void addTitle(final String title) {
        Sentence s = addSentence(title);
        List<Sentence> sentences = new ArrayList<Sentence>();
        sentences.add(s);
        Paragraph p = new Paragraph(sentences);
        List<Paragraph> paragraphs = new ArrayList<Paragraph>();
        paragraphs.add(p);
        Section section = new Section(paragraphs, SectionAnnotation.TITLE);
        m_sections.add(section);
    }
    
    /**
     * Creates a new {@link org.knime.ext.textprocessing.data.Paragraph} out of 
     * the current list of {@link org.knime.ext.textprocessing.data.Sentence}s
     * and adds it to the current list of paragraphs. After adding the current
     * list of sentences to the new paragraph a new empty list is created. 
     */
    public void createNewParagraph() {
        if (m_sentences != null && m_sentences.size() > 0) {
            Paragraph p = new Paragraph(m_sentences);
            m_paragraphs.add(p);
        }
        m_sentences = new ArrayList<Sentence>();
    }
    
    /**
     * Creates a new {@link org.knime.ext.textprocessing.data.Section} out of
     * the current list of {@link org.knime.ext.textprocessing.data.Paragraph}s
     * and ads it to the current list of sections. After adding the current
     * list of paragraphs to the new section a new empty list is created.  
     * The given {@link org.knime.ext.textprocessing.data.SectionAnnotation} 
     * is added to the section and specifies its role. 
     * 
     * @param annotation The 
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} to add to the
     * section. 
     */
    public void createNewSection(final SectionAnnotation annotation) {
        if (m_paragraphs != null && m_paragraphs.size() > 0) {
            Section s = new Section(m_paragraphs, annotation);
            m_sections.add(s);
        }
        m_paragraphs = new ArrayList<Paragraph>();
    }    
    
    
    
    /**
     * Tokenizes the given sentence and adds it as 
     * {@link org.knime.ext.textprocessing.data.Sentence} to the current list
     * of sentences.
     * 
     * @param sentence The sentence to tokenize and to add to the current list
     * of sentences. 
     */
    public void addNewSentence(final String sentence) {
        Sentence s = addSentence(sentence);
        if (m_sentences == null) {
            m_sentences = new ArrayList<Sentence>();
        }
        m_sentences.add(s);
    }    
    
    /**
     * Tokenizes the given paragraph and adds it as a 
     * {@link org.knime.ext.textprocessing.data.Paragraph} to the current list
     * of paragraphs.
     * 
     * @param paragraph The paragraph to tokenize and to add to the current list
     * of paragraphs. 
     */
    public void addNewParagraph(final String paragraph) {
        Paragraph p = addParagraph(paragraph);
        if (m_paragraphs == null) {
            m_paragraphs = new ArrayList<Paragraph>();
        }
        m_paragraphs.add(p);
    }
    
    

    
    
    
    
    
    
    private Paragraph addParagraph(final String paragraph) {
        List<String> strSentences = 
            DefaultTokenization.detectSentences(paragraph);
        List<Sentence> sentences = new ArrayList<Sentence>(); 
        for (String s : strSentences) {
            sentences.add(addSentence(s));
        }
        return new Paragraph(sentences);
    }
    
    private Sentence addSentence(final String sentence) {
        List<String> tokens = DefaultTokenization.tokenizeSentence(sentence);
        return addSentence(tokens);
    }
    
    private Sentence addSentence(final List<String> words) {
        List<Term> terms = new ArrayList<Term>();
        for (String s : words) {
            if (!m_words.containsKey(s)) {
                Word w = new Word(s);
                m_words.put(s, w);
            }
            Word word = m_words.get(s);
            
            List<Word> termWords = new ArrayList<Word>();
            termWords.add(word);
            Term t = new Term(termWords);
            terms.add(t);
        }
        return new Sentence(terms, new Word(words.get(words.size() - 1)));
    }

    
    
    
    /**
     * Creates the word cache by running through all words of the document and 
     * storing them as keys into the cache <code>Hashtable</code>, which is 
     * returned. As values a list of term is build, containing all the terms 
     * which consist of the current word. 
     * 
     * @param doc The document containing the text to build the cache out of.
     * @return The word cache as <code>Hashtable</code> with words as keys
     * and a lists of terms, containing the words as values.
     */
    public static Hashtable<Word, List<Term>> buildWordCache(
            final Document doc) {
        List<Section> sections = doc.getSections();
        Hashtable<Word, List<Term>> cache = new Hashtable<Word, List<Term>>();
        
        // Through all sections
        for (Section s : sections) {
            
            // Through all paragraphs
            List<Paragraph> paragraphs = s.getParagraphs();
            for (Paragraph p : paragraphs) {
                
                // Through all sentences
                List<Sentence> sentences = p.getSentences();
                for (Sentence sn : sentences) {
                    
                    // Through all terms
                    List<Term> terms = sn.getTerms();
                    for (Term t : terms) {
                        
                        // Through all words
                        List<Word> words = t.getWords();
                        for (Word w : words) {
                            
                            // word exists in cache add term to trm list
                            if (cache.containsKey(w)) {
                                cache.get(w).add(t);
                            } else {
                                List<Term> list = new ArrayList<Term>();
                                list.add(t);
                                cache.put(w, list);
                            }
                        }
                    }
                }
            }
        }
        return cache;
    }    
}

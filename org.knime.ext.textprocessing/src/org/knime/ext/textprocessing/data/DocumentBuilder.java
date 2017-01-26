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
 * History
 *   14.02.2008 (Kilian Thiel): created
 */
package org.knime.ext.textprocessing.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.ext.textprocessing.nodes.tokenization.DefaultTokenization;
import org.knime.ext.textprocessing.nodes.tokenization.Tokenizer;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;

/**
 * A utility class which helps building up a
 * {@link org.knime.ext.textprocessing.data.Document} by providing methods which
 * allow to add sections, paragraphs and sentences in an easy way, create the a
 * documents word cache and much more.<br/>
 * <br/>
 * Example for building up a {@link org.knime.ext.textprocessing.data.Document}:
 * <br/>
 * <code>
 * DocumentBuilder db = new DocumentBuilder();<br/>
 * db.addAuthor(new Author("John", "Public"));<br/>
 * // add some more details like
 * {@link org.knime.ext.textprocessing.data.DocumentType},
 * {@link org.knime.ext.textprocessing.data.DocumentSource}, etc. ... <br/>
 * // add a title
 * db.addTitle("A simple title");<br/>
 * // add some sentences <br/>
 * db.addSentence("This is a simple sentence.");<br/>
 * db.addSentence("This is another simple sentence.");<br/>
 * // group sentences to paragraph<br/>
 * db.createNewParagraph();<br/>
 * // group paragraph to section<br/>
 * db.createNewSection(SectionAnnotation.CHAPTER);<br/>
 * // finally create document<br/>
 * Document d = db.createDocument(); <br/>
 * </code>
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class DocumentBuilder {
    private List<Term> m_terms = new ArrayList<Term>();

    private List<Sentence> m_sentences = new ArrayList<Sentence>();

    private List<Paragraph> m_paragraphs = new ArrayList<Paragraph>();

    private List<Section> m_sections = new ArrayList<Section>();

    private PublicationDate m_date = new PublicationDate();

    private File m_docFile = null;

    private DocumentType m_type = DocumentType.UNKNOWN;

    private final Set<Author> m_authors = new LinkedHashSet<Author>();

    private final Set<DocumentSource> m_sources = new LinkedHashSet<DocumentSource>();

    private final Set<DocumentCategory> m_categories = new LinkedHashSet<DocumentCategory>();

    private final HashMap<String, String> m_metaInfo = new LinkedHashMap<String, String>();

    private final Tokenizer m_sentenceTokenizer;

    private final Tokenizer m_wordTokenizer;

    /**
     * Creates new empty instance of {@code DocumentBuilder}.
     * @deprecated Use {@link #DocumentBuilder(String)} instead for tokenizer selection support.
     */
    @Deprecated
    public DocumentBuilder() {
        // initialize the tokenizer with the old standard tokenizer for backwards compatibility
        this(TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates new instance of {@code DocumentBuilder} with specified word tokenizer.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public DocumentBuilder(final String tokenizerName) {
        m_wordTokenizer = DefaultTokenization.getWordTokenizer(tokenizerName);
        m_sentenceTokenizer = DefaultTokenization.getSentenceTokenizer(tokenizerName);
    }

    /**
     * Creates new instance of {@code DocumentBuilder} and sets the meta
     * information of the given {@code Document}.<br/>
     * The meta information to copy is: the documents authors, the source, the
     * category, the type, the file and the publication date.<br/>
     * The text data like title or sections are not copied.
     * Only use this constructor if you want to create a document based on another document.
     * If you want to add new content based on Strings (e.g. with {@link #addSentence(String sentence)})
     * use {@link #DocumentBuilder(Document doc, String tokenizerName)}
     * @param doc The document containing the meta information to copy.
     */
    public DocumentBuilder(final Document doc) {
        // initialize the tokenizer with the tokenizer from preference page
        this(doc, TextprocessingPreferenceInitializer.tokenizerName());
    }

    /**
     * Creates new instance of {@code DocumentBuilder} and sets the meta
     * information of the given {@code Document}.<br/>
     * The meta information to copy is: the documents authors, the source, the
     * category, the type, the file and the publication date.<br/>
     * The text data like title or sections are not copied.
     * Use this constructor if you are willing to add new information based on strings
     * (e.g. with {@link #addSentence(String sentence)}).
     * @param doc The document containing the meta information to copy.
     * @param tokenizerName The tokenizer used for word tokenization.
     * @since 3.3
     */
    public DocumentBuilder(final Document doc, final String tokenizerName) {
        this(tokenizerName);
        // Add authors
        for (Author a : doc.getAuthors()) {
            addAuthor(a);
        }

        // Add source
        for (DocumentSource s : doc.getSources()) {
            addDocumentSource(s);
        }

        // Add categories
        for (DocumentCategory c : doc.getCategories()) {
            addDocumentCategory(c);
        }

        // Add type
        setDocumentType(doc.getType());

        // Add file
        setDocumentFile(doc.getDocFile());

        // Add publication date
        setPublicationDate(doc.getPubDate());

        // Add meta info
        addMetaInformation(doc.getMetaInformation());
    }

    /**
     * Builds a new {@link org.knime.ext.textprocessing.data.Document} instance containing the given data, such as
     * authors, sections, etc.
     * @param sections the section to set
     * @param type the type to set
     * @param authors the authors to set
     * @param sources the sources to set
     * @param categories the categories to set
     * @param date the publication date to set
     * @param docFile the file of the document to set
     * @param metaInfo the document meta info to set.
     * @return A new {@link org.knime.ext.textprocessing.data.Document} instance with given data.
     * @since 3.0
     */
    public static final Document createDocument(final List<Section> sections, final DocumentType type,
        final Set<Author> authors, final Set<DocumentSource> sources, final Set<DocumentCategory> categories,
        final PublicationDate date, final File docFile, final DocumentMetaInfo metaInfo) {
        return new Document(sections, type, authors, sources, categories, date, docFile, metaInfo);
    }

    /**
     * Builds a new {@link org.knime.ext.textprocessing.data.Document} instance
     * with the specified data, like authors, sections, etc.
     *
     * @return a new {@link org.knime.ext.textprocessing.data.Document} instance
     *         with the specified data.
     */
    public Document createDocument() {
        return new Document(m_sections, m_type, m_authors, m_sources,
                m_categories, m_date, m_docFile,
                new DocumentMetaInfo(m_metaInfo));
    }

    /**
     * Adds the key value pair to the meta information of the document.
     * @param key The key of the meta information.
     * @param value The value of the meta information.
     * @since 2.8
     */
    public void addMetaInformation(final String key, final String value) {
        if (key != null && value != null) {
            m_metaInfo.put(key, value);
        }
    }

    /**
     * Adds all the key value pairs of the given {@link DocumentMetaInfo} to the
     * meta info section of the current document to build.
     * @param metaInfo The {@link DocumentMetaInfo} containing the key value
     * pairs to add.
     * @since 2.8
     */
    public void addMetaInformation(final DocumentMetaInfo metaInfo) {
        if (metaInfo != null && metaInfo.getClass() != null
                && metaInfo.getMetaInfoKeys().size() > 0) {
            for (String key : metaInfo.getMetaInfoKeys()) {
                addMetaInformation(key, metaInfo.getMetaInfoValue(key));
            }
        }
    }

    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.Author} to the
     * list of authors.
     *
     * @param author the author to add to the authors list.
     */
    public void addAuthor(final Author author) {
        if (author != null) {
            m_authors.add(author);
        }
    }

    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.DocumentSource}
     * to the list of sources.
     *
     * @param source the source to add to the sources list.
     */
    public void addDocumentSource(final DocumentSource source) {
        if (source != null) {
            m_sources.add(source);
        }
    }

    /**
     * Adds the given {@link org.knime.ext.textprocessing.data.DocumentCategory}
     * to the list of categories.
     *
     * @param category the category to add to the categories list.
     */
    public void addDocumentCategory(final DocumentCategory category) {
        if (category != null) {
            m_categories.add(category);
        }
    }

    /**
     * @param date The date to set as
     *            {@link org.knime.ext.textprocessing.data.PublicationDate}.
     */
    public void setPublicationDate(final PublicationDate date) {
        if (date != null) {
            m_date = date;
        }
    }

    /**
     * @param file The file containing the document.
     */
    public void setDocumentFile(final File file) {
        if (file != null) {
            m_docFile = file;
        }
    }

    /**
     * @param type The type to set as
     *            {@link org.knime.ext.textprocessing.data.DocumentType}.
     */
    public void setDocumentType(final DocumentType type) {
        if (type != null) {
            m_type = type;
        }
    }

    /**
     * Tokenizes the given title and add it as a
     * {@link org.knime.ext.textprocessing.data.Section} with <code>TITLE</code>
     * annotation to the list of sections.
     *
     * @param title The title to tokenize and to add as section.
     */
    public void addTitle(final String title) {
        if (title != null && title.length() > 0) {
            Sentence s = internalAddSentence(title);
            List<Sentence> sentences = new ArrayList<Sentence>();
            sentences.add(s);
            Paragraph p = new Paragraph(sentences);
            List<Paragraph> paragraphs = new ArrayList<Paragraph>();
            paragraphs.add(p);
            Section section = new Section(paragraphs, SectionAnnotation.TITLE);
            m_sections.add(section);
        }
    }

    /**
     * Creates a new {@link org.knime.ext.textprocessing.data.Sentence} out of
     * the current list of {@link org.knime.ext.textprocessing.data.Term}s and
     * adds it to the current list of sentences. After adding the current list
     * of term to the new sentences a new empty term list is created.
     */
    public void createNewSentence() {
        if (m_terms != null && m_terms.size() > 0) {
            Sentence s = new Sentence(m_terms);
            m_sentences.add(s);
        }
        m_terms = new ArrayList<Term>();
    }

    /**
     * Creates a new {@link org.knime.ext.textprocessing.data.Paragraph} out of
     * the current list of {@link org.knime.ext.textprocessing.data.Sentence}s
     * and adds it to the current list of paragraphs. After adding the current
     * list of sentences to the new paragraph a new empty sentence list is
     * created.
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
     * and ads it to the current list of sections. After adding the current list
     * of paragraphs to the new section a new empty list is created. The given
     * {@link org.knime.ext.textprocessing.data.SectionAnnotation} is added to
     * the section and specifies its role.
     *
     * @param annotation The
     *            {@link org.knime.ext.textprocessing.data.SectionAnnotation} to
     *            add to the section.
     */
    public void createNewSection(final SectionAnnotation annotation) {
        if (m_paragraphs != null && m_paragraphs.size() > 0) {
            Section s = new Section(m_paragraphs, annotation);
            m_sections.add(s);
        }
        m_paragraphs = new ArrayList<Paragraph>();
    }

    /**
     * Adds the given term to the list of terms.
     *
     * @param term The term to add to add to the current list of terms.
     */
    public void addTerm(final Term term) {
        if (term != null && m_terms != null) {
            m_terms.add(term);
        } else if (m_terms == null) {
            m_terms = new ArrayList<Term>();
            m_terms.add(term);
        }
    }

    /**
     * Tokenizes the given sentence and adds it as
     * {@link org.knime.ext.textprocessing.data.Sentence} to the current list of
     * sentences.
     *
     * @param sentence The sentence to tokenize and to add to the current list
     *            of sentences.
     */
    public void addSentence(final String sentence) {
        Sentence s = internalAddSentence(sentence);
        if (m_sentences == null) {
            m_sentences = new ArrayList<Sentence>();
        }
        if (s != null) {
            m_sentences.add(s);
        }
    }

    /**
     * Adds the given sentence to the list of sentences.
     *
     * @param sentence The sentence to add to add to the current list of
     *            sentences.
     */
    public void addSentence(final Sentence sentence) {
        if (sentence != null && m_sentences != null) {
            m_sentences.add(sentence);
        }
    }

    /**
     * Tokenizes the given paragraph and adds it as a
     * {@link org.knime.ext.textprocessing.data.Paragraph} to the current list
     * of paragraphs.
     *
     * @param paragraph The paragraph to tokenize and to add to the current list
     *            of paragraphs.
     */
    public void addParagraph(final String paragraph) {
        Paragraph p = internalAddParagraph(paragraph);
        if (m_paragraphs == null) {
            m_paragraphs = new ArrayList<Paragraph>();
        }
        if (p != null) {
            m_paragraphs.add(p);
        }
    }

    /**
     * Add the given paragraph to the current list of paragraphs.
     *
     * @param paragraph The paragraph to add to the current list of paragraphs.
     */
    public void addParagraph(final Paragraph paragraph) {
        if (paragraph != null && m_paragraphs != null) {
            m_paragraphs.add(paragraph);
        }
    }

    /**
     * Tokenizes the given section and adds it as a instance of
     * {@link org.knime.ext.textprocessing.data.Section} to the current list of
     * sections.
     *
     * @param section The section to tokenize and to add to the current section
     *            list.
     * @param annotation The annotation of the section to create.
     */
    public void addSection(final String section,
            final SectionAnnotation annotation) {
        Section s = internalAddSection(section, annotation);
        if (m_sections == null) {
            m_sections = new ArrayList<Section>();
        }
        if (s != null) {
            m_sections.add(s);
        }
    }

    /**
     * Adds the given section to the current list of sections.
     *
     * @param section The section to add to the current section list.
     */
    public void addSection(final Section section) {
        if (section != null && m_sections != null) {
            m_sections.add(section);
        }
    }

    /**
     * Sets the given sections as sections to add to the document.
     * @param sections The sections to add to the document.
     * @since 2.8
     */
    public void setSections(final List<Section> sections) {
        m_sections = sections;
    }

    /**
     * Tokenizes the given string and creates a section out of it. The given
     * annotation represents the sections annotation.
     *
     * @param section The string representing the text of the section.
     * @param annotation The annotation representing the role of the section.
     * @return The created instance of
     *         {@link org.knime.ext.textprocessing.data.Section}.
     */
    private Section internalAddSection(final String section,
            final SectionAnnotation annotation) {
        if (section != null && section.length() > 0 && !section.equals("")) {
            List<Sentence> sentences = createSentenceList(section);
            Paragraph p = new Paragraph(sentences);
            List<Paragraph> paragraphs = new ArrayList<Paragraph>();
            paragraphs.add(p);
            return new Section(paragraphs, annotation);
        }
        return null;
    }

    /**
     * Tokenizes the given string and creates a paragraph out of it.
     *
     * @param paragraph The string representing the text of the paragraph.
     * @return The created instance of
     *         {@link org.knime.ext.textprocessing.data.Paragraph}.
     */
    private Paragraph internalAddParagraph(final String paragraph) {
        if (paragraph != null && paragraph.length() > 0) {
            List<Sentence> sentences = createSentenceList(paragraph);
            return new Paragraph(sentences);
        }
        return null;
    }

    /**
     * Creates and returns list of sentences from given text.
     * @param text The text to create a list of sentences from
     * @return A list of sentences.
     */
    private List<Sentence> createSentenceList(final String text) {
        if (text != null && !text.isEmpty()) {
            List<String> strSentences = m_sentenceTokenizer.tokenize(text);
            List<Sentence> sentences = new ArrayList<Sentence>();

            String cpyText = text;
            for (int i = 0; i < strSentences.size(); i++) {
                final String sentence = strSentences.get(i);
                final String whiteSpaceSuffix;
                String nextSentence = null;
                if (i < strSentences.size() - 1) {
                    nextSentence = strSentences.get(i + 1);
                }

                //
                // extract whitespace suffix characters
                //

                int tokenStart = cpyText.indexOf(sentence);
                cpyText = cpyText.substring(tokenStart + sentence.length());

                if (nextSentence != null) {
                    int nextTokenStart = cpyText.indexOf(nextSentence);
                    whiteSpaceSuffix = cpyText.substring(0, nextTokenStart);
                    cpyText = cpyText.substring(nextTokenStart);
                } else {
                    if (cpyText.length() > 0) {
                        whiteSpaceSuffix = cpyText;
                    } else {
                        whiteSpaceSuffix = "";
                    }
                }

                sentences.add(internalAddSentence(sentence + whiteSpaceSuffix));
            }

            return sentences;
        }
        return null;
    }

    /**
     * Tokenizes the given string and creates a sentence out of it.
     *
     * @param sentence The string representing the text of the sentence.
     * @return The created instance of
     *         {@link org.knime.ext.textprocessing.data.Sentence}.
     */
    private Sentence internalAddSentence(final String sentence) {
        if (sentence != null) {
            // get tokens
            List<String> tokens = m_wordTokenizer.tokenize(sentence);

            if (tokens != null) {
                List<Term> terms = new ArrayList<Term>(tokens.size());
                String cpySentence = sentence;

                for (int i = 0; i < tokens.size(); i++) {
                    final String token = tokens.get(i);
                    final String whiteSpaceSuffix;
                    String nextToken = null;
                    if (i < tokens.size() - 1) {
                        nextToken = tokens.get(i + 1);
                    }

                    //
                    // extract whitespace suffix characters
                    //

                    int tokenStart = cpySentence.indexOf(token);
                    cpySentence = cpySentence.substring(tokenStart + token.length());

                    if (nextToken != null) {
                        int nextTokenStart = cpySentence.indexOf(nextToken);
                        whiteSpaceSuffix = cpySentence.substring(0, nextTokenStart);
                        cpySentence = cpySentence.substring(nextTokenStart);
                    } else {
                        if (cpySentence.length() > 0) {
                            whiteSpaceSuffix = cpySentence;
                        } else {
                            whiteSpaceSuffix = "";
                        }
                    }

                    // create word with token and whitespace suffix characters
                    Word w = new Word(token, whiteSpaceSuffix);
                    List<Word> termWords = new ArrayList<Word>(1);
                    termWords.add(w);
                    Term t = new Term(termWords, new ArrayList<Tag>(1), false);
                    terms.add(t);
                }
                return new Sentence(terms);
            }
        }
        return null;
    }

    /**
     * @return an unmodifiable list of all current sections.
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(m_sections);
    }
}

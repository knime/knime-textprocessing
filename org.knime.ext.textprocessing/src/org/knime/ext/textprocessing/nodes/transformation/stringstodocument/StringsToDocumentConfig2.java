/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General  License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General  License for more details.
 *
 *  You should have received a copy of the GNU General  License
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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import java.time.LocalDate;

import org.knime.ext.textprocessing.data.DocumentType;

/**
 * Holds all necessary variables needed to build a document out of a data row, like certain indices for title, author or
 * full text column, Publication date, document source, category, and type.
 *
 * @author Hermann Azong & Julian Bunzel, KNIME.com, Berlin, Germany
 * @since 3.5
 */
class StringsToDocumentConfig2 {

    /**
     * The default publication date.
     */
    static final LocalDate DEF_DOCUMENT_PUBDATE = LocalDate.now();

    /**
     * The default document title.
     */
    static final String DEF_DOCUMENT_TITLE = "";

    /**
     * The default document source.
     */
    static final String DEF_DOCUMENT_SOURCE = "";

    /**
     * The default document category.
     */
    static final String DEF_DOCUMENT_CATEGORY = "";

    /**
     * The default document type.
     */
    static final String DEF_DOCUMENT_TYPE = DocumentType.UNKNOWN.toString();

    /**
     * The default split value for author strings.
     */
    static final String DEF_AUTHORS_SPLITCHAR = ", ";

    /**
     * The default author names.
     */
    static final String DEF_AUTHOR_NAMES = "-";

    /**
     * The default "use category column" setting.
     */
    static final boolean DEF_USE_CATCOLUMN = false;

    /**
     * The default "use source column" setting.
     */
    static final boolean DEF_USE_SOURCECOLUMN = false;

    /**
     * The default "use publication date column" setting.
     */
    static final boolean DEF_USE_PUBDATECOLUMN = false;

    /**
     * The default "use title column" setting.
     */
    static final boolean DEF_USE_TITLECOLUMN = true;

    /**
     * The default "use authors column" setting.
     */
    static final boolean DEF_USE_AUTHORSCOLUMN = true;

    /**
     * The default column name for the new document column.
     */
    static final String DEF_DOC_COLUMN = "Document";

    /**
     * The minimum number of threads.
     */
    static final int MIN_THREADS = 1;

    private int m_titleColumnIndex;

    private int m_fulltextColumnIndex;

    private int m_authorColumnIndex;

    private int m_categoryColumnIndex;

    private int m_sourceColumnIndex;

    private int m_pubDateColumnIndex;

    private boolean m_useTitleColumn;

    private boolean m_useAuthorsColumn;

    private boolean m_useCatColumn;

    private boolean m_useSourceColumn;

    private boolean m_usePubDateColumn;

    private String m_authorFirstName;

    private String m_authorLastName;

    private String m_authorsSplitChar;

    private String m_docSource;

    private String m_docCat;

    private String m_docType;

    private LocalDate m_publicationDate;

    // getter and setter for document title members
    boolean getUseTitleColumn() {
        return m_useTitleColumn;
    }

    void setUseTitleColumn(final boolean useTitleColumn) {
        this.m_useTitleColumn = useTitleColumn;
    }

    int getTitleColumnIndex() {
        return m_titleColumnIndex;
    }

    void setTitleColumnIndex(final int titleColumnIndex) {
        m_titleColumnIndex = titleColumnIndex;
    }

    // getter and setter for author members
    String getAuthorFirstName() {
        return m_authorFirstName;
    }

    void setAuthorFirstName(final String authorFirstName) {
        this.m_authorFirstName = authorFirstName;
    }

    String getAuthorLastName() {
        return m_authorLastName;
    }

    void setAuthorLastName(final String authorLastName) {
        this.m_authorLastName = authorLastName;
    }

    boolean getUseAuthorsColumn() {
        return m_useAuthorsColumn;
    }

    void setUseAuthorsColumn(final boolean useAuthorsColumn) {
        this.m_useAuthorsColumn = useAuthorsColumn;
    }

    int getAuthorsColumnIndex() {
        return m_authorColumnIndex;
    }

    void setAuthorsColumnIndex(final int authorsColumnIndex) {
        m_authorColumnIndex = authorsColumnIndex;
    }

    String getAuthorsSplitChar() {
        return m_authorsSplitChar;
    }

    void setAuthorsSplitChar(final String authorsSplitChar) {
        m_authorsSplitChar = authorsSplitChar;
    }

    // getter and setter for fulltext members
    int getFulltextColumnIndex() {
        return m_fulltextColumnIndex;
    }

    void setFulltextColumnIndex(final int fulltextColumnIndex) {
        m_fulltextColumnIndex = fulltextColumnIndex;
    }

    // getter and setter for document category members
    void setCategoryColumnIndex(final int categoryColumnIndex) {
        m_categoryColumnIndex = categoryColumnIndex;
    }

    int getCategoryColumnIndex() {
        return m_categoryColumnIndex;
    }

    void setUseCatColumn(final boolean useCatColumn) {
        m_useCatColumn = useCatColumn;
    }

    boolean getUseCatColumn() {
        return m_useCatColumn;
    }

    String getDocCat() {
        return m_docCat;
    }

    void setDocCat(final String docCat) {
        m_docCat = docCat;
    }

    // getter and setter for document source members
    void setUseSourceColumn(final boolean useSourceColumn) {
        m_useSourceColumn = useSourceColumn;
    }

    boolean getUseSourceColumn() {
        return m_useSourceColumn;
    }

    void setSourceColumnIndex(final int sourceColumnIndex) {
        m_sourceColumnIndex = sourceColumnIndex;
    }

    int getSourceColumnIndex() {
        return m_sourceColumnIndex;
    }

    String getDocSource() {
        return m_docSource;
    }

    void setDocSource(final String docSource) {
        m_docSource = docSource;
    }

    // getter and setter for document type members
    String getDocType() {
        return m_docType;
    }

    void setDocType(final String docType) {
        m_docType = docType;
    }

    // getter and setter pubdate members
    boolean getUsePubDateColumn() {
        return m_usePubDateColumn;
    }

    void setUsePubDateColumn(final boolean usePubDateColumn) {
        this.m_usePubDateColumn = usePubDateColumn;
    }

    LocalDate getPublicationDate() {
        return m_publicationDate;
    }

    void setPublicationDate(final LocalDate publicationDate) {
        m_publicationDate = publicationDate;
    }

    int getPubDateColumnIndex() {
        return m_pubDateColumnIndex;
    }

    void setPubDateColumnIndex(final int pubDateColumnIndex) {
        m_pubDateColumnIndex = pubDateColumnIndex;
    }
}

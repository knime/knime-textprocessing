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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;

/**
 * Holds all necessary variables needed to build a document out of a data row, like certain indices for title, author or
 * full text column, publication date, document source, category, and type.
 *
 * @author Hermann Azong, KNIME.com, Berlin, Germany
 */
public class StringsToDocumentConfig {

    /**
     * The default document publication date.
     */
    static final String DEF_DOCUMENT_PUBDATE = PublicationDate.getToday();

    /**
     * The default document title
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
     * The default "use title column" setting.
     */
    static final boolean DEF_USE_TITLECOLUMN = false;

    /**
     * The default "use authors column" setting.
     */
    static final boolean DEF_USE_AUTHORSCOLUMN = false;

    private int m_titleStringIndex = -1;

    private int m_fulltextStringIndex = -1;

    private int m_authorsStringIndex = -1;

    private int m_categoryStringIndex = -1;

    private int m_sourceStringIndex = -1;

    private boolean m_useTitleColumn = DEF_USE_TITLECOLUMN;

    private boolean m_useAuthorsColumn = DEF_USE_AUTHORSCOLUMN;

    private boolean m_useCatColumn = DEF_USE_CATCOLUMN;

    private boolean m_useSourceColumn = DEF_USE_SOURCECOLUMN;

    private String m_docTitle = DEF_DOCUMENT_TITLE;

    private String m_authorNames = DEF_AUTHOR_NAMES;

    private String m_authorsSplitChar = DEF_AUTHORS_SPLITCHAR;

    private String m_docSource = DEF_DOCUMENT_SOURCE;

    private String m_docCat = DEF_DOCUMENT_CATEGORY;

    private String m_docType = DEF_DOCUMENT_TYPE;

    private String m_publicationDate = DEF_DOCUMENT_PUBDATE;

    /**
     * @return the titleStringIndex
     */
    public int getTitleStringIndex() {
        return m_titleStringIndex;
    }

    /**
     * @param titleStringIndex the titleStringIndex to set
     */
    public void setTitleStringIndex(final int titleStringIndex) {
        m_titleStringIndex = titleStringIndex;
    }

    /**
     * @return the authorsStringIndex
     */
    public int getAuthorsStringIndex() {
        return m_authorsStringIndex;
    }

    /**
     * @param authorsStringIndex the authorsStringIndex to set
     */
    public void setAuthorsStringIndex(final int authorsStringIndex) {
        m_authorsStringIndex = authorsStringIndex;
    }

    /**
     * @return the authorsSplitChar
     */
    public String getAuthorsSplitChar() {
        return m_authorsSplitChar;
    }

    /**
     * @param authorsSplitChar the authorsSplitChar to set
     */
    public void setAuthorsSplitChar(final String authorsSplitChar) {
        m_authorsSplitChar = authorsSplitChar;
    }

    /**
     * @return the fulltextStringIndex
     */
    public int getFulltextStringIndex() {
        return m_fulltextStringIndex;
    }

    /**
     * @param fulltextStringIndex the fulltextStringIndex to set
     */
    public void setFulltextStringIndex(final int fulltextStringIndex) {
        m_fulltextStringIndex = fulltextStringIndex;
    }

    /**
     * @return the docSource
     */
    public String getDocSource() {
        return m_docSource;
    }

    /**
     * @param docSource the docSource to set
     */
    public void setDocSource(final String docSource) {
        m_docSource = docSource;
    }

    /**
     * @return the docCat
     */
    public String getDocCat() {
        return m_docCat;
    }

    /**
     * @param docCat the docCat to set
     */
    public void setDocCat(final String docCat) {
        m_docCat = docCat;
    }

    /**
     * @return the docType
     */
    public String getDocType() {
        return m_docType;
    }

    /**
     * @param docType the docType to set
     */
    public void setDocType(final String docType) {
        m_docType = docType;
    }

    /**
     * @return the publicationDate
     */
    public String getPublicationDate() {
        return m_publicationDate;
    }

    /**
     * @param publicationDate the publicationDate to set
     */
    public void setPublicationDate(final String publicationDate) {
        m_publicationDate = publicationDate;
    }

    /**
     * @param categoryStringIndex the categoryStringIndex to set
     * @since 2.6
     */
    public void setCategoryStringIndex(final int categoryStringIndex) {
        m_categoryStringIndex = categoryStringIndex;
    }

    /**
     * @return the categoryStringIndex
     * @since 2.6
     */
    public int getCategoryStringIndex() {
        return m_categoryStringIndex;
    }

    /**
     * @param sourceStringIndex the sourceStringIndex to set
     * @since 2.6
     */
    public void setSourceStringIndex(final int sourceStringIndex) {
        m_sourceStringIndex = sourceStringIndex;
    }

    /**
     * @return the sourceStringIndex
     * @since 2.6
     */
    public int getSourceStringIndex() {
        return m_sourceStringIndex;
    }

    /**
     * @param useCatColumn the useCatColumn to set
     * @since 2.6
     */
    public void setUseCatColumn(final boolean useCatColumn) {
        m_useCatColumn = useCatColumn;
    }

    /**
     * @return the useCatColumn
     * @since 2.6
     */
    public boolean getUseCatColumn() {
        return m_useCatColumn;
    }

    /**
     * @param useSourceColumn the useSourceColumn to set
     * @since 2.6
     */
    public void setUseSourceColumn(final boolean useSourceColumn) {
        m_useSourceColumn = useSourceColumn;
    }

    /**
     * @return the useSourceColumn
     * @since 2.6
     */
    public boolean getUseSourceColumn() {
        return m_useSourceColumn;
    }

    /**
     * @return the m_useTitleColumn
     * @since 3.3
     */
    public boolean getUseTitleColumn() {
        return m_useTitleColumn;
    }

    /**
     * @param useTitleColumn the m_useTitleColumn to set
     * @since 3.3
     */
    public void setUseTitleColumn(final boolean useTitleColumn) {
        this.m_useTitleColumn = useTitleColumn;
    }

    /**
     * @return the m_useAuthorsColumn
     * @since 3.3
     */
    public boolean getUseAuthorsColumn() {
        return m_useAuthorsColumn;
    }

    /**
     * @param useAuthorsColumn the m_useAuthorsColumn to set
     * @since 3.3
     */
    public void setUseAuthorsColumn(final boolean useAuthorsColumn) {
        this.m_useAuthorsColumn = useAuthorsColumn;
    }

    /**
     * @return the m_docTitle
     * @since 3.3
     */
    public String getDocTitle() {
        return m_docTitle;
    }

    /**
     * @param docTitle the m_docTitle to set
     * @since 3.3
     */
    public void setDocTitle(final String docTitle) {
        this.m_docTitle = docTitle;
    }

    /**
     * @return the m_authorNames
     * @since 3.3
     */
    public String getAuthorNames() {
        return m_authorNames;
    }

    /**
     * @param authorNames the m_authorNames to set
     * @since 3.3
     */
    public void setAuthorNames(final String authorNames) {
        this.m_authorNames = authorNames;
    }
}

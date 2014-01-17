/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by 
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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;

/**
 * Holds all necessary variables needed to build a document out of a data row,
 * like certain indices for title, author or full text column, publication
 * date, document source, category, and type.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public class StringsToDocumentConfig {

    /**
     * The default document publication date.
     */
    static final String DEF_DOCUMENT_PUBDATE = PublicationDate.getToday();

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

    private int m_titleStringIndex = -1;

    private int m_fulltextStringIndex = -1;

    private int m_authorsStringIndex = -1;

    private int m_categoryStringIndex = -1;

    private int m_sourceStringIndex = -1;

    private boolean m_useCatColumn = DEF_USE_CATCOLUMN;

    private boolean m_useSourceColumn = DEF_USE_SOURCECOLUMN;

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
}

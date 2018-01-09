/*
 * ------------------------------------------------------------------------
 *
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
 *   17.01.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentdataassigner;

import java.time.LocalDate;

import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.nodes.transformation.documentdataassigner.DocumentDataAssignerNodeDialog.ReplaceOrAppend;

/**
 * The {@code DocumentDataAssignerConfig} contains the default values for the setting models and is used to pass
 * information from the NodeModel to the {@code DocumentDataAssignerCellFactory}.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
class DocumentDataAssignerConfig {
    // default values for setting models.

    /** The default publication date. */
    static final LocalDate DEF_DOCUMENT_PUBDATE = LocalDate.now();

    /** The default string for the document source. */
    static final String DEF_DOCUMENT_SOURCE = "";

    /** The default string for the document category. */
    static final String DEF_DOCUMENT_CATEGORY = "";

    /** The default string for the document type. */
    static final String DEF_DOCUMENT_TYPE = DocumentType.UNKNOWN.toString();

    /** The default string splitting author names. */
    static final String DEF_AUTHORSSPLIT_STR = ", ";

    /** The default value for using an incoming column as authors column. */
    static final boolean DEF_USE_AUTHORSCOLUMN = false;

    /** The default value for using an incoming column as category column. */
    static final boolean DEF_USE_CATCOLUMN = false;

    /** The default value for using an incoming column as source column. */
    static final boolean DEF_USE_SOURCECOLUMN = false;

    /** The default value for using an incoming column as pubdate column. */
    static final boolean DEF_USE_PUBDATECOLUMN = false;

    /** The default name of the column that replaces the input column. */
    static final String DEF_REPLACE_OR_APPEND_COL = ReplaceOrAppend.getDefault().name();

    /** The default name of the appended document column. */
    static final String DEF_APPEND_COLNAME = "Document (appended)";


    /* Following information is used for DocumentDataAssingerCellFactory.
     * The values are initialized here and can be set in the DocumentDataAssignerNodeModel while using the set-Methods.
     * Then the created config will be forwarded to the cell factory.
     */

    /** The publication date used for creating a new document. */
    private LocalDate m_docPubDate = DEF_DOCUMENT_PUBDATE;

    /** The document source used for creating a new document. */
    private String m_docSource = DEF_DOCUMENT_SOURCE;

    /** The document category used for creating a new document. */
    private String m_docCategory = DEF_DOCUMENT_CATEGORY;

    /** The document type used for creating a new document. */
    private String m_docType = DEF_DOCUMENT_TYPE;

    /** The index of the incoming document column. */
    private int m_documentColumnIndex = -1;

    /** The index of the incoming authors column. */
    private int m_authorsColumnIndex = -1;

    /** The index of the incoming category column. */
    private int m_categoryColumnIndex = -1;

    /** The index of the incoming source column. */
    private int m_sourceColumnIndex = -1;

    /** The index of the incoming pubdate column. */
    private int m_pubDateColumnIndex = -1;

    /** The string splitting author names while processing the authors column. */
    private String m_authorsSplitStr = DEF_AUTHORSSPLIT_STR;


    /**
     * @return The document column index.
     */
    int getDocumentColumnIndex() {
        return m_documentColumnIndex;
    }

    /**
     * @param documentColumnIndex Sets the document column index.
     */
    void setDocumentColumnIndex(final int documentColumnIndex) {
        this.m_documentColumnIndex = documentColumnIndex;
    }

    /**
     * @return The authors column index.
     */
    int getAuthorsColumnIndex() {
        return m_authorsColumnIndex;
    }

    /**
     * @param authorsColumnIndex Sets the authors column index.
     */
    void setAuthorsColumnIndex(final int authorsColumnIndex) {
        this.m_authorsColumnIndex = authorsColumnIndex;
    }

    /**
     * @return The category column index.
     */
    int getCategoryColumnIndex() {
        return m_categoryColumnIndex;
    }

    /**
     * @param categoryColumnIndex Sets the category column index.
     */
    void setCategoryColumnIndex(final int categoryColumnIndex) {
        this.m_categoryColumnIndex = categoryColumnIndex;
    }

    /**
     * @return The source column index.
     */
    int getSourceColumnIndex() {
        return m_sourceColumnIndex;
    }

    /**
     * @param sourceColumnIndex Sets The source column index.
     */
    void setSourceColumnIndex(final int sourceColumnIndex) {
        this.m_sourceColumnIndex = sourceColumnIndex;
    }

    /**
     * @return The publication date column index.
     */
    int getPubDateColumnIndex() {
        return m_pubDateColumnIndex;
    }

    /**
     * @param pubDateColumnIndex The publication date column index.
     */
    void setPubDateColumnIndex(final int pubDateColumnIndex) {
        this.m_pubDateColumnIndex = pubDateColumnIndex;
    }

    /**
     * @return The publication date.
     */
    LocalDate getDocPubDate() {
        return m_docPubDate;
    }

    /**
     * @param docPubDate Sets the publication date.
     */
    void setDocPubDate(final LocalDate docPubDate) {
        this.m_docPubDate = docPubDate;
    }

    /**
     * @return The document source.
     */
    String getDocSource() {
        return m_docSource;
    }

    /**
     * @param docSource Sets the document source.
     */
    void setDocSource(final String docSource) {
        this.m_docSource = docSource;
    }

    /**
     * @return The document category.
     */
    String getDocCategory() {
        return m_docCategory;
    }

    /**
     * @param docCategory Sets the document category.
     */
    void setDocCategory(final String docCategory) {
        this.m_docCategory = docCategory;
    }

    /**
     * @return The doc type.
     */
    String getDocType() {
        return m_docType;
    }

    /**
     * @param docType Sets the document type.
     */
    void setDocType(final String docType) {
        this.m_docType = docType;
    }

    /**
     * @return The delimiter for splitting author names.
     */
    String getAuthorsSplitStr() {
        return m_authorsSplitStr;
    }

    /**
     * @param authorsSplitStr Sets the delimiter for splitting author names.
     */
    void setAuthorsSplitStr(final String authorsSplitStr) {
        this.m_authorsSplitStr = authorsSplitStr;
    }

}

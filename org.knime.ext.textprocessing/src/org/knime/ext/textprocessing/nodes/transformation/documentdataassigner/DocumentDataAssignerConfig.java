/*
 * ------------------------------------------------------------------------
 *
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
 *   17.01.2017 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.transformation.documentdataassigner;

import org.knime.core.node.KNIMEConstants;
import org.knime.ext.textprocessing.data.DocumentType;
import org.knime.ext.textprocessing.data.PublicationDate;

/**
 * The {@code DocumentDataAssignerConfig} contains the default values for the setting models and is used to pass
 * information from the NodeModel to the {@code DocumentDataAssignerCellFactory}.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class DocumentDataAssignerConfig {
    // default values for setting models.

    /** The default publication date. */
    static final String DEF_DOCUMENT_PUBDATE = PublicationDate.getToday();

    /** The default string for the document source. */
    static final String DEF_DOCUMENT_SOURCE = "";

    /** The default string for the document category. */
    static final String DEF_DOCUMENT_CATEGORY = "";

    /** The default string for the document type. */
    static final String DEF_DOCUMENT_TYPE = DocumentType.UNKNOWN.toString();

    /** The default string splitting author names. */
    static final String DEF_AUTHORSSPLIT_STR = ", ";

    /** The default authors first name. */
    static final String DEF_AUTHOR_FIRST_NAME = "-";

    /** The default authors last name. */
    static final String DEF_AUTHOR_LAST_NAME = "-";

    /** The default value for using an incoming column as authors column. */
    static final boolean DEF_USE_AUTHORSCOLUMN = false;

    /** The default value for using an incoming column as category column. */
    static final boolean DEF_USE_CATCOLUMN = false;

    /** The default value for using an incoming column as source column. */
    static final boolean DEF_USE_SOURCECOLUMN = false;

    /** The default value for using an incoming column as pubdate column. */
    static final boolean DEF_USE_PUBDATECOLUMN = false;

    /** The default number of threads. */
    static final int DEF_THREADS = Math.max(1, Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
        (int)Math.ceil(Runtime.getRuntime().availableProcessors())));

    /** The min number of threads. */
    static final int MIN_THREADS = 1;

    /** The max number of threads. */
    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    /** The default value for replacing the document column. */
    static final boolean DEF_REPLACE_DOCCOL = false;

    /* Following information is used for DocumentDataAssingerCellFactory.
     * The values are initialized here and can be set in the DocumentDataAssignerNodeModel while using the set-Methods.
     * Then the created config will be forwarded to the cell factory.
     */

    /** The publication date used for creating a new document. */
    private String m_docPubDate = DEF_DOCUMENT_PUBDATE;

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

    /** The authors first name used for the new document. */
    private String m_authorsFirstName = DEF_AUTHOR_FIRST_NAME;

    /** The authors last name used for the new document. */
    private String m_authorsLastName = DEF_AUTHOR_LAST_NAME;

    /** The number of threads used to create new document cells. */
    private int m_numberOfThreads = DEF_THREADS;

    /**
     * @return The document column index.
     */
    public int getDocumentColumnIndex() {
        return m_documentColumnIndex;
    }

    /**
     * @param documentColumnIndex Sets the document column index.
     */
    public void setDocumentColumnIndex(final int documentColumnIndex) {
        this.m_documentColumnIndex = documentColumnIndex;
    }

    /**
     * @return The authors column index.
     */
    public int getAuthorsColumnIndex() {
        return m_authorsColumnIndex;
    }

    /**
     * @param authorsColumnIndex Sets the authors column index.
     */
    public void setAuthorsColumnIndex(final int authorsColumnIndex) {
        this.m_authorsColumnIndex = authorsColumnIndex;
    }

    /**
     * @return The category column index.
     */
    public int getCategoryColumnIndex() {
        return m_categoryColumnIndex;
    }

    /**
     * @param categoryColumnIndex Sets the category column index.
     */
    public void setCategoryColumnIndex(final int categoryColumnIndex) {
        this.m_categoryColumnIndex = categoryColumnIndex;
    }

    /**
     * @return The source column index.
     */
    public int getSourceColumnIndex() {
        return m_sourceColumnIndex;
    }

    /**
     * @param sourceColumnIndex Sets The source column index.
     */
    public void setSourceColumnIndex(final int sourceColumnIndex) {
        this.m_sourceColumnIndex = sourceColumnIndex;
    }

    /**
     * @return The publication date column index.
     */
    public int getPubDateColumnIndex() {
        return m_pubDateColumnIndex;
    }

    /**
     * @param pubDateColumnIndex The publication date column index.
     */
    public void setPubDateColumnIndex(final int pubDateColumnIndex) {
        this.m_pubDateColumnIndex = pubDateColumnIndex;
    }

    /**
     * @return The publication date.
     */
    public String getDocPubDate() {
        return m_docPubDate;
    }

    /**
     * @param docPubDate Sets the publication date.
     */
    public void setDocPubDate(final String docPubDate) {
        this.m_docPubDate = docPubDate;
    }

    /**
     * @return The document source.
     */
    public String getDocSource() {
        return m_docSource;
    }

    /**
     * @param docSource Sets the document source.
     */
    public void setDocSource(final String docSource) {
        this.m_docSource = docSource;
    }

    /**
     * @return The document category.
     */
    public String getDocCategory() {
        return m_docCategory;
    }

    /**
     * @param docCategory Sets the document category.
     */
    public void setDocCategory(final String docCategory) {
        this.m_docCategory = docCategory;
    }

    /**
     * @return The doc type.
     */
    public String getDocType() {
        return m_docType;
    }

    /**
     * @param docType Sets the document type.
     */
    public void setDocType(final String docType) {
        this.m_docType = docType;
    }

    /**
     * @return The delimiter for splitting author names.
     */
    public String getAuthorsSplitStr() {
        return m_authorsSplitStr;
    }

    /**
     * @param authorsSplitStr Sets the delimiter for splitting author names.
     */
    public void setAuthorsSplitStr(final String authorsSplitStr) {
        this.m_authorsSplitStr = authorsSplitStr;
    }

    /**
     * @return The authors first name.
     */
    public String getAuthorsFirstName() {
        return m_authorsFirstName;
    }

    /**
     * @param authorsFirstName Sets the authors first name..
     */
    public void setAuthorsFirstName(final String authorsFirstName) {
        this.m_authorsFirstName = authorsFirstName;
    }

    /**
     * @return The authors last name.
     */
    public String getAuthorsLastName() {
        return m_authorsLastName;
    }

    /**
     * @param authorsLastName Sets the authors last name.
     */
    public void setAuthorsLastName(final String authorsLastName) {
        this.m_authorsLastName = authorsLastName;
    }

    /**
     * @return The number of threads.
     */
    public int getNumberOfThreads() {
        return m_numberOfThreads;
    }

    /**
     * @param numberOfThreads Sets the number of threads.
     */
    public void setNumberOfThreads(final int numberOfThreads) {
        this.m_numberOfThreads = numberOfThreads;
    }

}

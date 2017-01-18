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
 *
 * @author Julian Bunzel, KNIME.com Berlin
 */
public class DocumentDataAssignerConfig {

    static final String DEF_DOCUMENT_PUBDATE = PublicationDate.getToday();

    static final String DEF_DOCUMENT_SOURCE = "";

    static final String DEF_DOCUMENT_CATEGORY = "";

    static final String DEF_DOCUMENT_TYPE = DocumentType.UNKNOWN.toString();

    static final String DEF_AUTHORS_NAME = "-";

    static final String DEF_AUTHORSSPLIT_STR = ", ";

    static final String DEF_AUTHOR_FIRST_NAME = "-";

    static final String DEF_AUTHOR_LAST_NAME = "-";

    static final boolean DEF_USE_AUTHORSCOLUMN = false;

    static final boolean DEF_USE_CATCOLUMN = false;

    static final boolean DEF_USE_SOURCECOLUMN = false;

    static final boolean DEF_USE_PUBDATECOLUMN = false;

    /** The default number of threads. */
    static final int DEF_THREADS = Math.max(1, Math.min(KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads() / 4,
        (int)Math.ceil(Runtime.getRuntime().availableProcessors())));

    /** The min number of threads. */
    static final int MIN_THREADS = 1;

    /** The max number of threads. */
    static final int MAX_THREADS = KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads();

    static final boolean DEF_REPLACE_DOCCOL = false;

    private String m_docPubDate = DEF_DOCUMENT_PUBDATE;

    private String m_docSource = DEF_DOCUMENT_SOURCE;

    private String m_docCategory = DEF_DOCUMENT_CATEGORY;

    private String m_docType = DEF_DOCUMENT_TYPE;

    private int m_documentColumnIndex = -1;

    private int m_authorsColumnIndex = -1;

    private int m_categoryColumnIndex = -1;

    private int m_sourceColumnIndex = -1;

    private int m_pubDateColumnIndex = -1;

    private boolean m_useAuthorsColumn = DEF_USE_AUTHORSCOLUMN;

    private boolean m_useSourceColumn = DEF_USE_SOURCECOLUMN;

    private boolean m_usePubDateColumn = DEF_USE_PUBDATECOLUMN;

    private boolean m_useCategoryColumn = DEF_USE_CATCOLUMN;

    private String m_authorsSplitStr = DEF_AUTHORSSPLIT_STR;

    private String m_authorsFirstName = DEF_AUTHOR_FIRST_NAME;

    private String m_authorsLastName = DEF_AUTHOR_LAST_NAME;

    private int m_numberOfThreads = DEF_THREADS;

    /**
     * @return the m_documentColumnIndex
     */
    public int getDocumentColumnIndex() {
        return m_documentColumnIndex;
    }

    /**
     * @param documentColumnIndex the m_documentColumnIndex to set
     */
    public void setDocumentColumnIndex(final int documentColumnIndex) {
        this.m_documentColumnIndex = documentColumnIndex;
    }

    /**
     * @return the m_authorsColumnIndex
     */
    public int getAuthorsColumnIndex() {
        return m_authorsColumnIndex;
    }

    /**
     * @param authorsColumnIndex the m_authorsColumnIndex to set
     */
    public void setAuthorsColumnIndex(final int authorsColumnIndex) {
        this.m_authorsColumnIndex = authorsColumnIndex;
    }

    /**
     * @return the m_categoryColumnIndex
     */
    public int getCategoryColumnIndex() {
        return m_categoryColumnIndex;
    }

    /**
     * @param categoryColumnIndex the m_categoryColumnIndex to set
     */
    public void setCategoryColumnIndex(final int categoryColumnIndex) {
        this.m_categoryColumnIndex = categoryColumnIndex;
    }

    /**
     * @return the m_sourceColumnIndex
     */
    public int getSourceColumnIndex() {
        return m_sourceColumnIndex;
    }

    /**
     * @param sourceColumnIndex the m_sourceColumnIndex to set
     */
    public void setSourceColumnIndex(final int sourceColumnIndex) {
        this.m_sourceColumnIndex = sourceColumnIndex;
    }

    /**
     * @return the m_pubDateColumnIndex
     */
    public int getPubDateColumnIndex() {
        return m_pubDateColumnIndex;
    }

    /**
     * @param pubDateColumnIndex the m_pubDateColumnIndex to set
     */
    public void setPubDateColumnIndex(final int pubDateColumnIndex) {
        this.m_pubDateColumnIndex = pubDateColumnIndex;
    }

    /**
     * @return the m_docPubDate
     */
    public String getDocPubDate() {
        return m_docPubDate;
    }

    /**
     * @param docPubDate the m_docPubDate to set
     */
    public void setDocPubDate(final String docPubDate) {
        this.m_docPubDate = docPubDate;
    }

    /**
     * @return the m_docSource
     */
    public String getDocSource() {
        return m_docSource;
    }

    /**
     * @param docSource the m_docSource to set
     */
    public void setDocSource(final String docSource) {
        this.m_docSource = docSource;
    }

    /**
     * @return the m_docCategory
     */
    public String getDocCategory() {
        return m_docCategory;
    }

    /**
     * @param docCategory the m_docCategory to set
     */
    public void setDocCategory(final String docCategory) {
        this.m_docCategory = docCategory;
    }

    /**
     * @return the m_docType
     */
    public String getDocType() {
        return m_docType;
    }

    /**
     * @param docType the m_docType to set
     */
    public void setDocType(final String docType) {
        this.m_docType = docType;
    }

    /**
     * @return the m_useAuthorsColumn
     */
    public boolean isUseAuthorsColumn() {
        return m_useAuthorsColumn;
    }

    /**
     * @param useAuthorsColumn the m_useAuthorsColumn to set
     */
    public void setUseAuthorsColumn(final boolean useAuthorsColumn) {
        this.m_useAuthorsColumn = useAuthorsColumn;
    }

    /**
     * @return the m_useSourceColumn
     */
    public boolean isUseSourceColumn() {
        return m_useSourceColumn;
    }

    /**
     * @param useSourceColumn the m_useSourceColumn to set
     */
    public void setUseSourceColumn(final boolean useSourceColumn) {
        this.m_useSourceColumn = useSourceColumn;
    }

    /**
     * @return the m_usePubDateColumn
     */
    public boolean isUsePubDateColumn() {
        return m_usePubDateColumn;
    }

    /**
     * @param usePubDateColumn the m_usePubDateColumn to set
     */
    public void setUsePubDateColumn(final boolean usePubDateColumn) {
        this.m_usePubDateColumn = usePubDateColumn;
    }

    /**
     * @return the m_useCategoryColumn
     */
    public boolean isUseCategoryColumn() {
        return m_useCategoryColumn;
    }

    /**
     * @param useCategoryColumn the m_useCategoryColumn to set
     */
    public void setUseCategoryColumn(final boolean useCategoryColumn) {
        this.m_useCategoryColumn = useCategoryColumn;
    }

    /**
     * @return the m_authorsSplitStr
     */
    public String getAuthorsSplitStr() {
        return m_authorsSplitStr;
    }

    /**
     * @param m_authorsSplitStr the m_authorsSplitStr to set
     */
    public void setAuthorsSplitStr(final String m_authorsSplitStr) {
        this.m_authorsSplitStr = m_authorsSplitStr;
    }

    /**
     * @return the m_authorsFirstName
     */
    public String getAuthorsFirstName() {
        return m_authorsFirstName;
    }

    /**
     * @param authorsFirstName the m_authorsFirstName to set
     */
    public void setAuthorsFirstName(final String authorsFirstName) {
        this.m_authorsFirstName = authorsFirstName;
    }

    /**
     * @return the m_authorsLastName
     */
    public String getAuthorsLastName() {
        return m_authorsLastName;
    }

    /**
     * @param authorsLastName the m_authorsLastName to set
     */
    public void setAuthorsLastName(final String authorsLastName) {
        this.m_authorsLastName = authorsLastName;
    }

    /**
     * @return the m_numberOfThreads
     */
    public int getNumberOfThreads() {
        return m_numberOfThreads;
    }

    /**
     * @param numberOfThreads the m_numberOfThreads to set
     */
    public void setNumberOfThreads(final int numberOfThreads) {
        this.m_numberOfThreads = numberOfThreads;
    }

}

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
package org.knime.ext.textprocessing.nodes.transformation.documentdatainserter;

/**
 *
 * @author Julian Bunzel, KNIME.com Berlin
 */
public class DocumentDataInserterConfigKeys {

    DocumentDataInserterConfigKeys() {
        /* empty */}

    /**
     * The configuration key for the document column.
     */
    static final String CFGKEY_DOCCOL = "DocumentCol";

    /**
     * The configuration key of the authors column.
     */
    static final String CFGKEY_AUTHORSCOL = "AuthorsCol";

    /**
     * configuration key for the publication date column
     */
    static final String CFGKEY_PUBDATECOL = "PubdateCol";

    /**
     * The configuration key of the "use authors column" setting.
     *
     */
    static final String CFGKEY_USE_AUTHORSCOLUMN = "useAuthorsColumn";

    /**
     * The configuration key of the author name split string.
     */
    static final String CFGKEY_AUTHORSPLIT_STR = "AuthorSplitChar";

    /**
     * The configuration key of the author name placeholder for first name
     */
    static final String CFGKEY_AUTHOR_FIRST_NAME = "AuthorFirstName";

    /**
     * The configuration key of the author name placeholder for last name
     */
    static final String CFGKEY_AUTHOR_LAST_NAME = "AuthorLastName";

    /**
     * The configuration key of the document source.
     */
    static final String CFGKEY_DOCSOURCE = "DocumentSource";

    /**
     * The configuration key of the document category.
     */
    static final String CFGKEY_DOCCAT = "DocumentCategory";

    /**
     * The configuration key of the document type.
     */
    static final String CFGKEY_DOCTYPE = "DocumentType";

    /**
     * The configuration key of the publication date.
     */
    static final String CFGKEY_PUBDATE = "PublicationDate";

    /**
     * The configuration key of the "use publication date column"
     */
    static final String CFGKEY_USE_PUBDATECOLUMN = "UsePubDateColumn";

    /**
     * The configuration key of the "use category column" setting.
     */
    static final String CFGKEY_USE_CATCOLUMN = "UseCategoryColumn";

    /**
     * The configuration key of the category column setting.
     */
    static final String CFGKEY_CATCOLUMN = "CategoryColumn";

    /**
     * The configuration key of the "use source column" setting.
     */
    static final String CFGKEY_USE_SOURCECOLUMN = "UseSourceColumn";

    /**
     * The configuration key of the source column setting.
     */
    static final String CFGKEY_SOURCECOLUMN = "SourceColumn";

    /**
     * The configuration key of the number of threads to use.
     */
    static final String CFGKEY_THREADS = "Number of threads";
}

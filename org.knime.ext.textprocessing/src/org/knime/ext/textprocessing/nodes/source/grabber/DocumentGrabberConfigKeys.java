/*
========================================================================
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
 *   05.07.2007 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.grabber;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentGrabberConfigKeys {

    private DocumentGrabberConfigKeys() { }

    /**
     * Config Key for the query.
     */
    public static final String CFGKEY_QUERY = "Query";

    /**
     * Config Key for the database.
     */
    public static final String CFGKEY_DATATBASE = "Database";

    /**
     * Config Key for deleting file after parsing.
     */
    public static final String CFGKEY_DELETE = "DeleteAfterParse";

    /**
     * Config Key for number of maximal results.
     */
    public static final String CFGKEY_MAX_RESULTS = "MaximalResults";

    /**
     * Config Key for the directory to save the documents to.
     */
    public static final String CFGKEY_DIR = "Directory";

    /**
     * Config Key for the documents category.
     */
    public static final String CFGKEY_DOC_CAT = "DocumentCategrory";

    /**
     * Config Key for the document type.
     */
    public static final String CFGKEY_DOC_TYPE = "DocumentType";

    /**
     * Config Key for the extract meta info flag.
     * @since 2.7
     */
    public static final String CFGKEY_EXTRACT_META_INFO = "ExtractMetaInfo";

    /**
     * Config Key for the extract meta info flag.
     * @since 2.8
     */
    public static final String CFGKEY_APPEND_QUERYCOLUM = "AppendQueryColumn";
}

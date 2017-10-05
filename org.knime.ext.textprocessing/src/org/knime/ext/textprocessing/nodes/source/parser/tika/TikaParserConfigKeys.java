/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 *   08.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
final class TikaParserConfigKeys {
    private TikaParserConfigKeys() {
    }

    /**
     * The configuration key of the path of the directory containing the files to parse.
     */
    static final String CFGKEY_PATH = "Path";

    /**
     * The configuration key of the column containing the documents or strings.
     */
    static final String CFGKEY_COL = "Column";

    /**
     * The configuration key of the recursive flag (if set the specified directory is search recursively).
     */
    static final String CFGKEY_RECURSIVE = "Rec";

    /**
     * The configuration key of the "ignore hidden files" flag.
     */
    static final String CFGKEY_IGNORE_HIDDENFILES = "IgnoreHiddenFiles";

    /**
     * The configuration key of the list of the file types (either file extensions or MIME-Types).
     */
    static final String CFGKEY_FILTER_LIST = "TypeFilterList";

    /**
     * The configuration key of the list of the metadata information that are to be extracted.
     */
    static final String CFGKEY_COLUMNS_LIST = "ColumnsList";

    /**
     * The configuration key of the "create error column" flag.
     */
    static final String CFGKEY_ERROR_COLUMN = "ErrorColumn";

    /**
     * The configuration key of the name of the error column.
     */
    static final String CFGKEY_ERROR_COLUMN_NAME = "ErrorColumnName";

    /**
     * The configuration key of the type button to choose between file extension or MIME-Type.
     */
    static final String CFGKEY_TYPE = "Type";

    /**
     * The configuration key of the "extract attachments" flag.
     */
    static final String CFGKEY_EXTRACT_BOOLEAN = "ExtractAttachments";

    /**
     * The configuration key of the directory path where attachments will be extracted.
     */
    static final String CFGKEY_EXTRACT_PATH = "ExtractAttachmentsPath";

    /**
     * The configuration key of the input password for encrypted files.
     */
    static final String CFGKEY_CREDENTIALS = "CredentialsKey";

    /**
     * The configuration key of the "parse encrypted files" flag.
     */
    static final String CFGKEY_ENCRYPTED = "ExtractEncrypted";

    /**
     * The configuration key of the "extract inline images for PDFs" flag.
     */
    static final String CFGKEY_EXTRACT_INLINE_IMGS = "ExtractInlineImagesPDF";

}

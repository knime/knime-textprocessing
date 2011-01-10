/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
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
 *   19.02.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.source.parser;

/**
 * Holds the configuration keys of the
 * {@link org.knime.ext.textprocessing.nodes.source.parser.DocumentParserNodeModel}
 * node.
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class DocumentParserConfigKeys {

    private DocumentParserConfigKeys() { }

    /**
     * The configuration key of the path of the directory containing the files
     * to parse.
     */
    public static final String CFGKEY_PATH = "Path";

    /**
     * The configuration key of the recursive flag (if set the specified
     * directory is search recursively).
     */
    public static final String CFGKEY_RECURSIVE = "Rec";

    /**
     * The configuration key of the category to set to the documents.
     */
    public static final String CFGKEY_CATEGORY = "Cat";

    /**
     * The configuration key of the source to set to the documents.
     */
    public static final String CFGKEY_SOURCE = "Source";

    /**
     * The configuration key of the type of the document.
     */
    public static final String CFGKEY_DOCTYPE = "Type";

    /**
     * The configuration key of the "ignore hidden files" flag.
     */
    public static final String CFGKEY_IGNORE_HIDDENFILES = "IgnoreHiddenFiles";

    /**
     * The configuration key of the charset settings string.
     */
    public static final String CFGKEY_CHARSET = "Charset";
}

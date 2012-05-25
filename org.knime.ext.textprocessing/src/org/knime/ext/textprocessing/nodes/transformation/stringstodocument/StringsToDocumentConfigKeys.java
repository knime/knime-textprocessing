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
 *   29.07.2008 (thiel): created
 */
package org.knime.ext.textprocessing.nodes.transformation.stringstodocument;

/**
 *
 * @author Kilian Thiel, University of Konstanz
 */
public final class StringsToDocumentConfigKeys {

    private StringsToDocumentConfigKeys() { /* empty */ }

    /**
     * The configuration key of the title column.
     */
    public static final String CFGKEY_TITLECOL = "TitleCol";

    /**
     * The configuration key of the text column.
     */
    public static final String CFGKEY_TEXTCOL = "TextCol";

    /**
     * The configuration key of the authors column.
     */
    public static final String CFGKEY_AUTHORSCOL = "AuthorsCol";

    /**
     * The configuration key of the author name split string.
     */
    public static final String CFGKEY_AUTHORSPLIT_STR = "AuthorSplitChar";

    /**
     * The configuration key of the document source.
     */
    public static final String CFGKEY_DOCSOURCE = "DocumentSource";

    /**
     * The configuration key of the document category.
     */
    public static final String CFGKEY_DOCCAT = "DocumentCategory";

    /**
     * The configuration key of the document type.
     */
    public static final String CFGKEY_DOCTYPE = "DocumentType";

    /**
     * The configuration key of the publication date.
     */
    public static final String CFGKEY_PUBDATE = "PublicationDate";

    /**
     * The configuration key of the "use category column" setting.
     * @since 2.6
     */
    public static final String CFGKEY_USE_CATCOLUMN = "UseCategoryColumn";

    /**
     * The configuration key of the category column setting.
     * @since 2.6
     */
    public static final String CFGKEY_CATCOLUMN = "CategoryColumn";

    /**
     * The configuration key of the "use source column" setting.
     * @since 2.6
     */
    public static final String CFGKEY_USE_SOURCECOLUMN = "UseSourceColumn";

    /**
     * The configuration key of the source column setting.
     * @since 2.6
     */
    public static final String CFGKEY_SOURCECOLUMN = "SourceColumn";
}

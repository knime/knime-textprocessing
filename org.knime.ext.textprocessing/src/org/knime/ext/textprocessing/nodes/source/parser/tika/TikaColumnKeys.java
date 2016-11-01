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
 *   11.06.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.util.LinkedHashMap;

import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public final class TikaColumnKeys {

    /**
     * The name of the file, including the extension.
     */
    public static final String COL_FILEPATH = "Filepath";

    /**
     * The MIME-type of the file.
     */
    public static final String COL_MIME_TYPE = "MIME Type";

    /**
     * The title of the file.
     */
    public static final String COL_TITLE = "Title";

    /**
     * The creator of the file.
     */
    public static final String COL_CREATOR = "Author";

    /**
     * The contributor, if exists, of the file.
     */
    public static final String COL_CONTRIBUTOR = "Contributor";

    /**
     * The date on which the file is created.
     */
    public static final String COL_CREATED = "Date Created";

    /**
     * The description of the file.
     */
    public static final String COL_DESCRIPTION = "Description";

    /**
     * The keywords, if any, that are embedded to the file.
     */
    public static final String COL_KEYWORDS = "Keywords";

    /**
     * The language used in the file.
     */
    public static final String COL_LANGUAGE = "Language";

    /**
     * The date on which the file is last modified.
     */
    public static final String COL_MODIFIED = "Date Modified";

    /**
     * The name of the last author who modified the file.
     */
    public static final String COL_MODIFIER = "Last Author";

    /**
     * The name of the publisher of the file.
     */
    public static final String COL_PUBLISHER = "Publisher";

    /**
     * The date on which the file was last printed.
     */
    public static final String COL_PRINT_DATE = "Print Date";

    /**
     * The rating of the file.
     */
    public static final String COL_RATING = "Rating";

    /**
     * Any information about the rights of the file.
     */
    public static final String COL_RIGHTS = "Rights";

    /**
     * A reference to any related resources.
     */
    public static final String COL_RELATION = "Relation";

    /**
     * A reference to a resource from which the file is derived.
     */
    public static final String COL_SOURCE = "Source";

    /**
     * The nature or genre of the file.
     */
    public static final String COL_TYPE = "Type";

    /**
     * The identity of the file by means of a formal identification system.
     */
    public static final String COL_IDENTIFIER = "Identifier";

    /**
     * The format of file could be the media-type or dimensions of the file.
     */
    public static final String COL_FORMAT = "Format";

    /**
     * The extent of the scope of the file's content.
     */
    public static final String COL_COVERAGE = "Coverage";

    /**
     * The tool that was used to create the file.
     */
    public static final String COL_CREATOR_TOOL = "Creator Tool";

    /**
     * Any comment that are embedded in the file.
     */
    public static final String COL_COMMENT = "Comment";

    /**
     * The date on which the medata of the file was last changed.
     */
    public static final String COL_METADATA_DATE = "Metadata Date";

    /**
     * The content of the file.
     */
    public static final String COL_CONTENT = "Content";

    /**
     * The mapping between the column's name and its metadata property in Tika.
     */
    public static final LinkedHashMap<String,Property> COLUMN_PROPERTY_MAP = new LinkedHashMap<String,Property>(){
        private static final long serialVersionUID = 1L;
    {
        put(COL_FILEPATH,null) ;
        put(COL_MIME_TYPE,null) ;
        put(COL_TITLE,TikaCoreProperties.TITLE) ;
        put(COL_CREATOR,TikaCoreProperties.CREATOR) ;
        put(COL_CONTRIBUTOR,TikaCoreProperties.CONTRIBUTOR) ;
        put(COL_CREATED,TikaCoreProperties.CREATED) ;
        put(COL_DESCRIPTION,TikaCoreProperties.DESCRIPTION) ;
        put(COL_KEYWORDS,TikaCoreProperties.KEYWORDS) ;
        put(COL_LANGUAGE,TikaCoreProperties.LANGUAGE) ;
        put(COL_MODIFIED,TikaCoreProperties.MODIFIED) ;
        put(COL_MODIFIER,TikaCoreProperties.MODIFIER) ;
        put(COL_PUBLISHER,TikaCoreProperties.PUBLISHER) ;
        put(COL_PRINT_DATE,TikaCoreProperties.PRINT_DATE) ;
        put(COL_RATING,TikaCoreProperties.RATING) ;
        put(COL_RIGHTS,TikaCoreProperties.RIGHTS) ;
        put(COL_RELATION,TikaCoreProperties.RELATION) ;
        put(COL_SOURCE,TikaCoreProperties.SOURCE) ;
        put(COL_TYPE,TikaCoreProperties.TYPE) ;
        put(COL_IDENTIFIER,TikaCoreProperties.IDENTIFIER) ;
        put(COL_FORMAT,TikaCoreProperties.FORMAT) ;
        put(COL_COVERAGE,TikaCoreProperties.COVERAGE) ;
        put(COL_CREATOR_TOOL,TikaCoreProperties.CREATOR_TOOL) ;
        put(COL_COMMENT,TikaCoreProperties.COMMENTS) ;
        put(COL_METADATA_DATE,TikaCoreProperties.METADATA_DATE) ;
        put(COL_CONTENT,null) ;

    }};

    /**
     * The total number of all available columns.
     */
    public static final int NO_COLUMNS = COLUMN_PROPERTY_MAP.size();


}

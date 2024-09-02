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
 *   07.11.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.source.parser.tika;

import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.tika.metadata.Office;
import org.apache.tika.metadata.Property;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelPassword;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

/**
 * Config class to handle all Settingsmodel and default values for Tika nodes
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaParserConfig {

    /**
     * The default path of the directory containing the files to parse.
     */
    public static final String DEFAULT_PATH = System.getProperty("user.home");

    /**
     * The name of the document column to parse.
     */
    public static final String DEFAULT_COLNAME = "";

    /**
     * The default value of the recursive flag (if set <code>true</code> the specified directory is search recursively).
     */
    public static final boolean DEFAULT_RECURSIVE = false;

    /**
     * The default value of the ignore hidden files flag (if set <code>true</code> the hidden files will be not
     * considered for parsing.
     */
    public static final boolean DEFAULT_IGNORE_HIDDENFILES = true;

    /**
     * The action command value for choosing file extension in the dialog selection.
     */
    public static final String EXT_TYPE = "Extension";

    /**
     * The action command value for choosing MIME-Type in the dialog selection.
     */
    public static final String MIME_TYPE = "MIME";

    /**
     * The default value of the action command for choosing file extension in the dialog selection.
     */
    public static final String DEFAULT_TYPE = EXT_TYPE;

    /**
     * All supported file types in Tika.
     */
    public static final Set<MediaType> VALID_TYPES = new AutoDetectParser().getSupportedTypes(new ParseContext());

    /**
     * The list of all MIME-Types that will be shown in the dialog.
     */
    public static final String[] MIMETYPE_LIST = TikaParser.getMimeTypes();

    /**
     * The list of all file extensions that will be shown in the dialog.
     */
    public static final String[] EXTENSION_LIST = TikaParser.getExtensions();

    /**
     * The default list that will be shown in the dialog. The default is the list of file extensions.
     */
    public static final String[] DEFAULT_TYPE_LIST = EXTENSION_LIST;

    /**
     * The default list of meta data information to be parsed.
     */
    public static final String[] DEFAULT_COLUMNS_LIST =
        TikaColumnKeys.COLUMN_PROPERTY_MAP.keySet().toArray(new String[TikaColumnKeys.COLUMN_PROPERTY_MAP.size()]);

    /**
     * The default value of the "create error column" flag.
     */
    public static final boolean DEFAULT_ERROR_COLUMN = false;

    /**
     * The default value of the name of the error column.
     */
    public static final String DEFAULT_ERROR_COLUMN_NAME = "Error Output";

    /**
     * The default value of the "extract attachments" flag.
     */
    public static final boolean DEFAULT_EXTRACT = false;

    /**
     * The default path of the directory containing the extracted attachment files.
     */
    public static final String DEFAULT_EXTRACT_PATH = System.getProperty("user.home");

    /**
     * The default value of the "extract encrypted" flag.
     */
    public static final boolean DEFAULT_ENCRYPTED = false;

    /**
     * Column names for the second output port
     */
    public static final String[] OUTPUT_TWO_COL_NAMES = {"Files", "Attachments", "Type"};

    /**
     * The default value of the "extract inline images for PDFs" flag.
     */
    public static final boolean DEFAULT_EXTRACT_INLINE_IMGS = false;

    /**
     * @return SettingsModelString contains the string of the path to the input directory.
     */
    public static SettingsModelString getPathModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_PATH, DEFAULT_PATH);
    }

    /**
     * @return SettingsModelString the name of the input document column.
     */
    public static SettingsModelString getColModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_COL, DEFAULT_COLNAME);
    }

    /**
     * @return SettingsModelBoolean to decide whether to look in the input dir recursively.
     */
    public static SettingsModelBoolean getRecursiveModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_RECURSIVE, DEFAULT_RECURSIVE);
    }

    /**
     * @return SettingsModelBoolean to decide whether to ignore hidden files.
     */
    public static SettingsModelBoolean getIgnoreHiddenFilesModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_IGNORE_HIDDENFILES, DEFAULT_IGNORE_HIDDENFILES);
    }

    /**
     * @return SettingsModelStringArray an array of the selected output columns.
     */
    public static SettingsModelStringArray getColumnModel() {
        return new SettingsModelStringArray(TikaParserConfigKeys.CFGKEY_COLUMNS_LIST, DEFAULT_COLUMNS_LIST);
    }

    /**
     * @return SettingsModelBoolean to decide whether an error column should be created.
     */
    public static SettingsModelBoolean getErrorColumnModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_ERROR_COLUMN, DEFAULT_ERROR_COLUMN);
    }

    /**
     * @param errorColModel boolean to decide whether an error column should be created.
     * @return SettingsModelString that contains the name of the error column.
     */
    public static SettingsModelString getErrorColumnNameModel(final SettingsModelBoolean errorColModel) {
        SettingsModelString s =
            new SettingsModelString(TikaParserConfigKeys.CFGKEY_ERROR_COLUMN_NAME, DEFAULT_ERROR_COLUMN_NAME);
        errorColModel.addChangeListener(e -> s.setEnabled(errorColModel.getBooleanValue()));
        s.setEnabled(errorColModel.getBooleanValue());
        return s;
    }

    /**
     * @return SettingsModelString contains the selected file type (MIME or extension).
     */
    public static SettingsModelString getTypeModel() {
        return new SettingsModelString(TikaParserConfigKeys.CFGKEY_TYPE, DEFAULT_TYPE);
    }

    /**
     * @return SettingsModelBoolean to decide whether to do any extraction of embedded files.
     */
    public static SettingsModelBoolean getExtractAttachmentModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_EXTRACT_BOOLEAN, DEFAULT_EXTRACT);
    }

    /**
     * @param enableModel boolean to decide whether to do any extraction of embedded files.
     * @return SettingsModelString that contains the directory path where the extracted files should be located.
     */
    public static SettingsModelString getExtractPathModel(final SettingsModelBoolean enableModel) {
        SettingsModelString s = new SettingsModelString(TikaParserConfigKeys.CFGKEY_EXTRACT_PATH, DEFAULT_EXTRACT_PATH);
        enableModel.addChangeListener(e -> s.setEnabled(enableModel.getBooleanValue()));
        s.setEnabled(enableModel.getBooleanValue());
        return s;
    }

    /**
     * @param authBooleanModel boolean to decide whether encrypted files should be parsed.
     * @return SettingsModelPassword that contains the password of any encrypted files.
     */
    public static SettingsModelPassword getCredentialsPWD(final SettingsModelBoolean authBooleanModel) {
        final SettingsModelPassword s = new SettingsModelPassword(TikaParserConfigKeys.CFGKEY_CREDENTIALS,
            ";Op5~pK{31AIN^eH~Ab`:YaiKM8CM`8_Dw:1Kl4_WHrvuAXO", "");
        authBooleanModel.addChangeListener(e -> s.setEnabled(authBooleanModel.getBooleanValue()));
        s.setEnabled(authBooleanModel.getBooleanValue());
        return s;
    }

    /**
     * @param authBooleanModel boolean to decide whether encrypted files should be parsed.
     * @return SettingsModelString that contains the password of any encrypted files.
     * @deprecated Use {@link #getCredentialsPWD(SettingsModelBoolean)} instead.
     */
    @Deprecated
    public static SettingsModelString getCredentials(final SettingsModelBoolean authBooleanModel) {
        final SettingsModelString s = new SettingsModelString(TikaParserConfigKeys.CFGKEY_CREDENTIALS, "");
        authBooleanModel.addChangeListener(e -> s.setEnabled(authBooleanModel.getBooleanValue()));
        s.setEnabled(authBooleanModel.getBooleanValue());
        return s;
    }

    /**
     * @return SettingsModelBoolean to decide whether encrypted files should be parsed.
     */
    public static SettingsModelBoolean getAuthBooleanModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_ENCRYPTED, DEFAULT_ENCRYPTED);
    }

    /**
     * @return SettingsModelFilterString the list of all selected file types to be parsed.
     */
    public static SettingsModelFilterString getFilterModel() {
        return new SettingsModelFilterString(TikaParserConfigKeys.CFGKEY_FILTER_LIST, DEFAULT_TYPE_LIST, new String[0]);
    }

    /**
     * @return SettingsModelBoolean to decide whether to extract inline images in PDFs.
     */
    public static SettingsModelBoolean getExtractInlineImagesModel() {
        return new SettingsModelBoolean(TikaParserConfigKeys.CFGKEY_EXTRACT_INLINE_IMGS, DEFAULT_EXTRACT_INLINE_IMGS);
    }

    /**
     *
     * @author Andisa Dewi, KNIME.com, Berlin, Germany
     */
    public final static class TikaColumnKeys {

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
        public static final LinkedHashMap<String, Property> COLUMN_PROPERTY_MAP =
            new LinkedHashMap<String, Property>() {
                private static final long serialVersionUID = 1L;
                {
                    put(COL_FILEPATH, null);
                    put(COL_MIME_TYPE, null);
                    put(COL_TITLE, TikaCoreProperties.TITLE);
                    put(COL_CREATOR, TikaCoreProperties.CREATOR);
                    put(COL_CONTRIBUTOR, TikaCoreProperties.CONTRIBUTOR);
                    put(COL_CREATED, TikaCoreProperties.CREATED);
                    put(COL_DESCRIPTION, TikaCoreProperties.DESCRIPTION);
                    put(COL_KEYWORDS, Office.KEYWORDS);
                    put(COL_LANGUAGE, TikaCoreProperties.LANGUAGE);
                    put(COL_MODIFIED, TikaCoreProperties.MODIFIED);
                    put(COL_MODIFIER, TikaCoreProperties.MODIFIER);
                    put(COL_PUBLISHER, TikaCoreProperties.PUBLISHER);
                    put(COL_PRINT_DATE, TikaCoreProperties.PRINT_DATE);
                    put(COL_RATING, TikaCoreProperties.RATING);
                    put(COL_RIGHTS, TikaCoreProperties.RIGHTS);
                    put(COL_RELATION, TikaCoreProperties.RELATION);
                    put(COL_SOURCE, TikaCoreProperties.SOURCE);
                    put(COL_TYPE, TikaCoreProperties.TYPE);
                    put(COL_IDENTIFIER, TikaCoreProperties.IDENTIFIER);
                    put(COL_FORMAT, TikaCoreProperties.FORMAT);
                    put(COL_COVERAGE, TikaCoreProperties.COVERAGE);
                    put(COL_CREATOR_TOOL, TikaCoreProperties.CREATOR_TOOL);
                    put(COL_COMMENT, TikaCoreProperties.COMMENTS);
                    put(COL_METADATA_DATE, TikaCoreProperties.METADATA_DATE);
                    put(COL_CONTENT, null);

                }
            };

        /**
         * The total number of all available columns.
         */
        public static final int NO_COLUMNS = COLUMN_PROPERTY_MAP.size();

    }

}

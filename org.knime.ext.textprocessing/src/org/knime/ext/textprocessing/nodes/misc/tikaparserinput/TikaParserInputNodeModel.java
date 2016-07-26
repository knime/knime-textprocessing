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
 *   26.07.2016 (andisadewi): created
 */
package org.knime.ext.textprocessing.nodes.misc.tikaparserinput;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.streamable.simple.SimpleStreamableFunctionNodeModel;
import org.knime.core.util.FileUtil;
import org.knime.ext.textprocessing.nodes.source.parser.tika.TikaColumnKeys;

/**
 *
 * @author Andisa Dewi, KNIME.com, Berlin, Germany
 */
public class TikaParserInputNodeModel extends SimpleStreamableFunctionNodeModel{
    /**
     * The name of the language column to parse.
     */
    static final String DEFAULT_COLNAME = "";

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

    private static final Set<MediaType> VALID_TYPES = new AutoDetectParser().getSupportedTypes(new ParseContext());

    /**
     * The list of all MIME-Types that will be shown in the dialog.
     */
    public static final String[] MIMETYPE_LIST;

    static {
        MIMETYPE_LIST = getMimeTypes();
    }

    /**
     * The list of all file extensions that will be shown in the dialog.
     */
    public static final String[] EXTENSION_LIST;

    static {
        EXTENSION_LIST = getExtensions();
    }

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
     * The default value of the "extract attachments" flag.
     */
    public static final boolean DEFAULT_EXTRACT = false;

    /**
     * The default path of the directory containing the extracted attachment files.
     */
    public static final String DEFAULT_EXTRACT_PATH = System.getProperty("user.home");

    private static final String[] OUTPUT_TWO_COL_NAMES = {"Files", "Attachments"};

    private static final NodeLogger LOGGER = NodeLogger.getLogger(TikaParserInputNodeModel.class);

    private SettingsModelString m_colModel = TikaParserInputNodeDialog.getColModel();

    private SettingsModelBoolean m_recursiveModel = TikaParserInputNodeDialog.getRecursiveModel();

    private SettingsModelBoolean m_ignoreHiddenFilesModel = TikaParserInputNodeDialog.getIgnoreHiddenFilesModel();

    private SettingsModelString m_typesModel = TikaParserInputNodeDialog.getTypeModel();

    private SettingsModelStringArray m_typeListModel = TikaParserInputNodeDialog.getTypeListModel();

    private SettingsModelStringArray m_columnModel = TikaParserInputNodeDialog.getColumnModel();

    private SettingsModelBoolean m_extractBooleanModel = TikaParserInputNodeDialog.getExtractBooleanModel();

    private SettingsModelString m_extractPathModel = TikaParserInputNodeDialog.getExtractPathModel();

    private DataTableSpec createOutputTableSpec(final List<String> selectedColumns) {
        DataColumnSpec[] cspecs = new DataColumnSpec[selectedColumns.size()];
        int i = 0;
        for (String col : selectedColumns) {
            cspecs[i] = new DataColumnSpecCreator(col, StringCell.TYPE).createSpec();
            i++;
        }

        return new DataTableSpec(cspecs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_colModel.saveSettingsTo(settings);
        m_recursiveModel.saveSettingsTo(settings);
        m_ignoreHiddenFilesModel.saveSettingsTo(settings);
        m_typesModel.saveSettingsTo(settings);
        m_columnModel.saveSettingsTo(settings);
        m_typeListModel.saveSettingsTo(settings);
        m_extractBooleanModel.saveSettingsTo(settings);
        m_extractPathModel.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.validateSettings(settings);
        m_recursiveModel.validateSettings(settings);
        m_ignoreHiddenFilesModel.validateSettings(settings);
        m_typesModel.validateSettings(settings);
        m_columnModel.validateSettings(settings);
        m_typeListModel.validateSettings(settings);
        m_extractBooleanModel.validateSettings(settings);
        m_extractPathModel.validateSettings(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_colModel.loadSettingsFrom(settings);
        m_recursiveModel.loadSettingsFrom(settings);
        m_ignoreHiddenFilesModel.loadSettingsFrom(settings);
        m_typesModel.loadSettingsFrom(settings);
        m_columnModel.loadSettingsFrom(settings);
        m_typeListModel.loadSettingsFrom(settings);
        m_extractBooleanModel.loadSettingsFrom(settings);
        m_extractPathModel.loadSettingsFrom(settings);

    }

    private static String[] getMimeTypes() {
        Iterator<MediaType> it = VALID_TYPES.iterator();
        List<String> list = new ArrayList<String>();
        while (it.hasNext()) {
            list.add(it.next().toString());
        }
        Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
        return list.toArray(new String[list.size()]);
    }

    private static String[] getExtensions() {
        List<String> result = new ArrayList<String>();
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        Iterator<MediaType> mimeTypes = VALID_TYPES.iterator();
        while (mimeTypes.hasNext()) {
            String mime = mimeTypes.next().toString();

            try {
                List<String> extList = allTypes.forName(mime).getExtensions();
                if (!extList.isEmpty()) {
                    for (String s : extList) {
                        String withoutDot = s.substring(1, s.length());
                        if (!result.contains(withoutDot)) {
                            result.add(withoutDot);
                        }
                    }
                }
            } catch (MimeTypeException e) {
                LOGGER.error("Could not fetch MIME type: " + mime, new MimeTypeException("Fetching MIME type failed!"));
            }
        }
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result.toArray(new String[result.size()]);
    }

    private static File getFile(final String file) throws InvalidSettingsException {
        File f = null;
        try {
            // first try if file string is an URL (files in drop dir come as URLs)
            final URL url = new URL(file);
            f = FileUtil.getFileFromURL(url);
        } catch (MalformedURLException e) {
            // if no URL try string as path to file
            f = new File(file);
        }

        if (f.isDirectory() | !f.exists() || !f.canRead()) {
            throw new InvalidSettingsException("Selected file: " + file + " cannot be accessed!");
        }

        return f;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ColumnRearranger createColumnRearranger(final DataTableSpec in) throws InvalidSettingsException {
        int colIndex = -1;
        colIndex = in.findColumnIndex(m_colModel.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < in.getNumColumns(); i++) {
                if (in.getColumnSpec(i).getType().isCompatible(StringValue.class)) {
                    colIndex = i;
                    LOGGER.info("Guessing string column \"" + in.getColumnSpec(i).getName() + "\".");
                    break;
                }
            }
        }

        if (colIndex < 0) {
            throw new InvalidSettingsException("String column not set");
        }
        m_colModel.setStringValue(in.getColumnSpec(colIndex).getName());
        return null;
    }

}

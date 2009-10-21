/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2009
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
 *   Jun 16, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.nodes.misc.keywordextractor.keygraph;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * Dialog for the keygraph node.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public class KeygraphNodeDialog extends DefaultNodeSettingsPane {

    // Configuration strings
    private static final String CFGKEY_NR_HIGHKEY_TERMS = "nrHighKeyTerms";
    private static final String CFGKEY_NR_HIGHFREQ_TERMS = "nrHighFreqTerms";
    private static final String CFGKEY_NR_KEYWORDS = "nrKeywords";
    private static final String CFGKEY_IGNORE_TERM_TAGS = "ignoreTermTags";
    private static final String CFGKEY_DOCUMENT_COLUMN_NAME =
        "documentColumnName";

    // Default values
    private static final int DEFAULT_NR_HIGHFREQ_TERMS = 30;
    private static final int DEFAULT_NR_HIGHKEY_TERMS = 12;
    private static final int DEFAULT_NR_KEYWORDS = 10;
    private static final boolean DEFAULT_IGNORE_TERM_TAGS = true;
    private static final String DEFAULT_DOCUMENT_COLUMN_NAME = "Document";

    /**
     * Creates a basic dialog for the Keygraph node
     */
    @SuppressWarnings("unchecked")
    public KeygraphNodeDialog() {
        super();

        addDialogComponent(new DialogComponentColumnNameSelection(
                createSetDocumentColumnNameModel(),
                "Document column:", 0, DocumentValue.class));

        addDialogComponent(new DialogComponentNumber(
                createSetNrKeywordsModel(),
                    "Number of keywords to extract:", /*step*/ 1));

        addDialogComponent(new DialogComponentNumber(
                createSetNrHighFreqTermsModel(),
                    "Size of the high frequency terms set:", /*step*/ 1));

        addDialogComponent(new DialogComponentNumber(
                createSetNrHighKeyTermsModel(),
                    "Size of the high key terms set:", /*step*/ 1));

        addDialogComponent(new DialogComponentBoolean(
                createSetIgnoreTermTagsModel(),
                    "Ignore tags"));
    }

    /**
     * @return a setting model for the number of keywords to extract
     */
    public static SettingsModelIntegerBounded createSetNrKeywordsModel() {
        return new SettingsModelIntegerBounded(
                CFGKEY_NR_KEYWORDS,
                DEFAULT_NR_KEYWORDS,
                1, Integer.MAX_VALUE);
    }

    /**
     * @return a setting model for the 'ignore term tags' option
     */
    public static SettingsModelBoolean createSetIgnoreTermTagsModel() {
        return new SettingsModelBoolean(
                CFGKEY_IGNORE_TERM_TAGS,
                DEFAULT_IGNORE_TERM_TAGS);
    }

    /**
     * @return a setting model for the number of terms to include in the
     * high frequency set
     */
    public static SettingsModelIntegerBounded createSetNrHighFreqTermsModel() {
        return new SettingsModelIntegerBounded(
                CFGKEY_NR_HIGHFREQ_TERMS,
                DEFAULT_NR_HIGHFREQ_TERMS,
                1, Integer.MAX_VALUE);
    }

    /**
     * @return a setting model for the number of terms to include in the high
     * key set
     */
    public static SettingsModelIntegerBounded createSetNrHighKeyTermsModel() {
        return new SettingsModelIntegerBounded(
                CFGKEY_NR_HIGHKEY_TERMS,
                DEFAULT_NR_HIGHKEY_TERMS,
                1, Integer.MAX_VALUE);
    }

    /**
     * @return a setting model for the document column name picker
     */
    public static SettingsModelString createSetDocumentColumnNameModel() {
        return new SettingsModelString(
                CFGKEY_DOCUMENT_COLUMN_NAME,
                DEFAULT_DOCUMENT_COLUMN_NAME);
    }
}

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
 *   09.09.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

/**
 * Summarizing all ConfigKeys necessary for the TagCloud dialog components.
 *
 * @author Iris Adae, University of Konstanz
 */
public final class TagCloudConfigKeys {

    private TagCloudConfigKeys() { }
    
    /**
     * The configuration key for the column containing he documents to process.
     */
    public static final String CFG_KEY_TERM_COL = "TermCol";

    /**
     * The configuration key for the column containing he value to process.
     */
    public static final String CFG_KEY_VALUE_COL = "ValCol";

    /**
     * Configuration key and label for the minimal font size adjustment.
     */
    public static final String CFG_MINFONTSIZE = "minimal fontsize:";

    /**
     * Configuration key and label for the maximal font size adjustment.
     */
    public static final String CFG_MAXFONTSIZE = "maximal fontsize:";

    /**
     * Configuration key and label for the font size distribution.
     */
    public static final String CFG_TYPEOFFSCALC =
            "Kind of fontsize distribution";

    /**
     * Entryset for the kind of font size calculation.
     */
    public static final String[] CFG_TYPEOFFSCALCI =
            {"linear", "logarithmic", "exponential"};

    /**
     * Configuration key and label for the kind of tagcloud.
     */
    public static final String CFG_TYPEOFTCCALC = "Kind of TagCloud";

    /**
     * Entry set for the different kinds of font size calculation.
     */
    public static final String[] CFG_TYPEOFTCCALCI =
            {"Simple Table", "Alphabetic Table", "Size-Sorted Table",
                    "InsideOut Table"};

    /**
     * Configuration key and label for ignoring the tags.
     */
    public static final String CFGKEY_IGNORE_TAGS = "Ignore tags";

    /** Default number of rows to use. */
    public static final int DEFAULT_NO_OF_ROWS = 2500;

    /** Settings name for the take all rows select box. */
    public static final String CFGKEY_ALL_ROWS = "allRows";

    /** Settings name of the number of rows. */
    public static final String CFGKEY_NO_OF_ROWS = "noOfRows";

    /**
     * Label for displaying all rows.
     */
    public static final String ALL_ROWS_LABEL = "Display all rows";

    /**
     * Label for the number of rows to display.
     */
    public static final String NO_OF_ROWS_LABEL = "No. of rows to display:";



}

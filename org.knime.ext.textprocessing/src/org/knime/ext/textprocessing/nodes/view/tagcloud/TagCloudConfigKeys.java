/*
 * ------------------------------------------------------------------------
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
 *   09.09.2008 (Iris Adae): created
 */
package org.knime.ext.textprocessing.nodes.view.tagcloud;

/**
 * Summarizing all ConfigKeys necessary for the TagCloud dialog components.
 *
 * @author Iris Adae, University of Konstanz
 * @deprecated
 */
@Deprecated
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

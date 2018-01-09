/*
 * ------------------------------------------------------------------------
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
 *   Jun 16, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.nodes.misc.keywordextractor.chisquare;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;

/**
 * Dialog for the keyword extractor node.
 *
 * @author Pierre-Francois Laquerre, University of Konstanz
 */
public class KeywordExtractorNodeDialog extends DefaultNodeSettingsPane {

    // Configuration strings
    private static final String CFGKEY_FREQUENT_TERMS_PROPORTION =
            "frequentTermsProportion";
    private static final String CFGKEY_NR_KEYWORDS = "nrKeywords";
    private static final String CFGKEY_IGNORE_TERM_TAGS = "ignoreTermTags";
    private static final String CFGKEY_PMI_THRESHOLD = "PMIThreshold";
    private static final String CFGKEY_L1_THRESHOLD = "L1Threshold";
    private static final String CFGKEY_DOCUMENT_COLUMN_NAME =
        "documentColumnName";

    // Default values
    private static final int DEFAULT_FREQUENT_TERMS_PROPORTION = 30;
    private static final int DEFAULT_NR_KEYWORDS = 10;
    private static final boolean DEFAULT_IGNORE_TERM_TAGS = false;
    private static final double DEFAULT_PMI_THRESHOLD = Math.log(2);
    private static final double DEFAULT_L1_THRESHOLD = 0.4;
    private static final String DEFAULT_DOCUMENT_COLUMN_NAME = "";

    /**
     * Creates a basic dialog for the Keyword Extractor node.
     */
    @SuppressWarnings("unchecked")
    public KeywordExtractorNodeDialog() {
        super();

        addDialogComponent(new DialogComponentColumnNameSelection(
                createSetDocumentColumnNameModel(),
                "Document column:", 0, DocumentValue.class));

        addDialogComponent(new DialogComponentNumber(
                createSetNrKeywordsModel(),
                    "Number of keywords to extract:", /*step*/ 1));

        addDialogComponent(new DialogComponentNumber(
                createSetFrequentTermsProportionModel(),
                    "Percentage of unique terms in the document to use for "
                  + "the chi-square measures:", /*step*/ 1));

        addDialogComponent(new DialogComponentBoolean(
                createSetIgnoreTermTagsModel(),
                    "Ignore tags"));

        addDialogComponent(new DialogComponentNumber(
                createSetPMIThresholdModel(),
                    "Pointwise mutual information threshold:", .01));

        addDialogComponent(new DialogComponentNumber(
                createSetL1ThresholdModel(),
                    "Normalized L1 norm threshold:", .01));
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
     * @return a setting model for the Pointwise Mutual Information threshold.
     */
    public static SettingsModelDoubleBounded createSetPMIThresholdModel() {
        return new SettingsModelDoubleBounded(
                CFGKEY_PMI_THRESHOLD,
                DEFAULT_PMI_THRESHOLD,
                0.0, 1.0);
    }

    /**
     * @return a setting model for the L1 threshold
     */
    public static SettingsModelDoubleBounded createSetL1ThresholdModel() {
        return new SettingsModelDoubleBounded(
                CFGKEY_L1_THRESHOLD,
                DEFAULT_L1_THRESHOLD,
                0.0, 1.0);
    }

    /**
     * @return a setting model for the proportion of terms to use for clusters
     */
    public static SettingsModelIntegerBounded createSetFrequentTermsProportionModel() {
        return new SettingsModelIntegerBounded(
                CFGKEY_FREQUENT_TERMS_PROPORTION,
                DEFAULT_FREQUENT_TERMS_PROPORTION,
                1, 100);
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

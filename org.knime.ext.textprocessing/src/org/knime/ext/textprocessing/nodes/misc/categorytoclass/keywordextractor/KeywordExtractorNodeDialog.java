/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * ---------------------------------------------------------------------
 *
 * History
 *   Jun 16, 2008 (Pierre-Francois Laquerre): created
 */
package org.knime.ext.textprocessing.nodes.misc.categorytoclass.keywordextractor;

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
    private static final String DEFAULT_DOCUMENT_COLUMN_NAME = "Document";


    /**
     * Creates a basic dialog for the Keyword Extractor node
     */
    @SuppressWarnings("unchecked")
    public KeywordExtractorNodeDialog() {
        super();

        addDialogComponent(new DialogComponentColumnNameSelection(
                createSetDocumentColumnNameModel(),
                "Document column", 0, DocumentValue.class));

        addDialogComponent(new DialogComponentNumber(
                createSetNrKeywordsModel(),
                    "Number of keywords to extract:", /*step*/ 1));

        addDialogComponent(new DialogComponentNumber(
                createSetFrequentTermsProportionModel(),
                    "Percentage of unique terms in the document to use for " +
                    "the chi-square measures:", /*step*/ 1));

        addDialogComponent(new DialogComponentBoolean(
                createSetIgnoreTermTagsModel(),
                    "Ignore tags:"));

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

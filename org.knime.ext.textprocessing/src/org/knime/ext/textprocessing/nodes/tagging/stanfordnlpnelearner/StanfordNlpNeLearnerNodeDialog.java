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
 *   07.07.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnelearner;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.data.StringValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.TagFactory;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;

/**
 * The node dialog for the StanfordNLP NE Learner node.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeLearnerNodeDialog extends DefaultNodeSettingsPane {

    /**
     * The default value for the tag type SettingsModel.
     */
    private static final String DEF_TAG_TYPE = "NE";

    /**
     * The default value for the tag value SettingsModel.
     */
    private static final String DEF_TAG_VALUE = "UNKNOWN";

    /**
     * The default value for the "use class feature" flag.
     */
    private static final boolean DEF_USE_CLASS_FEATURE = true;

    /**
     * The default value for the "use word" flag.
     */
    private static final boolean DEF_USE_WORD = true;

    /**
     * The default value for the "use n-grams" flag.
     */
    private static final boolean DEF_USE_NGRAMS = true;

    /**
     * The default value for the "no mid n-grams" flag.
     */
    private static final boolean DEF_NO_MID_NGRAMS = true;

    /**
     * The default value for the "max n-gram length".
     */
    private static final int DEF_MAX_NGRAM_LENG = 6;

    /**
     * The default value for the "use prev" flag.
     */
    private static final boolean DEF_USE_PREV = true;

    /**
     * The default value for the "use next" flag.
     */
    private static final boolean DEF_USE_NEXT = true;

    /**
     * The default value for the "use sequences" flag.
     */
    private static final boolean DEF_USE_SEQUENCES = true;

    /**
     * The default value for the "use prev sequences" flag.
     */
    private static final boolean DEF_USE_PREV_SEQUENCES = true;

    /**
     * The default value for the "max left" option.
     */
    private static final int DEF_MAX_LEFT = 1;

    /**
     * The default value for the "use type sequences" flag.
     */
    private static final boolean DEF_USE_TYPE_SEQS = true;

    /**
     * The default value for the "use type sequences 2" flag.
     */
    private static final boolean DEF_USE_TYPE_SEQS2 = true;

    /**
     * The default value for the "use type y sequences" flag.
     */
    private static final boolean DEF_USE_TYPE_Y_SEQS = true;

    /**
     * The default value for the "word shape" option.
     */
    private static final String DEF_WORDSHAPE = "chris2useLC";

    /**
     * The default value for the "use disjunctive" flag.
     */
    private static final boolean DEF_USE_DISJUNCTIVE = true;

    /**
     * The default value for the "case sensitivity" flag.
     */
    private static final boolean DEF_CASE_SENSITIVITY = true;

    /**
     * @return Creates and returns the string settings model containing the name of the column with the documents to
     *         learn the model with.
     */
    public static final SettingsModelString createDocumentColumnModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFG_KEY_DOCUMENT_COL, "");
    }

    /**
     * @return Creates and returns the string settings model containing the name of the column with the dictionary to
     *         learn the model with.
     */
    public static final SettingsModelString createKnownEntitiesColumnModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFG_KEY_STRING_COL, "");
    }

    /**
     * @return Creates and returns the string settings model containing the tag type (e.g. NE).
     */
    public static final SettingsModelString createTagTypeModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFGKEY_TAG_TYPE,
            DEF_TAG_TYPE);
    }

    /**
     * @return Creates and returns the string settings model containing the tag value (e.g. PERSON).
     */
    public static final SettingsModelString createTagValueModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFGKEY_TAG_VALUE,
            DEF_TAG_VALUE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use class feature" flag. For more information
     *         about this option, check the
     *         <a href="http://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/ie/NERFeatureFactory.html">
     *         NERFeatureFactory</a> Java documentation from StanfordNLP.
     */
    public static final SettingsModelBoolean createUseClassFeatureModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_CLASS_FEATURE,
            DEF_USE_CLASS_FEATURE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use word" flag.
     */
    public static final SettingsModelBoolean createUseWordModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_WORD,
            DEF_USE_WORD);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use n-grams" flag.
     */
    public static final SettingsModelBoolean createUseNGramsModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_NGRAMS,
            DEF_USE_NGRAMS);
    }

    /**
     * @return Creates and returns the boolean settings model for the "no mid n-grams" flag.
     */
    public static final SettingsModelBoolean createNoMidNGramsModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_NO_MID_NGRAMS,
            DEF_NO_MID_NGRAMS);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use previous" flag.
     */
    public static final SettingsModelBoolean createUsePrevModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_PREV,
            DEF_USE_PREV);
    }

    /**
     * @return Creates and returns the integer settings model for the "maximal n-gram length".
     */
    public static final SettingsModelIntegerBounded createMaxNGramLengthModel() {
        return new SettingsModelIntegerBounded(StanfordNlpNeLearnerConfigKeys.CFGKEY_MAX_NGRAM_LENG,
            DEF_MAX_NGRAM_LENG, 0, Integer.MAX_VALUE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use next" flag.
     */
    public static final SettingsModelBoolean createUseNextModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_NEXT,
            DEF_USE_NEXT);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use sequences" flag.
     */
    public static final SettingsModelBoolean createUseSequencesModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_SEQUENCES,
            DEF_USE_SEQUENCES);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use prev sequences" flag.
     */
    public static final SettingsModelBoolean createUsePrevSequencesModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_PREV_SEQUENCES,
            DEF_USE_PREV_SEQUENCES);
    }

    /**
     * @return Creates and returns the integer settings model for the "max left" settings.
     */
    public static final SettingsModelIntegerBounded createMaxLeftModel() {
        return new SettingsModelIntegerBounded(StanfordNlpNeLearnerConfigKeys.CFGKEY_MAX_LEFT,
            DEF_MAX_LEFT, -1, Integer.MAX_VALUE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use type sequences" flag.
     */
    public static final SettingsModelBoolean createUseTypeSeqsModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_TYPE_SEQS,
            DEF_USE_TYPE_SEQS);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use type sequences 2" flag.
     */
    public static final SettingsModelBoolean createUseTypeSeqs2Model() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_TYPE_SEQS2,
            DEF_USE_TYPE_SEQS2);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use type y sequences" flag.
     */
    public static final SettingsModelBoolean createUseTypeYSeqsModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_TYPE_Y_SEQS,
            DEF_USE_TYPE_Y_SEQS);
    }

    /**
     * @return Creates and returns the string settings model for the "word shape" option.
     */
    public static final SettingsModelString createWordShapeModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFGKEY_WORDSHAPE,
            DEF_WORDSHAPE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use disjunctive" flag.
     */
    public static final SettingsModelBoolean createUseDisjunctiveModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFGKEY_USE_DISJUNCTIVE,
            DEF_USE_DISJUNCTIVE);
    }

    /**
     * @return Creates and returns the boolean settings model for the "use disjunctive" flag.
     */
    public static final SettingsModelString createTokenizerModel() {
        return new SettingsModelString(StanfordNlpNeLearnerConfigKeys.CFGKEY_TOKENIZER, "");
    }

    /**
     * @return Creates and returns the boolean settings model for the "case sensitivity" flag.
     */
    static final SettingsModelBoolean createCaseSensitivityModel() {
        return new SettingsModelBoolean(StanfordNlpNeLearnerConfigKeys.CFG_KEY_CASE_SENSITIVITY,
            DEF_CASE_SENSITIVITY);
    }

    private final SettingsModelString m_tagtypemodel;

    private final DialogComponentStringSelection m_tagSelection;

    private static final List<String> m_wordShapes =
        Arrays.asList("none", "dan1", "chris1", "dan2", "dan2useLC", "dan2bio", "dan2bioUseLC", "jenny1",
            "jenny1useLC", "chris2", "chris2useLC", "chris3", "chris3useLC", "chris4");

    /**
     * Creates an new instance of {@code StanfordNlpNeLearnerNodeDialog}.
     */
    @SuppressWarnings("unchecked")
    public StanfordNlpNeLearnerNodeDialog() {
        super();
        renameTab("Options", "Learner Options");
        selectTab("Learner Options");
        setHorizontalPlacement(true);
        final SettingsModelString docColModel = createDocumentColumnModel();
        final DialogComponentColumnNameSelection docComp =
            new DialogComponentColumnNameSelection(docColModel, "Document column", 0, DocumentValue.class);
        docComp.setToolTipText("The documents to train the model with.");
        addDialogComponent(docComp);
        setHorizontalPlacement(true);

        final SettingsModelString knownEntitiesColModel = createKnownEntitiesColumnModel();
        final DialogComponentColumnNameSelection knownEntitiesComp =
            new DialogComponentColumnNameSelection(knownEntitiesColModel, "Dictionary column", 1, StringValue.class);
        docComp.setToolTipText("The dictionary to train the model with.");
        addDialogComponent(knownEntitiesComp);
        setHorizontalPlacement(false);

        // tag type model
        m_tagtypemodel = createTagTypeModel();
        m_tagtypemodel.addChangeListener(new InternalChangeListener());

        // tag list

        final String selectedTagType = m_tagtypemodel.getStringValue();
        final List<String> tags = TagFactory.getInstance().getTagSetByType(selectedTagType).asStringList();
        m_tagSelection = new DialogComponentStringSelection(createTagValueModel(), "Tag value", tags);
        setHorizontalPlacement(true);
        addDialogComponent(
            new DialogComponentStringSelection(m_tagtypemodel, "Tag type", TagFactory.getInstance().getTagTypes()));
        addDialogComponent(m_tagSelection);

        setHorizontalPlacement(false);

        final Collection<String> tokenizerList = TokenizerFactoryRegistry.getTokenizerFactoryMap().keySet();
        addDialogComponent(new DialogComponentStringSelection(createTokenizerModel(), "Word tokenizer", tokenizerList));

        createNewTab("Learner Properties");
        selectTab("Learner Properties");
        createNewGroup("Order of the CRF");
        addDialogComponent(new DialogComponentNumberEdit(createMaxLeftModel(), "Max Left"));

        createNewGroup("Training features");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(createUseClassFeatureModel(), "Use Class Feature"));
        addDialogComponent(new DialogComponentBoolean(createUseWordModel(), "Use Word"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(createUseNGramsModel(), "Use NGrams"));
        addDialogComponent(new DialogComponentBoolean(createNoMidNGramsModel(), "No Mid NGrams"));
        addDialogComponent(new DialogComponentNumberEdit(createMaxNGramLengthModel(), "Max NGram Length"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(createUsePrevModel(), "Use Prev"));
        addDialogComponent(new DialogComponentBoolean(createUseNextModel(), "Use Next"));
        addDialogComponent(new DialogComponentBoolean(createUseDisjunctiveModel(), "Use Disjunctive"));
        setHorizontalPlacement(false);
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(createUseSequencesModel(), "Use Sequences"));
        addDialogComponent(new DialogComponentBoolean(createUsePrevSequencesModel(), "Use Prev Sequences"));
        setHorizontalPlacement(false);

        createNewGroup("Word shape features");
        setHorizontalPlacement(true);
        addDialogComponent(new DialogComponentBoolean(createUseTypeSeqsModel(), "Use Type Seqs"));
        addDialogComponent(new DialogComponentBoolean(createUseTypeSeqs2Model(), "Use Type Seqs2"));
        addDialogComponent(new DialogComponentBoolean(createUseTypeYSeqsModel(), "Use Type Y Seqs"));
        setHorizontalPlacement(false);
        addDialogComponent(new DialogComponentStringSelection(createWordShapeModel(), "Word Shape", m_wordShapes));
        final DialogComponentBoolean caseSensComp =
                new DialogComponentBoolean(createCaseSensitivityModel(), "Case Sensitivity");
        caseSensComp.setToolTipText("Select if words from dictionary should be handled in a case sensitive manner.");
        addDialogComponent(caseSensComp);
    }

    /**
     *
     * @author Julian Bunzel, KNIME.com
     */
    class InternalChangeListener implements ChangeListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void stateChanged(final ChangeEvent e) {
            final String selectedTagType = m_tagtypemodel.getStringValue();
            final List<String> tags = TagFactory.getInstance().getTagSetByType(selectedTagType).asStringList();
            m_tagSelection.replaceListItems(tags, "");
        }
    }

}

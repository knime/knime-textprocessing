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
 *   07.07.2016 (Julian Bunzel): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnelearner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.StringValue;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.NERModelPortObjectSpec;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.StanfordNERModelPortObject;
import org.knime.ext.textprocessing.data.Tag;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.Word;
import org.knime.ext.textprocessing.nodes.tagging.dict.wildcard.MultiTermRegexDocumentTagger;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import com.google.common.io.Files;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeLearnerNodeModel extends NodeModel {

    /**
     * The default value for the tag type SettingsModel.
     */
    static final String DEF_TAG_TYPE = "NE";

    /**
     * The default value for the tag value SettingsModel.
     */
    static final String DEF_TAG_VALUE = "UNKNOWN";

    /**
     * The default value for the "use class feature" flag.
     */
    static final boolean DEF_USE_CLASS_FEATURE = true;

    /**
     * The default value for the "use word" flag.
     */
    static final boolean DEF_USE_WORD = true;

    /**
     * The default value for the "use n-grams" flag.
     */
    static final boolean DEF_USE_NGRAMS = true;

    /**
     * The default value for the "no mid n-grams" flag.
     */
    static final boolean DEF_NO_MID_NGRAMS = true;

    /**
     * The default value for the "max n-gram length".
     */
    static final int DEF_MAX_NGRAM_LENG = 6;

    /**
     * The default value for the "use prev" flag.
     */
    static final boolean DEF_USE_PREV = true;

    /**
     * The default value for the "use next" flag.
     */
    static final boolean DEF_USE_NEXT = true;

    /**
     * The default value for the "use sequences" flag.
     */
    static final boolean DEF_USE_SEQUENCES = true;

    /**
     * The default value for the "use prev sequences" flag.
     */
    static final boolean DEF_USE_PREV_SEQUENCES = true;

    /**
     * The default value for the "max left" option.
     */
    static final int DEF_MAX_LEFT = 1;

    /**
     * The default value for the "use type sequences" flag.
     */
    static final boolean DEF_USE_TYPE_SEQS = true;

    /**
     * The default value for the "use type sequences 2" flag.
     */
    static final boolean DEF_USE_TYPE_SEQS2 = true;

    /**
     * The default value for the "use type y sequences" flag.
     */
    static final boolean DEF_USE_TYPE_Y_SEQS = true;

    /**
     * The default value for the "word shape" option.
     */
    static final String DEF_WORDSHAPE = "chris2useLC";

    /**
     * The default value for the "use disjunctive" flag.
     */
    static final boolean DEF_USE_DISJUNCTIVE = true;

    private SettingsModelString m_docColumnModel = StanfordNlpNeLearnerNodeDialog.createDocumentColumnModel();

    private SettingsModelString m_knownEntitiesColumnModel =
        StanfordNlpNeLearnerNodeDialog.createKnownEntitiesColumnModel();

    private SettingsModelString m_tagValueModel = StanfordNlpNeLearnerNodeDialog.createTagValueModel();

    private SettingsModelString m_tagTypeModel = StanfordNlpNeLearnerNodeDialog.createTagTypeModel();

    private Tag m_tag;

    // Properties settings models

    private SettingsModelBoolean m_useClassFeature = StanfordNlpNeLearnerNodeDialog.createUseClassFeatureModel();

    private SettingsModelBoolean m_useWord = StanfordNlpNeLearnerNodeDialog.createUseWordModel();

    private SettingsModelBoolean m_useNGrams = StanfordNlpNeLearnerNodeDialog.createUseNGramsModel();

    private SettingsModelBoolean m_noMidNGrams = StanfordNlpNeLearnerNodeDialog.createNoMidNGramsModel();

    private SettingsModelBoolean m_usePrev = StanfordNlpNeLearnerNodeDialog.createUsePrevModel();

    private SettingsModelBoolean m_useNext = StanfordNlpNeLearnerNodeDialog.createUseNextModel();

    private SettingsModelBoolean m_useSequences = StanfordNlpNeLearnerNodeDialog.createUseSequencesModel();

    private SettingsModelBoolean m_usePrevSequences = StanfordNlpNeLearnerNodeDialog.createUsePrevSequencesModel();

    private SettingsModelBoolean m_useTypeSeqs = StanfordNlpNeLearnerNodeDialog.createUseTypeSeqsModel();

    private SettingsModelBoolean m_useTypeSeqs2 = StanfordNlpNeLearnerNodeDialog.createUseTypeSeqs2Model();

    private SettingsModelBoolean m_useTypeySequences = StanfordNlpNeLearnerNodeDialog.createUseTypeYSeqsModel();

    private SettingsModelBoolean m_useDisjunctive = StanfordNlpNeLearnerNodeDialog.createUseDisjunctiveModel();

    private SettingsModelString m_wordShape = StanfordNlpNeLearnerNodeDialog.createWordShapeModel();

    private SettingsModelIntegerBounded m_maxLeft = StanfordNlpNeLearnerNodeDialog.createMaxLeftModel();

    private SettingsModelIntegerBounded m_maxNGramLeng = StanfordNlpNeLearnerNodeDialog.createMaxNGramLengthModel();

    private SettingsModelString m_tokenizer = StanfordNlpNeLearnerNodeDialog.createTokenizerModel();

    /**
     *
     */
    protected StanfordNlpNeLearnerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE, BufferedDataTable.TYPE},
            new PortType[]{PortTypeRegistry.getInstance().getPortType(StanfordNERModelPortObject.class)});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

        // checking for document column in first input table and string column in second input table
        DataTableSpec spec = (DataTableSpec)inSpecs[0];
        DataTableSpec spec2 = (DataTableSpec)inSpecs[1];
        DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        DataTableSpecVerifier verifier2 = new DataTableSpecVerifier((DataTableSpec)inSpecs[1]);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier2.verifyMinimumStringCells(1, true);

        // verifying the selected document column or select the first suitable column if none has been set yet
        ColumnSelectionVerifier docColSelVerifier =
            new ColumnSelectionVerifier(m_docColumnModel, spec, DocumentValue.class);
        if (docColSelVerifier.hasWarningMessage()) {
            setWarningMessage(docColSelVerifier.getWarningMessage());
        }

        // verifying the selected string column or select the first suitable column if none has been set yet
        ColumnSelectionVerifier dictColSelVerifier =
                new ColumnSelectionVerifier(m_knownEntitiesColumnModel, spec2, StringValue.class);
            if (dictColSelVerifier.hasWarningMessage()) {
                setWarningMessage(dictColSelVerifier.getWarningMessage());
            }

        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizer.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizer.getStringValue());
        }

        // check tokenizer settings from incoming document column
        DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(spec);
        if (!dataTableSpecVerifier.verifyTokenizer(spec.findColumnIndex(m_docColumnModel.getStringValue()),
            m_tokenizer.getStringValue())) {
            setWarningMessage(dataTableSpecVerifier.getTokenizerWarningMsg());
        }

        return new PortObjectSpec[]{new NERModelPortObjectSpec(m_tokenizer.getStringValue())};
    }

    @Override
    protected PortObject[] execute(final PortObject[] data, final ExecutionContext exec) throws Exception {
        // check input data
        assert (data != null && data[0] != null);

        // get specs and data tables
        final DataTableSpec docTableSpec = (DataTableSpec)data[0].getSpec();
        final DataTableSpec knownEntitiesTableSpec = (DataTableSpec)data[1].getSpec();
        final BufferedDataTable docDataInput = (BufferedDataTable)data[0];
        final BufferedDataTable knownEntitiesDataInput = (BufferedDataTable)data[1];
        final Set<Pattern> knownEntitiesPatternSet = new LinkedHashSet<Pattern>();
        final Set<String> knownEntitiesStringSet = new LinkedHashSet<String>();

        // iterate through columns
        for (int i = 0; i < knownEntitiesTableSpec.getNumColumns(); i++) {
            // iterate through rows if column with correct name has been found
            if (knownEntitiesTableSpec.getColumnSpec(i).getName().equals(m_knownEntitiesColumnModel.getStringValue())) {
                for (DataRow row : knownEntitiesDataInput) {
                    if (!row.getCell(i).isMissing()) {
                        // add every known entity to the specified array list
                        String cellText = row.getCell(i).toString();
                        // TODO: Add case sensitivity
                        if (!cellText.trim().isEmpty()) {
                            knownEntitiesStringSet.add(cellText);
                            knownEntitiesPatternSet.add(Pattern.compile(cellText));
                        }
                    }
                }
            }
        }

        if (knownEntitiesPatternSet.size() == 0) {
            setWarningMessage("Trained model on empty dictionary.");
        }

        // create tag for document tagger
        m_tag = new Tag(m_tagValueModel.getStringValue(), m_tagTypeModel.getStringValue());

        // create tagger based on known entities
        // TODO: Case sensitivity!
        MultiTermRegexDocumentTagger tagger =
            new MultiTermRegexDocumentTagger(true, knownEntitiesPatternSet, m_tag, true, m_tokenizer.getStringValue());

        // create UUID to add them to the file path to avoid cases where two instances of the node model used the same file path at the same time
        String tempDir = KNIMEConstants.getKNIMETempDir() + "/";
        String m_modelPath = tempDir + "oM-" + UUID.randomUUID().toString() + ".crf.ser.gz";
        String m_annotatedDocPath = tempDir + "aD-" + UUID.randomUUID().toString() + ".tsv";

        // create files based on sentence list and known entities
        File m_annotatedDocFile = new File(m_annotatedDocPath);
        PrintWriter sentenceFileWriter = new PrintWriter(m_annotatedDocFile, "UTF-8");

        int missingValueCounter = 0;

        // tag documents and transform sentences to strings while tagged terms get stanfordnlp annotation
        // iterate through columns
        for (int i = 0; i < docTableSpec.getNumColumns(); i++) {
            // iterate through rows if column with correct name has been found
            if (docTableSpec.getColumnSpec(i).getName().equals(m_docColumnModel.getStringValue())) {
                int counter = 0;
                for (DataRow row : docDataInput) {
                    //set progress bar
                    counter++;
                    double progress = (counter / (double)docDataInput.size()) / (2.0);
                    exec.setProgress(progress, "Preparing documents");

                    if (!row.getCell(i).isMissing() && row.getCell(i).getType().isCompatible(DocumentValue.class)) {
                        Document doc = ((DocumentValue)row.getCell(i)).getDocument();
                        Document taggedDoc = tagger.tag(doc);
                        Iterator<Sentence> si = taggedDoc.sentenceIterator();
                        while (si.hasNext()) {
                            Sentence s = si.next();
                            List<Term> termList = s.getTerms();
                            Iterator<Term> ti = termList.iterator();
                            while (ti.hasNext()) {
                                Term t = ti.next();
                                String termText = t.getText();
                                String termTextWithWsSuffix = t.getTextWithWsSuffix();
                                if (knownEntitiesStringSet.contains(termText)
                                    || knownEntitiesStringSet.contains(termTextWithWsSuffix)) {
                                    if (t.getWords().size() > 1) {
                                        for (Word w : t.getWords()) {
                                            sentenceFileWriter
                                                .println(w.getText() + "\t" + m_tagValueModel.getStringValue());
                                        }
                                    } else {
                                        sentenceFileWriter.println(termText + "\t" + m_tagValueModel.getStringValue());
                                    }
                                } else if (!knownEntitiesStringSet.contains(termText)
                                    || !knownEntitiesStringSet.contains(termTextWithWsSuffix)) {
                                    sentenceFileWriter.println(termText + "\tO");
                                }
                            }
                        }
                    } else {
                        missingValueCounter++;
                    }
                }
                if (counter == 0) {
                    setWarningMessage("Node created an empty model.");
                }
            }
        }

        sentenceFileWriter.close();

        exec.setProgress(0.75, "Learning model.");

        Properties props = new StanfordNlpNeLearnerPropFileGenerator(m_annotatedDocPath,
            m_useClassFeature.getBooleanValue(), m_useWord.getBooleanValue(), m_useNGrams.getBooleanValue(),
            m_noMidNGrams.getBooleanValue(), m_maxNGramLeng.getIntValue(), m_usePrev.getBooleanValue(),
            m_useNext.getBooleanValue(), m_useDisjunctive.getBooleanValue(), m_useSequences.getBooleanValue(),
            m_usePrevSequences.getBooleanValue(), m_maxLeft.getIntValue(), m_useTypeSeqs.getBooleanValue(),
            m_useTypeSeqs2.getBooleanValue(), m_useTypeySequences.getBooleanValue(), m_wordShape.getStringValue())
                .getPropFile();
        SeqClassifierFlags flags = new SeqClassifierFlags(props);
        CRFClassifier<CoreLabel> crf = new CRFClassifier<CoreLabel>(flags);
        crf.train();
        crf.serializeClassifier(m_modelPath);

        File outputModel = new File(m_modelPath);
        byte[] modelOutputBuffer = Files.toByteArray(outputModel);

        outputModel.delete();
        m_annotatedDocFile.delete();

        if (missingValueCounter == 1) {
            setWarningMessage(missingValueCounter + " row has been ignored due to missing value.");
        } else if (missingValueCounter > 1) {
            setWarningMessage(missingValueCounter + " rows have been ignored due to missing values.");
        }

        return new PortObject[]{new StanfordNERModelPortObject(modelOutputBuffer, m_tag, knownEntitiesStringSet,
            m_tokenizer.getStringValue())};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_tagValueModel.saveSettingsTo(settings);
        m_tagTypeModel.saveSettingsTo(settings);
        m_docColumnModel.saveSettingsTo(settings);
        m_knownEntitiesColumnModel.saveSettingsTo(settings);
        m_useClassFeature.saveSettingsTo(settings);
        m_useWord.saveSettingsTo(settings);
        m_useNGrams.saveSettingsTo(settings);
        m_noMidNGrams.saveSettingsTo(settings);
        m_usePrev.saveSettingsTo(settings);
        m_useNext.saveSettingsTo(settings);
        m_useSequences.saveSettingsTo(settings);
        m_usePrevSequences.saveSettingsTo(settings);
        m_useTypeSeqs.saveSettingsTo(settings);
        m_useTypeSeqs2.saveSettingsTo(settings);
        m_useTypeySequences.saveSettingsTo(settings);
        m_useDisjunctive.saveSettingsTo(settings);
        m_wordShape.saveSettingsTo(settings);
        m_maxLeft.saveSettingsTo(settings);
        m_maxNGramLeng.saveSettingsTo(settings);
        m_tokenizer.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_tagValueModel.validateSettings(settings);
        m_tagTypeModel.validateSettings(settings);
        m_docColumnModel.validateSettings(settings);
        m_knownEntitiesColumnModel.validateSettings(settings);
        m_useClassFeature.validateSettings(settings);
        m_useWord.validateSettings(settings);
        m_useNGrams.validateSettings(settings);
        m_noMidNGrams.validateSettings(settings);
        m_usePrev.validateSettings(settings);
        m_useNext.validateSettings(settings);
        m_useSequences.validateSettings(settings);
        m_usePrevSequences.validateSettings(settings);
        m_useTypeSeqs.validateSettings(settings);
        m_useTypeSeqs2.validateSettings(settings);
        m_useTypeySequences.validateSettings(settings);
        m_useDisjunctive.validateSettings(settings);
        m_wordShape.validateSettings(settings);
        m_maxLeft.validateSettings(settings);
        m_maxNGramLeng.validateSettings(settings);

        // only validate settings if settings contain SettingsModel key (for backwards compatibility)
        if (settings.containsKey(m_tokenizer.getKey())) {
            m_tokenizer.validateSettings(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_tagValueModel.loadSettingsFrom(settings);
        m_tagTypeModel.loadSettingsFrom(settings);
        m_docColumnModel.loadSettingsFrom(settings);
        m_knownEntitiesColumnModel.loadSettingsFrom(settings);
        m_useClassFeature.loadSettingsFrom(settings);
        m_useWord.loadSettingsFrom(settings);
        m_useNGrams.loadSettingsFrom(settings);
        m_noMidNGrams.loadSettingsFrom(settings);
        m_usePrev.loadSettingsFrom(settings);
        m_useNext.loadSettingsFrom(settings);
        m_useSequences.loadSettingsFrom(settings);
        m_usePrevSequences.loadSettingsFrom(settings);
        m_useTypeSeqs.loadSettingsFrom(settings);
        m_useTypeSeqs2.loadSettingsFrom(settings);
        m_useTypeySequences.loadSettingsFrom(settings);
        m_useDisjunctive.loadSettingsFrom(settings);
        m_wordShape.loadSettingsFrom(settings);
        m_maxLeft.loadSettingsFrom(settings);
        m_maxNGramLeng.loadSettingsFrom(settings);


        // only load settings if settings contain SettingsModel key (for backwards compatibility)
        if (settings.containsKey(m_tokenizer.getKey())) {
            m_tokenizer.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub
    }

}

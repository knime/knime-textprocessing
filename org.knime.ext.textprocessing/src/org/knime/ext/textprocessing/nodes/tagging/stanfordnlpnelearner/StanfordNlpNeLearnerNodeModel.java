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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

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
import org.knime.ext.textprocessing.nodes.tagging.dict.wildcard.MultiTermRegexDocumentTagger;
import org.knime.ext.textprocessing.nodes.tokenization.MissingTokenizerException;
import org.knime.ext.textprocessing.nodes.tokenization.TokenizerFactoryRegistry;
import org.knime.ext.textprocessing.preferences.TextprocessingPreferenceInitializer;
import org.knime.ext.textprocessing.util.ColumnSelectionVerifier;
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;

/**
 * The {@code NodeModel} for the StanfordNLP NE Learner node.
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeLearnerNodeModel extends NodeModel {

    private final SettingsModelString m_docColumnModel = StanfordNlpNeLearnerNodeDialog.createDocumentColumnModel();

    private final SettingsModelString m_knownEntitiesColumnModel =
        StanfordNlpNeLearnerNodeDialog.createKnownEntitiesColumnModel();

    private final SettingsModelString m_tagValueModel = StanfordNlpNeLearnerNodeDialog.createTagValueModel();

    private final SettingsModelString m_tagTypeModel = StanfordNlpNeLearnerNodeDialog.createTagTypeModel();

    // Properties settings models

    private final SettingsModelBoolean m_useClassFeature = StanfordNlpNeLearnerNodeDialog.createUseClassFeatureModel();

    private final SettingsModelBoolean m_useWord = StanfordNlpNeLearnerNodeDialog.createUseWordModel();

    private final SettingsModelBoolean m_useNGrams = StanfordNlpNeLearnerNodeDialog.createUseNGramsModel();

    private final SettingsModelBoolean m_noMidNGrams = StanfordNlpNeLearnerNodeDialog.createNoMidNGramsModel();

    private final SettingsModelBoolean m_usePrev = StanfordNlpNeLearnerNodeDialog.createUsePrevModel();

    private final SettingsModelBoolean m_useNext = StanfordNlpNeLearnerNodeDialog.createUseNextModel();

    private final SettingsModelBoolean m_useSequences = StanfordNlpNeLearnerNodeDialog.createUseSequencesModel();

    private final SettingsModelBoolean m_usePrevSequences =
        StanfordNlpNeLearnerNodeDialog.createUsePrevSequencesModel();

    private final SettingsModelBoolean m_useTypeSeqs = StanfordNlpNeLearnerNodeDialog.createUseTypeSeqsModel();

    private final SettingsModelBoolean m_useTypeSeqs2 = StanfordNlpNeLearnerNodeDialog.createUseTypeSeqs2Model();

    private final SettingsModelBoolean m_useTypeySequences = StanfordNlpNeLearnerNodeDialog.createUseTypeYSeqsModel();

    private final SettingsModelBoolean m_useDisjunctive = StanfordNlpNeLearnerNodeDialog.createUseDisjunctiveModel();

    private final SettingsModelString m_wordShape = StanfordNlpNeLearnerNodeDialog.createWordShapeModel();

    private final SettingsModelIntegerBounded m_maxLeft = StanfordNlpNeLearnerNodeDialog.createMaxLeftModel();

    private final SettingsModelIntegerBounded m_maxNGramLeng =
        StanfordNlpNeLearnerNodeDialog.createMaxNGramLengthModel();

    private final SettingsModelString m_tokenizer = StanfordNlpNeLearnerNodeDialog.createTokenizerModel();

    private final SettingsModelBoolean m_caseSensitivity = StanfordNlpNeLearnerNodeDialog.createCaseSensitivityModel();

    /**
     * Creates a new instance of {@code StanfordNlpNeLearnerNodeModel}.
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
        final DataTableSpec spec = (DataTableSpec)inSpecs[0];
        final DataTableSpec spec2 = (DataTableSpec)inSpecs[1];
        final DataTableSpecVerifier verifier = new DataTableSpecVerifier(spec);
        final DataTableSpecVerifier verifier2 = new DataTableSpecVerifier((DataTableSpec)inSpecs[1]);
        verifier.verifyMinimumDocumentCells(1, true);
        verifier2.verifyMinimumStringCells(1, true);

        // verifying the selected columns or select the first suitable column if none has been set yet and throw warning
        // if present
        ColumnSelectionVerifier.verifyColumn(m_docColumnModel, spec, DocumentValue.class, null)
            .ifPresent(a -> setWarningMessage(a));

        // verifying the selected string column or select the first suitable column if none has been set yet
        ColumnSelectionVerifier.verifyColumn(m_knownEntitiesColumnModel, spec2, StringValue.class, null)
            .ifPresent(a -> setWarningMessage(a));

        // check tokenizer settings from incoming document column
        final DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(spec);
        final String tokenizerFromInput =
            dataTableSpecVerifier.getTokenizerFromInputDocCol(spec.findColumnIndex(m_docColumnModel.getStringValue()));
        if (m_tokenizer.getStringValue().isEmpty()) {
            if (tokenizerFromInput != null) {
                m_tokenizer.setStringValue(tokenizerFromInput);
                setWarningMessage("Auto select: Using  '" + m_tokenizer.getStringValue()
                    + "' as word tokenizer based on incoming documents.");
            } else {
                m_tokenizer.setStringValue(TextprocessingPreferenceInitializer.tokenizerName());
            }
        }
        if (!dataTableSpecVerifier.verifyTokenizer(m_tokenizer.getStringValue())) {
            setWarningMessage(dataTableSpecVerifier.getTokenizerWarningMsg());
        }

        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizer.getStringValue())) {
            throw new MissingTokenizerException(m_tokenizer.getStringValue());
        }

        return new PortObjectSpec[]{new NERModelPortObjectSpec(m_tokenizer.getStringValue())};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] data, final ExecutionContext exec) throws Exception {
        // check input data
        assert ((data != null) && (data[0] != null));

        // get data table
        final BufferedDataTable docTable = (BufferedDataTable)data[0];

        // get dictionary as string and regex pattern
        final Set<String> knownEntitiesStringSet = getDictionary((BufferedDataTable)data[1]);
        final Set<Pattern> knownEntitiesPatternSet = knownEntitiesStringSet.stream()//
            .map(s -> Pattern.compile(s))//
            .collect(Collectors.toSet());

        // create tag for document tagger
        final Tag tag = new Tag(m_tagValueModel.getStringValue(), m_tagTypeModel.getStringValue());

        // create tagger based on known entities
        final MultiTermRegexDocumentTagger tagger = new MultiTermRegexDocumentTagger(true, knownEntitiesPatternSet, tag,
            m_caseSensitivity.getBooleanValue(), m_tokenizer.getStringValue());

        // create UUID to add them to the file path to avoid cases..
        // .. where two instances of the node model used the same file path at the same time
        final String tempDir = KNIMEConstants.getKNIMETempDir() + "/";
        final String annotatedDocPath = tempDir + "aD-" + UUID.randomUUID().toString() + ".tsv";

        // create files based on sentence list and known entities
        final File annotatedDocFile = new File(annotatedDocPath);
        int rowCounter = 0;
        int missingValueCounter = 0;
        try (final PrintWriter sentenceFileWriter = new PrintWriter(annotatedDocFile, "UTF-8")) {
            final int colIndex = docTable.getDataTableSpec().findColumnIndex(m_docColumnModel.getStringValue());
            // tag documents and transform sentences to strings while tagged terms get stanfordnlp annotation
            for (final DataRow row : docTable) {
                //set progress bar
                rowCounter++;
                final double progress = (rowCounter / (double)docTable.size()) / (2.0);
                exec.setProgress(progress, "Preparing documents");

                if (!row.getCell(colIndex).isMissing()
                    && row.getCell(colIndex).getType().isCompatible(DocumentValue.class)) {
                    final Document doc = ((DocumentValue)row.getCell(colIndex)).getDocument();
                    final Document taggedDoc = tagger.tag(doc);
                    taggedDoc.sentenceIterator()
                        .forEachRemaining(s -> writeAnnotationData(s, knownEntitiesStringSet, sentenceFileWriter));
                    sentenceFileWriter.println();
                } else {
                    missingValueCounter++;
                }
            }
        }

        // train model
        exec.setProgress(0.75, "Learning model.");

        final Properties props = new StanfordNlpNeLearnerPropFileGenerator(annotatedDocPath,
            m_useClassFeature.getBooleanValue(), m_useWord.getBooleanValue(), m_useNGrams.getBooleanValue(),
            m_noMidNGrams.getBooleanValue(), m_maxNGramLeng.getIntValue(), m_usePrev.getBooleanValue(),
            m_useNext.getBooleanValue(), m_useDisjunctive.getBooleanValue(), m_useSequences.getBooleanValue(),
            m_usePrevSequences.getBooleanValue(), m_maxLeft.getIntValue(), m_useTypeSeqs.getBooleanValue(),
            m_useTypeSeqs2.getBooleanValue(), m_useTypeySequences.getBooleanValue(), m_wordShape.getStringValue())
                .getPropFile();
        final SeqClassifierFlags flags = new SeqClassifierFlags(props);
        final CRFClassifier<CoreLabel> crf = new CRFClassifier<>(flags);
        try {
            crf.train();
        } finally {
            java.nio.file.Files.delete(annotatedDocFile.toPath());
        }

        // Serialize model to byte array
        byte[] modelOutputBuffer;
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (final ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(baos))) {
                crf.serializeClassifier(oos);
            }
            modelOutputBuffer = baos.toByteArray();
        }

        // set warning messages if necessary
        if (knownEntitiesPatternSet.isEmpty()) {
            setWarningMessage("Trained model on empty dictionary.");
        } else if (rowCounter == 0) {
            setWarningMessage("Node created an empty model.");
        } else if (missingValueCounter == 1) {
            setWarningMessage(missingValueCounter + " row has been ignored due to missing value.");
        } else if (missingValueCounter > 1) {
            setWarningMessage(missingValueCounter + " rows have been ignored due to missing values.");
        }

        return new PortObject[]{new StanfordNERModelPortObject(modelOutputBuffer, tag, knownEntitiesStringSet,
            m_tokenizer.getStringValue())};
    }

    /**
     * Writes annotation data for each term of sentence to a specified {@link PrintWriter}.
     * Terms contained in the known entities set are annotated with the specified tag value.
     * Other terms are annotated with 'O' meaning that the term could not be found in the known entities set.
     *
     * @param sentence The {@code sentence}.
     * @param knownEntitiesStringSet The set of known entities from the dictionary.
     * @param sentenceFileWriter The {@code PrintWriter} to write the terms to.
     */
    private final void writeAnnotationData(final Sentence sentence, final Set<String> knownEntitiesStringSet,
        final PrintWriter sentenceFileWriter) {
        final Iterator<Term> termIterator = sentence.getTerms().iterator();
        while (termIterator.hasNext()) {
            final Term t = termIterator.next();
            final String termText = t.getText();
            final String termTextWithWsSuffix = t.getTextWithWsSuffix();
            if (knownEntitiesStringSet.contains(m_caseSensitivity.getBooleanValue() ? termText : termText.toLowerCase())
                || knownEntitiesStringSet.contains(
                    m_caseSensitivity.getBooleanValue() ? termTextWithWsSuffix : termTextWithWsSuffix.toLowerCase())) {
                t.getWords().stream()//
                    .forEach(w -> sentenceFileWriter.println(w.getText() + "\t" + m_tagValueModel.getStringValue()));
            } else {
                sentenceFileWriter.println(termText + "\tO");
            }
        }
    }

    /**
     * Creates a {@link Set} of known entities from the specified dictionary column.
     *
     * @param dataTable The data table.
     */
    private final Set<String> getDictionary(final BufferedDataTable dataTable) {
        final Set<String> knownEntitiesStringSet = new LinkedHashSet<>();
        final int colIndex = dataTable.getDataTableSpec().findColumnIndex(m_knownEntitiesColumnModel.getStringValue());
        if (colIndex < 0) {
            return knownEntitiesStringSet;
        }
        for (final DataRow row : dataTable) {
            if (!row.getCell(colIndex).isMissing()) {
                // add every known entity to the specified array list
                final String cellText = m_caseSensitivity.getBooleanValue() ? row.getCell(colIndex).toString()
                    : row.getCell(colIndex).toString().toLowerCase();
                if (!cellText.trim().isEmpty()) {
                    knownEntitiesStringSet.add(cellText);
                }
            }
        }
        return knownEntitiesStringSet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // Nothing to do here...
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
        m_caseSensitivity.saveSettingsTo(settings);
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
        if (settings.containsKey(m_caseSensitivity.getConfigName())) {
            m_caseSensitivity.validateSettings(settings);
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
        if (settings.containsKey(m_caseSensitivity.getConfigName())) {
            m_caseSensitivity.loadSettingsFrom(settings);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // Nothing to do here...
    }

}

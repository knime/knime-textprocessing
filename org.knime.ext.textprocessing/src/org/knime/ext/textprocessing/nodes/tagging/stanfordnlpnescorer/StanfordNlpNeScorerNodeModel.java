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
 *   01.09.2016 (Julian): created
 */
package org.knime.ext.textprocessing.nodes.tagging.stanfordnlpnescorer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.KNIMEConstants;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
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
import org.knime.ext.textprocessing.util.DataTableSpecVerifier;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;

/**
 *
 * @author Julian Bunzel, KNIME.com, Berlin, Germany
 */
public class StanfordNlpNeScorerNodeModel extends NodeModel {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(StanfordNlpNeScorerNodeModel.class);

    private SettingsModelString m_docColumnModel = StanfordNlpNeScorerNodeDialog.createDocumentColumnModel();

    private String m_docColumnName;

    private Set<String> m_usedDict;

    private Tag m_tag;

    private StanfordNERModelPortObject m_inputModelPortObject;

    private CRFClassifier<CoreLabel> m_inputModel;

    private String m_tokenizerName;

    /**
     * An array of DataColumnSpecs for the scores table.
     */
    private static final DataColumnSpec[] QUALITY_MEASURES_SPECS =
        new DataColumnSpec[]{new DataColumnSpecCreator("Precision", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("Recall", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("F1", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("TP", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("FP", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("FN", IntCell.TYPE).createSpec()};

    /**
     * The constructor for the {@code StanfordNlpNeScorerNodeModel}.
     */
    public StanfordNlpNeScorerNodeModel() {
        super(
            new PortType[]{BufferedDataTable.TYPE,
                PortTypeRegistry.getInstance().getPortType(StanfordNERModelPortObject.class, false)},
            new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        DataTableSpec spec = (DataTableSpec)inSpecs[0];
        NERModelPortObjectSpec modelSpec = (NERModelPortObjectSpec)inSpecs[1];
        // guessing the document column
        int colIndex = spec.findColumnIndex(m_docColumnModel.getStringValue());
        if (colIndex < 0) {
            for (int i = 0; i < spec.getNumColumns(); i++) {
                if (spec.getColumnSpec(i).getType().isCompatible(DocumentValue.class)) {
                    colIndex = i;
                    m_docColumnName = spec.getColumnSpec(i).getName();
                    break;
                }
            }
        } else if (colIndex >= 0) {
            m_docColumnName = m_docColumnModel.getStringValue();
        }

        // check if specific tokenizer is installed
        if (!TokenizerFactoryRegistry.getTokenizerFactoryMap().containsKey(m_tokenizerName)) {
            throw new MissingTokenizerException(m_tokenizerName);
        }

        //check tokenizer settings from incoming document column
        DataTableSpecVerifier dataTableSpecVerifier = new DataTableSpecVerifier(spec);
        if (!dataTableSpecVerifier.verifyTokenizer(colIndex, modelSpec.getTokenizerName())) {
            setWarningMessage(
                "Tokenization of input documents (" + dataTableSpecVerifier.getTokenizerFromInputDocCol(colIndex)
                    + ") differs to tokenizer used in learner node (" + modelSpec.getTokenizerName() + ").");
        }

        return new DataTableSpec[]{new DataTableSpec(QUALITY_MEASURES_SPECS)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {

        m_inputModelPortObject = (StanfordNERModelPortObject)inObjects[1];
        m_inputModel = m_inputModelPortObject.getNERModel();
        m_usedDict = m_inputModelPortObject.getDictSet();
        m_tag = m_inputModelPortObject.getTag();
        m_tokenizerName = m_inputModelPortObject.getTokenizerName();

        //create a BufferedDataContainer for the scoring values
        BufferedDataContainer accTable = exec.createDataContainer(new DataTableSpec(QUALITY_MEASURES_SPECS));

        // build pattern set from dictionary
        DataTableSpec docTableSpec = (DataTableSpec)inObjects[0].getSpec();
        BufferedDataTable docDataInput = (BufferedDataTable)inObjects[0];
        Set<Pattern> knownEntitiesPatternSet = new LinkedHashSet<Pattern>();
        for (String word : m_usedDict) {
            knownEntitiesPatternSet.add(Pattern.compile(word));
        }

        // create dictionary tagger to tag the input documents with the dictionary used for building the model
        MultiTermRegexDocumentTagger tagger =
            new MultiTermRegexDocumentTagger(true, knownEntitiesPatternSet, m_tag, true, m_tokenizerName);

        // create UUID to add them to the file path to avoid cases where two instances of the node model used the same file path at the same time
        String tempDir = KNIMEConstants.getKNIMETempDir() + "/";
        String m_annotatedTestFilePath = tempDir + "aD-" + UUID.randomUUID().toString() + ".tsv";

        // create the annotated test file
        File m_annotatedTestFile = new File(m_annotatedTestFilePath);
        PrintWriter sentenceFileWriter = new PrintWriter(m_annotatedTestFile, "UTF-8");

        // tag documents and transform sentences to strings while tagged terms get StanfordNLP annotation
        // iterate through columns
        for (int i = 0; i < docTableSpec.getNumColumns(); i++) {
            // iterate through rows if column with correct name has been found
            if (docTableSpec.getColumnSpec(i).getName().equals(m_docColumnName)) {
                int counter = 0;
                for (DataRow row : docDataInput) {
                    //set progress bar
                    counter++;
                    double progress = (counter / (double)docDataInput.size()) / (3.0);
                    exec.setProgress(progress, "Preparing documents for validation");
                    exec.checkCanceled();

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
                            if (m_usedDict.contains(termText) || m_usedDict.contains(termTextWithWsSuffix)) {
                                sentenceFileWriter.println(termText + "\t" + m_tag.getTagValue());
                            } else if (!m_usedDict.contains(termText) || !m_usedDict.contains(termTextWithWsSuffix)) {
                                sentenceFileWriter.println(termText + "\tO");
                            }
                        }
                    }
                }
            }
        }

        sentenceFileWriter.close();

        exec.setProgress(0.5, "Validate model");
        // we need to catch the System.err output, because unfortunately there is no method to get the scores
        // maybe countResults method works, but at the moment this is fine
        // Create a stream to hold the output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream errOutput = new PrintStream(baos);
        // save the old System.err
        PrintStream oldErr = System.err;
        // set the err output stream to own output stream
        System.setErr(errOutput);
        // classify the documents with our model
        DocumentReaderAndWriter<CoreLabel> raw = m_inputModel.makeReaderAndWriter();
        m_inputModel.classifyAndWriteAnswers(m_annotatedTestFilePath, raw, true);
        // set the err output back to standard output stream
        System.err.flush();
        System.setErr(oldErr);

        DataRow stats = new DefaultRow(new RowKey("Row0"),
            new DataCell[]{DataType.getMissingCell(), DataType.getMissingCell(), DataType.getMissingCell(),
                DataType.getMissingCell(), DataType.getMissingCell(), DataType.getMissingCell()});

        try {
            // get values from output stream
            boolean canParseModelQuality = false;
            String caughtScores = baos.toString();
            String totalScores[] = caughtScores.split("Totals\t");
            if (totalScores.length >= 2) {
                String[] scores = totalScores[1].split("\t");
                if (scores.length >= 6) {
                    Locale loc = Locale.getDefault();
                    NumberFormat format = NumberFormat.getInstance(loc);
                    Double precision = format.parse(scores[0]).doubleValue();
                    Double recall = format.parse(scores[1]).doubleValue();
                    Double f1 = format.parse(scores[2]).doubleValue();
                    int tp = Integer.parseInt(scores[3]);
                    int fp = Integer.parseInt(scores[4]);
                    int fn = Integer.parseInt(scores[5].split("\r")[0]);
                    // create the scores row and add it to the BufferedDataContainer we created in the beginning
                    stats = new DefaultRow(new RowKey("Row0"), new DataCell[]{new DoubleCell(precision),
                        new DoubleCell(recall), new DoubleCell(f1), new IntCell(tp), new IntCell(fp), new IntCell(fn)});
                    canParseModelQuality = true;
                }
            }

            if (!canParseModelQuality) {
                LOGGER.error("Could not read quality measures of model validation.");
            }
        } catch (NumberFormatException e) {
            LOGGER.error("Could not parse quality measures of model validation.");
        }
        accTable.addRowToTable(stats);

        m_annotatedTestFile.delete();

        accTable.close();

        return new BufferedDataTable[]{accTable.getTable()};
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
        m_docColumnModel.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColumnModel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_docColumnModel.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Auto-generated method stub

    }

}

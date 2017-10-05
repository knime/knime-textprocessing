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
 * -------------------------------------------------------------------
 */

package org.knime.ext.textprocessing.nodes.cooccurrencecounter;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.knime.base.data.sort.SortedTable;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
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
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.util.MutableInteger;
import org.knime.core.util.ThreadPool;
import org.knime.ext.textprocessing.data.Document;
import org.knime.ext.textprocessing.data.DocumentCell;
import org.knime.ext.textprocessing.data.DocumentValue;
import org.knime.ext.textprocessing.data.Paragraph;
import org.knime.ext.textprocessing.data.Section;
import org.knime.ext.textprocessing.data.SectionAnnotation;
import org.knime.ext.textprocessing.data.Sentence;
import org.knime.ext.textprocessing.data.Term;
import org.knime.ext.textprocessing.data.TermValue;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactory;
import org.knime.ext.textprocessing.util.TextContainerDataCellFactoryBuilder;


/**
 * Node model class.
 * @author Tobias Koetter, University of Konstanz
 */
public class CooccurrenceCounterNodeModel extends NodeModel {

    private final SettingsModelString m_docCol = createDocColModel();

    private final SettingsModelString m_termCol = createTermColModel();

    private final SettingsModelBoolean m_sort = createSortModel();

    private final SettingsModelBoolean m_checkTags = createCheckTagsModel();

    private final SettingsModelBoolean m_skipMetaInfo = createSkipMetaInfoSection();

    private final SettingsModelString m_coocLevel = createCoocLevelModel();

    private final SettingsModelInteger m_procCount = createProcessCountModel();

    private TextContainerDataCellFactory m_termFac = TextContainerDataCellFactoryBuilder.createTermCellFactory();

    /**Constructor for class CooccurrenceCounterNodeModel.
     *
     */
    public CooccurrenceCounterNodeModel() {
        super(1, 1);
    }

    /**
     * @return the co-occurrence level model
     */
    static SettingsModelString createCoocLevelModel() {
        return new SettingsModelString("inclNeighbors", CooccurrenceLevel.getDefault().getActionCommand());
    }

    /**
     * @return the check term tags model
     */
    static SettingsModelBoolean createCheckTagsModel() {
        return new SettingsModelBoolean("checkTags", true);
    }

    /**
     * @return the check term tags model
     */
    static SettingsModelBoolean createSkipMetaInfoSection() {
        return new SettingsModelBoolean("skipMetaInfoSection", false);
    }

    /**
     * @return the number of processes model
     */
    static SettingsModelInteger createProcessCountModel() {
        return new SettingsModelIntegerBounded("noOfThreads", KNIMEConstants.GLOBAL_THREAD_POOL.getMaxThreads(), 1,
                                                Integer.MAX_VALUE);
    }

    /**
     * @return the sort input table model
     */
    static SettingsModelBoolean createSortModel() {
        return new SettingsModelBoolean("sortInputTable", true);
    }

    /**
     * @return the document column settings model
     */
    static SettingsModelString createDocColModel() {
        return new SettingsModelString("documentColumn", null);
    }

    /**
     * @return the term column settings model
     */
    static SettingsModelString createTermColModel() {
        return new SettingsModelString("termColumn", null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
        final DataTableSpec spec = inSpecs[0];
        if (m_docCol.getStringValue() == null && m_termCol.getStringValue() == null) {
            //preselect the first matching column
            m_docCol.setStringValue(findCompatibleColumn(spec, DocumentValue.class));
            if (m_docCol.getStringValue() == null) {
                throw new InvalidSettingsException("Input table contains no document column");
            }
            m_termCol.setStringValue(findCompatibleColumn(spec, TermValue.class));
            if (m_termCol.getStringValue() == null) {
                throw new InvalidSettingsException( "Input table contains no term column");
            }
        }
        //check that the table contains the selected columns
        if (!spec.containsName(m_docCol.getStringValue())) {
            throw new InvalidSettingsException("Input table does not contain document column "
                    + m_docCol.getStringValue());
        }
        if (!spec.containsName(m_termCol.getStringValue())) {
            throw new InvalidSettingsException("Input table does not contain term column "
        + m_termCol.getStringValue());
        }
        return new DataTableSpec[] {createResultSpec(spec)};
    }

    private static String findCompatibleColumn(final DataTableSpec spec,
            final Class<? extends DataValue> valueClass) {
        for (final DataColumnSpec colSpec : spec) {
            if (colSpec.getType().isCompatible(valueClass)) {
                return colSpec.getName();
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
            throws Exception {
        final DataTableSpec spec = inData[0].getDataTableSpec();
        final int docIdx = spec.findColumnIndex(m_docCol.getStringValue());
        final int termIdx = spec.findColumnIndex(m_termCol.getStringValue());
        final int rowCount = inData[0].getRowCount();
        DataTable table;
        ExecutionContext myExec;
        if (m_sort.getBooleanValue()) {
            exec.setMessage("Sorting table...");
            final LinkedList<String> inclList = new LinkedList<String>();
            inclList.add(m_docCol.getStringValue());
            table = new SortedTable(inData[0], inclList, new boolean[] {true}, exec.createSubExecutionContext(0.2));
            myExec = exec.createSubExecutionContext(0.8);
        } else {
            table = inData[0];
            myExec = exec;
        }
        final BufferedDataContainer dc = exec.createDataContainer(createResultSpec(spec));
        DataCell previousDocCell = null;
        final boolean checkTags = m_checkTags.getBooleanValue();
        final boolean skipMetaInfo = m_skipMetaInfo.getBooleanValue();
        TermChecker terms = new TermChecker(checkTags);
        int docRowCounter = 0;
        int totalRowCounter = 0;
        final AtomicInteger rowId = new AtomicInteger();
        final AtomicInteger progressCounter = new AtomicInteger();
        exec.setMessage("Processing documents...");
      //initialize the thread pool
        final ThreadPool pool = KNIMEConstants.GLOBAL_THREAD_POOL.createSubPool();
        //The semaphore restricts the number of concurrent processes
        final Semaphore semaphore = new Semaphore(m_procCount.getIntValue());
        for (final DataRow row : table) {
            docRowCounter++;
            totalRowCounter++;
            myExec.setMessage("Reading row " + totalRowCounter + " of " + rowCount);
            exec.checkCanceled();
            final DataCell docCell = row.getCell(docIdx);
            if (docCell.isMissing()) {
                //skip missing document cells
                continue;
            }
            final DataCell termCell = row.getCell(termIdx);
            if (termCell.isMissing()) {
                //skip missing term cells
                continue;
            }
            final Term term = ((TermValue)termCell).getTermValue();
            if (previousDocCell == null) {
                previousDocCell = docCell;
            }
            if (previousDocCell.equals(docCell)) {
                terms.addTerm(term);
            } else {
                pool.enqueue(processDocument(myExec, rowCount, progressCounter, docRowCounter, semaphore, rowId, dc,
                                                previousDocCell, terms, skipMetaInfo, checkTags));
                previousDocCell = docCell;
                terms = new TermChecker(checkTags);
                terms.addTerm(term);
                docRowCounter = 0;
            }
        }
        //process the last document
        exec.setMessage("Processing documents...");
        pool.enqueue(processDocument(myExec, rowCount, progressCounter, docRowCounter, semaphore, rowId, dc,
                                        previousDocCell, terms, skipMetaInfo, checkTags));
        pool.waitForTermination();
        dc.close();
        return new BufferedDataTable[] {dc.getTable()};
    }

    private Runnable processDocument(final ExecutionMonitor exec,
            final int totalRowCount, final AtomicInteger progressCounter,
            final int docRowCounter, final Semaphore semaphore,
            final AtomicInteger rowId, final BufferedDataContainer dc,
            final DataCell docCell, final TermChecker terms, final boolean skipMetaInformation,
            final boolean checkTags)
    throws CanceledExecutionException {
        exec.checkCanceled();
        return new Runnable() {
            @Override
            public void run() {
                try {
                    semaphore.acquire();
                    final Document doc = ((DocumentValue)docCell).getDocument();
                    final HashMap<TermTuple, TermTuple> tuples = new LinkedHashMap<TermTuple, TermTuple>();
                    final List<Section> sections = doc.getSections();
                    final Map<TermContainer, MutableInteger> documentTerms =
                            new LinkedHashMap<TermContainer, MutableInteger>();
                    final Map<TermContainer, MutableInteger> titleTerms =
                        new LinkedHashMap<TermContainer, MutableInteger>();
                    final Map<TermContainer, MutableInteger> sectionTerms =
                        new LinkedHashMap<TermContainer, MutableInteger>();
                    final Map<TermContainer, MutableInteger> paragraphTerms =
                        new LinkedHashMap<TermContainer, MutableInteger>();
                    final Map<TermContainer, MutableInteger> sentenceTerms =
                        new LinkedHashMap<TermContainer, MutableInteger>();
                    for (final Section section : sections) {
                        final SectionAnnotation annotation = section.getAnnotation();
                        final boolean title = SectionAnnotation.TITLE.equals(annotation)
                                                || SectionAnnotation.CONFERENCE_TITLE.equals(annotation)
                                                    || SectionAnnotation.JOURNAL_TITLE.equals(annotation);
                        if (skipMetaInformation && SectionAnnotation.META_INFORMATION.equals(annotation)) {
                            //this is a meta information section that should be skipped -> continue
                            continue;
                        }
                        final List<Paragraph> paragraphs = section.getParagraphs();
                        for (final Paragraph paragraph : paragraphs) {
                            final List<Sentence> sentences = paragraph.getSentences();
                            TermContainer previousTermContainer = null;
                            for (final Sentence sentence : sentences) {
                                exec.checkCanceled();
                                final List<Term> termList = sentence.getTerms();
                                for (final Term term : termList) {
                                    if (terms.containsTerm(term)) {
                                        final TermContainer termContainer = new TermContainer(checkTags, term);
                                        if (inclDoc()) {
                                            addTerm(documentTerms, termContainer);
                                        }
                                        if (inclSection()) {
                                            addTerm(sectionTerms, termContainer);
                                        }
                                        if (inclParagraph()) {
                                            addTerm(paragraphTerms, termContainer);
                                        }
                                        if (inclSentence()) {
                                            addTerm(sentenceTerms, termContainer);
                                        }
                                        //tread the title section extra
                                        if (title && inclTitle()) {
                                            addTerm(titleTerms, termContainer);
                                        }
                                        if (inclNeighbors()) {
                                            if (previousTermContainer != null) {
                                                //the two terms are neighbors
                                                processNeighbors(tuples, previousTermContainer,
                                                    termContainer);
                                            }
                                            previousTermContainer = termContainer;
                                        }
                                    } else {
                                        previousTermContainer = null;
                                    }
                                }
                                //process all terms that co-occur in this sentence
                                processTerms(tuples, sentenceTerms, CooccurrenceLevel.SENTENCE);
                                //count neighbors only within a sentence
                                previousTermContainer = null;
                            }
                            //process all terms that co-occur in this paragraph
                            processTerms(tuples, paragraphTerms, CooccurrenceLevel.PARAGRAPH);
                        }
                        if (title) {
                            //process all terms that co-occur in the title of this document
                            processTerms(tuples, titleTerms, CooccurrenceLevel.TITLE);
                        }
                        //process all terms that co-occur in this section
                        processTerms(tuples, sectionTerms, CooccurrenceLevel.SECTION);
                    }
                    //process all terms that co-occur in this document
                    processTerms(tuples, documentTerms, CooccurrenceLevel.DOCUMENT);
                    //create a data row for each tuple
                    createRows(exec, rowId, dc, docCell, tuples);
                    exec.setProgress(progressCounter.addAndGet(docRowCounter) / (double)totalRowCount);
                } catch (final CanceledExecutionException e) {
                    // this exception is handled outside of the thread
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                } finally {
                    semaphore.release();
                }
            }
        };
    }

    /**
     * @param containerMap the term container and their occurrence counter
     * @param termContainer the term to add to the map
     */
    void addTerm(final Map<TermContainer, MutableInteger> containerMap, final TermContainer termContainer) {
        MutableInteger counter = containerMap.get(termContainer);
        if (counter == null) {
            counter = new MutableInteger(0);
            containerMap.put(termContainer, counter);
        }
        counter.inc();
    }

    /**
     * @param tuples {@link Map} that holds all {@link TermTuple}s
     * @param terms the terms to add to the tuple map
     * @param occurrence the type of occurrence
     */
    void processTerms(final Map<TermTuple, TermTuple> tuples, final Map<TermContainer, MutableInteger> terms,
            final CooccurrenceLevel occurrence) {
        if (terms.size() < 2) {
            //the map might contain one element which we need to remove as well
            terms.clear();
            return;
        }
        for (final Entry<TermContainer, MutableInteger> outerTerm : terms.entrySet()) {
            boolean skip = true;
            for (final Entry<TermContainer, MutableInteger> innerTerm : terms.entrySet()) {
                if (skip) {
                    if (innerTerm.equals(outerTerm)) {
                        skip = false;
                    }
                    continue;
                }
                final TermTuple tuple = new TermTuple(outerTerm.getKey(), innerTerm.getKey());
                TermTuple termTuple = tuples.get(tuple);
                if (termTuple == null) {
                    tuples.put(tuple, tuple);
                    termTuple = tuple;
                }
                termTuple.inc(occurrence, outerTerm.getValue().intValue(), innerTerm.getValue().intValue());
            }
        }
        terms.clear();
    }

    /**
     * @param tuples the {@link TermTuple}s
     * @param termContainer1 the first term
     * @param termContainer2 the second term
     */
    void processNeighbors(final Map<TermTuple, TermTuple> tuples, final TermContainer termContainer1,
            final TermContainer termContainer2) {
        //ignore terms that are equal
        if (termContainer1.equals(termContainer2)) {
            return;
        }
        final TermTuple tuple = new TermTuple(termContainer1, termContainer2);
        TermTuple termTuple = tuples.get(tuple);
        if (termTuple == null) {
            tuples.put(tuple, tuple);
            termTuple = tuple;
        }
        termTuple.incNeighbors();
    }

    /**
     * @param exec provide progress and cancellation
     * @param rowId the {@link AtomicInteger} that holds the row id
     * @param dc the data container to use
     * @param docCell the {@link DocumentCell} that contains the given tuples
     * @param tuples the tuples that occurred in the given document
     * @throws CanceledExecutionException if the operation has been canceled
     */
    void createRows(final ExecutionMonitor exec,
            final AtomicInteger rowId, final BufferedDataContainer dc,
            final DataCell docCell, final HashMap<TermTuple, TermTuple> tuples)
    throws CanceledExecutionException {
        synchronized (dc) {
            //we synchronize the whole block to ensure that the tuples of a
            //document added consecutively to the table
            for (final TermTuple tuple : tuples.keySet()) {
                exec.checkCanceled();
                final List<DataCell> cells = new LinkedList<DataCell>();
                cells.add(docCell);
                if (m_checkTags.getBooleanValue()) {
                    cells.add(m_termFac.createDataCell(tuple.getTerm1()));
                    cells.add(m_termFac.createDataCell(tuple.getTerm2()));
                } else {
                    cells.add(new StringCell(tuple.getTerm1().getText()));
                    cells.add(new StringCell(tuple.getTerm2().getText()));
                }
                if (inclDoc()) {
                    cells.add(new IntCell(tuple.getDocument()));
                }
                if (inclSection()) {
                    cells.add(new IntCell(tuple.getSection()));
                }
                if (inclParagraph()) {
                    cells.add(new IntCell(tuple.getParagraph()));
                }
                if (inclSentence()) {
                    cells.add(new IntCell(tuple.getSentence()));
                }
                if (inclNeighbors()) {
                    cells.add(new IntCell(tuple.getNeighbor()));
                }
                if (inclTitle()) {
                    cells.add(new IntCell(tuple.getTitle()));
                }
                final DefaultRow row = new DefaultRow(
                        RowKey.createRowKey(rowId.getAndIncrement()), cells);
                dc.addRowToTable(row);
            }
        }
    }

    private DataTableSpec createResultSpec(final DataTableSpec spec) {
        final List<DataColumnSpec> specs = new LinkedList<DataColumnSpec>();
        specs.add(spec.getColumnSpec(m_docCol.getStringValue()));
        final DataColumnSpecCreator creator;
        if (m_checkTags.getBooleanValue()) {
            creator = new DataColumnSpecCreator(
                    spec.getColumnSpec(m_termCol.getStringValue()));
        } else {
            creator = new DataColumnSpecCreator("Dummy", StringCell.TYPE);
        }
        creator.setName("Term1");
        specs.add(creator.createSpec());
        creator.setName("Term2");
        specs.add(creator.createSpec());
        creator.setType(IntCell.TYPE);
        if (inclDoc()) {
            creator.setName("Document cooccurrence");
            specs.add(creator.createSpec());
        }
        if (inclSection()) {
            creator.setName("Section cooccurrence");
            specs.add(creator.createSpec());
        }
        if (inclParagraph()) {
            creator.setName("Paragraph cooccurrence");
            specs.add(creator.createSpec());
        }
        if (inclSentence()) {
            creator.setName("Sentence cooccurrence");
            specs.add(creator.createSpec());
        }
        if (inclNeighbors()) {
            creator.setName("Neighbor count");
            specs.add(creator.createSpec());
        }
        if (inclTitle()) {
            creator.setName("Title cooccurrence");
            specs.add(creator.createSpec());
        }
        return new DataTableSpec(specs.toArray(new DataColumnSpec[0]));
    }

    /**
     * @return <code>true</code> if document co-occurrence should be calculated
     */
    boolean inclDoc() {
        return includes(CooccurrenceLevel.DOCUMENT);
    }

    /**
     * @return <code>true</code> if paragraph co-occurrence should be calculated
     */
    boolean inclParagraph() {
        return includes(CooccurrenceLevel.PARAGRAPH);
    }

    /**
     * @return <code>true</code> if section co-occurrence should be calculated
     */
    boolean inclSection() {
        return includes(CooccurrenceLevel.SECTION);
    }

    /**
     * @return <code>true</code> if sentence co-occurrence should be calculated
     */
    boolean inclSentence() {
        return includes(CooccurrenceLevel.SENTENCE);
    }

    /**
     * @return <code>true</code> if title co-occurrence should be calculated
     */
    boolean inclTitle() {
        return includes(CooccurrenceLevel.TITLE);
    }

    /**
     * @return <code>true</code> if sentence co-occurrence should be calculated
     */
    boolean inclNeighbors() {
        return includes(CooccurrenceLevel.NEIGHBOR);
    }

    /**
     * @param level the {@link CooccurrenceLevel} to check for
     * @return <code>true</code> if the user selected {@link CooccurrenceLevel}
     * includes the given level
     */
    private boolean includes(final CooccurrenceLevel level) {
        final CooccurrenceLevel selectedLevel = CooccurrenceLevel.getCooccurrenceLevel(m_coocLevel.getStringValue());
        return level.getLevel() <= selectedLevel.getLevel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_docCol.saveSettingsTo(settings);
        m_termCol.saveSettingsTo(settings);
        m_sort.saveSettingsTo(settings);
        m_procCount.saveSettingsTo(settings);
        m_checkTags.saveSettingsTo(settings);
        m_coocLevel.saveSettingsTo(settings);
        m_skipMetaInfo.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_docCol.validateSettings(settings);
        m_termCol.validateSettings(settings);
        m_sort.validateSettings(settings);
        m_procCount.validateSettings(settings);
        m_checkTags.validateSettings(settings);
        m_coocLevel.validateSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        m_docCol.loadSettingsFrom(settings);
        m_termCol.loadSettingsFrom(settings);
        m_sort.loadSettingsFrom(settings);
        m_procCount.loadSettingsFrom(settings);
        m_checkTags.loadSettingsFrom(settings);
        m_coocLevel.loadSettingsFrom(settings);
        try {
            m_skipMetaInfo.loadSettingsFrom(settings);
        } catch (Exception e) {
            //new introduced in KNIME 2.8
            m_skipMetaInfo.setBooleanValue(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir,
            final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
        // nothing to do

    }

}
